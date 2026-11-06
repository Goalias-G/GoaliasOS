package com.goalias.mcp.tools.support;

import cn.hutool.json.JSONUtil;
import io.modelcontextprotocol.spec.McpSchema;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Supplier;

/**
 * MCP 工具结果构建工具
 * <p>
 * 统一将业务对象序列化为 JSON 文本返回，异常统一包装为错误结果，
 * 自行控制日期格式与金额单位，规避 SDK 默认序列化差异。
 *
 * @author Goalias
 */
public final class McpResults {

    private McpResults() {
    }

    /**
     * 执行业务逻辑并包装为成功结果（JSON 文本）
     *
     * @param action 业务逻辑
     * @return 工具调用结果
     */
    public static McpSchema.CallToolResult call(Supplier<Object> action) {
        try {
            return success(action.get());
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return error(message);
        }
    }

    /**
     * 成功结果
     *
     * @param result 业务结果对象
     * @return 工具调用结果
     */
    public static McpSchema.CallToolResult success(Object result) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(JSONUtil.toJsonStr(result))
                .build();
    }

    /**
     * 错误结果
     *
     * @param message 错误信息
     * @return 工具调用结果
     */
    public static McpSchema.CallToolResult error(String message) {
        return McpSchema.CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }

    /**
     * 金额分转元（保留两位小数）
     *
     * @param amountCent 金额（分）
     * @return 金额（元）
     */
    public static BigDecimal centToYuan(Long amountCent) {
        return amountCent == null ? BigDecimal.ZERO
                : BigDecimal.valueOf(amountCent).movePointLeft(2).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 金额元转分
     *
     * @param amountYuan 金额（元）
     * @return 金额（分）
     */
    public static long yuanToCent(BigDecimal amountYuan) {
        return amountYuan.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValue();
    }

    /**
     * 读取整型参数（带默认值）
     *
     * @param args          工具入参
     * @param key           参数名
     * @param defaultValue  默认值
     * @return 参数值
     */
    public static int getInt(Map<String, Object> args, String key, int defaultValue) {
        Object value = args.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    /**
     * 读取可空长整型参数
     *
     * @param args 工具入参
     * @param key  参数名
     * @return 参数值（不存在时为 null）
     */
    public static Long getOptionalLong(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    /**
     * 读取字符串参数（带默认值）
     *
     * @param args         工具入参
     * @param key          参数名
     * @param defaultValue 默认值
     * @return 参数值
     */
    public static String getString(Map<String, Object> args, String key, String defaultValue) {
        Object value = args.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    /**
     * 读取必填字符串参数
     *
     * @param args 工具入参
     * @param key  参数名
     * @return 参数值
     * @throws IllegalArgumentException 参数缺失时抛出
     */
    public static String getRequiredString(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (value == null || String.valueOf(value).isBlank()) {
            throw new IllegalArgumentException("缺少必填参数: " + key);
        }
        return String.valueOf(value).trim();
    }

    /**
     * 读取必填金额参数（元）
     *
     * @param args 工具入参
     * @param key  参数名
     * @return 金额（元）
     * @throws IllegalArgumentException 参数缺失或非法时抛出
     */
    public static BigDecimal getRequiredAmount(Map<String, Object> args, String key) {
        Object value = args.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("缺少必填金额参数: " + key);
        }
        return BigDecimal.valueOf(number.doubleValue());
    }
}
