package top.yixz.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 新建账号请求
 *
 * @author yixz
 * @date 2026-10-29
 * @description roles 传角色编码数组，如 ["STUDENT"] 或 ["TEACHER", "ADMIN"]。
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度 3 到 32 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度 6 到 64 位")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String nickname;

    @NotEmpty(message = "角色不能为空")
    private List<String> roles;
}
