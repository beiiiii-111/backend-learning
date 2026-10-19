package top.yixz.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（课后挑战）。
 * 挑战要求的三件事在实体上的落点：
 * 1. 事务原子性由 OrderService.createOrder 上的 @Transactional 保证；
 * 2. 逻辑删除：deleted 加 @TableLogic，deleteById 变成 UPDATE deleted=1，查询自动追加 deleted=0；
 * 3. 自动填充：createTime / updateTime / deleted 标 @TableField(fill = ...)，由 MyMetaObjectHandler 统一填。
 */
@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号，业务上唯一 */
    private String orderNo;

    private Long userId;

    private Long productId;

    private Integer quantity;

    /** 订单金额 = 商品单价 * 数量，精确计算用 DECIMAL -> BigDecimal */
    private BigDecimal amount;

    /** 0 待支付 / 1 已支付 / 2 已取消 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
