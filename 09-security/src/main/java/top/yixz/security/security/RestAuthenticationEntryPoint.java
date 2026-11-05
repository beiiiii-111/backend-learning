package top.yixz.security.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import top.yixz.security.common.ErrorCode;

import java.io.IOException;

/**
 * 未认证处理器
 *
 * @author yixz
 * @date 2026-10-29
 * @description 不带令牌或令牌无效进到这里。默认行为是 302 跳登录页，
 * 前后端分离时要的是 application/json + HTTP 401，前端拦截器靠它触发跳转。
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final RestErrorWriter errorWriter;

    public RestAuthenticationEntryPoint(RestErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorWriter.write(response, 401,
                ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
    }
}
