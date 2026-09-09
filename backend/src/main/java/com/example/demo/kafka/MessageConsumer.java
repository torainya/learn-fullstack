package com.example.demo.kafka;

import com.example.demo.config.KafkaTopicConfig;
import com.example.demo.entity.Message;
import com.example.demo.repository.MessageRepository;
import com.example.demo.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka 消费者。
 *
 * 注意这里的处理顺序（顺序很重要）：
 * 1. 更新 PostgreSQL 状态为 CONSUMED
 * 2. Redis 计数器 +1
 *
 * 如果处理失败，Spring Kafka 默认会重试后进入 DLT（死信 Topic），
 * 保证消息不丢 —— 这是消息队列最核心的价值之一。
 */
@Component
public class MessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(MessageConsumer.class);

    private final MessageRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;

    public MessageConsumer(MessageRepository repository, RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = KafkaTopicConfig.MESSAGE_TOPIC, groupId = "demo-group")
    public void onMessage(Message message) {
        log.info("收到 Kafka 消息: id={}, content={}", message.getId(), message.getContent());

        repository.findById(message.getId()).ifPresent(m -> {
            m.setStatus(Message.Status.CONSUMED);
            repository.save(m);
        });

        // Redis 计数器：INCR 是原子操作，天然适合做统计
        redisTemplate.opsForValue().increment(MessageService.CONSUMED_COUNTER);
    }
}
