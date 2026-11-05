package top.yixz.security.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import top.yixz.security.security.SecurityUser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应
 *
 * @author yixz
 * @date 2026-10-29
 * @description tokenType 固定 Bearer，前端拼 "Bearer " + token 放请求头。
 */
@Data
@Builder
public class LoginResponse {

    private String token;

    private String tokenType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;

    private String username;

    private String nickname;

    private List<String> roles;

    public static LoginResponse of(String token, LocalDateTime expiresAt, SecurityUser user) {
        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(expiresAt)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(user.getRoleNames())
                .build();
    }
}
