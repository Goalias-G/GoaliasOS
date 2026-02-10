package com.goalias.chat.chat.service.proxy;

import cn.hutool.json.JSONObject;
import com.goalias.chat.chat.service.IChatCostService;
import com.goalias.chat.chat.service.IChatService;
import com.goalias.common.chat.entity.chat.Message;
import com.goalias.common.chat.request.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * 统一计费代理类
 * 自动处理所有ChatService的AI回复保存和计费逻辑
 */
@Slf4j
@RequiredArgsConstructor
public class BillingChatServiceProxy implements IChatService {

    private final IChatService delegate;
    private final IChatCostService chatCostService;

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        // 🔥 在AI回复开始前检查余额是否充足
        if (!chatCostService.checkBalanceSufficient(chatRequest)) {
            String errorMsg = "余额不足，无法使用AI服务，请充值后再试";
            log.warn("余额不足阻止AI回复，用户ID: {}, 模型: {}",
                    chatRequest.getUserId(), chatRequest.getModel());
            try {
                emitter.send(errorMsg);
                emitter.complete();
            } catch (IOException e) {
                log.error("推送流异常，用户ID: {}, 模型: {}",
                        chatRequest.getUserId(), chatRequest.getModel());
                emitter.complete();
                throw new RuntimeException(errorMsg);
            }
            return emitter;
        }

        log.info("余额检查通过，开始AI回复，用户ID: {}, 模型: {}",
                chatRequest.getUserId(), chatRequest.getModel());

        // 创建增强的SseEmitter，自动收集AI回复
        BillingSseEmitter billingEmitter = new BillingSseEmitter(emitter, chatRequest, chatCostService);

        try {
            return delegate.chat(chatRequest, billingEmitter);
        } catch (Exception e) {
            log.error("聊天服务执行失败", e);
            throw e;
        }
    }

    @Override
    public String simpleChat(ChatRequest chatRequest) {
        return delegate.simpleChat(chatRequest);
    }

    @Override
    public String getProviderName() {
        return delegate.getProviderName();
    }

    /**
     * 增强的SseEmitter，自动处理AI回复的保存和计费
     */
    private static class BillingSseEmitter extends SseEmitter {
        private final SseEmitter delegate;
        private final ChatRequest chatRequest;
        private final IChatCostService chatCostService;
        private final StringBuilder aiResponseBuilder = new StringBuilder();
        private final AtomicBoolean completed = new AtomicBoolean(false);

        public BillingSseEmitter(SseEmitter delegate, ChatRequest chatRequest, IChatCostService chatCostService) {
            super(delegate.getTimeout());
            this.delegate = delegate;
            this.chatRequest = chatRequest;
            this.chatCostService = chatCostService;
        }

        @Override
        public void send(Object object) throws IOException {
            // 先发送给前端
            delegate.send(object);

            // 提取AI回复内容并累积
            String content = extractContentFromSseData(object);
            if (content != null && !content.trim().isEmpty()) {
                aiResponseBuilder.append(content);
            }
        }

        @Override
        public void complete() {
            if (completed.compareAndSet(false, true)) {
                try {
                    // AI回复完成，保存消息和计费
                    saveAiResponseAndBilling();
                    delegate.complete();
                } catch (Exception e) {
                    log.error("保存AI回复和计费失败", e);
                    delegate.completeWithError(e);
                }
            }
        }

        @Override
        public void completeWithError(Throwable ex) {
            if (completed.compareAndSet(false, true)) {
                log.warn("AI回复出错，跳过计费", ex);
                delegate.completeWithError(ex);
            }
        }

        /**
         * 保存AI回复并进行计费
         */
        private void saveAiResponseAndBilling() {
            String aiResponse = aiResponseBuilder.toString().trim();
            if (aiResponse.isEmpty()) {
                log.warn("AI回复内容为空，跳过保存和计费");
                return;
            }
            log.debug("保存AI完整回复内容：{}", aiResponse);
            try {
                // 创建AI回复的ChatRequest
                ChatRequest aiChatRequest = new ChatRequest();
                aiChatRequest.setUserId(chatRequest.getUserId());
                aiChatRequest.setSessionId(chatRequest.getSessionId());
                aiChatRequest.setRole(Message.Role.ASSISTANT.getName());
                aiChatRequest.setModel(chatRequest.getModel());
                aiChatRequest.setPrompt(aiResponse);
                aiChatRequest.setMessageId(chatRequest.getMessageId());

                // 发布计费事件
                chatCostService.publishBillingEvent(aiChatRequest);

                // 保存AI回复消息
                chatCostService.saveMessage(aiChatRequest);


                log.info("AI回复保存和计费完成，用户ID: {}, 会话ID: {}, 回复长度: {}",
                        chatRequest.getUserId(), chatRequest.getSessionId(), aiResponse.length());

            } catch (Exception e) {
                log.error("保存AI回复和计费失败，用户ID: {}, 会话ID: {}",
                        chatRequest.getUserId(), chatRequest.getSessionId(), e);
                // 不抛出异常，避免影响用户体验
            }
        }

        /**
         * 从SSE数据中提取AI回复内容
         * 适配不同AI服务的数据格式
         */
        private String extractContentFromSseData(Object sseData) {
            if (sseData == null) {
                return null;
            }

            String dataStr = sseData.toString();

            // 过滤明显的控制信号
            if (isControlSignal(dataStr)) {
                return null;
            }

            // 策略1: 直接字符串内容（DeepSeek等简单格式）
            String directContent = extractDirectContent(dataStr);
            if (directContent != null) {
                return directContent;
            }

            // 策略3: SSE事件格式解析
            return extractSseEventContent(dataStr);
        }

        /**
         * 判断是否为控制信号
         */
        private boolean isControlSignal(String data) {
            if (data == null || data.trim().isEmpty()) {
                return true;
            }

            String trimmed = data.trim();
            return "[DONE]".equals(trimmed)
                    || "null".equals(trimmed)
                    || trimmed.startsWith("event:")
                    || trimmed.startsWith("id:")
                    || trimmed.startsWith("retry:");
        }

        /**
         * 提取直接文本内容
         */
        private String extractDirectContent(String data) {
            // 如果是纯文本且长度合理，直接返回
            if (!data.isEmpty() && data.length() < 1000 && !data.startsWith("{") && !data.startsWith("[")) {
                return data;
            }
            return null;
        }

        /**
         * 提取JSON格式内容
         */
        private String extractJsonContent(String data) {
            try {
                JSONObject jsonData = new JSONObject(data);
                // 简化的JSON解析
                if (jsonData.containsKey("content")) {
                    return parseContentFromJson(jsonData);
                }
            } catch (Exception e) {
                log.debug("JSON解析失败: {}", e.getMessage());
            }
            return null;
        }

        /**
         * 提取SSE事件格式内容
         */
        private String extractSseEventContent(String data) {
            if (data.startsWith("data:")) {
                String jsonPart = data.substring(5).trim();
                return extractJsonContent(jsonPart);
            }
            return null;
        }

        /**
         * 从JSON字符串中解析内容
         */
        private String parseContentFromJson(JSONObject jsonObject) {
            // 简化的JSON解析，实际项目中建议使用Jackson
            if (jsonObject.containsKey("content")) {
                return jsonObject.getStr("content");
            }
            return null;
        }

        // 委托其他方法到原始emitter
        @Override
        public void onCompletion(Runnable callback) {
            delegate.onCompletion(callback);
        }

        @Override
        public void onError(Consumer<Throwable> callback) {
            delegate.onError(callback);
        }

        @Override
        public void onTimeout(Runnable callback) {
            delegate.onTimeout(callback);
        }
    }
}
