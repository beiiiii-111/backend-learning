package top.yixz.monitor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import top.yixz.monitor.health.MaintenanceHealthIndicator;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务接口 + 管理接口：
 * 1. /api/visit 每次访问给 Micrometer 计数器 +1（可在 /actuator/metrics 看到）；
 * 2. /api/maintenance 切换自定义健康指标的维护开关。
 */
@RestController
public class AdminController {

    private final MaintenanceHealthIndicator maintenance;
    private final AtomicLong visitCount = new AtomicLong();

    public AdminController(MaintenanceHealthIndicator maintenance,
                           io.micrometer.core.instrument.MeterRegistry registry) {
        this.maintenance = maintenance;
        // 注册自定义计数器，tag 区分接口来源
        this.visitCounter = registry.counter("biz.visit.total", "api", "visit");
    }

    private final io.micrometer.core.instrument.Counter visitCounter;

    @GetMapping("/api/visit")
    public Map<String, Object> visit() {
        long n = visitCount.incrementAndGet();
        visitCounter.increment();
        return Map.of("message", "欢迎访问", "yourVisit", n);
    }

    @PostMapping("/api/maintenance")
    public ResponseEntity<Map<String, Object>> toggle(@RequestParam boolean on) {
        maintenance.setMaintenance(on);
        return ResponseEntity.ok(Map.of("maintenance", on,
                "hint", "查看 /actuator/health 观察整体状态变化"));
    }
}
