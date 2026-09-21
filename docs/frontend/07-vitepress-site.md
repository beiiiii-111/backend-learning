# 07 · 用 VitePress 搭建本笔记站

> - 难度：进阶
> - 前置：[01 · 环境准备](./01-env-setup.md)
> - 预计时长：约 60 分钟
> - 版本：VitePress 1.6.4 / Node v22.22.2

## 1. 本节导读

你正在看的这个站点本身就是这一节的产物。记录下来是因为它把前面几节的知识点串成了一个真实工程：

1. **Markdown 直接变成网页**：不用写一行 HTML；
2. **目录结构即导航结构**：文件怎么放，侧边栏就长什么样；
3. **push 之后自动上线**：GitHub Actions + GitHub Pages。

---

## 2. VitePress 是什么

VitePress = **Vite + Markdown + Vue 驱动的静态站点生成器**。

| 对比 | 传统做法 | VitePress |
| --- | --- | --- |
| 写文档 | 手写 HTML / Word 转 HTML | 直接写 Markdown |
| 改样式 | 改 CSS 再刷新 | 支持 Vue 组件，热更新 |
| 上线 | 手动传服务器 | `git push` 自动部署 |

最大的好处：**笔记源码就是能版本管理的 Markdown**，diff 清晰、回滚方便、和代码放同一个仓库。

---

## 3. 目录结构

```
backend-learning/
├── docs/                       # 站点根目录（srcDir）
│   ├── .vitepress/
│   │   ├── config.mts          # 站点配置：标题、导航、侧边栏
│   │   └── theme/
│   │       ├── index.ts        # 引入自定义主题
│   │       └── style.css       # 自定义样式
│   ├── index.md                # 首页
│   ├── frontend/               # 前端笔记（本文所在）
│   └── backend/                # 后端笔记
├── package.json
└── .github/workflows/deploy.yml  # 自动部署
```

**约定 > 配置**：`docs/` 下的每个 `.md` 文件对应一个网页，`01-env-setup.md` 的访问路径就是 `/frontend/01-env-setup`。

## 4. 最小可运行配置

`docs/.vitepress/config.mts`：

```ts
import { defineConfig } from 'vitepress'

export default defineConfig({
  title: '訫的学习笔记',
  description: '前后端工程化学习笔记',
  base: '/backend-learning/',       // 部署在 GitHub Pages 子路径时必须配

  themeConfig: {
    nav: [
      { text: '首页', link: '/' }
    ],
    sidebar: {
      '/frontend/': [ /* 前端分组 */ ],
      '/backend/':  [ /* 后端分组 */ ]
    }
  }
})
```

> `sidebar` 用**对象形式**而不是数组，是因为首页/前端/后端需要三套不同的侧边栏，key 是路径前缀。

`package.json` 脚本：

```json
{
  "scripts": {
    "dev": "vitepress dev docs",
    "build": "vitepress build docs",
    "preview": "vitepress preview docs"
  }
}
```

**`docs` 参数不能省**。不写的话 VitePress 会把仓库根目录当成站点源码，导致 `docs/index.md` 找不到、页面 404。

---

## 5. 自定义一点点设计感

默认主题已经够用，想加点个性就扩展它——`docs/.vitepress/theme/index.ts`：

```ts
import DefaultTheme from 'vitepress/theme'
import './style.css'

export default DefaultTheme
```

然后在 `style.css` 里只做三件事就够了，风格依然克制：

```css
:root {
  --vp-c-brand-1: #2f6fed;        /* 品牌色 */
  --vp-home-hero-name-color: transparent;
}

/* 1. 正文宽度稍微放宽，长代码块不挤 */
.vp-doc { --vp-content-width: 820px; }

/* 2. 卡片、表格统一圆角 + 细边框 */
.vp-doc table { border-radius: 10px; overflow: hidden; }

/* 3. 行内代码加淡背景，代码块右上加圆角 */
.vp-doc :not(pre) > code {
  background: #f1f5f9;
  border-radius: 6px;
}
```

**原则**：只覆盖 CSS 变量和少数几个类名，**不要重写整套主题**。这样升级 VitePress 时不容易炸。

---

## 6. 自动部署到 GitHub Pages

`.github/workflows/deploy.yml`：

```yaml
name: Deploy VitePress
on:
  push:
    branches: [main]
permissions:
  contents: read
  pages: write
  id-token: write
jobs:
  deploy:
    runs-on: ubuntu-latest
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 22
      - run: npm install
      - run: npm run build
      - uses: actions/configure-pages@v4
      - uses: actions/upload-pages-artifact@v3
        with:
          path: docs/.vitepress/dist
      - uses: actions/deploy-pages@v4
        id: deployment
```

仓库网页：**Settings → Pages → Source 选 GitHub Actions**，之后每次 `git push` 就自动重新部署。

---

## 7. 踩坑清单（本次实操真实遇到）

### 坑 1：`.vitepress` 建成了文件而不是文件夹

**现象**：截图里 VS Code 显示它是 TypeScript 图标的一个条目，`ls` 看到 size 是 **0**。

**原因**：想「新建文件夹」却用了「新建文件」。判断标准很简单——**文件夹能展开，文件不能**。

**解决**：删掉这个空文件，用「New Folder」重新建同名文件夹。

### 坑 2：VS Code 里标签页带白点 `●`

说明**文件没有保存**，磁盘上还是空的。写了半天 `index.md`，网页却 404，就是这个原因。按 `Ctrl + S` 再重启服务。

### 坑 3：脚本少了 `docs` 参数

`"dev": "vitepress dev"` 会让根目录变成站点根目录，表现为首页 404、`docs/.vitepress/dist` 里没有产物。改成 `vitepress dev docs` 解决。

### 坑 4：Markdown 里链接到仓库外的源码目录

笔记里原来写的 `[代码](../01-quickstart)` 指向仓库根目录下的 Java 模块。部署后这个链接是死的，因为 VitePress 的 srcDir 是 `docs/`，外面的目录不会被打包。

**解决**：改成完整的 GitHub 地址：

```md
[01-quickstart](https://github.com/beiiiii-111/backend-learning/tree/main/01-quickstart)
```

### 坑 5：新建配置文件后必须重启

`config.mts` 的改动**不会热更新**。改完要在终端 `Ctrl + C` 停掉，重新 `npm run dev`。

### 坑 6：部署后样式全丢、点了导航 404

因为 GitHub Pages 给的是 `用户名.github.io/仓库名/` 这样的**子路径**，必须在 `config.mts` 里配 `base: '/backend-learning/'`，且首尾都要有斜杠。

---

## 8. 本节小结

- VitePress 让 Markdown 直接成为可读、可版本管理的笔记网站；
- 目录结构决定路由结构，配置集中在 `config.mts`；
- 定制样式优先改 CSS 变量，别重写主题；
- 部署只需要一个 workflow + 正确的 `base`。

---

## 参考

- 官方文档：[VitePress 中文站](https://vitepress.dev/)
- 本站仓库：[beiiiii-111/backend-learning](https://github.com/beiiiii-111/backend-learning)
- 上一节：[06 · 插槽与主题切换](./06-slot-theme.md)
