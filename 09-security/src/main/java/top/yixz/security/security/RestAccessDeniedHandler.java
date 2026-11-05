package top.yixz.security.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import top.yixz.security.common.ErrorCode;

import java.io.IOException;

/**
 * 越权处理器
 *
 * @author yixz
 * @date 2026-10-29
 * @description 已认证但无权访问（URL 规则拦下的）到这里，返回 HTTP 403 + JSON。
 * 401 和 403 分开：前者要重新登录，后者只能提示，不能混成一个分支。
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final RestErrorWriter errorWriter;

    public RestAccessDeniedHandler(RestErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorWriter.write(response, 403,
                ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }
}
