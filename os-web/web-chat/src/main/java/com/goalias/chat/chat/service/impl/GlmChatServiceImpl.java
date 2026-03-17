package com.goalias.chat.chat.service.impl;

import com.goalias.chat.chat.handler.FunctionCallExecutor;
import com.goalias.chat.chat.factory.FunctionCallsFactory;
import com.goalias.chat.chat.handler.FunctionCallResponseHandler;
import com.goalias.chat.chat.service.IChatService;
import com.goalias.chat.chat.support.ChatServiceHelper;
import com.goalias.chat.domain.ChatModel;
import com.goalias.chat.enums.ChatModeType;
import com.goalias.chat.service.IChatModelService;
import com.goalias.common.chat.request.ChatRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiChatModel;
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;


/**
 * 智谱模型
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GlmChatServiceImpl implements IChatService {

    private final IChatModelService chatModelService;

    private final FunctionCallExecutor toolExecutor;

    private final FunctionCallsFactory toolFactory;

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        StreamingChatModel model = ZhipuAiStreamingChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .model(chatModel.getModelName())
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
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
            log.error("智谱请求失败：{}", e.getMessage());
            ChatServiceHelper.onStreamError(emitter, e.getMessage());
        }

        return emitter;
    }

    @Override
    public String simpleChat(ChatRequest chatRequest) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        ZhipuAiChatModel model = ZhipuAiChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .model(chatModel.getModelName())
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
                .build();

        dev.langchain4j.model.chat.request.ChatRequest.Builder requestBuilder = toLangChainToolRequest(chatRequest);
        dev.langchain4j.model.chat.request.ChatRequest request = requestBuilder.responseFormat(ResponseFormat.JSON).build();

        ChatResponse response = model.chat(request);
        log.info("智谱 simpleChat 请求成功：{}", response);
        ChatServiceHelper.recordTokenUsage(response);
        return response.aiMessage().text();
    }

    @Override
    public String getProviderName() {
        return ChatModeType.GLM.getCode();
    }
}
