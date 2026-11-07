package top.yixz.test.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import top.yixz.test.repository.OrderRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 单元测试：只测 OrderService 自身逻辑，Repository 用 Mockito 替身。
 * 不启动 Spring 容器，运行速度快。
 */
@DisplayName("OrderService 单元测试")
class OrderServiceTest {

    private OrderRepository repository;
    private OrderService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(OrderRepository.class);
        service = new OrderService(repository);
    }

    @Test
    @DisplayName("正常下单：仓库 save 被调用，状态为 CREATED")
    void create_ok() {
        Order saved = new Order(1L, "键盘", 2, new BigDecimal("199"), "CREATED");
        when(repository.save(any(Order.class))).thenReturn(saved);

        Order order = service.createOrder("键盘", 2, new BigDecimal("199"));

        assertThat(order.getStatus()).isEqualTo("CREATED");
        verify(repository).save(any(Order.class));
    }

    @Test
    @DisplayName("下单数量越界 → IllegalArgumentException")
    void create_quantity_out_of_range() {
        assertThatThrownBy(() -> service.createOrder("鼠标", 100, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1~99");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("商品名为空或 null → 拒绝")
    void create_blank_item(String item) {
        assertThatThrownBy(() -> service.createOrder(item, 1, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("商品名");
    }

    @ParameterizedTest
    @CsvSource({
            "1,  100.00, 100.00",   // 无折扣
            "9,  100.00, 900.00",   // 无折扣
            "10, 100.00, 900.00",   // 9 折
            "49, 100.00, 4410.00",  // 9 折
            "50, 100.00, 4000.00",  // 8 折
            "99, 10.555, 835.96"    // 8 折 + 四舍五入
    })
    @DisplayName("折扣档位与金额精度（参数化）")
    void discount_tiers(int quantity, String price, String expected) {
        Order order = new Order(1L, "测试", quantity, new BigDecimal(price), "CREATED");
        assertThat(service.calculateTotal(order)).isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -3})
    @DisplayName("单价/数量为非正数 → 拒绝")
    void create_non_positive(int quantity) {
        assertThatThrownBy(() -> service.createOrder("x", quantity, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("支付：CREATED → PAID")
    void pay_ok() {
        Order saved = new Order(7L, "显示器", 1, new BigDecimal("999"), "CREATED");
        when(repository.findById(7L)).thenReturn(saved);
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order paid = service.pay(7L);

        assertThat(paid.getStatus()).isEqualTo("PAID");
    }

    @Test
    @DisplayName("订单不存在 → IllegalArgumentException")
    void pay_not_found() {
        when(repository.findById(999L)).thenReturn(null);
        assertThatThrownBy(() -> service.pay(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("重复支付 → IllegalStateException")
    void pay_twice_rejected() {
        Order paid = new Order(7L, "显示器", 1, new BigDecimal("999"), "PAID");
        when(repository.findById(7L)).thenReturn(paid);
        assertThatThrownBy(() -> service.pay(7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREATED");
    }
}
