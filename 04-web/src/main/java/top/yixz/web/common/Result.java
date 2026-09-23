package top.yixz.web.common;

import lombok.Data;

/**
 * 统一响应体：所有接口都用它包装返回值
 *
 * @author yxzhang
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

    /** 成功，带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    /** 成功，无数据（删除等场景） */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /** 失败，按错误码枚举 */
    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /** 失败，自定义错误码与提示（如参数校验的具体错误信息） */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
