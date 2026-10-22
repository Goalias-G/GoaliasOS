package com.goalias.chat.chat.service;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import com.goalias.common.chat.request.ChatRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    default dev.langchain4j.model.chat.request.ChatRequest.Builder toLangChainRequest(ChatRequest request) {
        List<ChatMessage> messages = new ArrayList<>();
        for (com.goalias.common.chat.entity.chat.Message msg : request.getMessages()) {
            messages.add(toLangChainMessage(msg));
        }
        return dev.langchain4j.model.chat.request.ChatRequest.builder()
                .messages(messages);
    }

    /**
     * 将前端消息转换为 langchain4j 消息。
     * content 为数组时按 OpenAI 多模态格式（[{type:text},{type:image_url}]）解析，
     * 图片转为 ImageContent，供视觉模型识别；文本消息不受影响
     */
    static ChatMessage toLangChainMessage(com.goalias.common.chat.entity.chat.Message msg) {
        String role = msg.getRole();
        if ("system".equals(role)) {
            return SystemMessage.from(extractText(msg.getContent()));
        }
        if ("assistant".equals(role)) {
            return AiMessage.from(extractText(msg.getContent()));
        }
        // 用户消息及未知角色统一按用户消息处理，保留多模态内容
        return UserMessage.from(toUserContents(msg.getContent()));
    }

    /**
     * 解析用户消息 content：字符串按纯文本处理；数组按多模态部件拆分文本与图片
     */
    static List<Content> toUserContents(Object content) {
        if (!(content instanceof List<?> parts)) {
            return List.of(TextContent.from(String.valueOf(content)));
        }
        List<Content> contents = new ArrayList<>();
        for (Object part : parts) {
            if (!(part instanceof Map<?, ?> partMap)) {
                continue;
            }
            String type = String.valueOf(partMap.get("type"));
            if ("text".equals(type) && Objects.nonNull(partMap.get("text"))) {
                contents.add(TextContent.from(partMap.get("text").toString()));
            } else if ("image_url".equals(type)) {
                Object imageUrl = partMap.get("image_url");
                String url = imageUrl instanceof Map<?, ?> urlMap && Objects.nonNull(urlMap.get("url"))
                        ? urlMap.get("url").toString() : String.valueOf(imageUrl);
                contents.add(toImageContent(url));
            }
        }
        // 部件均无法识别时退化为整体文本，避免构造出空消息
        if (contents.isEmpty()) {
            return List.of(TextContent.from(String.valueOf(content)));
        }
        return contents;
    }

    /**
     * 将图片地址转为图片内容：
     * data:image/...;base64,xxx 形式解析出 base64 数据与类型，其余按远程 URL 处理
     */
    static ImageContent toImageContent(String url) {
        if (url != null && url.startsWith("data:")) {
            int commaIndex = url.indexOf(',');
            int semicolonIndex = url.indexOf(';');
            if (commaIndex > 0 && semicolonIndex > 5 && semicolonIndex < commaIndex) {
                return ImageContent.from(Image.builder()
                        .base64Data(url.substring(commaIndex + 1))
                        .mimeType(url.substring(5, semicolonIndex))
                        .build());
            }
        }
        return ImageContent.from(Image.builder().url(URI.create(url)).build());
    }

    /**
     * 提取 content 中的纯文本，用于会话标题、提示词、向量检索等仅需文本的场景
     */
    static String extractText(Object content) {
        if (!(content instanceof List<?> parts)) {
            return content == null ? "" : content.toString();
        }
        StringBuilder text = new StringBuilder();
        for (Object part : parts) {
            if (part instanceof Map<?, ?> partMap && "text".equals(partMap.get("type"))
                    && Objects.nonNull(partMap.get("text"))) {
                if (!text.isEmpty()) {
                    text.append('\n');
                }
                text.append(partMap.get("text"));
            }
        }
        return text.toString();
    }

    /**
     * 获取此服务支持的模型服务商
     */
    String getProviderName();
}
