package top.yixz.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置项
 *
 * @author yixz
 * @date 2026-10-29
 * @description 绑定 application.yml 里的 app.jwt。签名密钥放配置文件不进代码，
 * 生产用环境变量 JWT_SECRET 覆盖。
 */
@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HS256 签名密钥，不短于 32 字节 */
    private String secret;

    private String issuer;

    /** 令牌有效期（分钟） */
    private int expireMinutes;
}
