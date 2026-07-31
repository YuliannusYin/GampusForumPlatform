package com.campus.forum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 接口文档配置
 * 适配 SpringBoot 3.x 与 Knife4j 4.x，使用 io.swagger.v3.oas.models 包
 *
 * @author campus
 */
@Configuration
public class Knife4jConfig {

    /**
     * 注册 OpenAPI 文档元信息
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园论坛API")
                        .version("1.0.0")
                        .description("校园论坛与社区交流平台接口文档"));
    }

    /**
     * 注册接口分组：默认分组包含所有接口
     *
     * @return GroupedOpenApi 实例
     */
    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/**")
                .build();
    }

}
