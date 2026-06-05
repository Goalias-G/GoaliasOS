package com.goalias.chat.service;

import com.goalias.chat.enums.PromptTemplateEnum;
import com.goalias.common.chat.request.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天管理服务接口
 * <p>
 * 拆分层：接口下沉到 service-chat，web-chat 仍保留实现 {@code SseServiceImpl}。
 *
 * @author Goalias
 * @since 2026-01-22
 */
public interface ISseService {

    /**
     * 客户端发送消息到服务端（SSE 流式响应）
     */
    SseEmitter sseChat(ChatRequest chatRequest);

    /**
     * 非流式对话：传入模板与参数，返回完整字符串
     */
    String simpleChat(ChatRequest chatRequest, PromptTemplateEnum promptTemplate, Object... args);
}
