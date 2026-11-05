package top.yixz.security.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import top.yixz.security.model.Role;

import java.util.List;
import java.util.Set;

/**
 * 安全框架认识的用户对象
 *
 * @author yixz
 * @date 2026-10-29
 * @description 与实体解耦：角色名（带 ROLE_ 前缀，供 hasRole 比对）和权限点
 * （原样字符串，供 hasAuthority 比对）都放进 authorities，两类字符串形态不同，
 * 混用就永远匹配不上。令牌里只放用户名，权限每次请求回库重新查。
 */
public class SecurityUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final boolean enabled;
    private final Set<Role> roles;
    private final Set<String> authorities;

    public SecurityUser(Long id, String username, String password, String nickname,
                        boolean enabled, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.enabled = enabled;
        this.roles = roles;
        // 角色名加 ROLE_ 前缀，权限点原样，两类都给
        this.authorities = Set.copyOf(
                java.util.stream.Stream.concat(
                        roles.stream().map(Role::toAuthority),
                        roles.stream().flatMap(r -> r.getPermissions().stream())
                ).toList());
    }

    /**
     * 由"账号 + 角色"聚合对象构建，实体不直接进安全框架
     */
    public static SecurityUser from(top.yixz.security.model.Account account) {
        return new SecurityUser(account.getId(), account.getUsername(), account.getPassword(),
                account.getNickname(), account.isEnabled(),
                java.util.Set.copyOf(account.getRoles()));
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public List<String> getRoleNames() {
        return roles.stream().map(Enum::name).toList();
    }

    public List<String> getAuthorityStrings() {
        return List.copyOf(authorities);
    }

    public boolean hasAuthority(String authority) {
        return authorities.contains(authority);
    }

    @Override
    public java.util.Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
