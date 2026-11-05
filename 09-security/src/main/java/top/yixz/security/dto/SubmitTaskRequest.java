package top.yixz.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交任务请求
 *
 * @author yixz
 * @date 2026-10-29
 * @description 没有 owner 字段：归属只能由服务端从令牌写入，
 * 请求体里塞什么都改不了提交人。
 */
@Data
public class SubmitTaskRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最长 100 字")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 2000, message = "内容最长 2000 字")
    private String content;
}
