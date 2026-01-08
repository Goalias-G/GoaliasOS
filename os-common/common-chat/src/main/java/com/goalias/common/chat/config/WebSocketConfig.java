package com.goalias.common.chat.config;

import cn.hutool.core.util.StrUtil;
import com.goalias.common.chat.config.properties.WebSocketProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket 配置
 *
 * @author zendwang
 */
@AutoConfiguration
@ConditionalOnProperty(value = "websocket.enabled", havingValue = "true")
@EnableConfigurationProperties(WebSocketProperties.class)
@EnableWebSocket
public class WebSocketConfig {

    @Bean
    public WebSocketConfigurer webSocketConfigurer(WebSocketProperties webSocketProperties) {
        // 如果WebSocket的路径为空，则设置默认路径为 "/ws"
        if (StrUtil.isBlank(webSocketProperties.getPath())) {
            webSocketProperties.setPath("/ws");
        }
        // 如果允许跨域访问的地址为空，则设置为 "*"，表示允许所有来源的跨域请求
        if (StrUtil.isBlank(webSocketProperties.getAllowedOrigins())) {
            webSocketProperties.setAllowedOrigins("*");
        }
        // 返回一个WebSocketConfigurer对象，用于配置WebSocket
        return registry -> registry
                // 添加WebSocket处理程序和拦截器到指定路径，设置允许的跨域来源
                .addHandler(new TextWebSocketHandler(), webSocketProperties.getPath())
                .setAllowedOrigins(webSocketProperties.getAllowedOrigins());
    }
}
