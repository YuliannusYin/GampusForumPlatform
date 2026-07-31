package com.campus.forum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园论坛与社区交流平台后端主启动类
 *
 * @author campus
 */
@SpringBootApplication
@MapperScan("com.campus.forum.mapper")
public class CampusForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusForumApplication.class, args);
        System.out.println("====== CampusForumPlatform 后端服务启动成功 ======");
    }

}
