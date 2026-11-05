package top.yixz.security.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 批改请求
 *
 * @author yixz
 * @date 2026-10-29
 * @description 分数 0 到 100；comment 可空但最长 500 字。
 * 注意 Controller 参数上必须加 @Valid，只写约束注解校验不会触发。
 */
@Data
public class ReviewTaskRequest {

    @NotNull(message = "分数不能为空")
    @Min(value = 0, message = "分数最低 0 分")
    @Max(value = 100, message = "分数最高 100 分")
    private Integer score;

    @Size(max = 500, message = "评语最长 500 字")
    private String comment;
}
