package top.yixz.test.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;
    private String item;
    private int quantity;
    private BigDecimal unitPrice;
    /** 订单状态：CREATED / PAID / CANCELLED */
    private String status;
}
