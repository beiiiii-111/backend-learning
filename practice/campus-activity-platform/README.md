# 校园活动管理平台 · 核心功能（前后端）

> AI 实践课程作业：依据《PRD》《API 接口定义》与数据库模型设计，实现活动发布、报名审核等核心模块的前后端编码。

## 功能范围（本次交付）

| 模块 | 接口 | 前端页面 |
|---|---|---|
| 用户 | 注册 / 登录（JWT）/ 当前用户 | 登录页 |
| 活动 | 发布、编辑、下架、分页列表（分类/关键词/仅可报名）、详情、审核通过/驳回 | 活动列表、活动详情、发布活动、审核中心 |
| 报名 | 报名（防超卖）、取消（释放名额）、我的报名、报名名单、CSV 导出 | 我的报名 |

**实施范围说明**：本作业为课程 MVP 交付，按数据库模型评审结论实施——单体架构 + 数据库条件更新防超卖（`UPDATE activity SET taken=taken+1 WHERE taken<capacity`，行锁保证原子性）；Redis 缓存、消息队列等列入二期优化，不在本次范围。

## 技术栈

- 后端：Spring Boot 3.2 / Spring Data JPA / **H2 文件数据库（免安装 MySQL）** / JWT（jjwt）
- 前端：Vue 3 / Vite / Element Plus / axios / vue-router

## 环境要求

- JDK 17+，Maven 3.8+
- 前端运行需 Node 18+（仅查看后端接口可跳过前端）

## 启动步骤

### 后端（约 1 分钟）

```bash
cd backend
mvn spring-boot:run
```

启动后：

- 接口基址：`http://localhost:8080/api/v1`
- 数据库控制台：`http://localhost:8080/h2-console`（JDBC URL：`jdbc:h2:file:./data/campus`，用户名 `sa`，密码空）
- 数据库与初始数据由 `schema.sql` / `data.sql` 在首次启动时自动创建，无需手动执行

**演示账号**（密码均为 `11111111`）：

| 账号 | 角色 |
|---|---|
| admin | 管理员（审核活动） |
| organizer | 组织者（发布活动） |
| student | 学生（报名） |

### 前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`（已配置代理解决跨域）。

### 快速验证流程（对应验收标准）

1. 用 `student` 登录 → 活动列表 → 进入「校园篮球联赛」→ 报名 → 「我的报名」可见
2. 用 `organizer` 登录 → 发布活动 → 提示"待审核"
3. 用 `admin` 登录 → 审核中心 → 通过/驳回（驳回必填原因）→ 学生端列表同步变化
4. 重复报名同一活动 → 提示"请勿重复报名"

## 核心设计对应关系

| 评审结论 | 代码位置 |
|---|---|
| 条件更新防超卖 | `ActivityRepository.increaseTaken()` / `SignupController.signup()` |
| 取消改状态不删行、名额释放 | `Signup.status=cancelled` + `decreaseTaken()` |
| 组合唯一索引防重复报名 | `Signup` 表 `uk_signup_user_activity` |
| 审核流（驳回必填原因） | `ActivityController.audit()` |
| JWT 鉴权 + 白名单 | `JwtUtil` / `AuthInterceptor` / `WebConfig` |
| 统一响应与异常 | `Result` / `GlobalExceptionHandler` |
