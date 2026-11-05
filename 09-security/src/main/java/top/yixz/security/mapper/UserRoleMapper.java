package top.yixz.security.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户角色关联 Mapper
 *
 * @author yixz
 * @date 2026-10-29
 * @description sec_user_role 是复合主键（user_id + role_id），没有自增列，
 * MyBatis-Plus 的 BaseMapper insert 不适用，所以全部手写 SQL。
 */
@Mapper
public interface UserRoleMapper {

    @Insert("INSERT INTO sec_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insert(@Param("userId") Long userId, @Param("roleId") Long roleId);

    @Delete("DELETE FROM sec_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sec_user_role WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}
