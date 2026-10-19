package top.yixz.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 容联云（云通讯）短信配置，把 application.yml 里 yixz.sms.ccp 下面的参数批量读进来
 *
 * @author yxzhang
 * @date 2026/10/14
 * @description CloopenConfig
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "yixz.sms.ccp")
public class CloopenConfig {
    private String serverIp;
    private String port;
    private String accountSID;
    private String accountToken;
    private String appId;
    private String templateId;
}
