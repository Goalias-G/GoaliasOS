package com.goalias.chat.chat.handler;

import com.goalias.chat.chat.factory.FunctionCallsFactory;
import com.goalias.chat.chat.support.ChatServiceHelper;
import com.goalias.common.chat.entity.chat.Message;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.core.utils.SpringUtils;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
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
 * 4. 工具结果回喂给 AI
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
     * 处理部分响应（流式文本片段）
     *
     * @param partialResponse 文本片段
     */
    @Override
    public void onPartialResponse(String partialResponse) {
        try {
            // 流式返回文本片段给客户端
            if (Objects.nonNull(partialResponse)){
                emitter.send(partialResponse);
            }
        } catch (Exception e) {
            log.error("发送消息片段失败", e);
            ChatServiceHelper.onStreamError(emitter, "发送消息失败: " + e.getMessage());
        }
    }

    /**
     * 处理完整响应
     * 检查是否有工具调用请求，如果有则执行工具并回喂结果
     *
     * @param completeResponse 完整响应
     */
    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        try {
            ChatServiceHelper.recordTokenUsage(completeResponse);

            // 检查是否有工具调用请求
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
     * 处理工具调用
     * 执行所有工具调用请求，并将结果回喂给 AI
     *
     * @param response AI 响应
     */
    private void handleToolCalls(ChatResponse response) {
        try {
            List<ToolExecutionRequest> requests = response.aiMessage().toolExecutionRequests();

            // 发送工具调用开始事件
            ChatServiceHelper.sendToolCallStartEvent(emitter, requests.size());

            List<ChatMessage> newMessages = buildMessagesWithToolCalls(response.aiMessage(), requests);

            log.debug("开始执行工具回喂，Messages{}", newMessages);
            // 回喂给 AI，生成最终回复
            feedbackToAI(newMessages);

        } catch (Exception e) {
            log.error("处理工具调用失败", e);
            ChatServiceHelper.onStreamError(emitter, "工具调用失败: " + e.getMessage());
        }
    }

    /**
     * 构建包含工具调用和结果的消息历史
     *
     * @param aiMessage AI 的工具调用消息
     * @param requests  工具执行请求列表
     * @return 新的消息历史
     */
    private List<ChatMessage> buildMessagesWithToolCalls(
            AiMessage aiMessage,
            List<ToolExecutionRequest> requests) throws IOException {

        // 复制原始消息历史
        List<ChatMessage> newMessages = new ArrayList<>();

        // 转换原始请求中的消息
        for (Message msg : originalRequest.getMessages()) {
            newMessages.add(convertMessage(msg));
        }

        // 添加 AI 的工具调用消息
        newMessages.add(aiMessage);

        Map<String, String> toolResults = new HashMap<>();
        // 执行所有工具调用并添加结果
        for (int i = 0; i < requests.size(); i++) {
            ToolExecutionRequest request = requests.get(i);
            // 发送工具执行中事件
            ChatServiceHelper.sendToolExecutingEvent(emitter, request.name(), i + 1, requests.size());

            // 执行工具
            long startTime = System.currentTimeMillis();
            String result = toolExecutor.execute(request);
            long executionTime = System.currentTimeMillis() - startTime;

            toolResults.put(request.name(), result);
            // 发送工具执行完成事件
            ChatServiceHelper.sendToolCompletedEvent(emitter, request.name(), executionTime);

            log.debug("工具 {} 执行完成，耗时: {}ms", request.name(), executionTime);
        }

        //Qwen不支持多个 ToolExecutionResultMessage 返回
        ToolExecutionResultMessage toolsResultMessage = ToolExecutionResultMessage.from(
                "combined-tool-results",
                "toolResults",
                toolResults.toString()
        );

        newMessages.add(toolsResultMessage);

        return newMessages;
    }

    /**
     * 将工具结果回喂给 AI，生成最终回复
     *
     * @param messages 包含工具结果的完整消息历史
     */
    private void feedbackToAI(List<ChatMessage> messages) throws IOException {
        // 发送 AI 思考事件
        ChatServiceHelper.sendAIThinkingEvent(emitter);

        FunctionCallsFactory toolFactory = SpringUtils.getBean(FunctionCallsFactory.class);

        // 构建新请求（包含工具规范，以便 AI 可以继续调用工具）
        dev.langchain4j.model.chat.request.ChatRequest feedbackRequest =
                dev.langchain4j.model.chat.request.ChatRequest.builder()
                        .messages(messages)
                        .parameters(ChatRequestParameters.builder()
                                .toolSpecifications(toolFactory.getToolSpecifications())
                                .build())
                        .build();

        // 再次调用 AI（使用新的处理器处理回喂响应）
        model.chat(feedbackRequest, new FeedbackResponseHandler());
    }

    /**
     * 回喂响应处理器
     * 处理 AI 基于工具结果生成的最终回复
     */
    private class FeedbackResponseHandler implements StreamingChatResponseHandler {

        @Override
        public void onPartialResponse(String partialResponse) {
            try {
                // 流式返回 AI 的最终回复
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
                    // 完成对话
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

    /**
     * 处理错误
     *
     * @param error 错误对象
     */
    @Override
    public void onError(Throwable error) {
        log.error("AI 响应错误", error);
        ChatServiceHelper.onStreamError(emitter, "AI 响应错误: " + error.getMessage());
    }

    // ==================== 辅助方法 ====================

    /**
     * 转换消息格式
     * 将消息格式转换为 langchain4j 的格式
     *
     * @return langchain4j 消息
     */
    private ChatMessage convertMessage(Message msg) {
        String role = msg.getRole();
        String content = msg.getContent().toString();

        return switch (role) {
            case "user" -> UserMessage.from(content);
            case "system" -> SystemMessage.from(content);
            case "assistant" -> AiMessage.from(content);
            default -> {
                log.warn("未知的消息角色: {}, 默认作为用户消息处理", role);
                yield UserMessage.from(content);
            }
        };
    }
}
