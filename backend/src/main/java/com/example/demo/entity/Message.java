package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息实体（对应数据库表 messages）。
 * 生命周期：用户提交 -> 保存为 PENDING -> 被 Kafka 消费后变为 CONSUMED。
 */
@Entity
@Table(name = "messages")
public class Message {

    public enum Status { PENDING, CONSUMED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 消息内容 */
    @Column(nullable = false, length = 200)
    private String content;

    /** 处理状态：PENDING = 已落库未消费，CONSUMED = Kafka 消费者已处理 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Message() {
    }

    public Message(String content) {
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
