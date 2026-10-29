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
 * 成绩实体，对应 t_score 表（学生与课程之间的中间表）
 *
 * <p>表里只存 student_id 和 course_id 两个 id，不存姓名和课程名
 * —— 名字改了只需改一处。代价是查的时候要自己 join 出来。</p>
 *
 * @author yixz
 * @description Score
 */
@Data
@TableName("t_score")
public class Score {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生 id，对应 t_student.id */
    private Long studentId;

    /** 课程 id，对应 t_course.id */
    private Long courseId;

    /**
     * DECIMAL(5,2) 分数。
     * 不能用 Float/Double：浮点数存 88.5 可能变成 88.499999，金额和分数一律用 BigDecimal。
     */
    private BigDecimal score;

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
