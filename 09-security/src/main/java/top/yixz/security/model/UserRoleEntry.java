package top.yixz.security.model;

import lombok.Data;

/**
 * 用户角色关联行
 *
 * @author yixz
 * @date 2026-10-29
 * @description sec_user_role 联 sec_role 查出的投影：拿到角色 id 的同时把
 * 角色编码也带出来，RoleMapper 直接返回这个对象，service 层再转 Role 枚举。
 */
@Data
public class UserRoleEntry {

    private Long userId;
    private Long roleId;
    /** 角色编码：STUDENT / TEACHER / ADMIN，与 Role 枚举同名 */
    private String code;
}
