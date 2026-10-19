package top.yixz.mybatis.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.yixz.mybatis.entity.Order;
import top.yixz.mybatis.entity.Product;
import top.yixz.mybatis.mapper.OrderMapper;
import top.yixz.mybatis.mapper.ProductMapper;
import top.yixz.mybatis.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author yxzhang
 * @date 2026/10/10
 * @description OrderServiceImpl
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final ProductMapper productMapper;

    public OrderServiceImpl(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 演示事务原子性的写法故意和 createUser 一样：先写库，再校验，校验失败抛异常回滚。
     *
     * 成功路径：INSERT t_order -> UPDATE t_product SET stock = stock - n
     * 失败路径：INSERT t_order -> 发现库存不够 -> 抛异常 -> 订单这一行被回滚
     *
     * 注意：@Transactional 靠 Spring 代理生效，Controller 调进来的就是代理方法，
     * 如果在这个类里 this.createOrder() 自调用，事务会失效。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Order createOrder(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("userId / productId 不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }

        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在：" + productId);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus(0);
        // 第一步写库：订单先落进去（createTime / deleted 由 MetaObjectHandler 自动填充）
        save(order);

        // 第二步：校验库存，不够就抛异常，观察第一步的 insert 是否被回滚
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足：剩余 " + product.getStock() + "，需要 " + quantity + "，事务回滚");
        }

        // 第三步：扣库存
        product.setStock(product.getStock() - quantity);
        productMapper.updateById(product);

        return order;
    }

    @Override
    public List<Order> listOrders() {
        return list();
    }

    /** 简单生成订单号：时间戳 + 随机三位，够演示用；生产环境要用发号器或数据库序列 */
    private String generateOrderNo() {
        return "ORD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
