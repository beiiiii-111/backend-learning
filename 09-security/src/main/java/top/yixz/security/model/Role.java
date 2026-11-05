package top.yixz.security.model;

import java.util.Set;

/**
 * 角色枚举
 *
 * @author yixz
 * @date 2026-10-29
 * @description 角色是运行期数据（存 sec_role 表），权限点跟着角色走留在代码里：
 * 接口绑权限点不绑角色，以后加"教务"角色只需给它配 task:read，接口代码不动。
 */
public enum Role {

    STUDENT(Set.of("task:submit", "task:read")),
    TEACHER(Set.of("task:review", "task:read")),
    ADMIN(Set.of("user:read", "user:write", "task:review", "task:read"));

    private final Set<String> permissions;

    Role(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    /**
     * 角色对应的安全字符串，带 ROLE_ 前缀，供 hasRole 比对
     */
    public String toAuthority() {
        return "ROLE_" + name();
    }
}
