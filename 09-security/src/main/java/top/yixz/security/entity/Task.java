package top.yixz.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务实体
 *
 * @author yixz
 * @date 2026-10-29
 * @description 归属用 ownerId 指向 sec_user.id，不冗余存用户名；
 * status 存字符串（SUBMITTED / REVIEWED），含义见 TaskStatus 枚举。
 */
@Data
@TableName("sec_task")
public class Task {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /** 提交人，指向 sec_user.id，只能由服务端从令牌写入 */
    private Long ownerId;

    private String status;

    /** 分数 0 到 100，未批改时为空 */
    private Integer score;

    private String comment;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;
}
