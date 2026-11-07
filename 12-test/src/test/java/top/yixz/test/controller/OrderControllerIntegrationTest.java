package top.yixz.test.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成测试：启动完整 Spring 容器，用 MockMvc 走 HTTP 全链路。
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("订单接口集成测试")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("POST /api/orders → 201，返回 id 与折扣总价")
    void create_returns_201_and_total() throws Exception {
        // 12 × 10.00，满 10 件打 9 折 → 108.00
        mvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("{\"item\":\"笔记本\",\"quantity\":12,\"unitPrice\":\"10.00\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.total").value(108.00));
    }

    @Test
    @DisplayName("数量超限 → 400 + 错误信息")
    void invalid_quantity_rejected() throws Exception {
        mvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("{\"item\":\"笔记本\",\"quantity\":0,\"unitPrice\":\"10\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("数量须在 1~99 之间"));
    }

    @Test
    @DisplayName("支付后再支付 → 409")
    void double_pay_conflict() throws Exception {
        String body = "{\"item\":\"鼠标\",\"quantity\":1,\"unitPrice\":\"50\"}";
        String id = mvc.perform(post("/api/orders").contentType("application/json").content(body))
                .andReturn().getResponse().getContentAsString();
        String orderId = id.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(post("/api/orders/" + orderId + "/pay")).andExpect(status().isOk());
        mvc.perform(post("/api/orders/" + orderId + "/pay")).andExpect(status().isConflict());
    }
}
