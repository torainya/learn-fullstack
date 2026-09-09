package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：跨域（CORS）。
 *
 * 什么时候需要 CORS？
 * - 开发时：前端 http://localhost:5173 直连后端 http://localhost:8080，端口不同即"跨域"，
 *   浏览器发 POST 会携带 Origin 头，后端必须明确放行。
 * - 生产时：前端页面由 nginx 托管，/api 走同源反向代理，本质是同源，
 *   但浏览器对 POST 仍会带 Origin 头，所以这里用 allowedOriginPatterns("*") 全放行
 *   （学习项目从简；生产环境更严谨的做法是只放行自己的域名，甚至完全去掉 CORS 配置）。
 *
 * 踩坑记录：如果只允许 localhost:5173，部署到服务器后浏览器提交会直接 403
 * （"Invalid CORS request"），而 curl 测试正常——因为 curl 不带 Origin 头。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
