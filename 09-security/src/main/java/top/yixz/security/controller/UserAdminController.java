package top.yixz.security.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.security.common.Result;
import top.yixz.security.dto.CreateUserRequest;
import top.yixz.security.dto.UpdateUserStatusRequest;
import top.yixz.security.security.SecurityUser;
import top.yixz.security.service.UserService;
import top.yixz.security.vo.UserProfile;

import java.util.List;

/**
 * 账号管理接口
 *
 * @author yixz
 * @date 2026-10-29
 * @description 第二道防线：URL 规则里已经 hasRole('ADMIN')，类上再拦一次。
 * 两处字符串形态一致（都用 ROLE_ 前缀的角色），不会互相干扰。
 * 删除账号时不能删当前登录人自己（30005）。
 */
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserService userService;

    public UserAdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public Result<List<UserProfile>> list() {
        return Result.ok(userService.listUsers());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public Result<UserProfile> create(@Valid @RequestBody CreateUserRequest request) {
        return Result.ok(userService.createUser(request));
    }

    @PatchMapping("/{username}/status")
    @PreAuthorize("hasAuthority('user:write')")
    public Result<UserProfile> toggleStatus(@AuthenticationPrincipal SecurityUser user,
                                            @PathVariable String username,
                                            @Valid @RequestBody UpdateUserStatusRequest request) {
        return Result.ok(userService.toggleStatus(username, user.getUsername(), request.enabled()));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasAuthority('user:write')")
    public Result<Void> delete(@AuthenticationPrincipal SecurityUser user,
                               @PathVariable String username) {
        userService.deleteUser(username, user.getUsername());
        return Result.ok();
    }
}
