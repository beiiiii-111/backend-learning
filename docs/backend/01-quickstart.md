# 01 · 快速入门 —— 从零启动第一个 Spring Boot 应用

> - 难度：入门
> - 前置模块：无
> - 预计时长：约 30 分钟
> - 对应代码：[01-quickstart](https://github.com/beiiiii-111/backend-learning/tree/main/01-quickstart)

## 1. 本节导读

学完本节，你应该能独立完成三件事：

1. 用 Maven 建出一个最小的 Spring Boot 模块骨架；
2. 启动应用，写出返回 JSON 的 REST 接口并自己验证；
3. 说清「启动类、控制器、实体类」三者的分工，以及一次请求是怎么走到你的方法里的。

**前置知识**：会一点 Java 语法（类、方法、包）即可，不要求会 Spring。

---

## 2. 概念铺垫

### 2.1 为什么需要 Spring Boot

纯 Servlet 时代起一个 Web 服务要干这些事：装 Tomcat、写 `web.xml`、手动管理几十个 jar 的版本兼容性。Spring Boot 的核心主张是**「约定优于配置」**：

- **内嵌容器**：`main` 方法一跑就是一个 Web 服务，不用部署 war，不用单独装 Tomcat；
- **自动装配**：根据 classpath 上有什么依赖，自动把该配的组件配好（引入 web 依赖 → 自动配好 Tomcat + Spring MVC + JSON 转换器）；
- **依赖管家**：`spring-boot-starter-parent` 锁定了几百个常用库的版本，你写依赖不用写 `<version>`，版本冲突的概率大幅下降。

### 2.2 什么是 REST 接口

浏览器或 curl 访问 `http://localhost:8888/user/info`，服务器返回一段 JSON，这就是一个 REST 接口。它的核心是「**用 URL 定位资源，用 HTTP 方法表达操作**」：

| 方法 | 含义 | 示例 |
| --- | --- | --- |
| GET | 查询 | `GET /user/info` |
| POST | 新增 | `POST /user` |
| PUT | 更新 | `PUT /user/1001` |
| DELETE | 删除 | `DELETE /user/1001` |

本节只用 GET —— 因为它不需要请求体，用浏览器就能直接验证，最适合建立第一个正反馈。

### 2.3 三个绕不开的词：容器、Bean、注入

这三个词后面每个模块都会出现，先建立印象：

- **容器（IoC 容器）**：Spring 启动后维护的一个「对象仓库」，负责对象的创建、装配、销毁；
- **Bean**：交给容器托管的对象就叫 Bean；
- **注入（DI）**：你不在代码里 `new`，而是让容器把造好的对象「塞」给你。

```java
@RestController
public class UserController {
    // 本节先直接 new —— 后面 02-config 会换成注入
    @GetMapping("/info")
    public User getUserInfo() {
        return new User(1000L, "张三", LocalDate.of(2000, 2, 4));
    }
}
```

> **为什么本节故意用 `new`？** 因为如果一上来就讲注入，你会同时面对「注解是什么」「容器怎么建」「对象哪来的」三个未知量。先让程序跑起来，再逐个替换掉 `new`，理解成本最低。

---

## 3. 环境准备

| 项目 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 21 | 各模块 `pom.xml` 中 `java.version` 已设为 21 |
| Maven | 3.8+ | 构建工具 |
| IDE | IntelliJ IDEA | 可选，命令行也能跑 |
| Spring Boot | 3.3.0（本模块） | 见下方说明 |

**关于本仓库的结构**：根目录 `pom.xml` 是聚合 POM（`<packaging>pom</packaging>`），用 `<modules>` 声明了 14 个子模块，并统一引入了 Web、Lombok、测试依赖。

本项目当前用的是 `spring-boot-starter-webmvc`（根 POM 引入）与 `spring-boot-starter-web`（本模块引入）两种写法，二者提供的是同一套 Web 能力，只是 Spring Boot 4.x 对 starter 做了更细的拆分与更名。**本模块只需其一即可，无需重复引入。**

---

## 4. 动手实战

我们从零搭出 `01-quickstart`，共 5 步。

### 步骤 1：建模块骨架

目标：让 Maven 认识这个子模块。

`01-quickstart/pom.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
        <relativePath/>
    </parent>

    <groupId>top.yixz</groupId>
    <artifactId>01-quickstart</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Web：提供 @RestController / Spring MVC / 内嵌 Tomcat / JSON 转换 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Lombok：提供 @Data 等注解，编译期生成样板代码 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <!-- 打可执行 jar 时排除 Lombok（它只在编译期有用） -->
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

讲解：

- `<parent>` 指向 `spring-boot-starter-parent`，由此获得依赖版本管理和默认插件配置，所以下面的依赖**都不用写 `<version>`**；
- `spring-boot-starter-web` 是一个「starter 聚合包」，一次性带来 Spring MVC + 内嵌 Tomcat + Jackson（JSON 库）；
- `<optional>true</optional>` 表示 Lombok 只在本模块编译期生效，不会传递给依赖本模块的其他模块；
- `spring-boot-maven-plugin` 提供 `spring-boot:run` 和打成可执行 fat jar 的能力。

验证：在 `01-quickstart` 目录下执行 `mvn compile`，出现 `BUILD SUCCESS` 即通过。

### 步骤 2：写启动类

目标：提供一个能启动整个应用的入口。

`src/main/java/top/yixz/config/quickstart/QuickStartApplication.java`：

```java
package top.yixz.config.quickstart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yxzhang
 * @date 2026/9/9
 * @description QuickStartApplication
 **/
@SpringBootApplication
public class QuickStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuickStartApplication.class, args);
    }
}
```

讲解：

- `@SpringBootApplication` 是**组合注解**，等于同时开启三件事：
  - `@Configuration`：本类可作为配置类，`@Bean` 方法可在此声明；
  - `@EnableAutoConfiguration`：自动装配，根据 classpath 依赖自动配置组件；
  - `@ComponentScan`：扫描**当前类所在包及其所有子包**，把带 `@Component` 系列注解的类注册成 Bean。
- `SpringApplication.run(...)` 做三件事：创建 IoC 容器 → 启动内嵌 Tomcat → 把请求分派准备好。

> ⚠️ **最容易踩的坑**：启动类必须放在**业务包的最外层**。本例中启动类在 `top.yixz.config.quickstart`，那么只有 `top.yixz.config.quickstart.*` 下的类会被扫描到（所以 `controller`、`entity` 能被识别）。如果把它放到 `top.yixz.config.quickstart.app`，`controller` 包就扫不到了，接口会 404。

### 步骤 3：写实体类 User

目标：定义一个「用户」数据模型，用 Lombok 省掉样板代码。

`src/main/java/top/yixz/config/quickstart/entity/User.java`：

```java
package top.yixz.config.quickstart.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author yxzhang
 * @date 2026/9/9
 * @description user
 **/
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {
    private Long id;
    private String name;
    private LocalDate birthday;
}
```

讲解：

- `@Data`：编译期自动生成 getter / setter / `toString` / `equals` / `hashCode`；
- `@NoArgsConstructor` / `@AllArgsConstructor`：生成无参构造器和全参构造器。代码里的 `new User(1000L, "张三", LocalDate.of(2000, 2, 4))` 用的就是全参构造器；
- **规范点 1**：`id` 用包装类型 `Long` 而非基本类型 `long`。POJO 属性统一用包装类型，才能表达「没赋值（null）」和「值为 0」的区别 —— 这在数据库字段可空时尤其重要；
- **规范点 2**：日期用 `LocalDate` 而不是 `Date` 或 `String`。`java.time` 包是不可变、线程安全的，也是现在的事实标准。

> Lombok 是**编译期**生效的：注解处理器在 javac 阶段就把方法写进 class 文件了，所以你不用在 IDE 里看到那些 getter，但运行时确实存在。IDEA 需要安装 Lombok 插件并开启 Annotation Processing，否则编辑器会飘红（不影响命令行编译）。

### 步骤 4：写控制器 UserController

目标：暴露两个 GET 接口。

`src/main/java/top/yixz/config/quickstart/controller/UserController.java`：

```java
package top.yixz.config.quickstart.controller;

import top.yixz.config.quickstart.entity.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yxzhang
 * @date 2026/9/9
 * @description UserController
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public User getUserInfo() {
        return new User(1000L, "张三", LocalDate.of(2000, 2, 4));
    }

    /**
     * 查询用户列表
     *
     * @return 全部用户
     */
    @GetMapping("/list")
    public List<User> listUsers() {
        return List.of(
                new User(1000L, "张三", LocalDate.of(2000, 2, 4)),
                new User(1001L, "李四", LocalDate.of(1999, 8, 15)),
                new User(1002L, "王五", LocalDate.of(2001, 6, 30))
        );
    }
}
```

讲解：

- `@RestController` = `@Controller` + `@ResponseBody`，含义是「方法返回值直接写进响应体」，而不是返回一个视图名去跳转页面；
- `@RequestMapping("/user")` 是**类级前缀**，`@GetMapping("/info")` 是**方法级子路径**，拼接后就是 `GET /user/info`。把公共前缀提到类上，是必须养成的习惯；
- `@GetMapping` 是 `@RequestMapping(method = RequestMethod.GET)` 的简写，同理有 `@PostMapping` 等；
- 返回 `User` 或 `List<User>` 时，Spring 会自动用 Jackson 序列化成 JSON —— **你不需要写任何转换代码**。

**一次请求的完整链路**：

```
浏览器 GET /user/info
   ↓
内嵌 Tomcat 接收 HTTP 请求
   ↓
DispatcherServlet（前端控制器）根据 URL 找 HandlerMapping
   ↓
匹配到 UserController#getUserInfo
   ↓
执行方法，拿到 User 对象
   ↓
Jackson 把 User 序列化成 JSON 写入响应体
   ↓
返回 200 OK + {"id":1000,...}
```

> **规范点**：对外暴露的接口方法必须是 `public`。Spring 虽然能通过反射调用私有方法，但这违反 Java 与 Spring 的约定，也会影响代理和 AOP（后面的模块会遇到）。

### 步骤 5：配置端口

目标：让应用监听 8888 端口。

`src/main/resources/application.yml`：

```yaml
server:
  port: 8888
```

讲解：

- Spring Boot 默认端口 8080，这里显式改为 8888；
- 用 `application.yml` 而非 `application.properties`：`yml` 层级更清晰，是当前主流选择；
- **缩进必须用空格，不能用 Tab**，且冒号后面必须有一个空格 —— 这是 YAML 语法硬要求，写错会启动失败。

---

## 5. 完整代码与目录结构

```
01-quickstart
├── pom.xml
└── src/main
    ├── java/top/yixz/config/quickstart
    │   ├── QuickStartApplication.java        # 启动类（最外层）
    │   ├── controller/UserController.java    # 控制器：接请求、返数据
    │   └── entity/User.java                  # 实体：数据模型
    └── resources/application.yml             # 配置：端口 8888
```

**分层职责一句话记忆**：

| 层 | 类 | 职责 | 不该干的事 |
| --- | --- | --- | --- |
| 启动类 | `QuickStartApplication` | 启动容器、触发扫描 | 写业务逻辑 |
| 控制器 | `UserController` | 接收 HTTP、返回数据 | 写复杂业务逻辑（后面交给 Service） |
| 实体 | `User` | 描述数据结构 | 写行为逻辑 |

---

## 6. 运行与验证

在 **`01-quickstart` 目录**下执行（本模块未继承仓库根 POM，建议在模块内运行）：

```bash
cd 01-quickstart
mvn spring-boot:run
```

看到类似日志即启动成功：

```
Tomcat started on port 8888
Started QuickStartApplication in 1.8 seconds
```

然后验证（新开一个终端）：

```bash
# 返回单个对象
curl http://localhost:8888/user/info
# 期望：{"id":1000,"name":"张三","birthday":"2000-02-04"}

# 返回数组
curl http://localhost:8888/user/list
# 期望：[{"id":1000,"name":"张三","birthday":"2000-02-04"},{...},{...}]
```

也可以直接在浏览器打开 `http://localhost:8888/user/info` —— GET 接口的最大好处就是能这样验证。

> `birthday` 输出 `2000-02-04` 而不是时间戳：Spring Boot 自动注册了 Jackson 的 Java 8 时间模块，`LocalDate` 默认按 ISO-8601（yyyy-MM-dd）序列化，无需任何配置。

**停止服务**：终端按 `Ctrl + C`。

---

## 7. 常见问题与避坑

| 现象 | 原因 | 解决 |
| --- | --- | --- |
| `Whitelabel Error Page` / 404 | 路径写错，或类没被扫描到 | 检查类级 `@RequestMapping` 与方法路径的拼接；确认启动类在包最外层 |
| 404 且控制台无报错 | 启动类包层级太深，controller 没进扫描范围 | 把启动类上移到业务根包 |
| `Port 8888 was already in use` | 端口被占（多半是上次没关） | `netstat -ano \| findstr :8888` 找到 PID，`taskkill /PID <pid> /F`；或改端口 |
| 返回 500，提示序列化失败 | 实体类没有 getter（漏了 `@Data`） | Jackson 靠 getter 序列化，补上 `@Data` |
| IDE 里 getter/setter 飘红但编译能过 | 未开启注解处理 | IDEA：Settings → Build → Compiler → Annotation Processors → 勾选 Enable；并安装 Lombok 插件 |
| 改了代码没生效 | 未重新编译 | 本模块未引入 devtools，需手动重启（或加上 `spring-boot-devtools` 依赖） |
| YAML 启动报解析错误 | 用了 Tab 缩进，或冒号后没空格 | 统一改空格缩进 |

---

## 8. 练习

### 8.1 随堂练习（约 10 分钟）

> 跟着做即可，做完立刻用 curl 验证，追求「当场跑通」。

| # | 练习 | 验证点 |
| --- | --- | --- |
| 1 | 给 `User` 加 `email` 字段，让 `/user/info` 返回它 | 响应里出现 `"email":...` |
| 2 | 新增 `GET /user/hello?name=张三`，用 `@RequestParam` 接收，返回 `hello, 张三` | 带参访问返回正确拼接 |
| 3 | 把端口改成 8889，重启验证 | 旧端口不通、新端口通 |

### 8.2 课后练习

- **基础**：给 `User` 增加 `email`、`phone` 两个字段，并让 `/user/info` 和 `/user/list` 都完整返回。
- **进阶**：实现 `GET /user/{id}`，用 `@PathVariable` 按 id 返回对应用户（提示：`new User(id, ...)`）。
- **挑战**：
  1. 把 `listUsers` 改成先构造 `List<User>` 变量再返回，体会「用 `List.of()` 创建不可变集合」与 `new ArrayList<>()` 的区别；
  2. 写一段话说明 `@RestController` 与 `@Controller` 的区别及各自适用场景（提示：前者返回值直接进响应体，后者通常返回视图名）。

---

## 9. 小结与知识地图

| 概念 | 一句话回顾 |
| --- | --- |
| Spring Boot | 约定优于配置，内嵌容器，自动装配 |
| starter | 一个依赖带来一组能力（web = MVC + Tomcat + Jackson） |
| 启动类 | `@SpringBootApplication` 开启自动装配 + 组件扫描，必须放在包最外层 |
| `@RestController` | 方法返回值直接作为响应体（自动转 JSON） |
| 路径拼接 | 类级 `@RequestMapping` + 方法级 `@GetMapping` |
| 实体类 + Lombok | `@Data` 省样板代码，属性用包装类型，日期用 `LocalDate` |

下一节 [02-config](./02-config.md) 会讲配置读取，同时正式引入**依赖注入** —— 控制器将不再 `new` 对象，而是让 Spring 注入进来。

---

## 10. 本模块代码现状检查（待整理项）

读代码时发现的几处不一致，属于真实工程里常见的「历史遗留」，建议后续统一。它们不影响本节运行，但值得你现在就意识到：

| # | 现状 | 问题 | 建议 |
| --- | --- | --- | --- |
| 1 | `01-quickstart/pom.xml` 的 `<parent>` 是 `spring-boot-starter-parent:3.3.0`，而根 POM 用的是 `4.1.1` | 本模块脱离了仓库统一依赖管理，版本与其他模块不一致 | 改为继承本仓库根 POM，或统一 Spring Boot 版本 |
| 2 | 包名是 `top.yixz.config.quickstart` | 中间的 `config` 是复制 `02-config` 模块时的残留，语义不对 | 重命名为 `top.yixz.quickstart` |
| 3 | 存在 `top.yixz.config.Main.java` | IDEA 新建项目时自动生成的示例类，与本模块无关（多个模块里都有） | 直接删除 |
| 4 | 端口 8888，其他模块端口未统一规划 | 多模块同时启动易冲突 | 约定按序号分配（如 8001、8002…） |
| 5 | 未引入 `spring-boot-devtools` | 改代码需手动重启 | 开发期可加上，生产不引入 |

> 这五条本身就是很好的一次练习：**先让代码跑起来，再回头收拾结构** —— 这是所有真实项目的常态。

---

> 本节遵循的规范（官方 + 阿里 Java 开发手册）：POJO 属性用包装类型；类名 UpperCamelCase、方法/变量 lowerCamelCase；对外方法 `public`；类头带 `@author` 与 `@date` 注释。
