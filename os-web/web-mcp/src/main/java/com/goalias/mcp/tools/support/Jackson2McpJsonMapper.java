package com.goalias.mcp.tools.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;

import java.io.IOException;

/**
 * 基于 Jackson 2 的 {@link McpJsonMapper} 适配器
 * <p>
 * MCP SDK 1.1.x 默认的 JSON 模块绑定 Jackson 3（tools.jackson），
 * 与 Spring Boot 管理的 Jackson 2 / jackson-annotations 2.x 存在同包名冲突，
 * 故排除官方 jackson3 模块，改用应用现有的 Jackson 2 ObjectMapper 委托实现。
 *
 * @author Goalias
 */
public final class Jackson2McpJsonMapper implements McpJsonMapper {

    private final ObjectMapper objectMapper;

    public Jackson2McpJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T readValue(String content, Class<T> type) throws IOException {
        return objectMapper.readValue(content, type);
    }

    @Override
    public <T> T readValue(byte[] content, Class<T> type) throws IOException {
        return objectMapper.readValue(content, type);
    }

    @Override
    public <T> T readValue(String content, TypeRef<T> type) throws IOException {
        return objectMapper.readValue(content, objectMapper.getTypeFactory().constructType(type.getType()));
    }

    @Override
    public <T> T readValue(byte[] content, TypeRef<T> type) throws IOException {
        return objectMapper.readValue(content, objectMapper.getTypeFactory().constructType(type.getType()));
    }

    @Override
    public <T> T convertValue(Object value, Class<T> type) {
        return objectMapper.convertValue(value, type);
    }

    @Override
    public <T> T convertValue(Object value, TypeRef<T> type) {
        return objectMapper.convertValue(value, objectMapper.getTypeFactory().constructType(type.getType()));
    }

    @Override
    public String writeValueAsString(Object value) throws IOException {
        return objectMapper.writeValueAsString(value);
    }

    @Override
    public byte[] writeValueAsBytes(Object value) throws IOException {
        return objectMapper.writeValueAsBytes(value);
    }
}
