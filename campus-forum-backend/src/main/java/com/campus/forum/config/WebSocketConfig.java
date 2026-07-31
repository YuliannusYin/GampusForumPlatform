package com.campus.forum.config;

import com.campus.forum.security.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 消息代理配置
 * - 端点：/ws/chat，启用 SockJS，允许跨域
 * - 应用目的地前缀：/app
 * - 简单代理：/queue、/topic
 * - 用户目的地前缀：/user（用于点对点推送）
 * - 客户端入站通道注册 JWT 鉴权拦截器
 *
 * @author campus
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** STOMP 鉴权拦截器 */
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    /**
     * 配置消息代理
     *
     * @param registry 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 应用目的地前缀：客户端发送消息到服务端 @MessageMapping 的前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 启用简单内存代理，客户端订阅 /queue、/topic 前缀
        registry.enableSimpleBroker("/queue", "/topic");
        // 点对点推送前缀，最终目的地为 /user/{userId}/queue/...
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 端点
     *
     * @param registry 端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * 配置客户端入站通道，注册 JWT 鉴权拦截器
     *
     * @param registration 通道注册器
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtHandshakeInterceptor);
    }

}
