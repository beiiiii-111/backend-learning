package top.yixz.test.service;

import org.springframework.stereotype.Service;
import top.yixz.test.repository.OrderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 订单服务：含业务规则，是单元测试的主要对象。
 *
 * 业务规则：
 * 1. 数量 1~99；单价必须为正；
 * 2. 折扣：满 10 件打 9 折，满 50 件打 8 折；
 * 3. 金额四舍五入保留 2 位小数。
 */
@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order createOrder(String item, int quantity, BigDecimal unitPrice) {
        if (item == null || item.isBlank()) {
            throw new IllegalArgumentException("商品名不能为空");
        }
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("数量须在 1~99 之间");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("单价必须为正数");
        }
        Order order = new Order(null, item, quantity, unitPrice, "CREATED");
        return repository.save(order);
    }

    /** 按数量档位计算折扣价 */
    public BigDecimal calculateTotal(Order order) {
        BigDecimal total = order.getUnitPrice()
                .multiply(BigDecimal.valueOf(order.getQuantity()));
        if (order.getQuantity() >= 50) {
            total = total.multiply(new BigDecimal("0.8"));
        } else if (order.getQuantity() >= 10) {
            total = total.multiply(new BigDecimal("0.9"));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public Order pay(Long id) {
        Order order = repository.findById(id);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在: " + id);
        }
        if (!"CREATED".equals(order.getStatus())) {
            throw new IllegalStateException("仅 CREATED 状态可支付，当前: " + order.getStatus());
        }
        order.setStatus("PAID");
        return repository.save(order);
    }
}
