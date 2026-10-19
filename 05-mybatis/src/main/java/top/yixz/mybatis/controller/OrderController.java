package top.yixz.mybatis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.mybatis.entity.Order;
import top.yixz.mybatis.service.OrderService;

import java.util.List;

/**
 * 订单接口（课后挑战）
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description OrderController
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单（一个事务里完成 创建订单 + 扣库存）。
     * 故意把 quantity 传得比库存大，就能看到「订单被回滚、库存没变」的事务原子性
     */
    @PostMapping
    public Order create(@RequestParam Long userId, @RequestParam Long productId,
                        @RequestParam Integer quantity) {
        return orderService.createOrder(userId, productId, quantity);
    }

    @GetMapping
    public List<Order> list() {
        return orderService.listOrders();
    }

    @GetMapping("/{id}")
    public Order getById(@PathVariable Long id) {
        return orderService.getById(id);
    }

    /** 逻辑删除：实际执行 UPDATE t_order SET deleted=1，物理行还在 */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return orderService.removeById(id);
    }
}
