package com.group6.campus.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** 错误码定义：与《API 接口定义》文档 1.2 节一致 */
@Getter
@AllArgsConstructor
public class ErrorCode {
    public static final int OK = 0;
    public static final int PARAM_INVALID = 40001;
    public static final int UNAUTHORIZED = 40101;
    public static final int FORBIDDEN = 40301;
    public static final int NOT_FOUND = 40401;
    public static final int CONFLICT = 40901;
    public static final int SERVER_ERROR = 50000;
}
