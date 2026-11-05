package top.yixz.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import top.yixz.security.common.BusinessException;
import top.yixz.security.common.ErrorCode;
import top.yixz.security.dto.LoginRequest;
import top.yixz.security.security.JwtTokenService;
import top.yixz.security.security.SecurityUser;
import top.yixz.security.vo.LoginResponse;
import top.yixz.security.vo.UserProfile;

/**
 * 认证服务
 *
 * @author yixz
 * @date 2026-10-29
 * @description 登录走框架的 AuthenticationManager，不自己比对密码。
 * 异常分支在这里翻译成业务码：停用 30002，其余（密码错、用户不存在、
 * 令牌参数问题）统一 30001。
 */
@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(), request.getPassword()));
            SecurityUser user = (SecurityUser) authentication.getPrincipal();
            String token = tokenService.issue(user.getUsername());
            return LoginResponse.of(token, tokenService.expiresAt(), user);
        } catch (DisabledException e) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        } catch (AuthenticationException e) {
            // 用户不存在和密码错误给同一个码，不暴露哪些用户名存在
            log.warn("登录失败：{}", request.getUsername());
            throw new BusinessException(ErrorCode.BAD_CREDENTIALS);
        }
    }

    public UserProfile me(SecurityUser user) {
        return UserProfile.from(user);
    }
}
