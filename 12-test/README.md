# 12-test 测试综合练习（模块 11）

订单场景的综合测试练习：**JUnit 5 单元测试（Mockito 替身）+ 参数化测试 + MockMvc 集成测试**。

## 运行测试

```bash
# 仓库根目录
mvn -pl 12-test test
```

## 测试矩阵

| 层级 | 测试类 | 技术点 | 用例数 |
|---|---|---|---|
| Service 单元测试 | `OrderServiceTest` | Mockito mock Repository、AssertJ 断言、`@ParameterizedTest` + `@CsvSource` 折扣档位表、`@NullAndBlankSource` 边界 | 13 |
| Controller 集成测试 | `OrderControllerIntegrationTest` | `@SpringBootTest` + `@AutoConfigureMockMvc`，验证状态码、JSON 字段、业务冲突 409 | 3 |

## 覆盖的业务规则

- 下单数量 1~99、单价为正、商品名非空；
- 折扣档位：满 10 件 9 折、满 50 件 8 折，金额保留 2 位；
- 支付状态机：仅 CREATED 可支付，重复支付返回 409。

## 断言风格

统一用 AssertJ 的流式断言（`assertThat(...).isEqualTo(...)`）与
`assertThatThrownBy(...).isInstanceOf(...)` 异常断言，报错信息可读性优于原生 JUnit。
