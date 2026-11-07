# 11-doc 接口文档综合练习（模块 10）

用 **springdoc-openapi** 给一套图书管理 CRUD 接口生成在线文档，并通过注解增强文档可读性。

## 快速开始

```bash
# 仓库根目录
mvn -pl 11-doc spring-boot:run
```

打开浏览器：

| 地址 | 说明 |
|---|---|
| http://localhost:8011/swagger-ui.html | Swagger UI 在线文档（可视化调试） |
| http://localhost:8011/v3/api-docs | OpenAPI 3 JSON（可导入 Apifox/Postman） |

## 用到的注解

| 注解 | 位置 | 作用 |
|---|---|---|
| `@Tag` | Controller 类 | 接口分组名与描述 |
| `@Operation` | 接口方法 | 接口 summary / description |
| `@Parameter` | 参数 | 路径/查询参数说明 |
| `@Schema` | DTO 字段 | 字段含义、示例值、必填、取值范围 |
| `OpenAPI` Bean | 配置类 | 全局标题、版本、联系人 |

## 验证步骤

1. 启动后打开 `/swagger-ui.html`，能看到「图书管理」分组和 4 个接口；
2. 在 Swagger UI 里直接调试 POST /api/books，返回 201 和 id；
3. GET /api/books 用 category 参数过滤，观察文档参数说明。
