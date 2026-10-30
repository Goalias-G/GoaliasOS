package com.goalias.chat.chat.handler;

import com.goalias.chat.chat.factory.FunctionCallsFactory;
import com.goalias.chat.chat.service.IChatService;
import com.goalias.chat.chat.support.ChatServiceHelper;
import com.goalias.common.chat.entity.chat.Message;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.utils.SpringUtils;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

/**
 * Function Call 响应处理器
 * 负责处理 AI 的响应，包括：
 * 1. 流式文本响应处理
 * 2. 工具调用请求检测和处理
 * 3. 多工具顺序执行
 * 4. 工具结果回喂给 AI（跨多轮累计会话，避免重复调用）
 * 5. SSE 事件发送
 */
@Slf4j
@RequiredArgsConstructor
public class FunctionCallResponseHandler implements StreamingChatResponseHandler {

    private final SseEmitter emitter;
    private final ChatRequest originalRequest;
    private final StreamingChatModel model;
    private final FunctionCallExecutor toolExecutor;

    /**
     * 跨多轮工具调用累计的会话消息。
     * 若每轮都从 originalRequest 重建，上一轮工具历史会丢失，模型易按系统提示重复整段流程。
     */
    private List<ChatMessage> conversationMessages;

    @Override
    public void onPartialResponse(String partialResponse) {
        try {
            if (Objects.nonNull(partialResponse)) {
                emitter.send(partialResponse);
            }
        } catch (Exception e) {
            log.error("发送消息片段失败", e);
            ChatServiceHelper.onStreamError(emitter, "发送消息失败: " + e.getMessage());
        }
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        try {
            ChatServiceHelper.recordTokenUsage(completeResponse);

            if (completeResponse.aiMessage().hasToolExecutionRequests()) {
                log.debug("检测到工具调用请求");
                handleToolCalls(completeResponse);
            } else {
                ChatServiceHelper.onStreamComplete(emitter);
            }
        } catch (Exception e) {
            log.error("处理完整响应失败", e);
            ChatServiceHelper.onStreamError(emitter, "处理响应失败: " + e.getMessage());
        }
    }

    /**
     * 处理工具调用：在累计会话上追加本轮 AI 工具请求与执行结果，再回喂
     */
    private void handleToolCalls(ChatResponse response) {
        try {
            List<ToolExecutionRequest> requests = response.aiMessage().toolExecutionRequests();
            ChatServiceHelper.sendToolCallStartEvent(emitter, requests.size());

            ensureConversationInitialized();
            appendToolRound(response.aiMessage(), requests);

            log.debug("开始执行工具回喂，Messages 轮次累计条数={}", conversationMessages.size());
            feedbackToAI(conversationMessages);
        } catch (Exception e) {
            log.error("处理工具调用失败", e);
            ChatServiceHelper.onStreamError(emitter, "工具调用失败: " + e.getMessage());
        }
    }

    private void ensureConversationInitialized() {
        if (conversationMessages != null) {
            return;
        }
        conversationMessages = new ArrayList<>();
        for (Message msg : originalRequest.getMessages()) {
            conversationMessages.add(convertMessage(msg));
        }
    }

    /**
     * 将本轮 AiMessage（含 tool requests）与工具结果追加到累计会话
     */
    private void appendToolRound(AiMessage aiMessage, List<ToolExecutionRequest> requests) throws IOException {
        conversationMessages.add(aiMessage);

        Map<String, String> toolResults = new LinkedHashMap<>();
        for (int i = 0; i < requests.size(); i++) {
            ToolExecutionRequest request = requests.get(i);
            ChatServiceHelper.sendToolExecutingEvent(emitter, request.name(), i + 1, requests.size());

            long startTime = System.currentTimeMillis();
            String result = toolExecutor.execute(request, originalRequest.getToken());
            long executionTime = System.currentTimeMillis() - startTime;

            toolResults.put(request.name(), result);
            ChatServiceHelper.sendToolCompletedEvent(emitter, request.name(), executionTime);
            log.debug("工具 {} 执行完成，耗时: {}ms", request.name(), executionTime);
        }

        if (requests.size() == 1) {
            // 单工具：绑定真实 request id，便于模型正确关联结果
            ToolExecutionRequest request = requests.get(0);
            conversationMessages.add(ToolExecutionResultMessage.from(request, toolResults.get(request.name())));
        } else {
            // 多工具：Qwen 等对多条 ToolExecutionResultMessage 支持不佳，合并为一条
            conversationMessages.add(ToolExecutionResultMessage.from(
                    "combined-tool-results",
                    "toolResults",
                    toolResults.toString()
            ));
        }
    }

    private void feedbackToAI(List<ChatMessage> messages) throws IOException {
        ChatServiceHelper.sendAIThinkingEvent(emitter);

        FunctionCallsFactory toolFactory = SpringUtils.getBean(FunctionCallsFactory.class);

        dev.langchain4j.model.chat.request.ChatRequest feedbackRequest =
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(messages)
                        .parameters(ChatRequestParameters.builder()
                                .toolSpecifications(toolFactory.getToolSpecifications())
                                .build())
                        .build();

        model.chat(feedbackRequest, new FeedbackResponseHandler());
    }

    private class FeedbackResponseHandler implements StreamingChatResponseHandler {

        @Override
        public void onPartialResponse(String partialResponse) {
            try {
                emitter.send(partialResponse);
            } catch (IOException e) {
                log.error("发送回喂响应失败", e);
            }
        }

        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            try {
                ChatServiceHelper.recordTokenUsage(completeResponse);

                if (completeResponse.aiMessage().hasToolExecutionRequests()) {
                    log.debug("检测到后续工具调用请求，继续处理");
                    handleToolCalls(completeResponse);
                } else {
                    log.debug("Function Call 流程完成");
                    ChatServiceHelper.onStreamComplete(emitter);
                }
            } catch (Exception e) {
                log.error("处理回喂响应失败", e);
                ChatServiceHelper.onStreamError(emitter, "处理回喂响应失败: " + e.getMessage());
            }
        }

        @Override
        public void onError(Throwable error) {
            log.error("回喂 AI 失败", error);
            ChatServiceHelper.onStreamError(emitter, "AI 处理失败: " + error.getMessage());
        }
    }

    @Override
    public void onError(Throwable error) {
        log.error("AI 响应错误", error);
        ChatServiceHelper.onStreamError(emitter, "AI 响应错误: " + error.getMessage());
    }

    private ChatMessage convertMessage(Message msg) {
        return IChatService.toLangChainMessage(msg);
    }
}
