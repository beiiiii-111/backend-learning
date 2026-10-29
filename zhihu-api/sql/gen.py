# -*- coding: utf-8 -*-
"""把知乎原始数据 data.json 清洗成 data_cleaned.csv，并生成建表 + 导入的 t_special.sql"""
import csv
import json
import os
from datetime import datetime, timezone, timedelta

BASE = os.path.dirname(os.path.abspath(__file__))
TZ = timezone(timedelta(hours=8))

CATEGORY_RULES = [
    # 练习 1：按 title / introduction 的内容关键词归类（命中即停，顺序即优先级）
    ("时事", ("两会", "每周看点", "媒体")),
    ("科技", ("数码", "科技", "隐形眼镜", "电子")),
    ("文化", ("美学", "文化", "人文")),
    ("生活", ("生活", "好物", "京东", "崂山")),
]


def clean(v):
    return (v or "").replace("\t", "").replace("\r", " ").replace("\n", " ").strip()


def pick_category(title, intro):
    text = title + intro
    for name, keywords in CATEGORY_RULES:
        if any(k in text for k in keywords):
            return name
    return "其他"


def main():
    with open(os.path.join(BASE, "data.json"), encoding="utf-8") as f:
        raw = json.load(f)

    rows = []
    for r in raw:
        rows.append({
            "id": int(r["id"]),
            "title": clean(r["title"]),
            "description": clean(r["introduction"]),
            "banner": r["banner"],
            "is_following": "TRUE" if r["is_following"] else "FALSE",
            "followers_count": r["followers_count"],
            "view_count": r["view_count"],
            "updated": r["updated"],
            "category": pick_category(clean(r["title"]), clean(r["introduction"])),
        })
    rows.sort(key=lambda x: x["updated"], reverse=True)

    # 1) data_cleaned.csv
    cols = ["id", "title", "description", "banner", "is_following",
            "followers_count", "view_count", "updated", "category"]
    with open(os.path.join(BASE, "data_cleaned.csv"), "w", encoding="utf-8-sig", newline="") as f:
        w = csv.DictWriter(f, fieldnames=cols, quoting=csv.QUOTE_ALL)
        w.writeheader()
        w.writerows(rows)

    # 2) t_special.sql
    def esc(v):
        return str(v).replace("\\", "\\\\").replace("'", "''")

    lines = [
        "-- 知乎专题表：建表 + 导入数据（Navicat / 命令行均可执行）",
        "CREATE DATABASE IF NOT EXISTS backend_learning DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;",
        "USE backend_learning;",
        "",
        "DROP TABLE IF EXISTS t_special;",
        "CREATE TABLE t_special (",
        "  id              BIGINT       NOT NULL COMMENT '专题ID',",
        "  title           VARCHAR(255) NOT NULL COMMENT '标题',",
        "  description     TEXT                  COMMENT '简介',",
        "  banner          VARCHAR(500)          COMMENT '封面图',",
        "  is_following    VARCHAR(10)  DEFAULT 'FALSE' COMMENT '是否关注',",
        "  followers_count INT          DEFAULT 0 COMMENT '关注数',",
        "  view_count      BIGINT       DEFAULT 0 COMMENT '浏览数',",
        "  updated         BIGINT       DEFAULT 0 COMMENT '更新时间(Unix时间戳)',",
        "  category        VARCHAR(100)          COMMENT '分类',",
        "  PRIMARY KEY (id)",
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知乎专题表';",
        "",
        "INSERT INTO t_special (id, title, description, banner, is_following, followers_count, view_count, updated, category) VALUES",
    ]
    vals = []
    for r in rows:
        vals.append("  ({id}, '{title}', '{description}', '{banner}', '{is_following}', "
                    "{followers_count}, {view_count}, {updated}, '{category}')".format(
                        id=r["id"],
                        title=esc(r["title"]),
                        description=esc(r["description"]),
                        banner=esc(r["banner"]),
                        is_following=r["is_following"],
                        followers_count=r["followers_count"],
                        view_count=r["view_count"],
                        updated=r["updated"],
                        category=r["category"],
                    ))
    lines.append(",\n".join(vals) + ";")
    lines.append("")
    lines.append("-- 想改分类规则，直接改下面的值再执行即可")
    for r in rows:
        lines.append("--   id=%s  %s" % (r["id"], r["title"]))

    with open(os.path.join(BASE, "t_special.sql"), "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

    print("共 %d 条" % len(rows))
    for r in rows:
        print("  %-8s %-20s updated=%s (%s)" % (
            r["category"], r["title"][:20],
            datetime.fromtimestamp(r["updated"], TZ).strftime("%Y-%m-%d %H:%M"),
            r["updated"]))


if __name__ == "__main__":
    main()
