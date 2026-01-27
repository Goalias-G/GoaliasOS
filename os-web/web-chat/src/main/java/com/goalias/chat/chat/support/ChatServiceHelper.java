package com.goalias.chat.chat.support;

import cn.hutool.json.JSONObject;
import com.goalias.chat.chat.util.SSEUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 抽取各聊天实现类的通用逻辑：
 * - 创建带开关的 SSE 监听器
 * - 统一的流错误处理（根据是否在重试场景决定通知或直接结束）
 * - 统一的完成处理（清理回调并 complete）
 * - Function Call 相关的 SSE 事件发送
 */
@Slf4j
public class ChatServiceHelper {

    public static void onStreamError(SseEmitter emitter, String errorMessage) {
        SSEUtil.sendErrorEvent(emitter, errorMessage);
        if (RetryNotifier.hasCallback(emitter)) {
            RetryNotifier.notifyFailure(emitter);
        } else {
            emitter.complete();
        }
    }

    public static void onStreamComplete(SseEmitter emitter) {
        try {
            emitter.complete();
        } finally {
            RetryNotifier.clear(emitter);
        }
    }

    // ==================== Function Call 相关 SSE 方法 ====================

    /**
     * 发送工具调用开始事件
     *
     * @param toolCount 工具数量
     */
    public static void sendToolCallStartEvent(SseEmitter emitter, int toolCount) throws IOException {
        JSONObject eventData = new JSONObject();
        eventData.set("type", "tool_call_start");
        eventData.set("message", String.format("正在调用 %d 个工具...", toolCount));
        eventData.set("toolCount", toolCount);
        eventData.set("timestamp", System.currentTimeMillis());

        emitter.send(eventData.toString());
    }

    /**
     * 发送工具执行中事件
     *
     * @param toolName 工具名称
     * @param current  当前执行的工具序号
     * @param total    总工具数量
     */
    public static void sendToolExecutingEvent(SseEmitter emitter, String toolName, int current, int total) throws IOException {
        JSONObject eventData = new JSONObject();
        eventData.set("type", "tool_executing");
        eventData.set("toolName", toolName);
        eventData.set("message", String.format("正在执行工具: %s (%d/%d)", toolName, current, total));
        eventData.set("current", current);
        eventData.set("total", total);
        eventData.set("timestamp", System.currentTimeMillis());

        emitter.send(eventData.toString());

        log.debug("发送工具执行中事件: {} ({}/{})", toolName, current, total);
    }

    /**
     * 发送工具执行完成事件
     *
     * @param toolName      工具名称
     * @param executionTime 执行耗时（毫秒）
     */
    public static void sendToolCompletedEvent(SseEmitter emitter, String toolName, long executionTime) throws IOException {
        JSONObject eventData = new JSONObject();
        eventData.set("type", "tool_completed");
        eventData.set("toolName", toolName);
        eventData.set("message", String.format("工具 %s 执行完成", toolName));
        eventData.set("executionTime", executionTime);
        eventData.set("timestamp", System.currentTimeMillis());

        emitter.send(eventData.toString());

        log.debug("发送工具执行完成事件: {}, 耗时: {}ms", toolName, executionTime);
    }

    /**
     * 发送 AI 思考事件
     */
    public static void sendAIThinkingEvent(SseEmitter emitter) throws IOException {
        JSONObject eventData = new JSONObject();
        eventData.set("type", "ai_thinking");
        eventData.set("message", "AI 正在分析工具结果...");
        eventData.set("timestamp", System.currentTimeMillis());

        emitter.send(eventData.toString());

        log.debug("发送 AI 思考事件");
    }
}


