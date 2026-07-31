package com.campus.forum.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置类
 * 允许前端跨域访问后端接口
 *
 * @author campus
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * 配置跨域映射
     * 允许所有源、所有方法、所有请求头，允许携带凭证，最大有效期 3600 秒
     *
     * @param registry 跨域注册器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许所有源
                .allowedOriginPatterns("*")
                // 允许所有 HTTP 方法
                .allowedMethods("*")
                // 允许所有请求头
                .allowedHeaders("*")
                // 允许携带凭证
                .allowCredentials(true)
                // 预检请求最大有效期（秒）
                .maxAge(3600);
    }

}
