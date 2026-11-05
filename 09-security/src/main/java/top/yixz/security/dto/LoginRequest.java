package top.yixz.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求
 *
 * @author yixz
 * @date 2026-10-29
 * @description 两个都不能为空；缺失时全局异常处理器取第一条校验消息返回 400。
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
