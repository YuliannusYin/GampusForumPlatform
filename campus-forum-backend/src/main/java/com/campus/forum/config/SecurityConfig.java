package com.campus.forum.config;

import com.campus.forum.security.JwtAccessDeniedHandler;
import com.campus.forum.security.JwtAuthenticationEntryPoint;
import com.campus.forum.security.JwtAuthenticationFilter;
import com.campus.forum.security.LoginUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;

/**
 * Spring Security 配置类
 * 基于 JWT 无状态认证，配置白名单、异常处理、JWT 过滤器等
 *
 * @author campus
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT 认证过滤器 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 认证失败入口（返回 401） */
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /** 权限不足处理器（返回 403） */
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * 安全过滤器链配置
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（前后端分离无需开启）
                .csrf(csrf -> csrf.disable())
                // 无状态会话
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 请求授权配置
                .authorizeHttpRequests(auth -> auth
                        // 白名单路径（Controller 已在 @RequestMapping 中带 /api 前缀，无需 context-path）
                        // 注意：/api/auth/logout 不在白名单中，需登录后访问
                        .requestMatchers(
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
                        ).permitAll()
                        // 公开浏览接口：板块、帖子、标签、用户主页的查询对游客开放，
                        // 写操作（POST/PUT/DELETE）由 @PreAuthorize 在方法级拦截
                        .requestMatchers(
                                org.springframework.http.HttpMethod.GET,
                                "/api/sections",
                                "/api/sections/**",
                                "/api/posts",
                                "/api/posts/**",
                                "/api/tags",
                                "/api/tags/**",
                                "/api/users",
                                "/api/users/**",
                                "/api/clubs",
                                "/api/clubs/**",
                                "/api/comments/**"
                        ).permitAll()
                        // 放行 OPTIONS 预检请求
                        .requestMatchers("OPTIONS").permitAll()
                        // 其余请求需认证
                        .anyRequest().authenticated())
                // 异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // 在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器：BCrypt
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     *
     * @param authenticationConfiguration 认证配置
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 默认 UserDetailsService 占位实现
     * Task 5 实现用户登录逻辑后会通过自定义 Bean 覆盖此实现
     *
     * @return 默认 UserDetailsService
     */
    @Bean
    @ConditionalOnMissingBean
    public UserDetailsService defaultUserDetailsService() {
        return username -> new LoginUserDetails(
                0L,
                username,
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                true
        );
    }

}
