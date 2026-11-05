package top.yixz.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 09-security 认证与授权案例
 *
 * @author yixz
 * @date 2026-10-29
 * @description 实训任务提交与批改系统：学生交任务、教师批改、管理员管账号，
 * 三种角色各做各的事。JwtProperties 通过 @ConfigurationPropertiesScan 绑定。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecurityApplication.class, args);
    }
}
