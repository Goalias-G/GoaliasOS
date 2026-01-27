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
import com.goalias.common.redis.service.RedisService;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final FunctionCallsFactory toolFactory;
    private final FunctionCallExecutor toolExecutor;

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModel chatModel = chatModelService.selectModelByName(chatRequest.getModel());

        // 扫描并获取工具列表
        List<ToolSpecification> tools = toolFactory.getToolSpecifications();

        StreamingChatModel model = QwenStreamingChatModel.builder()
                .apiKey(chatModel.getApiKey())
                .modelName(chatModel.getModelName())
                .temperature(chatRequest.getTemperature().floatValue())
                .topP(chatRequest.getTopP())
                .defaultRequestParameters(ChatRequestParameters.builder().toolSpecifications(tools).build())
                .build();

        dev.langchain4j.model.chat.request.ChatRequest langChainRequest = convertToLangChainRequest(chatRequest);

        try {
            // 使用 FunctionCallResponseHandler 处理响应
            model.chat(langChainRequest, new FunctionCallResponseHandler(
                    emitter,
                    chatRequest,
                    model,
                    tools,
                    toolExecutor,
                    redisService
            ));
        } catch (Exception e) {
            log.error("千问请求失败：{}", e.getMessage());
            ChatServiceHelper.onStreamError(emitter, e.getMessage());
        }

        return emitter;
    }

    @Override
    public String getProviderName() {
        return ChatModeType.QIANWEN.getCode();
    }
}
