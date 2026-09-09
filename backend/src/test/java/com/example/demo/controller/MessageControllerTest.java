package com.example.demo.controller;

import com.example.demo.entity.Message;
import com.example.demo.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口层单元测试（WebMvcTest 只加载 Web 层，不启动完整应用，
 * 因此不需要 MySQL/Redis/Kafka —— 测试要快、要隔离）。
 */
@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageService service;

    @Test
    void create_shouldReturn201AndSavedMessage() throws Exception {
        Message saved = new Message("hello");
        saved.setId(1L);
        when(service.create(anyString())).thenReturn(saved);

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_shouldReturn400WhenContentBlank() throws Exception {
        // 校验失败应返回 400，这是"测试先行"思维：先想清楚边界条件
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"  \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void latest_shouldReturnMessageList() throws Exception {
        Message m = new Message("world");
        m.setId(2L);
        when(service.latest()).thenReturn(List.of(m));

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("world"));
    }
}
