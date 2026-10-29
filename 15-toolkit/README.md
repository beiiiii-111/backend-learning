# 15-toolkit · Hutool 工具库实战

十二个真实项目里反复出现的需求，每个都是一个独立接口，数据全部放进程内存，不依赖数据库和 Redis。
端口 **8015**，统一前缀 `/toolkit`。教程原文见 `docs` 仓库的「15 · 工具库实战」。

## 启动

```bash
# 在仓库根目录
mvn -pl 15-toolkit spring-boot:run
# 或者直接跑 top.yixz.toolkit.ToolkitApplication
```

浏览器打开 <http://127.0.0.1:8015/system.html> 可看系统面板页。

> 端口被环境变量覆盖时，先清掉再启动：`env -u SERVER__PORT`。
> 本机 curl 走代理会导致 127.0.0.1 连不上，命令里统一加 `--noproxy '*'`。

## 十二个案例

| # | 案例 | 接口 | 用的 API | 真实使用场合 |
| --- | --- | --- | --- | --- |
| 1 | 图形验证码 | `GET /toolkit/captcha?type=line\|math`<br>`POST /toolkit/captcha/verify` | `CaptchaUtil`、`MathGenerator`、`TimedCache` | 登录、注册、发短信前的防刷 |
| 2 | 扫码登录 | `POST /qr-login/create`、`/scan`、`/confirm`、`/cancel`<br>`GET /qr-login/status`、`/ascii` | `QrCodeUtil`、`QrConfig`、`JWTUtil` | PC 端扫码登录、扫码授权 |
| 3 | 商品海报 | `POST /toolkit/poster` | `Img`、`ImgUtil`、`QrCodeUtil` | 分享海报、活动图、带二维码的推广图 |
| 4 | 短链服务 | `POST /toolkit/short-link`<br>`GET /toolkit/s/{code}`、`/short-link/{code}` | `RandomUtil`、`Validator`、`TimedCache` | 短信里的短链接、渠道点击统计 |
| 5 | Excel 导入导出 | `GET /excel/export`、`/template`、`/download/{file}`<br>`POST /excel/import` | `ExcelWriter`、`ExcelReader`、`@Alias` | 后台报表导出、批量导入（带错误行回传） |
| 6 | 分片上传 | `POST /chunk/init`、`/upload`、`/merge`<br>`GET /chunk/status`、`/download/{fileId}` | `FileUtil`、`DigestUtil` | 大文件上传、断点续传、秒传（MD5 校验） |
| 7 | 上传安全 | `POST /file/check`、`/compress`、`/zip` | `FileTypeUtil`、`ImgUtil`、`ZipUtil` | 上传校验（改扩展名伪装）、图片压缩、打包下载 |
| 8 | 开放接口签名 | `POST /sign/demo`（生成签名）<br>`POST /sign/order`（校验签名） | `SecureUtil.hmac`、`MessageDigest` | 给第三方/开放平台调用的 API 鉴权、防重放 |
| 9 | 敏感数据 | `POST /toolkit/customer`<br>`GET /customer/{id}`、`/{id}/raw`<br>`POST /customer/{id}/verify-password` | `AES`、`BCrypt`、`DesensitizedUtil`、`IdcardUtil` | 手机号身份证加密存储、按角色脱敏、密码校验 |
| 10 | 系统面板 | `GET /toolkit/system`、`/system.html` | `OshiUtil`、`ManagementFactory` | 运维监控页、服务器资源看板 |
| 11 | 接口巡检 | `POST /toolkit/inspect` | `HttpRequest`、`ThreadUtil` | 定时探测依赖服务可用性、拨测 |
| 12 | 文本清洗 | `POST /text/extract`、`/text/sensitive` | `ReUtil`、`SensitiveUtil`、`DFA` | 内容审核、日志脱敏、工单里自动识别手机号/单号 |

## 每个案例的关键点

1. **验证码**：答案存 `TimedCache`，带 TTL；校验成功后立刻删除，同一份验证码不能用两次。`debug-code: true` 时响应里直接带答案，只为方便 curl，线上必须关掉。生产换 Redis，否则多实例校验不过。
2. **扫码登录**：状态机 `WAITING → SCANNED → CONFIRMED`，判断和修改在同一个同步块里。轮询接口只返回状态，token 在 CONFIRMED 之后才给。
3. **商品海报**：合成顺序是「缩放 → 圆角 → 底栏 → 文字 → 二维码」，顺序错了字会被裁、二维码会被盖。输出宽度固定 600，别把原图尺寸直接当输出。
4. **短链**：只放行 http/https，`javascript:` 之类直接拒；跳转用 302 而不是 301，才能统计到每一次点击。短码碰撞要重试。
5. **Excel**：导出用 `@Alias` 绑中文表头；导入失败的行要写回一个错误文件，并给出错误行号，不能让用户自己找。
6. **分片上传**：`init` 记会话，`upload` 落盘成 `index.part`，`merge` 顺序拼接后再算 MD5 比对，不一致就删掉合并结果。`download/{fileId}` 的 fileId 必须校验格式，防止目录穿越。
7. **上传安全**：`FileTypeUtil` 读文件头，扩展名和真实类型不一致就拒；不能只看扩展名，改个后缀就能绕过。压缩、打包都限制在内存里做，别落一堆临时文件。
8. **签名**：`appId\ntimestamp\nnonce\nbody` 拼成规范串，HMAC-SHA256 出签名；校验时比较时间窗（±300s）、比对签名用常量时间比较、nonce 记下来防重放。签名覆盖请求体，改一个字节就失效。
9. **敏感数据**：手机号、身份证 AES 加密存，密码 BCrypt 哈希存；出站一律走独立的视图对象，默认脱敏，`role=admin` 才给全码。`/raw` 是给开发看的存储层快照。
10. **系统面板**：CPU、内存、磁盘、JVM 一次取全，`OshiUtil` 首次调用要预热（会加载 native 库），别放在高频接口里。
11. **接口巡检**：多线程并发探测，单个地址超时要独立，不能让一个慢地址拖垮整批；统计出成功数、失败数和平均耗时。生产必须加目标白名单，否则就是 SSRF 跳板。
12. **文本清洗**：正则先长后短（先身份证再手机号），否则身份证里的 11 位会被当成手机号；敏感词 DFA 树只初始化一次，词库要能热更新。

## 在 Apifox 里测

> 大白话版逐步点法见 [`docs/apifox-白话操作.md`](docs/apifox-白话操作.md)（从第 4 个案例开始，每个接口点哪里、填什么、应该看到什么）。

1. 项目 → 导入 → 「OpenAPI/Swagger」→ 选 `docs/apifox-openapi.json`，34 个接口一次性进来，示例值已填好。
2. 环境的前置 URL 保持 `http://127.0.0.1:8015`；Apifox 的「代理」要关掉，否则 127.0.0.1 会被代理拦掉。
3. 案例 4 的短链想看 302，先关掉「自动重定向」；文件类接口（海报、Excel、zip）在响应区直接「下载」即可存成文件。
4. 案例 8 的签名可以用前置脚本自动生成（Apifox 内置 CryptoJS）：

```js
const appId = 'toolkit-demo';
const secret = pm.environment.get('APP_SECRET') || '3f2a9c1e5b7d4086a1c3e5f7092b4d68';
const ts = Math.floor(Date.now() / 1000).toString();
const nonce = Math.random().toString(36).slice(2, 14);
const body = pm.request.body.raw || '';
const sign = CryptoJS.HmacSHA256(`${appId}\n${ts}\n${nonce}\n${body}`, secret).toString(CryptoJS.enc.Hex);
pm.request.headers.upsert({ key: 'X-App-Id', value: appId });
pm.request.headers.upsert({ key: 'X-Timestamp', value: ts });
pm.request.headers.upsert({ key: 'X-Nonce', value: nonce });
pm.request.headers.upsert({ key: 'X-Sign', value: sign });
```

   不想写脚本，就先跑一次 `/toolkit/sign/demo`，把返回的 `timestamp`、`nonce`、`sign` 抄进 `/toolkit/sign/order` 的请求头——但请求体必须和 demo 时用的 `body` 一模一样（连空格都一样），否则签名对不上。

5. 需要串起来的地方，用后置脚本把上一步的值存成环境变量：

```js
pm.environment.set('uploadId', pm.response.json().data.uploadId);   // 案例 6
pm.environment.set('code', pm.response.json().data.code);           // 案例 4
pm.environment.set('customerId', pm.response.json().data.id);       // 案例 9
```

## 冒烟测试

`scripts/smoke_test.py` 覆盖上面全部 12 个案例共 50 项断言（含几个应当被拒绝的反例）。

```bash
# 先启动应用，再另开一个终端
python 15-toolkit/scripts/smoke_test.py
```

最后一次结果：**50 通过 / 0 失败**。

## 已知的口子（教程第 17 章的练习）

- 文本清洗的 `phones` 会多出一个 `19491231002`，它是身份证号中间的一段。修法是先找出身份证区间再匹配手机号，或者加 `(?<!\d)` / `(?!\d)` 断言。
- 验证码、扫码会话、短链都存在进程内存，重启即清空；换 Redis 才能多实例部署。
- `debug-code`、`/customer/{id}/raw`、`/toolkit/system` 都是演示用，上生产要么关掉要么加鉴权。
