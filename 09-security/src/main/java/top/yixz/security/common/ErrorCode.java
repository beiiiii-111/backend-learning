package top.yixz.security.common;

import lombok.Getter;

/**
 * 错误码
 *
 * @author yixz
 * @date 2026-10-29
 * @description 200/400/401/403/404/500 是通用码，30001 起是本案例业务码。
 * 30001 同时覆盖"密码错"和"用户不存在"：分开提示等于告诉攻击者哪些用户名存在。
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),

    BAD_REQUEST(400, "参数校验失败"),
    UNAUTHORIZED(401, "未认证或令牌失效"),
    FORBIDDEN(403, "已认证但无权访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务端异常"),

    BAD_CREDENTIALS(30001, "用户名或密码错误"),
    ACCOUNT_DISABLED(30002, "账号已被禁用"),
    USER_NOT_FOUND(30003, "用户不存在"),
    USERNAME_EXISTS(30004, "用户名已存在"),
    CANNOT_DELETE_SELF(30005, "不能删除当前登录账号"),
    CANNOT_DISABLE_SELF(30006, "不能停用当前登录账号"),

    TASK_NOT_FOUND(30010, "任务不存在"),
    TASK_ALREADY_SUBMITTED(30011, "同一任务已提交，等待批改"),
    TASK_ALREADY_REVIEWED(30012, "该任务已批改，不能重复批改");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
