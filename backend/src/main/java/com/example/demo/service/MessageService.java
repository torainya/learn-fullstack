package com.example.demo.service;

import com.example.demo.config.KafkaTopicConfig;
import com.example.demo.entity.Message;
import com.example.demo.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 业务层，演示一次"写入 -> 发消息 -> 缓存"的完整链路：
 *
 * 1. 消息先落库 PostgreSQL（保证不丢，持久化是最终事实来源）
 * 2. 再发送到 Kafka（异步解耦，由消费者做后续处理）
 * 3. 查询走 Redis 缓存（减轻数据库压力）
 *
 * 这就是缓存、消息队列、数据库三者最典型的协作方式。
 */
@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    public static final String CACHE_KEY = "messages:latest";
    public static final String CONSUMED_COUNTER = "metrics:consumed";

    private final MessageRepository repository;
    private final KafkaTemplate<String, Message> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public MessageService(MessageRepository repository,
                          KafkaTemplate<String, Message> kafkaTemplate,
                          RedisTemplate<String, Object> redisTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    /** 创建消息：先落库（PENDING），再投递到 Kafka */
    @Transactional
    public Message create(String content) {
        Message saved = repository.save(new Message(content));
        kafkaTemplate.send(KafkaTopicConfig.MESSAGE_TOPIC, String.valueOf(saved.getId()), saved);
        log.info("消息已落库并发送到 Kafka, id={}", saved.getId());
        redisTemplate.delete(CACHE_KEY); // 新消息到达，让缓存失效（Cache Aside 模式）
        return saved;
    }

    /** 查询最新消息：先查 Redis 缓存，未命中再查 MySQL 并回填（Cache Aside 读路径） */
    @SuppressWarnings("unchecked")
    public List<Message> latest() {
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            log.debug("缓存命中");
            return (List<Message>) list;
        }
        List<Message> fromDb = repository.findTop20ByOrderByCreatedAtDesc();
        if (!fromDb.isEmpty()) {
            redisTemplate.opsForValue().set(CACHE_KEY, fromDb, 60, TimeUnit.SECONDS);
        }
        return fromDb;
    }

    /** 从 Redis 读取消费者处理条数（演示 Redis 计数器） */
    public long consumedCount() {
        Object v = redisTemplate.opsForValue().get(CONSUMED_COUNTER);
        return v instanceof Number n ? n.longValue() : 0L;
    }
}
