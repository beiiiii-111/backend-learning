package top.yixz.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.yixz.security.common.BusinessException;
import top.yixz.security.common.ErrorCode;
import top.yixz.security.dto.CreateUserRequest;
import top.yixz.security.entity.User;
import top.yixz.security.mapper.RoleMapper;
import top.yixz.security.mapper.UserMapper;
import top.yixz.security.mapper.UserRoleMapper;
import top.yixz.security.model.Role;
import top.yixz.security.model.UserRoleEntry;
import top.yixz.security.vo.UserProfile;

import java.util.List;

/**
 * 账号管理服务
 *
 * @author yixz
 * @date 2026-10-29
 * @description 新建账号密码必须先 BCrypt 再入库；角色关联和账号要同一个事务，
 * 只写了 sec_user 没写 sec_user_role 的账号是登录不上的（这是常见坑）。
 */
@Service
public class UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, RoleMapper roleMapper,
                       UserRoleMapper userRoleMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserProfile> listUsers() {
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .orderByAsc(User::getId)).stream()
                .map(this::toProfile)
                .toList();
    }

    @Transactional
    public UserProfile createUser(CreateUserRequest request) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (exists != null && exists > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        List<Role> roles = parseRoles(request.getRoles());

        User user = new User();
        user.setUsername(request.getUsername());
        // 密码入库前必须加密，明文进库这条红线不能碰
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEnabled(1);
        userMapper.insert(user);

        for (Role role : roles) {
            Long roleId = roleMapper.selectIdByCode(role.name());
            if (roleId == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在：" + role.name());
            }
            userRoleMapper.insert(user.getId(), roleId);
        }
        return profile(user, roles);
    }

    /**
     * 启用/停用账号：停用后新登录立刻被 30002 拦下，
     * 已签发的令牌也活不过下一次请求（JWT 过滤器每请求回库校验 enabled）。
     * 自己停自己等于把自己锁在门外，给 30006。
     */
    @Transactional
    public UserProfile toggleStatus(String username, String currentUsername, boolean enabled) {
        if (username.equals(currentUsername) && !enabled) {
            throw new BusinessException(ErrorCode.CANNOT_DISABLE_SELF);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setEnabled(enabled ? 1 : 0);
        userMapper.updateById(user);
        return toProfile(user);
    }

    @Transactional
    public void deleteUser(String username, String currentUsername) {
        if (username.equals(currentUsername)) {
            throw new BusinessException(ErrorCode.CANNOT_DELETE_SELF);
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        userRoleMapper.deleteByUserId(user.getId());
        userMapper.deleteById(user.getId());
    }

    private List<Role> parseRoles(List<String> codes) {
        try {
            return codes.stream().map(Role::valueOf).toList();
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "角色不存在：" + e.getMessage());
        }
    }

    private UserProfile toProfile(User user) {
        List<Role> roles = roleMapper.selectByUserId(user.getId()).stream()
                .map(UserRoleEntry::getCode)
                .map(Role::valueOf)
                .toList();
        return profile(user, roles);
    }

    private UserProfile profile(User user, List<Role> roles) {
        // 视图里同时给角色名（前端按角色分菜单）和权限点（前端按权限控制按钮）
        List<String> roleNames = roles.stream().map(Enum::name).toList();
        List<String> authorities = roles.stream()
                .flatMap(r -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(r.toAuthority()), r.getPermissions().stream()))
                .distinct()
                .toList();
        return UserProfile.builder()
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(roleNames)
                .authorities(authorities)
                .enabled(user.getEnabled() == 1)
                .build();
    }
}
