package top.yixz.security.common;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author yixz
 * @date 2026-10-29
 * @description 携带错误码，由全局异常处理器转成 Result。
 * 归属越权也用它：ErrorCode.FORBIDDEN 会被映射成真实 HTTP 403。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
