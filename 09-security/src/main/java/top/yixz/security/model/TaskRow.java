package top.yixz.security.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务联查投影
 *
 * @author yixz
 * @date 2026-10-29
 * @description sec_task 联 sec_user 查出来的行，owner / ownerNickname 是提交人的
 * 登录名和姓名。投影对象出 mapper 不出 service，实体不直接暴露给接口。
 */
@Data
public class TaskRow {

    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private String status;
    private Integer score;
    private String comment;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;

    /** 提交人登录名，联查 sec_user.username */
    private String owner;
    /** 提交人姓名，联查 sec_user.nickname */
    private String ownerNickname;
}
