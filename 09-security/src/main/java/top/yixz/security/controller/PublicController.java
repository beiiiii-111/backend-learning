package top.yixz.security.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.yixz.security.common.Result;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 公开接口
 *
 * @author yixz
 * @date 2026-10-29
 * @description 不需要登录就能访问，用来判断"后端到底起没起"，不查库。
 * 带上服务名和时间戳，页面上能确认打到了哪个服务、什么时候响应的。
 */
@RestController
public class PublicController {

    @GetMapping("/public/ping")
    public Result<Map<String, String>> ping() {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("status", "ok");
        data.put("service", "09-security");
        data.put("timestamp", LocalDateTime.now().withNano(0).toString());
        return Result.ok(data);
    }
}
