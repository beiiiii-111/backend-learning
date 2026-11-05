package top.yixz.security.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author yixz
 * @date 2026-10-29
 * @description 业务异常（30001 段）返回 HTTP 200 + 业务码，靠 code 区分；
 * 参数校验给 400；越权给真实 HTTP 403。未认证（401）不经过这里，
 * 在过滤链层就被 RestAuthenticationEntryPoint 拦下返回 JSON 了。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        log.warn("业务异常：{} {}", e.getErrorCode().getCode(), e.getMessage());
        return build(e.getErrorCode().getCode(), e.getMessage());
    }

    /**
     * 方法级鉴权（@PreAuthorize）失败抛出的 AccessDeniedException 也会到这，
     * 和服务层抛的 FORBIDDEN 走同一个出口，前端拿到的结构一致
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException e) {
        return build(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        return build(ErrorCode.BAD_REQUEST.getCode(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleNotReadable(HttpMessageNotReadableException e) {
        return build(ErrorCode.BAD_REQUEST.getCode(), "请求体不是合法的 JSON");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleOther(Exception e) {
        log.error("未捕获异常", e);
        return build(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage());
    }

    private ResponseEntity<Result<Void>> build(int code, String message) {
        return ResponseEntity.status(httpStatus(code))
                .body(Result.fail(code, message));
    }

    /**
     * 只有 400/403/404/500 用真实 HTTP 状态码，其余业务码一律 HTTP 200
     */
    private HttpStatus httpStatus(int code) {
        return switch (code) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 500 -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.OK;
        };
    }
}
