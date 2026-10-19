package top.yixz.redis.dto;

import lombok.Data;

/**
 * @author yxzhang
 * @date 2026/10/19
 * @description LoginRequest
 **/
@Data
public class LoginRequest {
    private String phone;
    private String code;
}
