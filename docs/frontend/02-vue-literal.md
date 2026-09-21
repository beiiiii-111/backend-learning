# 02 · Vue 字面量入门 —— 不用构建工具也能跑

> - 难度：入门
> - 前置：[01 · 环境准备](./01-env-setup.md)
> - 预计时长：约 20 分钟
> - 对应代码：[01-vue-literal](https://github.com/beiiiii-111/frontend-learning/tree/main/01-vue-literal)

## 1. 本节导读

这一节刻意 **不使用任何构建工具**，只用 CDN 引入 Vue，目的是看清三件事：

1. Vue 应用是怎么「挂」到页面上的；
2. <code v-pre>{{ }}</code> 里的东西为什么会自动变；
3. 数据为什么必须写在返回对象的函数里。

先把概念摸清，下一节再上 Vite 工程，就不会觉得工具链神秘了。

---

## 2. 完整代码

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <title>Vue3 字面量入门</title>
  <!-- 通过 CDN 引入 Vue3 全局构建版 -->
  <script src="https://cdn.jsdelivr.net/npm/vue@3/dist/vue.global.js"></script>
</head>
<body>
  <div id="app">
    <h1>{{ msg }}</h1>
    <p>计数：{{ count }}</p>
    <button @click="add">点我 +1</button>
  </div>

  <script>
    const { createApp } = Vue

    createApp({
      data() {
        return {
          msg: 'Vue 字面量练习',
          count: 0
        }
      },
      methods: {
        add() {
          this.count++
        }
      }
    }).mount('#app')
  </script>
</body>
</html>
```

双击 html 文件用浏览器打开即可，**不需要 `npm install`，不需要启动服务器**。

## 3. 逐行发生了什么

### 3.1 挂载：`mount('#app')`

`createApp(...)` 只是**创建了一个应用对象**，它还悬浮在内存里；只有调用 `.mount('#app')` 之后，Vue 才会接管 `#app` 这个 div，把它内部所有内容按模板规则重新渲染。

一句话：**不 mount，页面就是死的；mount 了，才变成 Vue 的地盘。**

### 3.2 响应式：`data()` 为什么是函数

```js
data() {
  return { count: 0 }
}
```

Vue 会在创建应用时对 `return` 出来的对象做**响应式代理**——简单理解：给每个属性装上监听器，值一变就通知页面刷新。

为什么是「返回对象的**函数**」而不是直接给对象？因为组件可能被复用多次，如果共用一个对象，A 组件改数据会污染 B 组件。写成函数，每次调用都生成**全新的独立副本**。

```js
// ❌ 错：所有实例共享同一个对象
data: { count: 0 }

// ✅ 对：每次调用返回新对象
data() { return { count: 0 } }
```

### 3.3 事件：`@click` 是 `v-on:click` 的简写

```html
<button @click="add">点我 +1</button>
```

点击时 Vue 调用 `methods` 里的 `add`，`this.count++` 修改响应式数据 → Vue 检测到变化 → 页面上 <code v-pre>{{ count }}</code> 自动更新。

注意顺序：**你永远只改数据，不要去操作 DOM。** 想让页面变，就改数据，这是 Vue 与原生 JS 最大的思维差异。

---

## 4. 运行效果

| 操作 | 页面表现 |
| --- | --- |
| 首次打开 | 显示「Vue 字面量练习」和「计数：0」 |
| 每点一次按钮 | 「计数」数字 +1，无需刷新页面 |

## 5. 踩坑清单

### 坑 1：页面原样显示 <code v-pre>{{ count }}</code>

**现象**：浏览器里看到的不是数字，而是花括号原文。

**原因**：Vue 没挂上去。常见三种情况——

1. CDN 没加载成功（断网，或 CDN 被墙；换成 BootCDN / unpkg 再试）；
2. 容器 id 写错（`#app` 和 html 里的 id 不一致）；
3. `mount` 那行压根没执行。

**排查**：按 F12 打开控制台，看有没有 Vue 相关的红色报错。

### 坑 2：直接 `this.count = count + 1` 不生效，或改成箭头函数后 `this` 失效

`methods` 里**不要用箭头函数**写方法，箭头函数没有自己的 `this`，会拿不到 Vue 实例：

```js
methods: {
  add: () => { this.count++ }   // ❌ this 不是 Vue 实例
  add() { this.count++ }        // ✅ 普通函数写法
}
```

### 坑 3：双击 html 打开时 CDN 加载失败

CDN 依赖网络，有时候公司/校园网会拦截。稳妥做法是先把 `vue.global.js` 下载到本地，用 `<script src="./vue.global.js">` 引入——这一步也是理解「为什么后来要用构建工具」的最好引入。

---

## 6. 本节小结

- Vue 三步走：`createApp` 建实例 → `mount` 挂到 DOM → 改数据驱动渲染；
- `data` 必须是返回对象的函数，保证组件实例之间互不干扰；
- 想改页面就改数据，永远不要手动操作 DOM。

---

## 参考

- 上一节：[01 · 环境准备](./01-env-setup.md)
- 下一节：[03 · Vue 基础指令合集](./03-vue-directives.md)
- 对应代码：[01-vue-literal/index.html](https://github.com/beiiiii-111/frontend-learning/blob/main/01-vue-literal/index.html)
