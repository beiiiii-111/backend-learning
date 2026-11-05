package top.yixz.security.vo;

import lombok.Builder;
import lombok.Data;
import top.yixz.security.security.SecurityUser;

import java.util.List;

/**
 * 账号视图
 *
 * @author yixz
 * @date 2026-10-29
 * @description 任何接口的响应里都不能出现 password 字段，出参一律用这个视图。
 * authorities 同时包含 ROLE_ 前缀的角色名和权限点，前端按它控制菜单显示。
 */
@Data
@Builder
public class UserProfile {

    private String username;
    private String nickname;
    private List<String> roles;
    private List<String> authorities;
    private boolean enabled;

    public static UserProfile from(SecurityUser user) {
        return UserProfile.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(user.getRoleNames())
                .authorities(user.getAuthorityStrings())
                .enabled(user.isEnabled())
                .build();
    }
}
