package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口。
 * Spring Boot 通过 @SpringBootApplication 完成组件扫描与自动装配，
 * 整个应用以一个普通的 main 方法启动 —— 这就是"约定优于配置"。
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
