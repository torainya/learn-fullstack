package com.example.demo.controller;

import com.example.demo.entity.Message;
import com.example.demo.service.MessageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST 控制器（接口层）。
 * URL 设计遵循 REST 风格：资源用名词，动作用 HTTP 方法。
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    /** 请求体 DTO：用 Bean Validation 做入参校验 */
    public record CreateRequest(
            @NotBlank(message = "内容不能为空")
            @Size(max = 200, message = "内容不能超过 200 字")
            String content) {
    }

    /** POST /api/messages —— 提交一条消息 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message create(@Valid @RequestBody CreateRequest req) {
        return service.create(req.content());
    }

    /** GET /api/messages —— 查询最新 20 条（走 Redis 缓存） */
    @GetMapping
    public List<Message> latest() {
        return service.latest();
    }

    /** GET /api/stats —— 已被 Kafka 消费者处理的消息总数 */
    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of("consumed", service.consumedCount());
    }
}
