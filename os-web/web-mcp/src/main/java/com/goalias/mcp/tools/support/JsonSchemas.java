package com.goalias.mcp.tools.support;

import io.modelcontextprotocol.spec.McpSchema;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 工具入参 JSON Schema 构建工具
 *
 * @author Goalias
 */
public final class JsonSchemas {

    private JsonSchemas() {
    }

    /**
     * 构建 object 类型入参 Schema
     *
     * @param properties 属性定义（属性名 -> 属性描述 Map）
     * @param required   必填属性名
     * @return JSON Schema
     */
    public static McpSchema.JsonSchema object(Map<String, Map<String, Object>> properties, String... required) {
        Map<String, Object> props = new LinkedHashMap<>(properties);
        List<String> requiredList = List.of(required);
        return new McpSchema.JsonSchema("object", props, requiredList, null, null, null);
    }

    /**
     * 无参数工具的空 Schema
     *
     * @return JSON Schema
     */
    public static McpSchema.JsonSchema empty() {
        return object(Map.of());
    }

    /**
     * 定义单个属性
     *
     * @param type        属性类型（integer/string/number/boolean）
     * @param description 属性描述
     * @return 属性定义
     */
    public static Map<String, Object> property(String type, String description) {
        Map<String, Object> prop = new LinkedHashMap<>();
        prop.put("type", type);
        prop.put("description", description);
        return prop;
    }

    /**
     * 定义带默认值的单个属性
     *
     * @param type        属性类型
     * @param description 属性描述
     * @param defaultValue 默认值
     * @return 属性定义
     */
    public static Map<String, Object> property(String type, String description, Object defaultValue) {
        Map<String, Object> prop = property(type, description);
        prop.put("default", defaultValue);
        return prop;
    }

    /**
     * 定义枚举属性
     *
     * @param description  属性描述
     * @param enumValues   枚举取值
     * @param defaultValue 默认值
     * @return 属性定义
     */
    public static Map<String, Object> enumProperty(String description, List<String> enumValues, String defaultValue) {
        Map<String, Object> prop = property("string", description);
        prop.put("enum", enumValues);
        prop.put("default", defaultValue);
        return prop;
    }
}
