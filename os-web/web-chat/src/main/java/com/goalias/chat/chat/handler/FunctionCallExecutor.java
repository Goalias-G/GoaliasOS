package com.goalias.chat.chat.handler;

import cn.hutool.json.JSONUtil;
import com.goalias.chat.chat.factory.FunctionCallsFactory;
import com.goalias.chat.chat.support.TtlTokenContext;
import com.goalias.chat.chat.tools.ToolMetadata;
import com.goalias.common.core.exception.ServiceException;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 工具执行器
 * 负责根据 AI 的工具调用请求执行实际的 Java 方法
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FunctionCallExecutor {

    private final FunctionCallsFactory toolFactory;

    /**
     * 线程池，用于执行工具调用（支持超时控制）
     */
    private final ThreadPoolTaskExecutor threadPool;

    /**
     * 执行工具调用
     *
     * @param request 工具执行请求
     * @return 执行结果（JSON 字符串）
     */
    public String execute(ToolExecutionRequest request) {
        return execute(request, null);
    }

    /**
     * 执行工具调用（携带用户 token 上下文）
     *
     * @param request 工具执行请求
     * @param token   当前用户的 Sa-Token 值，用于在异步线程恢复登录上下文
     * @return 执行结果（JSON 字符串）
     */
    public String execute(ToolExecutionRequest request, String token) {
        long startTime = System.currentTimeMillis();
        String toolName = request.name();

        try {
            log.info("开始执行工具: {}, 参数: {}", toolName, request.arguments());

            // 1. 查找工具元数据
            ToolMetadata metadata = toolFactory.getToolMetadata(toolName);
            if (metadata == null) {
                String error = "工具未找到: " + toolName;
                log.warn(error);
                return buildErrorResult(error);
            }

            // 检查工具是否启用
            if (!metadata.isEnabled()) {
                String error = "工具已禁用: " + toolName;
                log.warn(error);
                return buildErrorResult(error);
            }

            // 2. 解析参数
            Object[] args = parseArguments(request.arguments(), metadata);

            // 3. 执行方法（带超时控制，显式传递 token）
            Object result = executeWithTimeout(metadata, args, token);

            // 4. 返回结果
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("工具执行成功: {}, 耗时: {}ms", toolName, executionTime);

            return buildSuccessResult(result);

        } catch (TimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String error = String.format("工具执行超时: %s, 超时时间: %dms", toolName, executionTime);
            log.error(error, e);
            return buildErrorResult(error);

        } catch (Exception e) {
            String error = String.format("工具执行失败: %s, 原因: %s", toolName, e.getMessage());
            log.error(error, e);
            return buildErrorResult(error);
        }
    }

    /**
     * 解析 JSON 参数为 Java 对象数组
     *
     * @param argumentsJson JSON 参数字符串
     * @param metadata      工具元数据
     * @return 解析后的参数数组
     */
    private Object[] parseArguments(String argumentsJson, ToolMetadata metadata) {
        try {
            // 如果没有参数，返回空数组
            if (argumentsJson == null || argumentsJson.trim().isEmpty() || "{}".equals(argumentsJson.trim())) {
                return new Object[metadata.getParameterNames().length];
            }

            // 解析 JSON 为 Map
            Map<String, Object> argsMap = JSONUtil.toBean(argumentsJson, Map.class);

            // 获取参数类型和名称
            Class<?>[] parameterTypes = metadata.getParameterTypes();
            String[] parameterNames = metadata.getParameterNames();

            if (parameterTypes.length == 0) {
                return new Object[0];
            }

            // 按顺序构建参数数组
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                String paramName = parameterNames[i];
                Object paramValue = argsMap.get(paramName);

                if (paramValue == null) {
                    args[i] = null;
                    continue;
                }

                // 类型转换
                args[i] = convertParameter(paramValue, parameterTypes[i]);
            }

            return args;

        } catch (Exception e) {
            String error = String.format("参数解析失败: %s, 参数: %s", metadata.getName(), argumentsJson);
            log.error(error, e);
            throw new RuntimeException(error, e);
        }
    }

    /**
     * 转换参数类型
     *
     * @param value      原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private Object convertParameter(Object value, Class<?> targetType) {
        // 如果类型已经匹配，直接返回
        if (targetType.isInstance(value)) {
            return value;
        }

        // 基本类型转换
        if (targetType == String.class) {
            return value.toString();
        }

        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString());
        }

        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString());
        }

        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            return Double.parseDouble(value.toString());
        }

        if (targetType == Float.class || targetType == float.class) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return Float.parseFloat(value.toString());
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            return Boolean.parseBoolean(value.toString());
        }

        // 复杂类型：使用 Hutool 做 JSON 转换
        // 先将 value 转为 JSON 字符串，再转为目标类型
        String json = JSONUtil.toJsonStr(value);
        return JSONUtil.toBean(json, targetType);
    }

    /**
     * 带超时控制的方法执行
     *
     * @param metadata 工具元数据
     * @param args     参数数组
     * @param token    当前用户的 Sa-Token 值
     * @return 执行结果
     * @throws Exception 执行异常
     */
    private Object executeWithTimeout(ToolMetadata metadata, Object[] args, String token) throws Exception {
        Method method = metadata.getMethod();
        Object instance = metadata.getInstance();
        long timeout = metadata.getTimeout();

        // 提交执行任务，在异步线程内显式设置 TTL token 上下文
        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            // 在异步线程内设置当前用户的 token 上下文
            boolean needClearTtl = false;
            if (token != null && !token.isEmpty()) {
                TtlTokenContext.setCurrentToken(token);
                needClearTtl = true;
            }
            try {
                method.setAccessible(true);
                return method.invoke(instance, args);
            } catch (Exception e) {
                log.error("方法调用失败: {}", metadata.getName(), e);
                throw new RuntimeException("方法调用失败: " + e.getMessage(), e);
            } finally {
                // 清理当前线程的 TTL 上下文
                if (needClearTtl) {
                    TtlTokenContext.remove();
                }
            }
        }, threadPool);

        try {
            // 等待执行完成，带超时
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // 超时，取消任务
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            // 执行过程中的异常
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new ServiceException(cause.getMessage());
        }
    }

    /**
     * 构建成功结果
     *
     * @param result 执行结果
     * @return JSON 字符串
     */
    private String buildSuccessResult(Object result) {
        if (Objects.isNull(result)) {
            return "{\"success\": false, \"result\": \"执行失败\"}";
        }

        // 如果结果已经是字符串，直接返回
        if (result instanceof String) {
            return (String) result;
        }

        // 否则转为 JSON
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", true);
        resultMap.put("result", result);
        return JSONUtil.toJsonStr(resultMap);
    }

    /**
     * 构建错误结果
     *
     * @param message 错误信息
     * @return JSON 字符串
     */
    private String buildErrorResult(String message) {
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("success", false);
        errorMap.put("error", message);
        errorMap.put("timestamp", System.currentTimeMillis());
        return JSONUtil.toJsonStr(errorMap);
    }
}
