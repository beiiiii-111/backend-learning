# 13-actuator 应用监控综合练习（模块 12）

Spring Boot Actuator 综合练习：内置端点 + 自定义健康指标 + Micrometer 自定义业务指标。

## 快速开始

```bash
# 仓库根目录
mvn -pl 13-actuator spring-boot:run
```

## 端点清单

| 地址 | 说明 |
|---|---|
| http://localhost:8013/actuator/health | 健康检查（含自定义 maintenance 指标明细） |
| http://localhost:8013/actuator/info | 应用信息（来自 application.yml 的 info.*） |
| http://localhost:8013/actuator/metrics | 全部可用指标列表 |
| http://localhost:8013/actuator/metrics/biz.visit.total | 自定义业务计数器 |

## 综合演练步骤

```bash
# 1. 打几次业务接口
curl http://localhost:8013/api/visit

# 2. 看自定义计数器已累计
curl http://localhost:8013/actuator/metrics/biz.visit.total

# 3. 打开维护开关
curl -X POST "http://localhost:8013/api/maintenance?on=true"

# 4. 观察健康状态整体变为 OUT_OF_SERVICE
curl http://localhost:8013/actuator/health

# 5. 关闭维护，恢复 UP
curl -X POST "http://localhost:8013/api/maintenance?on=false"
```

## 实现要点

| 功能 | 实现 |
|---|---|
| 自定义健康指标 | 实现 `HealthIndicator` 接口，Bean 名去掉后缀即指标名（maintenance） |
| 自定义业务指标 | `MeterRegistry.counter("biz.visit.total", tags...)`，每次访问 +1 |
| 端点暴露控制 | `management.endpoints.web.exposure.include` 控制放开范围 |
| 健康明细 | `show-details: always` 暴露各组件状态 |
