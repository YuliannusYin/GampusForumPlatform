package com.campus.forum.security;

import com.campus.forum.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket STOMP 鉴权拦截器
 * 拦截 CONNECT 帧，从 StompHeaderAccessor 的 "Authorization" header（Bearer token）解析出用户 ID，
 * 存入 session attributes，并将用户 Principal 设置为 userId（供 convertAndSendToUser 使用）。
 * 同时支持从 "token" 原生 header 兜底提取 token。
 *
 * @author campus
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements ChannelInterceptor {

    /** Authorization 请求头名称 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer 前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** 兜底 token 原生 header 名称 */
    private static final String TOKEN_HEADER = "token";

    /** session attributes 中存储 userId 的键 */
    public static final String SESSION_USER_ID = "userId";

    private final JwtUtils jwtUtils;

    /**
     * 在消息发送前拦截，仅处理 CONNECT 命令
     *
     * @param message STOMP 消息
     * @param channel 消息通道
     * @return 原消息；若 CONNECT 鉴权失败则返回 null 拒绝连接
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        // 仅在 CONNECT 阶段进行鉴权
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Long userId = resolveUserId(accessor);
            if (userId == null) {
                log.warn("WebSocket CONNECT 鉴权失败：token 无效或缺失");
                return null;
            }
            // 存入 session attributes，供事件监听器维护在线状态使用
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                sessionAttributes.put(SESSION_USER_ID, userId);
            }
            // 设置 Principal 为 userId，便于 SimpMessagingTemplate.convertAndSendToUser 定位用户
            final Long principalUserId = userId;
            accessor.setUser(new Principal() {
                @Override
                public String getName() {
                    return String.valueOf(principalUserId);
                }
            });
            log.debug("WebSocket CONNECT 鉴权成功，userId={}", userId);
        }
        return message;
    }

    /**
     * 从 STOMP header 中提取并解析 token，返回用户 ID
     * 优先取 Authorization（Bearer），兜底取 token 原生 header
     *
     * @param accessor STOMP header 访问器
     * @return 用户 ID，解析失败返回 null
     */
    private Long resolveUserId(StompHeaderAccessor accessor) {
        String token = null;
        String authHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            token = authHeader.substring(BEARER_PREFIX.length()).trim();
        }
        if (!StringUtils.hasText(token)) {
            String nativeToken = accessor.getFirstNativeHeader(TOKEN_HEADER);
            if (StringUtils.hasText(nativeToken)) {
                token = nativeToken.trim();
            }
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            if (!jwtUtils.validateToken(token)) {
                return null;
            }
            return jwtUtils.getUserIdFromToken(token);
        } catch (Exception e) {
            log.debug("WebSocket token 解析失败：{}", e.getMessage());
            return null;
        }
    }

}
