package top.yixz.security.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.security.common.Result;
import top.yixz.security.dto.LoginRequest;
import top.yixz.security.security.SecurityUser;
import top.yixz.security.service.AuthService;
import top.yixz.security.vo.LoginResponse;
import top.yixz.security.vo.UserProfile;

/**
 * 认证接口
 *
 * @author yixz
 * @date 2026-10-29
 * @description /auth/login 放行，/auth/me 需要已认证。
 */
@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @GetMapping("/auth/me")
    public Result<UserProfile> me(@AuthenticationPrincipal SecurityUser user) {
        return Result.ok(authService.me(user));
    }
}
