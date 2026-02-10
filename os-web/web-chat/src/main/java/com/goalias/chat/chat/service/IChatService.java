package com.goalias.chat.chat.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import com.goalias.common.chat.request.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话Service接口
 *
 * @author Goalias
 * @since 2026-01-22 */
public interface IChatService {

    /**
     * 客户端发送消息到服务端
     *
     * @param chatRequest 请求对象
     */
    SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter);

    String simpleChat(ChatRequest chatRequest);

    default dev.langchain4j.model.chat.request.ChatRequest convertToLangChainRequest(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        for (com.goalias.common.chat.entity.chat.Message msg : request.getMessages()) {
            // 简单转换，您可以根据实际需求调整
            if ("user".equals(msg.getRole())) {
                messages.add(UserMessage.from(msg.getContent().toString()));
            } else if ("system".equals(msg.getRole())) {
                messages.add(SystemMessage.from(msg.getContent().toString()));
            } else if ("assistant".equals(msg.getRole())) {
                messages.add(AiMessage.from(msg.getContent().toString()));
            }
        }
        return dev.langchain4j.model.chat.request.ChatRequest.builder().messages(messages).build();
    }

    /**
     * 获取此服务支持的模型服务商
     */
    String getProviderName();
}
