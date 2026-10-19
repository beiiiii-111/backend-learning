#!/usr/bin/env python3
"""把 frontend-learning 的教程同步进笔记站，并把指向仓库外的链接降级为行内代码。

用法：
    python scripts/sync-frontend-docs.py

源默认是 D:\\frontend-learning\\docs，目标是本仓库的 docs/frontend/。
站点里没有源码仓库，所以凡是解析后跑出 docs/frontend/ 的相对链接，
都会从 [文字](路径) 改成 `路径`，避免站上出现 404。
"""
import re
import shutil
import sys
from pathlib import Path

SRC = Path(r"D:\frontend-learning\docs")
DST = Path(__file__).resolve().parent.parent / "docs" / "frontend"

LINK_RE = re.compile(r"\[([^\]]*)\]\(([^)]+)\)")
CODE_SPAN_RE = re.compile(r"`[^`\n]+`")
# 正文里引用标签名时是裸写的（如“重新生成所有<li>”），
# VitePress 会把它当真标签编译，必须转义
TAG_RE = re.compile(r"</?[a-zA-Z][a-zA-Z0-9-]*\b[^>\n]*>")


def escape_bare_tags(text: str) -> str:
    """把围栏代码块和行内代码之外的裸 HTML 标签转义成文本。"""
    out = []
    in_fence = False
    for line in text.split("\n"):
        if line.strip().startswith("```"):
            in_fence = not in_fence
            out.append(line)
            continue
        if in_fence:
            out.append(line)
            continue

        spans = []

        def keep(match: re.Match) -> str:
            spans.append(match.group(0))
            return f"\x00CODE{len(spans) - 1}\x00"

        protected = CODE_SPAN_RE.sub(keep, line)
        protected = TAG_RE.sub(
            lambda m: m.group(0).replace("<", "&lt;").replace(">", "&gt;"),
            protected,
        )
        protected = re.sub(
            r"\x00CODE(\d+)\x00", lambda m: spans[int(m.group(1))], protected
        )
        out.append(protected)
    return "\n".join(out)


def downgrade(match: re.Match, md_file: Path) -> str:
    label, target = match.group(1), match.group(2).strip()
    if not target or target.startswith(("http://", "https://", "mailto:", "#", "<")):
        return match.group(0)

    target_path = target.split("#")[0].strip()
    if not target_path:
        return match.group(0)

    resolved = (md_file.parent / target_path).resolve()
    try:
        resolved.relative_to(DST.resolve())
    except ValueError:
        # 逃出了站点目录 —— 站上点不开，降级成行内代码，只保留完整路径
        return f"`{target_path}`"

    return match.group(0)


def first_heading(md_file: Path, fallback: str) -> str:
    try:
        m = re.search(r"^#\s+(.+?)\s*$", md_file.read_text(encoding="utf-8", errors="ignore"), re.M)
        if m:
            return m.group(1)
    except OSError:
        pass
    return fallback


def section_index(section_dir: Path) -> str:
    """给教程的一个部分生成索引页，列出本层文件与子目录里的文章。"""
    name = section_dir.name.replace("_", " ")
    pretty = re.sub(r"^(\d+)-", r"\1 · ", name)
    lines = [f"# {pretty}", "", f"> 本部分共收录以下文章，按目录顺序排列。", ""]

    def rel_link(target: Path, label: str) -> str:
        rel = target.relative_to(section_dir).as_posix()
        return f"- [{label}]({rel})"

    entries = sorted(section_dir.iterdir(), key=lambda p: p.name)

    files = [e for e in entries if e.is_file() and e.suffix == ".md" and e.name != "README.md"]
    if files:
        for f in files:
            lines.append(rel_link(f, first_heading(f, f.stem)))
        lines.append("")

    for sub in [e for e in entries if e.is_dir()]:
        lines.append(f"## {re.sub(r'^(\d+)-', r'\1 · ', sub.name)}")
        lines.append("")
        sub_files = sorted(
            (p for p in sub.iterdir() if p.is_file() and p.suffix == ".md" and p.name != "README.md"),
            key=lambda p: p.name,
        )
        for f in sub_files:
            lines.append(rel_link(f, first_heading(f, f.stem)))
        lines.append("")

    return "\n".join(lines).rstrip() + "\n"


def protect_inline_mustache(text: str) -> str:
    """行内代码里的 {{ }} 会被 Vue 执行，包一层 <code v-pre> 保住原文。"""
    out = []
    in_fence = False
    for line in text.split("\n"):
        if line.strip().startswith("```"):
            in_fence = not in_fence
            out.append(line)
            continue
        if in_fence:
            out.append(line)
            continue

        def wrap(match: re.Match) -> str:
            code = match.group(0)[1:-1]
            if "{{" not in code and "}}" not in code:
                return match.group(0)
            escaped = (
                code.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            )
            return f"<code v-pre>{escaped}</code>"

        out.append(CODE_SPAN_RE.sub(wrap, line))
    return "\n".join(out)


def main() -> int:
    if not SRC.exists():
        print(f"源目录不存在：{SRC}")
        return 1

    if DST.exists():
        # 只清理源里已经没有的旧文件（通常是 0 个），不做整目录删除
        wanted = {
            p.relative_to(SRC).as_posix()
            for p in SRC.rglob("*")
            if p.is_file()
            and not any(part in {".vitepress", "node_modules", ".git"} for part in p.parts)
        }
        for old in DST.rglob("*"):
            # 部分索引页是本脚本生成的，源里没有，不能当成失效文件
            if old.is_file() and old.name == "index.md":
                continue
            if old.is_file() and old.relative_to(DST).as_posix() not in wanted:
                print(f"删除已失效文件：{old.relative_to(DST)}")
                old.unlink()
    else:
        DST.mkdir(parents=True)

    copied = 0
    fixed = 0
    changed_files = []

    for src_file in SRC.rglob("*"):
        if not src_file.is_file():
            continue
        if any(part in {".vitepress", "node_modules", ".git"} for part in src_file.parts):
            continue
        rel = src_file.relative_to(SRC)
        # 教程自带的 README.md 是这一块的首页，VitePress 只认 index.md
        if rel.as_posix() == "README.md":
            rel = rel.parent / "index.md"
        dst_file = DST / rel
        dst_file.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src_file, dst_file)
        copied += 1

        if src_file.suffix != ".md":
            continue

        text = src_file.read_text(encoding="utf-8", errors="ignore")
        text = text.replace("](./仓库Git地图)", "](./仓库Git地图.html)")
        # 部分目录本身没有索引页，链接改指向生成的 README.md
        text = re.sub(r"\]\(\./([^)]+?)/index\)", r"](./\1/README.md)", text)
        new_text = LINK_RE.sub(lambda m: downgrade(m, dst_file), text)
        new_text = escape_bare_tags(new_text)
        new_text = protect_inline_mustache(new_text)
        if new_text != text:
            count = len(LINK_RE.findall(text)) - len(LINK_RE.findall(new_text))
            fixed += count
            changed_files.append((str(rel), count))
            dst_file.write_text(new_text, encoding="utf-8")

    # 教程各部分的目录没有索引页，站内点不进去，这里补生成一份
    sections = []
    for entry in sorted(DST.iterdir(), key=lambda p: p.name):
        if not entry.is_dir():
            continue
        # 目录索引页 VitePress 只认 index.md，写成 README.md 会被判成死链
        index_file = entry / "index.md"
        index_file.write_text(section_index(entry), encoding="utf-8")
        sections.append(entry.name)

    print(f"已复制 {copied} 个文件到 {DST}")
    if sections:
        print(f"补生成 {len(sections)} 个部分索引页：{'、'.join(sections)}")
    print(f"降级跨仓库链接 {fixed} 处，涉及 {len(changed_files)} 个文件")
    for name, count in changed_files[:20]:
        print(f"  {count:>3} 处  {name}")
    if len(changed_files) > 20:
        print(f"  ... 其余 {len(changed_files) - 20} 个文件")
    return 0


if __name__ == "__main__":
    sys.exit(main())
