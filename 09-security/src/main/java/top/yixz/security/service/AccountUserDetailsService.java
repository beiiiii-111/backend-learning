package top.yixz.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import top.yixz.security.entity.User;
import top.yixz.security.mapper.RoleMapper;
import top.yixz.security.mapper.UserMapper;
import top.yixz.security.model.Account;
import top.yixz.security.model.Role;
import top.yixz.security.model.UserRoleEntry;
import top.yixz.security.security.SecurityUser;

import java.util.List;
import java.util.Set;

/**
 * 从库里组装安全框架要的用户对象
 *
 * @author yixz
 * @date 2026-10-29
 * @description 实体只是表映射，认证需要账号+角色两个信息，在这里合成 Account
 * 再转成 SecurityUser，不让实体去实现 UserDetails。
 * 用户不存在抛 UsernameNotFoundException：Spring 默认把它包装成
 * BadCredentialsException（隐藏用户是否存在），正好满足"统一 30001"的要求。
 * 每个带令牌的请求都会走一遍这里，权限永远是最新的——改角色立即生效。
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    public AccountUserDetailsService(UserMapper userMapper, RoleMapper roleMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public SecurityUser loadUserByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        List<Role> roles = roleMapper.selectByUserId(user.getId()).stream()
                .map(UserRoleEntry::getCode)
                .map(Role::valueOf)
                .toList();

        Account account = Account.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .enabled(user.getEnabled() == 1)
                .roles(roles)
                .build();

        return SecurityUser.from(account);
    }
}
