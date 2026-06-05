package com.goalias.chat.chat.service.impl;

import com.goalias.chat.chat.enums.EnableSearchType;
import com.goalias.chat.chat.factory.FunctionCallsFactory;
import com.goalias.chat.chat.handler.FunctionCallExecutor;
import com.goalias.chat.chat.handler.FunctionCallResponseHandler;
import com.goalias.chat.chat.service.IChatService;
import com.goalias.chat.chat.support.ChatServiceHelper;
import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.enums.ChatModeType;
import com.goalias.chat.service.IChatModelService;
import com.goalias.common.chat.request.ChatRequest;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;


/**
 * 阿里通义千问
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class QwenChatServiceImpl implements IChatService {

    private final IChatModelService chatModelService;

    private final FunctionCallExecutor toolExecutor;

    private final FunctionCallsFactory toolFactory;

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        StreamingChatModel model = QwenStreamingChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .temperature(chatRequest.getTemperature().floatValue())
                .topP(chatRequest.getTopP())
                .enableSearch(Objects.equals(chatModel.getEnableSearch(), EnableSearchType.YES.getCode()) ? chatRequest.getEnableSearch() : false)
                .build();

        dev.langchain4j.model.chat.request.ChatRequest.Builder requestBuilder = toLangChainToolRequest(chatRequest);
        dev.langchain4j.model.chat.request.ChatRequest request = requestBuilder.toolSpecifications(toolFactory.getToolSpecifications()).build();

        try {
            // 使用 FunctionCallResponseHandler 处理响应
            model.chat(request, new FunctionCallResponseHandler(
                    emitter,
                    chatRequest,
                    model,
                    toolExecutor
            ));
        } catch (Exception e) {
            log.error("千问请求失败：{}", e.getMessage());
            ChatServiceHelper.onStreamError(emitter, e.getMessage());
        }

        return emitter;
    }

    @Override
    public String simpleChat(ChatRequest chatRequest) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        QwenChatModel model = QwenChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .temperature(chatRequest.getTemperature().floatValue())
                .topP(chatRequest.getTopP())
                .build();
        dev.langchain4j.model.chat.request.ChatRequest.Builder requestBuilder = toLangChainToolRequest(chatRequest);
        dev.langchain4j.model.chat.request.ChatRequest request = requestBuilder
                .responseFormat(chatRequest.getIsJsonResponse() ? ResponseFormat.JSON : ResponseFormat.TEXT).build();
        ChatResponse response = model.chat(request);
        log.info("千问 simpleChat 请求成功：{}", response);
        ChatServiceHelper.recordTokenUsage(response);
        return response.aiMessage().text();
    }

    @Override
    public String getProviderName() {
        return ChatModeType.QIANWEN.getCode();
    }
}
