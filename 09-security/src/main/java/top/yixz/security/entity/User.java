package top.yixz.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 账号实体
 *
 * @author yixz
 * @date 2026-10-29
 * @description 只做 sec_user 一张表的映射，不实现 UserDetails；
 * password 是 BCrypt 密文，任何响应都不能带它。
 */
@Data
@TableName("sec_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    /** 1 启用 / 0 停用，停用账号登录会被拒 */
    private Integer enabled;

    private LocalDateTime createdAt;
}
