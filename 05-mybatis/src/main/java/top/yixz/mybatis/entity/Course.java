package top.yixz.mybatis.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体（课后进阶 1）。
 * 字段类型对照笔记 2.5 的映射表来选：
 * id          -> BIGINT UNSIGNED 自增主键 -> Long
 * name/teacher-> VARCHAR                -> String
 * credit      -> DECIMAL(3,1) 学分可能出现 2.5 这种半学分，金额/精确小数用定点数，不用 FLOAT -> BigDecimal
 * create/update_time -> DATETIME        -> LocalDateTime，交给 MetaObjectHandler 自动填充
 */
@Data
@TableName("t_course")
public class Course {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String teacher;

    private BigDecimal credit;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
