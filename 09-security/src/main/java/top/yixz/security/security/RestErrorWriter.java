package top.yixz.security.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import top.yixz.security.common.Result;

import java.io.IOException;

/**
 * Security 层错误写出
 *
 * @author yixz
 * @date 2026-10-29
 * @description 过滤链层（还没进 Controller）发生的 401/403 不能走
 * @RestControllerAdvice，只能自己往响应里写 JSON，格式与业务外壳一致。
 */
@Component
public class RestErrorWriter {

    private final ObjectMapper objectMapper;

    public RestErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, int httpStatus, int code, String message)
            throws IOException {
        response.setStatus(httpStatus);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, message)));
    }
}
