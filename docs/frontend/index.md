# 前端笔记

这一块记录 **Vue 3 + Vite + 工程化** 的学习过程，笔记全部来自 `frontend-learning` 仓库里真实跑过的代码。

## 学习路线

| # | 主题 | 核心收获 |
| --- | --- | --- |
| 01 | [环境准备](./01-env-setup.md) | Node / npm / Vite 三者分工，跑通第一个工程 |
| 02 | [Vue 字面量入门](./02-vue-literal.md) | 不用构建工具也能跑 Vue，理解响应式与挂载 |
| 03 | [Vue 基础指令合集](./03-vue-directives.md) | <code v-pre>{{ }}</code>、`:`、`@`、`v-model`、`v-for`、computed、watch |
| 04 | [组件拆分与事件](./04-component-event.md) | 待办清单：增删改 = 操作数组，`:key` 为什么不能用 index |
| 05 | [计算属性与动态样式](./05-props-style.md) | 下标驱动轮播、动态 class/style、`color-mix()` |
| 06 | [插槽与主题切换](./06-slot-theme.md) | `<slot />`、`:deep()`、CSS 变量驱动整体换色 |
| 07 | [用 VitePress 搭建本笔记站](./07-vitepress-site.md) | Markdown 变网页 + GitHub Actions 自动部署 |
| 08 | [条件渲染：学生名单多视图](./08-student-multi-view.md) | 一份数据四种展示，`v-if / v-else-if / v-else` 分支链 |

## 代码仓库

所有练习代码都在 👉 [beiiiii-111/frontend-learning](https://github.com/beiiiii-111/frontend-learning)，目录与上面的顺序一一对应。

## 环境

Windows 11 · Node v22.22.2 · npm 10.9.7 · Vue 3.5 · Vite 8
