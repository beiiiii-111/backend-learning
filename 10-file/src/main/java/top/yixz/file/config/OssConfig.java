package top.yixz.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun-oss")
@PropertySource("classpath:aliyun-oss.properties")
public class OssConfig {

    /** 如 oss-cn-hangzhou.aliyuncs.com */
    private String endpoint;

    private String bucket;

    /** 上传目录，如 img/ */
    private String dir;

    private String accessKey;

    private String secretKey;
}
