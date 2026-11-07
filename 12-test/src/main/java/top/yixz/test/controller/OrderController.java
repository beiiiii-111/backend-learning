package top.yixz.test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.yixz.test.service.Order;
import top.yixz.test.service.OrderService;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        String item = (String) body.get("item");
        int quantity = Integer.parseInt(String.valueOf(body.get("quantity")));
        BigDecimal price = new BigDecimal(String.valueOf(body.get("unitPrice")));
        Order order = service.createOrder(item, quantity, price);
        return Map.of("id", order.getId(), "item", order.getItem(),
                "quantity", order.getQuantity(), "status", order.getStatus(),
                "total", service.calculateTotal(order));
    }

    @PostMapping("/{id}/pay")
    public Map<String, Object> pay(@PathVariable Long id) {
        Order order = service.pay(id);
        return Map.of("id", order.getId(), "status", order.getStatus());
    }

    /** 全局兜底演示：参数错误 400，状态冲突 409 */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }
}
