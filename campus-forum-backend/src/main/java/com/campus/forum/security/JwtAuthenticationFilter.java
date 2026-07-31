package com.campus.forum.security;

import com.campus.forum.entity.Role;
import com.campus.forum.service.UserService;
import com.campus.forum.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器
 * 从请求头 Authorization 中提取 Bearer token，解析并校验，
 * 若有效则构建 LoginUserDetails 与 Authentication 放入 SecurityContext。
 * token 无效或过期则不设置上下文（让后续 Security 抛 401）。
 *
 * @author campus
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Authorization 请求头前缀 */
    private static final String BEARER_PREFIX = "Bearer ";

    /** Authorization 请求头名称 */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** 路径匹配器 */
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** 白名单路径（与 SecurityConfig 中保持一致）
     * 注意：/api/auth/logout 不在白名单中，需要 JWT 过滤器解析 token 并填充 SecurityContext */
    private static final String[] WHITELIST = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/doc.html",
            "/api/swagger-ui/**",
            "/api/v3/api-docs/**",
            "/api/webjars/**",
            "/api/favicon.ico",
            "/api/uploads/**",
            // WebSocket 端点：STOMP 鉴权在 ChannelInterceptor 的 CONNECT 帧完成
            "/ws/**"
    };

    private final JwtUtils jwtUtils;

    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中提取 token
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 解析并校验 token
            if (jwtUtils.validateToken(token)) {
                Claims claims = jwtUtils.parseToken(token);
                Long userId = claims.get(JwtUtils.CLAIM_USER_ID, Long.class);
                if (userId == null) {
                    Object raw = claims.get(JwtUtils.CLAIM_USER_ID);
                    if (raw instanceof Number) {
                        userId = ((Number) raw).longValue();
                    }
                }
                String username = claims.get(JwtUtils.CLAIM_USERNAME, String.class);
                if (username == null) {
                    username = claims.getSubject();
                }

                // 从数据库加载用户真实角色，确保管理员权限注解（hasRole('ADMIN')）生效
                List<Role> roles = userService.getUserRoles(userId);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority(role.getCode()))
                        .collect(Collectors.toList());

                // 构建登录用户详情（token 已校验通过，默认账号启用）
                LoginUserDetails userDetails = new LoginUserDetails(
                        userId,
                        username,
                        null,
                        authorities,
                        true
                );

                // 构建认证令牌并放入 SecurityContext
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.debug("JWT 解析失败，不设置 SecurityContext：{}", e.getMessage());
            // 不设置上下文，让后续 Security 抛出 401
        } finally {
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 判断当前请求是否应跳过过滤器（白名单或 OPTIONS 预检）
     *
     * @param request HTTP 请求
     * @return true 表示跳过
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        for (String pattern : WHITELIST) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从 Authorization 请求头中提取 Bearer token
     *
     * @param request HTTP 请求
     * @return token 字符串（无效时返回 null）
     */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }

}
