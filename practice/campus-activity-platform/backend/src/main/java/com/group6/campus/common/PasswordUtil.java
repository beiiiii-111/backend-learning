package com.group6.campus.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** 密码摘要：SHA-256(salt + 明文)。课程演示用；生产环境应使用 BCrypt 等自适应哈希 */
public class PasswordUtil {
    private static final String SALT = "campus";

    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((SALT + raw).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("密码摘要计算失败", e);
        }
    }
}
