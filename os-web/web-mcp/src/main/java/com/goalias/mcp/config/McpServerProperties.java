package com.goalias.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MCP Server 配置属性
 *
 * @author Goalias
 */
@Data
@ConfigurationProperties(prefix = "mcp")
public class McpServerProperties {

    /**
     * 是否启用 MCP Server
     */
    private boolean enabled = false;

    /**
     * 应用内端点路径（context-path 之下），完整路径为 /goalias-os/mcp
     */
    private String endpoint = "/mcp";

    /**
     * 客户端认证固定 Key，通过 Authorization: Bearer <key> 传递
     */
    private String apiKey;

    /**
     * 服务端名称
     */
    private String serverName = "goalias-os";

    /**
     * 服务端版本
     */
    private String serverVersion = "1.0.0";
}
