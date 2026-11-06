package com.goalias.mcp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goalias.mcp.auth.McpApiKeyFilter;
import com.goalias.mcp.tools.McpToolContributor;
import com.goalias.mcp.tools.support.Jackson2McpJsonMapper;
import com.goalias.mcp.tools.support.PermissiveJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * MCP Server 装配配置
 * <p>
 * 独立 Servlet 注册 Streamable HTTP 传输端点（不经过 DispatcherServlet，
 * 因此也不经过 Sa-Token MVC 拦截器，认证由 McpApiKeyFilter 独立完成）。
 * 通过 mcp.enabled 显式开启，Key 未配置或强度不足时启动即失败（fail-fast）。
 *
 * @author Goalias
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(McpServerProperties.class)
@ConditionalOnProperty(prefix = "mcp", name = "enabled", havingValue = "true")
public class McpServerConfiguration {

    /**
     * Streamable HTTP 传输提供者（本身即一个 Servlet）。
     * 显式注入 Jackson 2 版 McpJsonMapper：默认实现位于 mcp-json-jackson3（已被排除），
     * 其 Jackson 3 依赖与 Spring Boot 管理的 Jackson 2 同包名冲突。
     */
    @Bean
    public HttpServletStreamableServerTransportProvider mcpTransportProvider(McpServerProperties properties,
                                                                             ObjectMapper objectMapper) {
        validateApiKey(properties);
        return HttpServletStreamableServerTransportProvider.builder()
                .mcpEndpoint(properties.getEndpoint())
                .jsonMapper(new Jackson2McpJsonMapper(objectMapper))
                .build();
    }

    /**
     * MCP 同步服务端：装配全部工具提供者贡献的工具。
     * jsonMapper / jsonSchemaValidator 需显式指定，否则 SDK 走 ServiceLoader
     * 加载被排除的 jackson3 默认实现导致反序列化失败。
     */
    @Bean(destroyMethod = "close")
    public McpSyncServer mcpServer(HttpServletStreamableServerTransportProvider transportProvider,
                                   McpServerProperties properties,
                                   ObjectMapper objectMapper,
                                   List<McpToolContributor> contributors) {
        List<McpServerFeatures.SyncToolSpecification> toolSpecifications = contributors.stream()
                .flatMap(contributor -> contributor.toolSpecifications().stream())
                .toList();
        McpSyncServer server = McpServer.sync(transportProvider)
                .serverInfo(properties.getServerName(), properties.getServerVersion())
                .capabilities(McpSchema.ServerCapabilities.builder().tools(false).build())
                .tools(toolSpecifications)
                .jsonMapper(new Jackson2McpJsonMapper(objectMapper))
                .jsonSchemaValidator(new PermissiveJsonSchemaValidator())
                .build();
        log.info("[MCP] Server 启动完成，端点 {}，注册工具数 {}",
                properties.getEndpoint(), toolSpecifications.size());
        return server;
    }

    /**
     * MCP 端点 Servlet 注册（相对于 context-path，完整路径为 /goalias-os/mcp）
     */
    @Bean
    public ServletRegistrationBean<HttpServletStreamableServerTransportProvider> mcpServlet(
            HttpServletStreamableServerTransportProvider transportProvider, McpServerProperties properties) {
        return new ServletRegistrationBean<>(transportProvider, properties.getEndpoint());
    }

    /**
     * MCP 端点固定 Key 认证过滤器
     */
    @Bean
    public FilterRegistrationBean<McpApiKeyFilter> mcpApiKeyFilter(McpServerProperties properties) {
        FilterRegistrationBean<McpApiKeyFilter> registration =
                new FilterRegistrationBean<>(new McpApiKeyFilter(properties.getApiKey()));
        registration.addUrlPatterns(properties.getEndpoint());
        registration.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    /**
     * 启动 fail-fast：启用状态下 Key 必须已配置且具备基本强度
     */
    private void validateApiKey(McpServerProperties properties) {
        if (!StringUtils.hasText(properties.getApiKey()) || properties.getApiKey().trim().length() < 16) {
            throw new IllegalStateException(
                    "[MCP] 已启用 MCP Server（mcp.enabled=true），但 mcp.api-key 未配置或长度小于16，"
                            + "请通过环境变量 MCP_API_KEY 配置客户端认证 Key");
        }
    }
}
