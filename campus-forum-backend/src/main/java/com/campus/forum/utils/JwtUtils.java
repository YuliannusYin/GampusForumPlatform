package com.campus.forum.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 基于 jjwt 0.12.x 实现，提供 token 生成、解析、校验等功能
 *
 * @author campus
 */
@Slf4j
@Component
public class JwtUtils {

    /** JWT Claim 中的用户 ID 键 */
    public static final String CLAIM_USER_ID = "userId";

    /** JWT Claim 中的用户名键 */
    public static final String CLAIM_USERNAME = "username";

    /** Token 类型：access */
    public static final String TOKEN_TYPE_ACCESS = "access";

    /** Token 类型：refresh */
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    /** JWT Claim 中的 token 类型键 */
    public static final String CLAIM_TOKEN_TYPE = "type";

    @Value("${jwt.secret}")
    private String secret;

    /** 访问令牌过期时间（秒） */
    @Value("${jwt.access-token-expire}")
    private long accessTokenExpire;

    /** 刷新令牌过期时间（秒） */
    @Value("${jwt.refresh-token-expire}")
    private long refreshTokenExpire;

    /** 签名密钥对象 */
    private SecretKey key;

    /**
     * 初始化签名密钥
     * 校验密钥长度，secret 字节长度需 ≥32（256 bit）以满足 HS256 算法要求
     */
    @PostConstruct
    public void init() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("jwt.secret 长度需 ≥32 字符（256 bit），请检查配置");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("JwtUtils 初始化完成，accessTokenExpire={}s, refreshTokenExpire={}s",
                accessTokenExpire, refreshTokenExpire);
    }

    /**
     * 生成访问令牌（access token）
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return access token 字符串
     */
    public String generateAccessToken(Long userId, String username) {
        return buildToken(userId, username, TOKEN_TYPE_ACCESS, accessTokenExpire);
    }

    /**
     * 生成刷新令牌（refresh token）
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @return refresh token 字符串
     */
    public String generateRefreshToken(Long userId, String username) {
        return buildToken(userId, username, TOKEN_TYPE_REFRESH, refreshTokenExpire);
    }

    /**
     * 构建 token 的内部方法
     *
     * @param userId      用户 ID
     * @param username    用户名
     * @param tokenType   token 类型
     * @param expireSeconds 过期时间（秒）
     * @return token 字符串
     */
    private String buildToken(Long userId, String username, String tokenType, long expireSeconds) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000L);
        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 token，返回 Claims
     *
     * @param token token 字符串
     * @return Claims 载荷
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 token 中获取用户 ID
     *
     * @param token token 字符串
     * @return 用户 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get(CLAIM_USER_ID);
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 从 token 中获取用户名
     *
     * @param token token 字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        Object username = claims.get(CLAIM_USERNAME);
        return username != null ? username.toString() : claims.getSubject();
    }

    /**
     * 校验 token 是否有效（签名正确且未过期）
     *
     * @param token token 字符串
     * @return 有效返回 true，否则返回 false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("token 校验失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取访问令牌过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public long getAccessTokenExpire() {
        return accessTokenExpire;
    }

    /**
     * 获取刷新令牌过期时间（秒）
     *
     * @return 过期时间（秒）
     */
    public long getRefreshTokenExpire() {
        return refreshTokenExpire;
    }

}
