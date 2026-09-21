# 01 · 环境准备 —— Node、npm 与 Vite 脚手架

> - 难度：入门
> - 前置：会打开终端
> - 预计时长：约 30 分钟
> - 本机环境：Windows 11 / Node v22.22.2 / npm 10.9.7
> - 对应代码：[frontend-learning](https://github.com/beiiiii-111/frontend-learning)

## 1. 本节导读

学完本节，你应该能独立完成三件事：

1. 说清 **Node、npm、Vite** 三者的分工；
2. 从零创建一个能热更新的 Vue 3 工程并成功启动；
3. 认出初学者最容易卡住的两个坑：**装不上依赖** 和 **脚本无法执行**。

---

## 2. 概念铺垫

### 2.1 三者是什么关系

| 名词 | 身份 | 一句话解释 |
| --- | --- | --- |
| **Node.js** | 运行时 | 让 JavaScript 脱离浏览器、直接在电脑上跑。npm、Vite 都靠它启动 |
| **npm** | 包管理器 | 负责下载别人写好的库（如 `vue`、`vite`），并记录在项目里 |
| **Vite** | 构建工具 | 开发时起本地服务器、秒级热更新；上线时把源码打包压缩成静态文件 |

一句话类比：**Node 是发动机，npm 是零件仓库，Vite 是流水线**。

### 2.2 为什么看不到「编译」按钮

传统 Java 开发是「写完 → 编译 → 启动」；前端 Vite 是「改一行 → 浏览器自动刷新」。这个自动刷新的能力叫 **HMR（热模块替换）**，由 Vite 的开发服务器提供，新手阶段只要记住：**保持终端开着，改代码别重启**。

---

## 3. 实操：创建第一个工程

```powershell
# 1. 确认环境
node -v     # 本机：v22.22.2
npm -v      # 本机：10.9.7

# 2. 创建 Vite + Vue 模板（交互式取名，一路回车即可）
npm create vite@latest 02-vite-vue-demo

# 3. 进入目录装依赖
cd 02-vite-vue-demo
npm install

# 4. 启动开发服务器
npm run dev
```

终端输出下面这行就成功了：

```
Local: http://localhost:5173/
```

浏览器打开 `http://localhost:5173`，能看到 Vue 官方欢迎页。**之后每次改代码保存，页面会自动变化，不用重启。**

## 4. 目录结构速览

```
02-vite-vue-demo
├── index.html          # 唯一的 HTML 入口（浏览器真正加载的那个）
├── package.json        # 项目清单：依赖 + 启动脚本
├── vite.config.js      # Vite 配置文件
├── src/
│   ├── main.js         # 把 App.vue 挂载到 index.html 的 #app 上
│   └── App.vue         # 组件本体（模板 + 逻辑 + 样式写一起）
└── public/             # 静态资源，原样拷贝不参与打包
```

`App.vue` 这种 `.vue` 文件叫 **SFC（单文件组件）**，一个文件里包含三段：

```vue
<script setup>  <!-- 逻辑 -->
</script>

<template>
  <!-- 页面结构 -->
</template>

<style scoped>
  /* 样式，scoped 表示只作用于本组件 */
</style>
```

---

## 5. 踩坑清单

### 坑 1：安装时提示 `postinstall script skipped`

**现象**：安装依赖时出现 `npm warn install-scripts esbuild@x.x.x postinstall skipped`，或者 Vite 启动时报 esbuild 相关错误。

**原因**：新版 npm 出于安全考虑，默认拦截依赖包的安装脚本；而 esbuild（Vite 的底层依赖）必须跑这个脚本去下载它自己的二进制文件。

**解决**：

```powershell
npm install-scripts approve esbuild
npm install
```

同意后 `package.json` 里会出现一段记录：

```json
"allowScripts": { "esbuild@0.21.5": true }
```

### 坑 2：`Missing script: dev`

**现象**：执行 `npm run dev` 报 `npm error Missing script: "dev"`。

**原因**：`package.json` 里没有 `scripts` 字段——常见于自己 `npm init` 出来的空项目。

**解决**：补上脚本（注意 vite 后面的 `docs` 参数，指定文档目录时才需要）：

```json
"scripts": {
  "dev": "vite",
  "build": "vite build",
  "preview": "vite preview"
}
```

### 坑 3：PowerShell 路径写错

Git Bash 里的 `/d/xxx` 写法在 PowerShell 中无效，必须写 `D:\xxx`。两个终端命令对照：

| 操作 | Git Bash | PowerShell |
| --- | --- | --- |
| 进入项目 | `cd /d/backend-learning` | `cd D:\backend-learning` |
| 切到 D 盘 | `cd /d/` | `D:` |

最省事的办法：在资源管理器里进到目标文件夹，**右键 → 在终端中打开**，省掉所有路径问题。

---

## 6. 本节小结

- Node 跑 JS，npm 管依赖，Vite 管开发服务器和打包；
- `npm run dev` 之后别关终端，改代码靠热更新；
- 遇到报错先看是不是「安装被拦」或「脚本没配」。

---

## 参考

- 下一节：[02 · Vue 字面量入门](./02-vue-literal.md)
- 官方文档：[Vite 中文网](https://cn.vitejs.dev/) · [Vue 3 中文文档](https://cn.vuejs.org/)
