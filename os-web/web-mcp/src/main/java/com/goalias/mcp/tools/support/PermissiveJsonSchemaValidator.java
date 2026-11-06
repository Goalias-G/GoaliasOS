package com.goalias.mcp.tools.support;

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;

import java.util.Map;

/**
 * 宽松的 JSON Schema 校验器
 * <p>
 * 官方 jackson3 模块同时提供了默认 Schema 校验器，随模块一并排除后，
 * 此处提供放行实现：工具入参均由 handler 内的防御性解析
 * （默认值兜底 + 缺参/类型错误统一转为错误结果）完成校验。
 *
 * @author Goalias
 */
public final class PermissiveJsonSchemaValidator implements JsonSchemaValidator {

    @Override
    public ValidationResponse validate(Map<String, Object> arguments, Object schema) {
        return ValidationResponse.asValid(null);
    }
}
