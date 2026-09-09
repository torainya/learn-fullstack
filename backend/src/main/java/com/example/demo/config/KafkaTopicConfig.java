package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

/**
 * Kafka 配置：声明 Topic。
 * KafkaAdmin 启动时会自动在 Broker 上创建这里声明的 Topic（幂等）。
 * 分区数=3：允许消费者横向扩展；副本数=1：单机学习环境足够。
 */
@Configuration
public class KafkaTopicConfig {

    public static final String MESSAGE_TOPIC = "messages";

    @Bean
    public NewTopic messageTopic() {
        return TopicBuilder.name(MESSAGE_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
