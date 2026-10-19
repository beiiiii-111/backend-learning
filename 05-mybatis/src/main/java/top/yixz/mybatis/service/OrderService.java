package top.yixz.mybatis.service;

import top.yixz.mybatis.entity.Order;

import java.util.List;

/**
 * 订单 Service（课后挑战）
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description OrderService
 */
public interface OrderService extends com.baomidou.mybatisplus.spring.service.IService<Order> {

    /**
     * 创建订单 + 扣库存，两个写操作在同一个事务里：
     * 任何一步抛异常，已写入的订单回滚，库存也不会被扣
     */
    Order createOrder(Long userId, Long productId, Integer quantity);

    List<Order> listOrders();
}
