package top.yixz.config.properties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    @NotBlank(message = "应用名称 app.name 不能为空")
    private String name;

    private String author;

    @Min(value = 1, message = "app.port 必须大于等于 1")
    @Max(value = 65535, message = "app.port 必须小于等于 65535")
    private Integer port;

    @Min(value = 1, message = "app.max-count 必须大于等于 1")
    @Max(value = 1000, message = "app.max-count 必须小于等于 1000")
    private Integer maxCount;

    // ↓ 新增：邮箱
    @NotBlank(message = "邮箱 app.email 不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    // ↓ 新增：手机号（1开头，第二位3-9，共11位）
    @NotBlank(message = "手机号 app.phone 不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    // ↓ 新增：启动时间，对应 yml 的 start-time
    private LocalDateTime startTime;
}

