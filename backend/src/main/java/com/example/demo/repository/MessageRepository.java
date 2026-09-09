package com.example.demo.repository;

import com.example.demo.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 数据访问层（DAO）。
 * Spring Data JPA 会根据方法名自动生成 SQL，无需手写实现。
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /** 查询最新 20 条消息（方法名即查询语言） */
    List<Message> findTop20ByOrderByCreatedAtDesc();
}
