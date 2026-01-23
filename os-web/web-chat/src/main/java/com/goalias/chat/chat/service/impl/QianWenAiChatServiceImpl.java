package com.goalias.chat.chat.service.impl;

import com.goalias.chat.chat.service.IChatService;
import com.goalias.chat.chat.support.ChatServiceHelper;
import com.goalias.chat.chat.support.RetryNotifier;
import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.enums.ChatModeType;
import com.goalias.chat.service.IChatModelService;
import com.goalias.common.chat.request.ChatRequest;
import com.goalias.common.redis.constant.CacheNames;
import com.goalias.common.redis.service.RedisService;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;


/**
 * 阿里通义千问
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QianWenAiChatServiceImpl implements IChatService {

    private final IChatModelService chatModelService;

    private final RedisService redisService;

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());
        StreamingChatModel model = QwenStreamingChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
                .defaultRequestParameters(ChatRequestParameters.builder().toolSpecifications().build())  //TODO 实现Function calls
                .build();


        // 发送流式消息
        dev.langchain4j.model.chat.request.ChatRequest langChainRequest = convertToLangchainRequest(chatRequest); // Changed from ChatMessage to ChatRequest
        try {
            model.chat(langChainRequest, new StreamingChatResponseHandler() {
                @SneakyThrows
                @Override
                public void onPartialResponse(String partialResponse) {
                    emitter.send(partialResponse);
                    log.info("收到消息片段: {}", partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    if (completeResponse.aiMessage().hasToolExecutionRequests()) {
                        List<ToolExecutionRequest> toolExecutionRequests = completeResponse.aiMessage().toolExecutionRequests();
                        toolExecutionRequests.forEach(toolExecutionRequest -> {
                            log.info("toolExecutionRequest: {}", toolExecutionRequest);
                        });
                    }//TODO 抽取FC逻辑到ChatServiceHelper
                    emitter.complete();
                    redisService.hIncr(CacheNames.CHAT_TOKEN_INPUT, completeResponse.modelName(), completeResponse.tokenUsage().inputTokenCount().longValue());
                    redisService.hIncr(CacheNames.CHAT_TOKEN_OUTPUT, completeResponse.modelName(), completeResponse.tokenUsage().outputTokenCount().longValue());
                    log.info("消息结束，完整消息ID: {}", completeResponse);
                    RetryNotifier.clear(emitter);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("消息错误", error);
                    ChatServiceHelper.onStreamError(emitter, error.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("千问请求失败：{}", e.getMessage());
            ChatServiceHelper.onStreamError(emitter, e.getMessage());
        }

        return emitter;

    }

    /**
     * 工作流场景：支持 langchain4j handler
     */
    @Override
    public void chat(ChatRequest request, StreamingChatResponseHandler handler) {
        log.info("workflow chat, model: {}", request.getModel());

        ChatModel chatModel = chatModelService.selectModelByName(request.getModel());

        StreamingChatModel model = QwenStreamingChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .build();

        try {
            // 将 ruoyi-ai 的 ChatRequest 转换为 langchain4j 的格式
            dev.langchain4j.model.chat.request.ChatRequest chatRequest = convertToLangchainRequest(request);
            model.chat(chatRequest, handler);
        } catch (Exception e) {
            log.error("workflow 千问请求失败：{}", e.getMessage(), e);
            throw new RuntimeException("QianWen workflow chat failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return ChatModeType.QIANWEN.getCode();
    }


}
