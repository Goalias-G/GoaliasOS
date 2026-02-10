package com.goalias.chat.chat.service;

import com.goalias.chat.enums.PromptTemplateEnum;
import com.goalias.common.chat.request.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * 用户聊天管理Service接口
 *
 * @author Goalias
 * @since 2026-01-22 */
public interface ISseService {

    /**
     * 客户端发送消息到服务端
     * @param chatRequest 请求对象
     */
    SseEmitter sseChat(ChatRequest chatRequest);

    String simpleChat(ChatRequest chatRequest, PromptTemplateEnum promptTemplate);


}
