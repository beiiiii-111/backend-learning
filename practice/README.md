# practice · 综合练习项目

根目录下的 `01`~`15` 是分知识点的学习模块，这里是**成体系的小项目**，不挂在父 `pom.xml` 的 `<modules>` 里，各自独立构建。

| 目录 | 说明 | 技术栈 |
|---|---|---|
| [campus-activity-platform](campus-activity-platform/) | 校园活动管理平台（活动发布 / 报名 / 审核 / CSV 名单），前后端完整实现 | Spring Boot 3.2 + JPA + JWT + H2 / Vue 3 + Vite + Element Plus |

## 运行方式（以校园活动平台为例）

```bash
cd campus-activity-platform/backend && mvn spring-boot:run   # 后端 http://localhost:8080
cd campus-activity-platform/frontend && npm install && npm run dev   # 前端 http://localhost:5173
```

演示账号：`admin` / `organizer` / `student`，密码均为 `11111111`。详见各项目自己的 `README.md`。
