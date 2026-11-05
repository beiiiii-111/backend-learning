package com.group6.campus.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 统一响应结构：{ code, message, data } */
@Data
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.OK, "success", data);
    }

    public static Result<Void> ok() {
        return new Result<>(ErrorCode.OK, "success", null);
    }
}
