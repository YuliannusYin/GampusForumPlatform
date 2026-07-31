package com.campus.forum.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

/**
 * WebSocket 连接事件监听器
 * 监听 STOMP SessionConnectEvent 与 SessionDisconnectEvent，维护 Redis 中的用户在线状态。
 * - 连接建立：set online:{userId} = 1
 * - 连接断开：delete online:{userId}
 *
 * @author campus
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    /** Redis 在线状态 key 前缀，完整 key 为 online:{userId} */
    private static final String ONLINE_KEY_PREFIX = "online:";

    /** Redis 在线状态 value */
    private static final String ONLINE_VALUE = "1";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 会话连接事件：将用户标记为在线
     *
     * @param event 会话连接事件
     */
    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        Long userId = resolveUserId(event);
        if (userId == null) {
            return;
        }
        redisTemplate.opsForValue().set(ONLINE_KEY_PREFIX + userId, ONLINE_VALUE);
        log.debug("用户上线：userId={}", userId);
    }

    /**
     * 会话断开事件：移除用户在线状态
     *
     * @param event 会话断开事件
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        Long userId = resolveUserId(event);
        if (userId == null) {
            return;
        }
        redisTemplate.delete(ONLINE_KEY_PREFIX + userId);
        log.debug("用户离线：userId={}", userId);
    }

    /**
     * 从事件消息的 session attributes 中提取 userId
     *
     * @param event 事件
     * @return 用户 ID，不存在返回 null
     */
    private Long resolveUserId(org.springframework.web.socket.messaging.AbstractSubProtocolEvent event) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                event.getMessage(), StompHeaderAccessor.class);
        if (accessor == null) {
            return null;
        }
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }
        Object userId = sessionAttributes.get(JwtHandshakeInterceptor.SESSION_USER_ID);
        if (userId instanceof Long id) {
            return id;
        }
        if (userId instanceof Number n) {
            return n.longValue();
        }
        return null;
    }

}
