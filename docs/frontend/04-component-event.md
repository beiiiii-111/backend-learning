# 04 · 组件拆分与事件 —— 待办清单 Todo

> - 难度：入门 → 进阶
> - 前置：[03 · Vue 基础指令合集](./03-vue-directives.md)
> - 预计时长：约 60 分钟
> - 对应代码：[vue-project1](https://github.com/beiiiii-111/frontend-learning/tree/main/vue-project1)

## 1. 本节导读

这一节把上一节的语法拼成一个**真正能用的小应用**，核心收获三件事：

1. 一个组件该怎么写成一个「能复用的小模块」；
2. 列表的**增、删、勾选**分别对应什么操作；
3. 那些看起来像装饰品的细节里，其实藏着 Vue 的核心原则——尤其是 **不要用 index 做 key**。

---

## 2. 组件一：报名开关 Toggle

先用一个最小组件热热身：一个按钮，在「报名 / 退订」之间来回切，并统计点击次数。

```vue
<script setup>
import { ref } from 'vue'

const isSignedUp = ref(false)
const count = ref(0)

const handleClick = () => {
  isSignedUp.value = !isSignedUp.value
  count.value++
}
</script>

<template>
  <button class="btn" @click="handleClick">
    {{ isSignedUp ? '退订' : '报名' }}
  </button>

  <p :class="isSignedUp ? 'status--done' : 'status--idle'">
    {{ isSignedUp ? '已报名' : '未报名' }}
  </p>

  <p>你已经点击了 {{ count }} 次</p>
</template>
```

**关键点：`v-if` 是不需要的。** 同一个位置上展示两种文案，用三元表达式渲染即可；`v-if` 应该用在「整块区域要不要存在」的场景。

另一个关键是 `:class` **对象/三元写法**，让样式跟着状态走：

```html
<p :class="isSignedUp ? 'status--done' : 'status--idle'">
```

---

## 3. 组件二：待办清单 Todo

### 3.1 数据与方法

```vue
<script setup>
import { ref } from 'vue'

const todos = ref([
  { id: 1, text: '整理活动报名名单', done: false },
  { id: 2, text: '联系场地负责人',   done: false },
  { id: 3, text: '准备审核说明材料', done: false },
])

const doneCount = () => todos.value.filter((item) => item.done === true).length

const newText = ref('')
let nextId = 4

function addTodo() {
  const text = newText.value.trim()          // 去空格
  if (text === '') return                    // 空内容直接拦掉
  todos.value.push({ id: nextId++, text: text, done: false })
  newText.value = ''                         // 清空输入框
}

function removeTodo(id) {
  todos.value = todos.value.filter((item) => item.id !== id)
}
</script>
```

三个对应 DOM 操作的老方法，在 Vue 里全部变成了「**改数组**」：

| 操作 | 原生 JS 思路 | Vue 思路 |
| --- | --- | --- |
| 添加 | `createElement` + `appendChild` | `todos.value.push({...})` |
| 删除 | `removeChild` | `todos.value = todos.value.filter(...)` |
| 勾选 | 手动改 class | `v-model="item.done"` 自动同步 |

**为什么删除要重新赋值而不是 `splice`？** 两种都能生效，但 `filter` 返回新数组更「纯」：不改动原数组，避免副作用，也更符合后续使用状态管理库（Pinia）的习惯。

### 3.2 模板

```vue
<template>
  <div class="todo-app">
    <h2>记事录</h2>

    <div class="add-row">
      <input v-model="newText" placeholder="输入待办事项，按回车添加"
             @keyup.enter="addTodo" />
      <button class="add" @click="addTodo">添加</button>
    </div>

    <ul>
      <li v-for="item in todos" :key="item.id" :class="{ done: item.done }">
        <input type="checkbox" v-model="item.done" />
        <span class="text">{{ item.text }}</span>
        <button class="del" @click="removeTodo(item.id)">删除</button>
      </li>
    </ul>

    <p class="summary">共 {{ todos.length }} 项 · 已完成 {{ doneCount() }} 项</p>
  </div>
</template>
```

三个值得抄走的写法：

1. **`@keyup.enter`**：按键修饰符，回车即触发添加，不用让用户去点按钮；
2. **`:class="{ done: item.done }"`**：对象语法，键是 class 名、值是布尔值，为真才生效；
3. **`v-model` 绑到对象属性**：`v-model="item.done"` 可以直接把复选框状态和数组里某一项的字段绑在一起。

### 3.3 完成态样式

```css
/* 完成态：加删除线并变灰 */
.done .text {
  text-decoration: line-through;
  color: #94a3b8;
}
```

一行 CSS 就完成了「勾选 → 加删除线」，全程没有任何 JS 操作 DOM 的痕迹。

---

## 4. 运行效果

| 操作 | 现象 |
| --- | --- |
| 输入文字回车 / 点添加 | 列表新增一条，`共 N 项` 数字 +1，输入框自动清空 |
| 勾选复选框 | 该条文字变灰加删除线，`已完成` 计数实时变化 |
| 点删除 | 该项从列表消失，计数同步减少 |
| 输入空格后回车 | 什么都不发生（被 `trim()` 拦截） |

---

## 5. 踩坑清单

### 坑 1：`:key` 用了数组下标（最容易埋的雷）

```html
<li v-for="(item, index) in todos" :key="index">   <!-- ❌ -->
<li v-for="item in todos" :key="item.id">          <!-- ✅ -->
```

**为什么危险**：删除第 2 条后，原来的第 3 条下标从 2 变成 1，Vue 会误以为「只是第 2 条的内容变了」，于是保留它的 DOM 状态。表现就是——**勾选项错位、输入框内容跑到别的行**。

所以这里专门维护了 `nextId` 自增变量，保证每条 id 永远唯一：

```js
let nextId = 4
todos.value.push({ id: nextId++, text, done: false })
```

> 注意 `nextId` 用的是普通 `let` 而不是 `ref`——它是**参与渲染的临时计数器**，不需要响应式，也不需要 `export`。

### 坑 2：`ref` 数组直接用 `splice` 后页面没更新

在 Vue 3 里 `ref` 包装的数组支持响应式增删，但如果你用解构或中间变量绕一圈，可能丢掉代理。**稳妥写法**是先 `const arr = todos.value` 再操作，或直接整体重新赋值。

### 坑 3：输入框不清空

`newText.value = ''` 放在 `push` **之后**；如果误写成 `const text = newText.value.trim()` 之前就清空，`trim` 拿到的会是空串。

### 坑 4：`<style>` 忘写 `scoped` 导致样式串味

这个项目里多个组件都用了 `.btn`、`.text` 这类通用类名，不加 `scoped` 会互相覆盖。加上 `<style scoped>` 后，Vue 会自动给类名加哈希后缀，天然隔离。

### 坑 5：`v-for` 和 `v-if` 写在同一层

同一元素上同时写 `v-if` 和 `v-for` 会报警告，且优先级容易踩坑。要过滤列表请用 `computed`：

```js
const undone = computed(() => todos.value.filter(t => !t.done))
```

---

## 6. 本节小结

- 增删改查的本质是操作数据数组，不是操作 DOM；
- `:class` 对象语法让「样式跟着状态走」变成一行代码；
- `:key` 必须唯一且稳定——这是列表类需求的第一道防线。

---

## 参考

- 上一节：[03 · Vue 基础指令合集](./03-vue-directives.md)
- 下一节：[05 · props 与动态样式](./05-props-style.md)
- 对应代码：[src/Todo.vue](https://github.com/beiiiii-111/frontend-learning/blob/main/vue-project1/src/Todo.vue) · [src/Toggle.vue](https://github.com/beiiiii-111/frontend-learning/blob/main/vue-project1/src/Toggle.vue)
