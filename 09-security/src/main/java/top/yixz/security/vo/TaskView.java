package top.yixz.security.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import top.yixz.security.model.TaskRow;
import top.yixz.security.model.TaskStatus;

import java.time.LocalDateTime;

/**
 * 任务视图
 *
 * @author yixz
 * @date 2026-10-29
 * @description statusText 是后端给的中文，前端直接显示不做映射。
 * score / comment / reviewedAt 在待批改时为 null。
 */
@Data
@Builder
public class TaskView {

    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private String owner;
    private String ownerNickname;
    private String status;
    private String statusText;
    private Integer score;
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    public static TaskView from(TaskRow row) {
        return TaskView.builder()
                .id(row.getId())
                .title(row.getTitle())
                .content(row.getContent())
                .ownerId(row.getOwnerId())
                .owner(row.getOwner())
                .ownerNickname(row.getOwnerNickname())
                .status(row.getStatus())
                .statusText(TaskStatus.from(row.getStatus()).getText())
                .score(row.getScore())
                .comment(row.getComment())
                .submittedAt(row.getSubmittedAt())
                .reviewedAt(row.getReviewedAt())
                .build();
    }
}
