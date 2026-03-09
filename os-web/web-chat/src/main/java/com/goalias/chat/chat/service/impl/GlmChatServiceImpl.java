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
import dev.langchain4j.community.model.zhipu.ZhipuAiStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
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

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        StreamingChatModel model = ZhipuAiStreamingChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .model(chatModel.getModelName())
                .temperature(chatRequest.getTemperature())
                .topP(chatRequest.getTopP())
                .build();

        dev.langchain4j.model.chat.request.ChatRequest langChainRequest = toLangChainToolRequest(chatRequest);

        try {
            // 使用 FunctionCallResponseHandler 处理响应
            model.chat(langChainRequest, new FunctionCallResponseHandler(
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

        QwenChatModel model = QwenChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .temperature(chatRequest.getTemperature().floatValue())
                .topP(chatRequest.getTopP())
                .build();
        chatRequest.setEnableTool(false);
        ChatResponse response = model.chat(toLangChainToolRequest(chatRequest));
        ChatServiceHelper.recordTokenUsage(response);
        return response.aiMessage().text();
    }

    @Override
    public String getProviderName() {
        return ChatModeType.GLM.getCode();
    }
}
