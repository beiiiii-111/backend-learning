package top.yixz.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 用户实体，字段与 schema.sql 里的 t_user 一一对应
 *
 * @author yxzhang
 * @date 2026/10/10
 * @description User
 */
@Data
@TableName(value = "t_user", autoResultMap = true)
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** 密码存 SHA-256 摘要，接口不返回 */
    private String password;

    private String nickname;

    private Integer gender;

    private Integer age;

    private String memberLevel;

    private Integer points;

    private BigDecimal balance;

    private Float score;

    private String phone;

    private String email;

    private String avatar;

    private String intro;

    /** JSON 列，autoResultMap = true 才会走 typeHandler 反序列化 */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> tags;

    /**
     * 练习：score 之外的时间字段都补 @JsonFormat。
     * score 是 Float（近似小数），没有"格式"可言，Jackson 也不支持对数值用日期 pattern，
     * 所以格式化只发生在 DATE / TIME / DATETIME 这几类字段上。
     * 不加注解时 Jackson 3 默认输出带 T 的 ISO 格式（如 1995-01-15T00:00:00），
     * 加上后输入输出都是 SQL 风格的 yyyy-MM-dd HH:mm:ss。
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime workTime;

    private Boolean vip;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;

    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;
}
