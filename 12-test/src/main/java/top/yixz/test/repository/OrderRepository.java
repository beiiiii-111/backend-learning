package top.yixz.test.repository;

import org.springframework.stereotype.Repository;
import top.yixz.test.service.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存版订单仓库，模拟真实 DAO。
 * 单元测试里会用 Mockito 把它 mock 掉，不依赖这份数据。
 */
@Repository
public class OrderRepository {

    private final Map<Long, Order> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong();

    public Order save(Order order) {
        if (order.getId() == null) {
            order.setId(idGen.incrementAndGet());
        }
        store.put(order.getId(), order);
        return order;
    }

    public Order findById(Long id) {
        return store.get(id);
    }

    public int count() {
        return store.size();
    }
}
