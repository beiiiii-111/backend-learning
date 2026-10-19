package top.yixz.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author yxzhang
 * @date 2026/10/14
 * @description RedisController
 **/
@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 向 Redis 写入数据，并指定失效时间
     * POST /redis?key=name&value=软件&timeout=20
     * timeout 不传时默认 60 秒
     */
    @PostMapping
    public Map<String, Object> set(@RequestParam String key,
                                   @RequestParam String value,
                                   @RequestParam(defaultValue = "60") long timeout) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeout));

        // 把这次写入的参数回显出去，方便在接口调试工具里直接看到结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("timeout", timeout);
        return result;
    }
}
