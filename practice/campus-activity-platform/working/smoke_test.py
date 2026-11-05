import json
import urllib.request
import urllib.error

BASE = "http://localhost:8080/api/v1"


def call(method, path, data=None, token=None, expect_error=False):
    req = urllib.request.Request(BASE + path,
                                 json.dumps(data).encode() if data is not None else None,
                                 method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        return json.loads(urllib.request.urlopen(req, timeout=5).read())
    except urllib.error.HTTPError as e:
        return json.loads(e.read())


ok = 0
total = 0


def check(name, cond, detail=""):
    global ok, total
    total += 1
    status = "PASS" if cond else "FAIL"
    if cond:
        ok += 1
    print(f"[{status}] {name} {detail}")


# 1 登录
login = call("POST", "/users/login", {"username": "student", "password": "11111111"})
check("学生登录", login.get("code") == 0 and login["data"]["token"])
tok = login["data"]["token"]

# 2 活动列表
acts = call("GET", "/activities?page=1&size=5")
check("活动列表", acts.get("code") == 0 and len(acts["data"]["list"]) >= 2)
ball = next(a for a in acts["data"]["list"] if a["activityId"] == 1001)
check("列表含已报人数", ball["signedCount"] == 1 and ball["remaining"] == 99, f"{ball['signedCount']}/{ball['capacity']}")

# 3 报名（1002 未报过；1001 在初始数据中已报名）
sg = call("POST", "/activities/1002/signups", {}, tok)
check("报名成功", sg.get("code") == 0 and sg["data"]["signedCount"] == 1, str(sg.get("data")))

# 4 重复报名
dup = call("POST", "/activities/1002/signups", {}, tok)
check("重复报名拦截", dup.get("code") == 40901 and "重复" in dup["message"], dup.get("message"))

# 5 发布活动（组织者）
org = call("POST", "/users/login", {"username": "organizer", "password": "11111111"})
otok = org["data"]["token"]
create = call("POST", "/activities", {
    "title": "联调测试活动", "coverUrl": "", "category": "lecture",
    "startTime": "2026-12-01 14:00:00", "endTime": "2026-12-01 16:00:00",
    "signupDeadline": "2026-11-30 23:59:59", "location": "A101",
    "capacity": 50, "description": "自动化联调"}, otok)
check("发布活动→待审核", create.get("code") == 0 and create["data"]["status"] == "pending")
new_id = create["data"]["activityId"]

# 6 学生端看不到待审核活动
hidden = call("GET", f"/activities/{new_id}", token=tok)
check("待审核活动学生不可见", hidden.get("code") == 40401)

# 7 审核：驳回缺原因
adm = call("POST", "/users/login", {"username": "admin", "password": "11111111"})
atok = adm["data"]["token"]
rej = call("PUT", f"/activities/{new_id}/audit", {"action": "reject"}, atok)
check("驳回缺原因被拦截", rej.get("code") == 40001, rej.get("message"))

# 8 审核通过
approve = call("PUT", f"/activities/{new_id}/audit", {"action": "approve"}, atok)
check("审核通过", approve.get("code") == 0 and approve["data"]["status"] == "approved")

# 9 我的报名
mine = call("GET", "/signups/me", token=tok)
check("我的报名", mine.get("code") == 0 and len(mine["data"]["list"]) >= 1)

# 10 名单查看（组织者）
roster = call("GET", "/activities/1001/signups", token=otok)
check("报名名单", roster.get("code") == 0 and roster["data"]["list"][0]["nickname"] == "张同学")

# 11 无权限校验（学生看名单）
forb = call("GET", "/activities/1001/signups", token=tok)
check("学生看名单被拒", forb.get("code") == 40301)

print(f"\n结果: {ok}/{total} 通过")
