"""15-toolkit 十二个案例的端到端冒烟测试。"""
import hashlib
import http.client
import json
import os
import uuid
import urllib.parse

HOST, PORT = "127.0.0.1", 8015
TMP = os.path.join(os.environ.get("TEMP", "/tmp"), "toolkit-smoke")
os.makedirs(TMP, exist_ok=True)

passed, failed = [], []


def check(name, cond, detail=""):
    (passed if cond else failed).append(name)
    print(("  [OK]   " if cond else "  [FAIL] ") + name + (f"  {detail}" if detail else ""))


def req(method, path, body=None, headers=None, raw=False):
    conn = http.client.HTTPConnection(HOST, PORT, timeout=15)
    conn.request(method, path, body=body, headers=headers or {})
    resp = conn.getresponse()
    data = resp.read()
    ctype = resp.getheader("Content-Type", "")
    result = (resp.status, dict(resp.getheaders()), data)
    conn.close()
    if raw:
        return result
    try:
        return resp.status, json.loads(data.decode("utf-8"))
    except Exception:
        return resp.status, data


def json_req(method, path, obj=None, headers=None):
    h = {"Content-Type": "application/json; charset=utf-8"}
    h.update(headers or {})
    body = json.dumps(obj, ensure_ascii=False).encode("utf-8") if obj is not None else None
    return req(method, path, body, h)


def multipart(path, fields, files):
    boundary = "----smoke" + uuid.uuid4().hex
    out = b""
    for k, v in fields.items():
        out += (f"--{boundary}\r\nContent-Disposition: form-data; name=\"{k}\"\r\n\r\n{v}\r\n").encode()
    for k, (fname, content, ctype) in files:
        out += (f"--{boundary}\r\nContent-Disposition: form-data; name=\"{k}\"; filename=\"{fname}\"\r\n"
                f"Content-Type: {ctype}\r\n\r\n").encode() + content + b"\r\n"
    out += f"--{boundary}--\r\n".encode()
    return req("POST", path, out, {"Content-Type": f"multipart/form-data; boundary={boundary}"}, raw=True)


print("\n[1] 图形验证码")
st, js = req("GET", "/toolkit/captcha?type=line")
cap = js.get("data") or {}
check("GET /captcha?type=line 返回图片和 id", st == 200 and cap.get("captchaId") and cap.get("image"),
      f"status={st}")
cid, code = cap.get("captchaId"), cap.get("code")
st2, js2 = json_req("POST", "/toolkit/captcha/verify", {"captchaId": cid, "code": code})
check("校验正确验证码通过", st2 == 200, f"status={st2} {js2}")
st3, js3 = json_req("POST", "/toolkit/captcha/verify", {"captchaId": cid, "code": code})
check("同一验证码不可重复使用", st3 >= 400, f"status={st3} {js3}")
st4, js4 = req("GET", "/toolkit/captcha?type=math")
check("GET /captcha?type=math 出算术题", st4 == 200 and (js4.get("data") or {}).get("image"))

print("\n[2] 扫码登录")
st, js = req("POST", "/toolkit/qr-login/create")
t = (js.get("data") or {})
check("create 返回 ticket 和二维码", st == 200 and t.get("ticket") and t.get("image"), f"status={st}")
ticket = t.get("ticket")
st, js = req("GET", f"/toolkit/qr-login/status?ticket={ticket}")
check("初始状态 WAITING", (js.get("data") or {}).get("state") == "WAITING", str(js.get("data")))
st, js = req("POST", f"/toolkit/qr-login/scan?ticket={ticket}")
check("scan 后状态 SCANNED", (js.get("data") or {}).get("state") == "SCANNED", str(js.get("data")))
st, js = req("POST", f"/toolkit/qr-login/confirm?ticket={ticket}&phone=13800001111")
conf = js.get("data") or {}
check("confirm 后拿到 token", st == 200 and conf.get("state") == "CONFIRMED" and conf.get("token"),
      str(conf))
st, js = req("GET", "/toolkit/qr-login/ascii?ticket=" + str(ticket))
check("ascii 字符画接口可用", b"ticket" not in (js if isinstance(js, bytes) else b""))

print("\n[3] 商品海报")
st, headers, data = multipart("/toolkit/poster",
                              {"title": "秋季新品", "price": "2999.00", "qrContent": "https://example.com"},
                              [])
check("合成海报返回 PNG", st == 200 and data[:8] == b"\x89PNG\r\n\x1a\n", f"status={st} size={len(data)}")
poster_path = os.path.join(TMP, "poster.png")
open(poster_path, "wb").write(data)
st, headers, data = req("POST", "/toolkit/poster", raw=True)
check("不传参时用默认底图和默认文案", st == 200 and data[:8] == b"\x89PNG\r\n\x1a\n",
      f"status={st} size={len(data)}")

print("\n[4] 短链")
st, js = json_req("POST", "/toolkit/short-link",
                  {"url": "https://example.com/a/very/long/path?x=1&y=2", "ttlSeconds": 604800})
sl = js.get("data") or {}
check("创建短链返回 code", st == 200 and sl.get("code") and sl.get("shortUrl"), str(sl))
code = sl.get("code")
st, headers, data = req("GET", f"/toolkit/s/{code}", raw=True)
check("访问短链 302 跳转", st == 302 and headers.get("Location") ==
      "https://example.com/a/very/long/path?x=1&y=2", f"status={st} loc={headers.get('Location')}")
st, js = req("GET", f"/toolkit/short-link/{code}")
check("统计点击数 +1", (js.get("data") or {}).get("clicks") == 1, str(js.get("data")))
st, js = json_req("POST", "/toolkit/short-link", {"url": "javascript:alert(1)"})
check("非法协议被拒", st >= 400, f"status={st}")

print("\n[5] Excel 导入导出")
st, headers, data = req("GET", "/toolkit/excel/export", raw=True)
check("导出 xlsx", st == 200 and data[:2] == b"PK", f"status={st} size={len(data)}")
xlsx_path = os.path.join(TMP, "orders.xlsx")
open(xlsx_path, "wb").write(data)
st, headers, tpl = req("GET", "/toolkit/excel/template", raw=True)
check("下载导入模板", st == 200 and tpl[:2] == b"PK", f"status={st}")
st, headers, mp = multipart("/toolkit/excel/import", {},
                            [("file", ("orders.xlsx", data,
                                       "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))])
imp = json.loads(mp.decode("utf-8")).get("data") if mp[:1] == b"{" else {}
check("导入导出过的文件全部成功", st == 200 and imp and imp.get("total", 0) > 0 and imp.get("success") == imp.get("total"),
      str(imp)[:200])
st, headers, data2 = req("GET", "/toolkit/excel/download/..%2Fapplication.yml", raw=True)
check("下载接口拦目录穿越", st >= 400, f"status={st}")

print("\n[6] 分片上传")
big = os.path.join(TMP, "big.bin")
with open(big, "wb") as f:
    f.write(os.urandom(3 * 1024))
content = open(big, "rb").read()
md5 = hashlib.md5(content).hexdigest()
parts = [content[i * 1024:(i + 1) * 1024] for i in range(3)]
st, js = json_req("POST", "/toolkit/chunk/init",
                  {"fileName": "big.bin", "totalChunks": 3, "md5": md5})
init = js.get("data") or {}
check("init 返回 uploadId", st == 200 and init.get("uploadId"), str(init))
uid = init.get("uploadId")
for i, p in enumerate(parts):
    st, headers, body = multipart("/toolkit/chunk/upload", {"uploadId": uid, "index": str(i)},
                                  [("file", (f"part-{i}", p, "application/octet-stream"))])
    check(f"上传第 {i} 片", st == 200, f"status={st} {body[:120]}")
st, js = req("GET", f"/toolkit/chunk/status?uploadId={uid}")
stt = js.get("data") or {}
check("status 显示 3 片齐全", len(stt.get("uploaded") or []) == 3, str(stt))
st, js = json_req("POST", f"/toolkit/chunk/merge?uploadId={uid}", None)
mg = js.get("data") or {}
check("merge 返回 fileId", st == 200 and mg.get("fileId"), str(mg))
st, headers, merged = req("GET", f"/toolkit/chunk/download/{mg.get('fileId')}", raw=True)
check("下载合并结果字节一致", st == 200 and merged == content, f"size={len(merged)}")

print("\n[7] 上传安全")
st, headers, body = multipart("/toolkit/file/check", {},
                              [("file", ("fake.png", b"#!/bin/sh\necho hi\n", "image/png"))])
chk = json.loads(body.decode("utf-8")).get("data") if body[:1] == b"{" else {}
check("伪装 png 被识别并拒绝", st == 200 and chk.get("allowed") is False, str(chk))
st, headers, small = multipart("/toolkit/file/compress", {"maxWidth": "300"},
                               [("file", ("poster.png", open(poster_path, "rb").read(), "image/png"))])
check("压缩返回 PNG", st == 200 and small[:8] == b"\x89PNG\r\n\x1a\n", f"status={st} size={len(small)}")
st, headers, zipped = multipart("/toolkit/file/zip", {},
                                [("files", ("poster.png", open(poster_path, "rb").read(), "image/png")),
                                 ("files", ("orders.xlsx", open(xlsx_path, "rb").read(),
                                            "application/octet-stream"))])
check("打包 zip 可用", st == 200 and zipped[:2] == b"PK", f"status={st} size={len(zipped)}")

print("\n[8] 开放接口签名")
RAW_BODY = b'{"orderNo":"SO1"}'
st, js = json_req("POST", "/toolkit/sign/demo", {"body": RAW_BODY.decode()})
demo = js.get("data") or {}
check("demo 生成签名", st == 200 and demo.get("sign"), str(demo)[:160])
hdrs = {"X-App-Id": demo.get("appId", ""), "X-Timestamp": str(demo.get("timestamp")),
        "X-Nonce": demo.get("nonce", ""), "X-Sign": demo.get("sign", ""),
        "Content-Type": "application/json; charset=utf-8"}
st, js = req("POST", "/toolkit/sign/order", RAW_BODY, hdrs)
check("合法签名调用成功", st == 200, f"status={st} {js}")
bad = dict(hdrs)
bad["X-Sign"] = "0" * 64
st, js = req("POST", "/toolkit/sign/order", RAW_BODY, bad)
check("篡改签名被拒", st >= 400, f"status={st} {js}")
st, js = req("POST", "/toolkit/sign/order", RAW_BODY, hdrs)
check("nonce 重放被拒", st >= 400, f"status={st} {js}")
st, js = req("POST", "/toolkit/sign/order", b'{"orderNo":"SO2"}', hdrs)
check("改请求体后签名失效", st >= 400, f"status={st} {js}")

print("\n[9] 敏感数据")
st, js = json_req("POST", "/toolkit/customer",
                  {"name": "张三", "phone": "13800001234", "idCard": "11010519491231002X",
                   "password": "abc123", "level": "vip"})
cus = js.get("data") or {}
check("创建客户返回视图（创建时是本人信息，不脱敏）",
      st == 200 and cus.get("id") and cus.get("phone") == "13800001234", str(cus)[:160])
cid2 = cus.get("id")
st, js = json_req("GET", f"/toolkit/customer/{cid2}")
view = js.get("data") or {}
check("默认角色查询脱敏", st == 200 and view.get("phone") == "138****1234"
      and view.get("idCard") == "110105********002X", str(view)[:160])
st, js = json_req("GET", f"/toolkit/customer/{cid2}?role=admin")
admin = js.get("data") or {}
check("role=admin 返回完整值", st == 200 and admin.get("phone") == "13800001234", str(admin)[:160])
check("身份证解析出年龄/性别", view.get("age", 0) > 0 and view.get("gender") in ("男", "女"), str(view)[:160])
st, js = req("GET", f"/toolkit/customer/{cid2}/raw")
storage = js.get("data") or {}
dump = json.dumps(storage, ensure_ascii=False)
check("raw 看存储层：手机号/身份证是密文，密码是 BCrypt",
      st == 200 and storage.get("phoneEncrypted") and storage.get("idCardEncrypted")
      and str(storage.get("passwordHash", "")).startswith("$2")
      and "13800001234" not in dump, str(storage)[:160])
st, js = json_req("POST", f"/toolkit/customer/{cid2}/verify-password", {"password": "wrong"})
check("错误密码被拒", st >= 400, f"status={st} {js}")
st, js = json_req("POST", f"/toolkit/customer/{cid2}/verify-password", {"password": "abc123"})
check("正确密码通过（BCrypt 校验）", st == 200, f"status={st} {js}")

print("\n[10] 系统面板")
st, js = req("GET", "/toolkit/system")
sysd = js.get("data") or {}
check("系统信息含 CPU/内存/磁盘/JVM",
      st == 200 and sysd.get("cpu") and sysd.get("memory") and sysd.get("jvm"), str(sysd)[:160])
st, headers, page = req("GET", "/system.html", raw=True)
check("静态面板页可直接打开", st == 200 and b"<html" in page.lower(), f"status={st}")

print("\n[11] 接口巡检")
st, js = json_req("POST", "/toolkit/inspect",
                  {"urls": ["http://127.0.0.1:8015/toolkit/system",
                            "http://127.0.0.1:8015/toolkit/nothing",
                            "http://127.0.0.1:9/none", "not-a-url"]})
ins = js.get("data") or {}
check("4 个地址全部返回结果", st == 200 and ins.get("total") == 4, str(ins)[:200])
check("统计出成功/失败", ins.get("success") == 1 and ins.get("failed") == 3, str(ins)[:160])

print("\n[12] 文本清洗")
st, js = json_req("POST", "/toolkit/text/extract",
                  {"text": "联系 13800001234 或 service@example.com，身份证 11010519491231002X，"
                           "图片 http://cdn.example.com/a/b.png?x=1，IP 192.168.1.100。"})
ext = js.get("data") or {}
check("提取手机/邮箱/身份证/IP/URL",
      st == 200 and ext.get("phones") and ext.get("emails") and ext.get("idCards")
      and ext.get("urls") and ext.get("ips"), str(ext)[:220])
check("脱敏文本含星号", "*" in str(ext.get("maskedText")), str(ext.get("maskedText"))[:120])
st, js = json_req("POST", "/toolkit/text/sensitive",
                  {"text": "内容涉及赌博和诈骗，代开发票请绕行。"})
sen = js.get("data") or {}
check("敏感词命中 3 个", st == 200 and sen.get("hits") == ["赌博", "诈骗", "代开发票"], str(sen)[:200])
check("高亮标记 <mark>", sen.get("highlighted", "").count("<mark>") == 3, str(sen.get("highlighted")))

print("\n[13] 全局异常与 404")
st, js = req("GET", "/toolkit/nothing")
check("未知接口返回 4xx 且带业务码", st >= 400 and isinstance(js, dict) and js.get("code"), f"status={st} {js}")

print(f"\n==== 通过 {len(passed)} / 失败 {len(failed)} ====")
if failed:
    print("失败项：")
    for f in failed:
        print(" -", f)
