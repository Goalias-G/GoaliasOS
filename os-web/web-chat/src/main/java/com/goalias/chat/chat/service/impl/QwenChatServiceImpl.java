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
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
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

    private final Environment environment;

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        boolean enableSearch = Objects.equals(chatModel.getEnableSearch(), EnableSearchType.YES.getCode()) ? chatRequest.getEnableSearch() : false;
        Map<String, Object> enableSearchMap = Map.of(
                "enable_search", true,
                "enable_thinking", true,
                "search_options", Map.of("forced_search", true)
        );

        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .baseUrl(chatModel.getApiHost())
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
                .customParameters(enableSearch ? enableSearchMap : null)
                .returnThinking(true)
                .logRequests(environment.matchesProfiles("dev"))
                .logResponses(environment.matchesProfiles("dev"))
//                .isMultimodalModel(chatModel.getModelName().equals("qwen3.8-flash"))
                .build();

        dev.langchain4j.model.chat.request.ChatRequest.Builder requestBuilder = toLangChainRequest(chatRequest);
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
            log.error("AI Streaming Chat 请求失败：{}", e.getMessage());
            ChatServiceHelper.onStreamError(emitter, e.getMessage());
        }

        return emitter;
    }

    @Override
    public String simpleChat(ChatRequest chatRequest) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl(chatModel.getApiHost())
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
                .responseFormat(chatRequest.getIsJsonResponse() ? ResponseFormat.JSON : ResponseFormat.TEXT)
                .build();
        dev.langchain4j.model.chat.request.ChatRequest.Builder requestBuilder = toLangChainRequest(chatRequest);
        ChatResponse response = model.chat(requestBuilder.build());
        log.debug("AI Simple Chat 请求成功：{}", response);
        ChatServiceHelper.recordTokenUsage(response);
        return response.aiMessage().text();
    }

    @Override
    public String getProviderName() {
        return ChatModeType.QIANWEN.getCode();
    }
}
