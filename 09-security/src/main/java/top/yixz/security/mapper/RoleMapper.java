package top.yixz.security.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.yixz.security.model.UserRoleEntry;

import java.util.List;

/**
 * 角色 Mapper
 *
 * @author yixz
 * @date 2026-10-29
 * @description 只查角色字典和关联，权限点本身留在 Role 枚举里不进库。
 */
@Mapper
public interface RoleMapper {

    /**
     * 某用户的全部角色关联（含角色编码），认证与改角色后重新授权都用它
     */
    @Select("""
            SELECT ur.user_id, ur.role_id, r.code
            FROM sec_user_role ur
            JOIN sec_role r ON r.id = ur.role_id
            WHERE ur.user_id = #{userId}""")
    List<UserRoleEntry> selectByUserId(@Param("userId") Long userId);

    /**
     * 角色编码转 id，新建账号写关联表时用
     */
    @Select("SELECT id FROM sec_role WHERE code = #{code}")
    Long selectIdByCode(@Param("code") String code);
}
