# 02 · 配置管理 —— @Value、配置校验与 Apifox 联调小结

> - 难度：入门 → 进阶
> - 前置模块：[01-quickstart](./01-quickstart.md)
> - 预计时长：约 60 分钟
> - 对应代码：[02-config](../02-config)
> - 环境：JDK 21 / Spring Boot 4.1.1 / 端口 9090

## 1. 本节导读

这一节把「配置」这件事从能用到用对走了一遍，核心收获三件事：

1. **`@Value` 的六种用法**：基础注入、占位符引用、默认值、随机值、SpEL、多环境取值；
2. **配置校验**：用 `@Validated` + JSR-303 注解让错误配置在**启动阶段就暴露**，而不是等到线上出事故；
3. **Apifox 联调**：用插件自动同步接口，以及「连不上」时怎么快速定位是工具的问题还是后端的问题。

最后一节有一份**真实踩坑清单**，记录了本次实操中实际遇到并解决的 7 个错误，每一个都附了报错特征与修复方式。

---

## 2. `@Value` 用法全景

`@Value` 的作用是把配置值注入到字段上。下面是 `ConfigController` 里用到的全部六种写法。

### 2.1 六种写法对照

| 用法 | 写法 | 说明 |
| --- | --- | --- |
| 基础注入 | `@Value("${server.port}")` | 直接取配置项，类型自动转换（String → Integer） |
| 占位符引用 | `@Value("${app.author}")` | yml 里 `author: ${yixz.name}`，形成**链式引用** |
| 默认值 | `@Value("${app.remark:暂无备注}")` | 冒号后是默认值，配置缺失时不报错 |
| 随机值 | `@Value("${random.uuid}")` | Spring 内置，还有 `random.int`、`random.long` 等 |
| SpEL 表达式 | `@Value("#{${student.age} >= 18 ? '成年' : '未成年'}")` | 先解析 `${}`，再执行 `#{}` 里的表达式 |
| 多环境取值 | `@Value("${env.name}")` | 值来自 `application-{profile}.yml` |

> **默认值是最容易被忽略但最实用的一个。** `${key:默认值}` 在配置项可能缺失时（比如新增字段、灰度配置）能避免应用直接起不来。

### 2.2 完整示例代码

```java
@RestController
@RequestMapping("/config")
@RequiredArgsConstructor
public class ConfigController {

    private final AppProperties appProperties;
    private final EnvService envService;

    // 基础注入
    @Value("${server.port}")
    private Integer serverPort;
    @Value("${spring.application.name}")
    private String appName;

    // 占位符引用（app.author 在 yml 中引用了 yixz.name）
    @Value("${app.author}")
    private String author;

    // 默认值：app.remark 未配置时用冒号后的值
    @Value("${app.remark:暂无备注}")
    private String remark;

    // 随机值
    @Value("${random.uuid}")
    private String randomUuid;
    @Value("${random.int(1,100)}")
    private Integer randomInt;

    // SpEL：先取 student.age，再判断成年
    @Value("#{${student.age} >= 18 ? '成年' : '未成年'}")
    private String adult;

    // 多环境：值来自 application-{profile}.yml
    @Value("${env.name}")
    private String envName;
    @Value("${env.description}")
    private String envDescription;
}
```

### 2.3 对应的配置文件

```yaml
server:
  port: 9090

spring:
  application:
    name: 02-配置管理
  profiles:
    active: prod        # ← 注意缩进，必须在 spring 下面

app:
  name: 配置管理模块
  author: ${yixz.name}  # ← 链式引用 yixz.name
  port: 9090
  max-count: 100
  email: your@example.com
  phone: 13800138000
  start-time: 2026-09-16T13:00:00

yixz:
  name: 张心怡
  job: 学生

student:
  name: 张大三
  age: 23
  # ... 其余略
```

---

## 3. `@Value` 还是 `@ConfigurationProperties`？

这是配置管理最核心的一个选型问题。

| 维度 | `@Value` | `@ConfigurationProperties` |
| --- | --- | --- |
| 粒度 | 单个字段 | 一组相关配置（绑定整个对象） |
| 松散绑定 | 不支持（`user-name` 要写全） | 支持（`max-count` ↔ `maxCount`） |
| 类型安全 | 弱，靠运行时转换 | 强，编译期就是 Java 类型 |
| 校验 | 不支持 JSR-303 | 支持 `@Validated` + 校验注解 |
| 复杂结构 | 麻烦（List/Map 要 SpEL） | 天然支持嵌套对象、List、Map |
| 适用场景 | 取一两个零散值 | 一组业务配置（app、student、redis…） |

**结论**：零散取值用 `@Value`，成组的业务配置一律用 `@ConfigurationProperties`。本项目两种都用，正是为了对比体会。

### 嵌套结构示例

`StudentProperties` 展示了复杂配置的绑定方式：

```java
@Data
@Component
@ConfigurationProperties(prefix = "student")
public class StudentProperties {
    private String name;
    private Integer age;
    private List<String> hobbies;
    private Map<String, Integer> scores;
    private Address address;          // 嵌套对象
    private List<Course> courses;     // 对象列表

    @Data
    public static class Address {
        private String province;
        private String city;
    }

    @Data
    public static class Course {
        private String name;
        private Integer credit;
    }
}
```

对应 yml：

```yaml
student:
  name: 张大三
  age: 23
  hobbies:
    - 唱歌
    - 编程
  scores:
    chinese: 90
    math: 95
  address:
    province: 江苏省
    city: 无锡
  courses:
    - name: 高等数学
      credit: 4
```

---

## 4. 配置校验实践

### 4.1 核心思路

**让错误的配置在启动阶段就炸掉，而不是等到运行时才发现。**

做法是三件套：`@Component` + `@ConfigurationProperties` + `@Validated`。

```java
@Data
@Validated                                    // ← 开启校验，缺了这个注解校验不生效
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank(message = "应用名称 app.name 不能为空")
    private String name;

    private String author;

    @Min(value = 1, message = "app.port 必须大于等于 1")
    @Max(value = 65535, message = "app.port 必须小于等于 65535")
    private Integer port;

    @Min(value = 1, message = "app.max-count 必须大于等于 1")
    @Max(value = 1000, message = "app.max-count 必须小于等于 1000")
    private Integer maxCount;

    @NotBlank(message = "邮箱 app.email 不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "手机号 app.phone 不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private LocalDateTime startTime;
}
```

### 4.2 常用校验注解

| 注解 | 作用 | 示例 |
| --- | --- | --- |
| `@NotBlank` | 字符串非空且非全空格 | 名称、手机号 |
| `@NotNull` | 不能为 null（不检查空串） | 对象引用 |
| `@Min` / `@Max` | 数值范围 | 端口 1~65535 |
| `@Size` | 长度/集合大小范围 | 名单长度 |
| `@Email` | 邮箱格式 | 邮箱 |
| `@Pattern` | 正则匹配 | 手机号 `^1[3-9]\d{9}$` |

### 4.3 校验失败长什么样

配置写错时，应用**启动失败**，控制台给出结构化报错：

```
APPLICATION FAILED TO START

Failed to bind properties under 'app' to AppProperties:

    Property: app.phone
    Value: ""
    Origin: class path resource [application.yml] - 16:9
    Reason: 手机号格式不正确

    Property: app.port
    Value: "86820"
    Origin: class path resource [application.yml] - 13:9
    Reason: 端口号不能大于65535
```

注意它的三个要素：**Property（哪个字段）→ Value（实际值）→ Origin（在配置文件第几行）**。定位非常快。

> **关键认知**：这类错误发生在**应用启动前**，接口还没起来，所以**无法通过接口返回给前端**。它是 fail-fast 设计——宁可起不来，也不要带着错误配置跑。

### 4.4 依赖别漏

校验需要 `spring-boot-starter-validation`：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

注解用的是 `jakarta.validation.constraints.*`（Spring Boot 3+ 起，不再是 `javax`）。

---

## 5. Apifox 联调心得

### 5.1 工作流

1. IDEA 装 **Apifox Helper** 插件
2. 在 Apifox 生成访问令牌（账号设置 → API 访问令牌）
3. 插件里填入 Token，绑定目标项目
4. 项目右键 → **Upload to Apifox**，插件自动扫描 `@RestController` 生成接口文档

### 5.2 怎么判断"上传成功了"

**看 Apifox 左侧接口目录有没有出现你的 Controller**，比如 `ConfigController (5)`、`StudentController (1)`。

**不要**把 IDEA 控制台的 `退出代码为 0` 当作成功标志——那只代表本地扫描跑完了，不代表数据推到了 Apifox。日志里 `Scan endpoints from: xxxController` 也只是"扫描到"，不是"已上传"。

### 5.3 最常见的"连不上"：ECONNREFUSED

```
请求发生错误
connect ECONNREFUSED 127.0.0.1:9090
```

**这不是 Apifox 的问题，是后端没启动。**

`ECONNREFUSED` = 目标端口没有进程在监听。Apifox 只是个发请求的工具，它需要你的 Spring Boot 应用跑在 `localhost:9090` 上。

排查顺序：

1. IDEA 里有没有运行 `ConfigApplication`？
2. 控制台有没有 `Tomcat started on port(s): 9090`？
3. 如果端口不对，检查 `application.yml` 的 `server.port`

### 5.4 排查 Apifox 客户端本身

如果怀疑是插件/客户端的问题，按这个顺序查：

```bash
# 1. 客户端在不在跑
tasklist | grep -i apifox

# 2. 本地服务端口在不在监听（Apifox 客户端本地服务默认 4523）
netstat -ano | grep LISTENING | grep 4523
```

- 客户端进程存在 + 4523 在监听 → 客户端正常，问题在 Token 或后端
- 客户端没运行 → 打开 Apifox 客户端并登录
- Token 失效 → 重新生成填入（`Settings → Tools → Apifox Helper`）

插件的项目配置存在 `.idea/ApifoxUploaderProjectSetting.xml` 里，含 Token 和项目模块映射。

> **一个反直觉的坑**：用 curl 直接测 `api.apifox.cn` 会返回 302/403，这是网关拦截，**不能作为 Token 失效的依据**。判断是否真连上，以 Apifox 界面里有没有出现接口为准。

---

## 6. 踩坑清单（本次实战真实记录）

这些是本次实操中**实际遇到并解决**的错误，按发生顺序排列：

| # | 报错 / 现象 | 根本原因 | 修复方式 |
| --- | --- | --- | --- |
| 1 | `必须声明为抽象，或实现抽象方法 envInfo()` | 接口定义 `evInfo()`，实现类写 `envInfo()`，**方法名差一个字母** | 统一方法名为 `envInfo()` |
| 2 | `Could not resolve placeholder 'mqxu.name'` | `@Value("${mqxu.name}")`，但 yml 里是 `yixz.name` | 改成 `${yixz.name}` |
| 3 | profile 未激活，`env.name` 找不到 | yml 里 `profiles:` **顶格写**，脱离了 `spring`，`spring.profiles.active` 没生效 | `profiles` 缩进 2 格，与 `application` 对齐 |
| 4 | 环境切换失效 / Bean 冲突 | `ProdEnvService` 的 `@Profile("dev")` 写错，两个类都标了 dev | 改成 `@Profile("prod")` |
| 5 | `failed to convert String to LocalDateTime` | yml 写 `2026-9-16T13:00`，**月份没补零**，不符合 ISO 格式 | 改成 `2026-09-16T13:00:00` |
| 6 | `端口号不能大于65535` / `手机号格式不正确` | `app.port: 86820` 超限、`app.phone` 为空 | 改成合法值，校验通过才能启动 |
| 7 | Apifox `ECONNREFUSED 127.0.0.1:9090` | Spring Boot 应用没启动（当时正因上面的错误起不来） | 修完配置后启动应用 |

### 几个值得单独强调的教训

**① 方法名对不上时，`@Override` 会失效**

`implements` 了一个接口，但方法签名对不上，IDEA 会提示"类必须声明为抽象"。这时候第一反应应该是：**去核对接口里的方法签名**（方法名、返回值、参数），而不是怀疑 IDE 抽风。

**② YAML 缩进错了不会报错，只会静默失效**

`profiles` 顶格写，YAML 语法完全合法，解析器不报错，但 `spring.profiles.active` 就是不生效。这类问题**只能靠肉眼核对层级**。判断方法：看它缩进后是和谁对齐的，就属于谁的。

**③ `@Profile` 写错会造成两个方向的故障**

- 两个类标同一个 profile → 该环境下**多个 Bean**，注入时报 `NoUniqueBeanDefinitionException`
- 某个环境没有任何类匹配 → 该环境下**零个 Bean**，注入时报 `NoSuchBeanDefinitionException`

**④ 配置文件里的时间必须是标准 ISO 格式**

`LocalDateTime` 绑定要求 `yyyy-MM-ddTHH:mm:ss`，月、日、时、分、秒都要**补零**。`2026-9-16` 看着没问题，但解析必炸。

---

## 7. 验证清单

应用启动后，用浏览器或 Apifox 依次访问，确认每个功能点：

| 接口 | 预期结果 | 验证了什么 |
| --- | --- | --- |
| `GET /config/basic` | `服务器端口是：9090，应用名称是：02-配置管理` | `@Value` 基础注入 |
| `GET /config/my` | `我的姓名是：张心怡，职业是：学生` | 自定义配置 + 链式引用 |
| `GET /config/value` | 占位符/默认值/随机 UUID/随机整数/SpEL 成年 | 默认值、随机值、SpEL |
| `GET /config/env` | `当前环境：prod，生产环境；Profile Bean：我是 Prod 环境专属的 Bean` | 多环境 + `@Profile` |
| `GET /config/app` | 返回含 name/author/port/maxCount/email/phone/startTime 的 JSON | `@ConfigurationProperties` + 校验 |
| `GET /student/info` | 返回含 hobbies/scores/address/courses 的 JSON | 嵌套结构绑定 |

**多环境验证**：把 `spring.profiles.active` 分别改成 `dev` 和 `prod` 重启，`/config/env` 应该返回不同的环境描述和不同的 Profile Bean。这是检验 profile 配置是否真正生效最直接的方式。

---

## 8. 小结

这一节最有价值的三点认知：

1. **配置的错误要尽早暴露**。`@Validated` 让配置问题在启动阶段就拦下来，比运行时 NPE 或业务逻辑出错的代价小得多。
2. **YAML 的缩进是语义，不是格式**。缩进错一位，配置就静默失效，且不报错——这是 YAML 最阴险的地方。
3. **工具报错先分清责任边界**。Apifox 报 `ECONNREFUSED`，问题在后端没起，而不是 Apifox 连不上。排查时先问「这个错误是谁抛的」，能省掉大量无效尝试。

---

## 参考

- 对应代码：[02-config](../02-config)
- 上一节：[01 · 快速入门](./01-quickstart.md)
