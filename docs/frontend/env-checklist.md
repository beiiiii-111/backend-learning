# 环境自检清单

单元 1 的课堂产出。开课前照着逐项打勾，任一项不过就先别往下走。

## 1. 版本基线

| 检查项 | 要求 | 自检命令 | 结果 |
| --- | --- | --- | --- |
| Node.js | ≥ 22.12 LTS | `node -v` | ☐ |
| pnpm | ≥ 9（统一包管理器） | `pnpm -v` | ☐ |
| Git | ≥ 2.40 | `git -v` | ☐ |
| VS Code | 最新版 | `code -v` | ☐ |
| 浏览器 | Chrome / Edge 最新版 | 帮助 → 关于 | ☐ |

> 不要用 npm / yarn 装本仓库的依赖。pnpm 与 npm 的锁文件互不兼容，混用会装出两份依赖树。

## 2. 网络与镜像

| 检查项 | 自检命令 | 结果 |
| --- | --- | --- |
| npmmirror 镜像已生效 | `pnpm config get registry` 应返回 `https://registry.npmmirror.com` | ☐ |
| 能连通镜像源 | `pnpm view vue version` | ☐ |

本机 `.npmrc` 建议内容（各子工程已自带）：

```ini
node-linker=hoisted
registry=https://registry.npmmirror.com
```

## 3. 编辑器

| 检查项 | 说明 | 结果 |
| --- | --- | --- |
| Volar / Vue - Official 插件已装 | 提供 `.vue` 单文件组件高亮与类型检查 | ☐ |
| ESLint 插件已装 | 保存时按工程规则提示 | ☐ |
| Prettier 插件已装并设为默认格式化工具 | 缩进、引号、尾逗号统一 | ☐ |
| 已开启保存时自动格式化 | `editor.formatOnSave: true` | ☐ |
| 禁用 Vetur | 与 Volar 冲突，装了 Volar 就要卸掉 Vetur | ☐ |

## 4. Git 协作

| 检查项 | 自检命令 | 结果 |
| --- | --- | --- |
| 用户名与邮箱已配 | `git config --global user.name` / `user.email` | ☐ |
| SSH 可连通远端 | `ssh -T git@github.com` | ☐ |
| 换行符配置正确 | Windows 下 `git config --global core.autocrlf` 建议为 `input` | ☐ |

## 5. 工程跑通验证

```bash
cd module-02-vue-core/practice-app
pnpm install
pnpm dev
```

浏览器打开提示的地址，能看到「模块二 · Vue 3 核心基础练习」三个单元页签即通过。

## 常见故障

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| `pnpm install` 卡住不动 | 默认源在国外 | 换 npmmirror 镜像 |
| `pnpm dev` 报端口占用 | 5173 已被占 | 换端口或关掉占用进程 |
| 依赖装了但 import 报错 | pnpm 的软链接结构不被某些工具识别 | `.npmrc` 里加 `node-linker=hoisted` 后重装 |
| 构建报目录删除失败 | 沙箱 / 安全软件拦截了删除动作 | 手动清空 `dist/` 再构建 |
