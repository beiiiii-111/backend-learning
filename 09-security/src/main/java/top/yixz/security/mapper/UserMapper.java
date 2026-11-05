package top.yixz.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.yixz.security.entity.User;

/**
 * 账号 Mapper
 *
 * @author yixz
 * @date 2026-10-29
 * @description 通用 CRUD 由 MyBatis-Plus 提供，按 username 查用 LambdaQueryWrapper。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
