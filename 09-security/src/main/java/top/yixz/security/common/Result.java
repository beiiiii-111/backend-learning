package top.yixz.security.common;

import lombok.Data;

/**
 * 统一响应体
 *
 * @author yixz
 * @date 2026-10-29
 * @description 所有接口统一返回 code / message / data。
 * 业务失败走 HTTP 200 + 业务码，只有未认证（401）和越权（403）用真实 HTTP 状态码，
 * 前端拦截器才能只看状态码就决定跳登录还是弹提示。
 */
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
