package top.yixz.security.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 启用/停用账号请求体
 *
 * @author yixz
 * @date 2026-10-29
 * @description 只有一个开关字段，enabled 必传——缺省值容易把"想停用"存成"误启用"。
 */
public record UpdateUserStatusRequest(@NotNull(message = "enabled 不能为空") Boolean enabled) {
}
