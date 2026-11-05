package com.group6.campus.common;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 登录校验拦截器：白名单在 WebConfig 中配置，其余接口必须携带有效 token */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 未登录可浏览活动列表/详情（PRD 非功能要求）；带 token 则解析出身份供"仅创建者可见"判断
        if ("GET".equals(request.getMethod())
                && request.getRequestURI().matches("/api/v1/activities(/\\d+)?")) {
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                try {
                    Claims claims = jwtUtil.parse(auth.substring(7));
                    request.setAttribute("userId", Long.valueOf(claims.getSubject()));
                    request.setAttribute("role", claims.get("role", String.class));
                } catch (Exception ignored) {
                    // token 无效按未登录浏览处理
                }
            }
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw BizException.of(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        Claims claims = jwtUtil.parse(auth.substring(7));
        request.setAttribute("userId", Long.valueOf(claims.getSubject()));
        request.setAttribute("role", claims.get("role", String.class));
        return true;
    }
}
