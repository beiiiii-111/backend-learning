# 03 · Vue 基础指令合集 —— 八个语法点一次搞懂

> - 难度：入门
> - 前置：[02 · Vue 字面量入门](./02-vue-literal.md)
> - 预计时长：约 60 分钟
> - 对应代码：[02-vite-vue-demo](https://github.com/beiiiii-111/frontend-learning/tree/main/02-vite-vue-demo)

## 1. 本节导读

Vue 的页面功能基本就靠这几个语法撑起来，这一节把它们集中在一个组件里挨个练：

| 序号 | 语法 | 解决什么问题 |
| --- | --- | --- |
| 1 | <code v-pre>{{ }}</code> 插值 | 把数据显示到页面上 |
| 2 | `v-bind`（`:`） | 让 HTML **属性**的值跟着数据变 |
| 3 | `v-on`（`@`） | 绑定事件 |
| 4 | `v-model` | 表单输入和数据双向同步 |
| 5 | `v-if / v-else` | 条件渲染 |
| 6 | `v-for` | 列表循环渲染 |
| 7 | `computed` | 依赖数据算出来的值，带缓存 |
| 8 | `watch` | 监听数据变化做副作用 |

核心心法仍然是上一节那句：**你只负责改数据，页面交给 Vue。**

---

## 2. 数据准备

```vue
<script setup>
import { ref, computed, watch } from 'vue'

const name = ref('张三')
const imgUrl = ref('https://picsum.photos/100')
const isShow = ref(true)
const count = ref(0)
const list = ref([
  { id: 1, title: 'Vue 学习 1' },
  { id: 2, title: 'Vue 学习 2' },
  { id: 3, title: 'Vue 学习 3' },
])

const doubled = computed(() => count.value * 2)

watch(count, (newVal, oldVal) => {
  console.log(`count 从 ${oldVal} 变成了 ${newVal}`)
})

const changeName = () => { name.value = '李四' }
const add = () => { count.value++ }
</script>
```

> **`<script setup>` 是什么**：Vue 3 的写法糖，写在里面的变量模板可以直接用，不用再写 `return`。加上 `setup` 后不需要「导出一个对象」这层壳。

> **`ref` 的一个铁律**：`<script setup>` 里读写响应式数据必须 `.value`（如 `count.value++`），但在模板里**不需要**加 `.value`（写成 <code v-pre>{{ count }}</code> 即可）。这是新手最容易忘的地方。

---

## 3. 八个语法点逐个拆解

### 3.1 插值 <code v-pre>{{ }}</code>

```html
<p>姓名：{{ name }}</p>
```

双大括号里可以直接写 JS 表达式，比如计数加一、字符串反转，都能正常求值；但不能在里面写语句（`if (x) ...` 这种不行，要把逻辑搬到 `computed` 里）。

### 3.2 属性绑定 `v-bind`（简写 `:`）

```html
<img :src="imgUrl" alt="随机图" class="pic" />
```

插值语法只能写在标签**内容**里，写不进属性。想让属性值跟随数据，必须用 `v-bind`。改 `imgUrl.value = 'xxx.jpg'` 图片就会自动换。

### 3.3 事件绑定 `v-on`（简写 `@`）

```html
<button @click="changeName">点我改名字</button>
```

也可以直接内联写表达式：`@click="isShow = !isShow"`。

### 3.4 双向绑定 `v-model`

```html
<input v-model="name" placeholder="输入试试" />
```

**这是表单神器**。它等价于下面两件事的组合：

```html
<input :value="name" @input="name = $event.target.value" />
```

也就是说：`v-model` = 属性绑定 + 输入事件监听。它不只用于 input，也适用于 checkbox、radio、select。

### 3.5 条件渲染 `v-if / v-else`

```html
<span v-if="isShow">显示内容</span>
<span v-else>隐藏</span>
<button @click="isShow = !isShow">切换</button>
```

`v-if` 是真的**销毁/重建** DOM；如果只想隐藏（保留 DOM），用 `v-show`。频繁切换用 `v-show` 性能更好。

### 3.6 列表渲染 `v-for`

```html
<ul>
  <li v-for="item in list" :key="item.id">{{ item.title }}</li>
</ul>
```

**`:key` 一定要加**，而且要写唯一值（id），**不要用数组下标 index**。key 是 Vue 判断「这一项是不是变了」的身份证，用 index 会导致增删中间项时渲染错乱。

### 3.7 计算属性 `computed`

```js
const doubled = computed(() => count.value * 2)
```

模板里直接写变量名（<code v-pre>{{ doubled }}</code>），不用加括号调用。

**computed 与方法（methods）的区别**：computed 有缓存，依赖项不变时不重复计算；写在方法里则每次渲染都重算。所以「能用 computed 写的，别写成方法」。

### 3.8 侦听器 `watch`

```js
watch(count, (newVal, oldVal) => {
  console.log(`count 从 ${oldVal} 变成了 ${newVal}`)
})
```

`watch` 适合做 **副作用**：发请求、写日志、存本地缓存这类「数据变了要干点别的」的场景。

**怎么选？**

- 想得到一个派生值 → `computed`
- 数据变了要执行动作（请求 / 打印 / 存盘）→ `watch`

---

## 4. 完整模板

```vue
<template>
  <div class="wrap">
    <p>1. 插值 —— 姓名：{{ name }}</p>

    <p>2. v-bind —— 图片：</p>
    <img :src="imgUrl" alt="随机图" class="pic" />

    <p>3. v-on —— <button @click="changeName">点我改名字</button></p>

    <p>4. v-model —— <input v-model="name" placeholder="输入试试" /></p>

    <p>5. v-if —— <span v-if="isShow">显示内容</span><span v-else>隐藏</span>
      <button @click="isShow = !isShow">切换</button></p>

    <p>6. v-for 列表：</p>
    <ul><li v-for="item in list" :key="item.id">{{ item.title }}</li></ul>

    <p>7. computed —— 计数：{{ count }}，翻倍：{{ doubled }}</p>

    <p>8. watch —— <button @click="add">count +1</button>（按 F12 看 Console）</p>
  </div>
</template>
```

## 5. 运行效果对照

| 操作 | 现象 |
| --- | --- |
| 点「点我改名字」 | 页面上「张三」变「李四」，输入框内容同步变 |
| 在输入框打字 | 上方文字实时跟着变（v-model 双向） |
| 点「切换」 | 「显示内容」↔「隐藏」切换 |
| 点「count +1」 | 计数加 1、翻倍值同步变化，F12 控制台打印一行日志 |

---

## 6. 踩坑清单

### 坑 1：忘写 `.value`

模板正常，但事件点了没反应，控制台也没报错——大概率是 `<script setup>` 里写成了 `count++` 而不是 `count.value++`。

### 坑 2：`v-for` 忘了 `:key`，或用了 index

控制台会警告；用 index 时，删除列表中间一项，会发现后面的复选框/输入框错乱到别的行上。

### 坑 3：computed 当成方法调用

模板里把它当方法调用、写成带括号的形式会报错；computed 是**属性**，直接写名字即可。反过来，`foo()` 这种真正的方法不缓存，每次渲染都执行。

### 坑 4：`<style>` 忘了加 `scoped`

`<style>` 标签不加 `scoped` 会全局生效，容易污染别的组件。加上 `scoped` 后样式只作用于本组件。

### 坑 5：组件根节点要求

Vue 3 支持**多个根节点**（Fragment），所以 `<template>` 里可以继续一边一个 `<section>`；但如果你要接 `:class`、`:style` 之类的透传，就要注意合并后的根节点行为。

---

## 7. 本节小结

- <code v-pre>{{ }}</code> 管文本，`:` 管属性，`@` 管事件，`v-model` 管表单；
- `v-if` 销毁 / `v-show` 隐藏，`v-for` 必须配唯一的 `:key`；
- 派生值用 `computed`，副作用用 `watch`。

---

## 参考

- 上一节：[02 · Vue 字面量入门](./02-vue-literal.md)
- 下一节：[04 · 组件拆分与事件](./04-component-event.md)
- 对应代码：[src/App.vue](https://github.com/beiiiii-111/frontend-learning/blob/main/02-vite-vue-demo/src/App.vue)
