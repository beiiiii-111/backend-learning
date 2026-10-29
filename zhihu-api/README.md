# zhihu-api（知乎专题页面前后端交互 · 后端）

Spring Boot 4.1.1 + MyBatis-Plus 3.5.17 + MySQL，为前端 `zhihu-app` 提供知乎专题数据接口。

## 数据表

库：`backend_learning`，表：`t_special`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT | 专题 ID（主键） |
| title | VARCHAR(255) | 标题 |
| description | TEXT | 简介（对应原始数据 introduction） |
| banner | VARCHAR(500) | 封面图 |
| is_following | VARCHAR(10) | 是否关注，TRUE / FALSE |
| followers_count | INT | 关注数 |
| view_count | BIGINT | 浏览数 |
| updated | BIGINT | 更新时间（Unix 时间戳，秒） |
| category | VARCHAR(100) | 分类 |

## 数据准备

`sql/` 目录下是完整的清洗链路：

1. `data.json` —— 从 `https://www.zhihu.com/api/v4/news_specials/list?limit=10&offset=0` 抓取后去掉外层属性，保留对象数组；
2. `data_cleaned.csv` —— 由 `data.json` 转换而来（也可用 https://data.page/json/csv 在线转换），去掉空行、删除 `section_list` 等无用字段、把 `is_following` 布尔值转成 `TRUE/FALSE`；
3. `t_special.sql` —— 建表 + 插入数据，Navicat 或命令行直接执行即可；
4. `gen.py` —— 由 `data.json` 重新生成 `data_cleaned.csv` 和 `t_special.sql` 的脚本。

```bash
# 命令行导入（注意加 --default-character-set=utf8mb4，否则中文乱码）
mysql --default-character-set=utf8mb4 -uroot -p backend_learning < sql/t_special.sql
```

分类规则按 `title` / `introduction` 的关键词命中：时事 / 科技 / 文化 / 生活。
想改成教程里的「按关注数划分」，执行下面两句即可：

```sql
UPDATE t_special SET category = '文化' WHERE followers_count > 100;
UPDATE t_special SET category = '科技' WHERE followers_count < 100;
```

## 接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/special/all` | 全部专题，按 updated 降序 |
| GET | `/api/v1/special/category/{category}` | 按分类筛选 |
| GET | `/api/v1/special/page?limit=3&offset=0` | 分页，返回 `{specials, total}` |
| GET | `/api/v1/special/categories` | 全部分类（前端下拉框用） |

统一返回结构：

```json
{ "code": 200, "msg": "数据获取成功", "data": [] }
```

## 运行

```bash
mvn -pl zhihu-api spring-boot:run
mvn -pl zhihu-api test     # 单元测试
```

启动后访问 <http://localhost:8080/api/v1/special/all>。
若控制台打印 `Tomcat started on port 5xxxx`（随机端口），说明 `server.port` 没生效，启动时显式指定即可：

```bash
mvn -pl zhihu-api spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"
```

## 目录结构

```
src/main/java/top/yixz/zhihu/
├── ZhihuApiApplication.java     启动类
├── common/ResponseResult.java   统一返回结果
├── config/WebConfig.java        CORS 跨域配置
├── entity/Special.java          专题实体
├── mapper/SpecialMapper.java    Mapper 接口
├── service/SpecialService.java  服务接口
│   └── impl/SpecialServiceImpl.java
└── controller/SpecialController.java
src/main/resources/
├── application.yml              数据源 + MP 配置
└── mapper/SpecialMapper.xml     自定义 SQL（分类 / 分页）
```
