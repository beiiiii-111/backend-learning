package com.group6.campus.common;

import lombok.Getter;

/** 业务异常：携带错误码，由 GlobalExceptionHandler 统一转为响应体 */
@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BizException of(int code, String message) {
        return new BizException(code, message);
    }
}
