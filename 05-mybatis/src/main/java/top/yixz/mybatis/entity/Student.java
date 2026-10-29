package top.yixz.mybatis.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生实体，对应 t_student 表
 *
 * <p>列名 class_name 靠 mybatis-plus.configuration.map-underscore-to-camel-case
 * 自动映射到属性 className，不需要写 @TableField。</p>
 *
 * @author yixz
 * @description Student
 */
@Data
@TableName("t_student")
public class Student {

    /** BIGINT UNSIGNED 自增主键，Java 用 Long */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** VARCHAR(32) 姓名 */
    private String name;

    /** TINYINT 性别：0 未知 / 1 男 / 2 女 */
    private Integer gender;

    /** TINYINT UNSIGNED 年龄 */
    private Integer age;

    /** VARCHAR(32) 班级，表里有 idx_class_name 索引 */
    private String className;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * TINYINT 逻辑删除标记。
     * 加了 @TableLogic 之后：removeById 发的是 UPDATE ... SET deleted=1，
     * 所有自动生成的查询都会追加 AND deleted=0。
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
