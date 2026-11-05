package top.yixz.security.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 账号聚合
 *
 * @author yixz
 * @date 2026-10-29
 * @description 认证需要"账号 + 角色"两个信息，而 User 实体只是 sec_user 一张表的
 * 映射。让实体实现 UserDetails 会把表结构和安全框架绑在一起，所以另起这个聚合类，
 * 由 AccountUserDetailsService 组装后交给 SecurityUser。
 */
@Getter
@Builder
public class Account {

    private Long id;
    private String username;
    /** BCrypt 密文，只用于框架比对，不允许出现在任何响应里 */
    private String password;
    private String nickname;
    private boolean enabled;
    private List<Role> roles;

    public List<String> getRoleNames() {
        return roles.stream().map(Enum::name).toList();
    }
}
