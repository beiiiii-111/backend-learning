package top.yixz.monitor.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自定义健康指标：模拟「维护开关」。
 * 拨到维护状态后 /actuator/health 整体变为 OUT_OF_SERVICE，便于演练。
 * 实际项目中可以换成：检查磁盘空间、依赖的第三方服务、数据库连通性等。
 */
@Component("maintenance")
public class MaintenanceHealthIndicator implements HealthIndicator {

    private final AtomicBoolean maintenance = new AtomicBoolean(false);

    @Override
    public Health health() {
        if (maintenance.get()) {
            return Health.outOfService().withDetail("reason", "系统维护中，暂停对外服务").build();
        }
        return Health.up().withDetail("message", "服务正常").build();
    }

    /** 供管理接口切换维护状态 */
    public void setMaintenance(boolean on) {
        maintenance.set(on);
    }

    public boolean isMaintenance() {
        return maintenance.get();
    }
}
