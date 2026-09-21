# 06 · 插槽与主题切换 —— 个人名片卡片

> - 难度：进阶
> - 前置：[05 · 计算属性与动态样式](./05-props-style.md)
> - 预计时长：约 50 分钟
> - 对应代码：[vue-project4](https://github.com/beiiiii-111/frontend-learning/tree/main/vue-project4)

## 1. 本节导读

这是目前做得最完整的一个组件，亮点有三个：

1. **`<slot>` 插槽**：让卡片外壳与内容解耦，同一张卡可以塞进任何东西；
2. **CSS 变量 + `:deep()`**：把「主题色」抽成一个变量，一处修改全组件联动；
3. **`color-mix()`**：一个品牌色自动派生出浅色背景与描边，不用手写三套颜色。

---

## 2. 插槽：外壳归我，内容归你

```vue
<template>
  <div class="card">
    <img :src="user.avatar" :alt="user.name" class="avatar" />
    <slot />
  </div>
</template>
```

`<slot />` 就像卡片里预留的一个**空洞**：谁用这个组件，就把写在使用标签中间的内容自动填进来。

```html
<!-- 使用方：标签中间的内容会替换掉 <slot /> -->
<ProfileCard>
  <h3>张心怡</h3>
  <p>前端学习小组 · 郑州</p>
</ProfileCard>
```

**为什么要有插槽？** 因为卡片作为外壳（圆角、阴影、头像、hover 效果）是稳定的，但里面放什么文字，每个页面都不一样。把「变的东西」交给使用方，这就是组件设计里最朴素的**开闭原则**。

### 2.1 给插槽内容兜底排版

插槽内容是外部传进来的，**通常没写 class**，所以需要用 `:deep()` 穿透 scoped 去统一它们的样式：

```css
/* 插槽内容通常没写 class，用 :deep 兜底排版 */
.card :deep(p)  { margin: 0; font-size: 14px; color: #6b7280; }
.card :deep(h3) { margin: 0 0 4px; font-size: 16px; font-weight: 600; }
```

> `:deep()` 的意思是「这条样式要作用到子组件/插槽内部」。因为 `scoped` 的属性选择器默认只打到本组件的元素上，不用 `:deep()` 的话，样式对插槽内容完全无效。

---

## 3. 主题色：一个变量驱动整张卡片

### 3.1 数据 + 内联绑定

```js
const themeColor = ref('#2f6fed')
```

```html
<section class="profile-card" :style="{ '--theme-color': themeColor }">
```

注意这里内联的是一个 **CSS 自定义属性**（名字带 `--`），这正是 Vue 3 官方推荐的动态主题方案：JS 只负责改一个值，具体这个颜色被多少地方引用，交给 CSS 决定。

```css
.profile-card {
  --theme-color: #2f6fed;   /* 默认值，会被 :style 内联覆盖 */
}
```

**为什么不用 JS 逐个改 class？** 因为 `--theme-color` 一旦变化，下面这些地方会**同时更新**，一行 JS 都不用写：

```css
/* 左侧竖条 */
.profile-card::before { background: var(--theme-color); }

/* 顶部渐变条（用 color-mix 自动调浅） */
.profile-card::after {
  background: linear-gradient(90deg,
    var(--theme-color),
    color-mix(in srgb, var(--theme-color) 35%, #ffffff));
}

/* 头像外圈 */
.avatar { box-shadow: 0 0 0 2px var(--theme-color); }
```

切换主题时加个过渡，视觉上就非常顺滑：

```css
.profile-card::before { transition: background .3s ease; }
```

### 3.2 按钮怎么切

```html
<button @click="themeColor = '#2f6fed'">蓝色主题</button>
<button @click="themeColor = '#0f9d58'">绿色主题</button>
<button @click="themeColor = '#e8710a'">橙色主题</button>
```

直接改 `ref` 的值即可，不需要 `querySelector`、不需要操作 classList。

---

## 4. `color-mix()`：一个品牌色派生一整套

这是现代 CSS 里很值得掌握的一个函数——把颜色按比例混：

```css
.role-tag {
  --c: #2563eb;                                  /* 只定义一个色相 */
  color: var(--c);                               /* 文字用原色 */
  background: color-mix(in srgb, var(--c) 10%, #fff);   /* 10% + 白 = 浅底 */
  border: 1px solid color-mix(in srgb, var(--c) 22%, #fff); /* 22% + 白 = 淡描边 */
}
.role-tag--organizer { --c: #7c3aed; }   /* 紫 */
.role-tag--auditor   { --c: #0891b2; }   /* 青 */
.role-tag--student   { --c: #2563eb; }   /* 蓝 */
```

三个角色标签，**每个只改一行 `--c`**，浅色背景和描边自动算出来。以前要手写 9 个颜色值，现在是 3 个。

同理用在特色标签上：

```css
.duty-tag {
  --c: #0f9d58;
  background: var(--c);
  box-shadow: 0 3px 10px -3px var(--c);   /* 阴影也用同一个色相 */
}
.duty-tag--off { --c: #94a3b8; }          /* 请假态：变灰 */
```

---

## 5. 其它值得抄走的细节

### 5.1 字典表代替 if-else

```js
const ROLE_TEXT = {
  organizer: '活动组织者',
  auditor: '审核员',
  student: '学生'
}
```

```html
{{ ROLE_TEXT[user.role] ?? '未知' }}
```

后端返回的是英文 key，展示要中文——**用一张映射表，而不是一串 `if / else`**。新增角色只改对象，不动模板。`??` 是空值合并运算符，取不到时给个兜底显示。

### 5.2 hover 微交互

```css
.profile-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 2px 4px rgba(16, 24, 40, .06),
              0 20px 40px -14px rgba(16, 24, 40, .24);
}
.card:hover .avatar {
  transform: scale(1.05);
  box-shadow: 0 0 0 3px var(--theme-color);
}
```

卡片微微上浮 + 头像放大 1.05 倍，成本两行 CSS，**观感提升最明显**。记得加 `transition` 才有过渡，否则会「跳」。

### 5.3 技能标签列表

```html
<ul class="skill-list">
  <li v-for="skill in skills" :key="skill">{{ skill }}</li>
</ul>
```

字符串数组作为 key 是完全合法的——只要它在这个列表里唯一。

---

## 6. 运行效果

| 操作 | 现象 |
| --- | --- |
| 点「蓝色/绿色/橙色主题」 | 左侧竖条、顶部渐变、头像外圈、按钮描边同时换色 |
| 鼠标移到昵称卡片上 | 卡片上浮 3px、阴影加深；头像放大一圈并加粗主题色描边 |
| 点「切换在岗状态」 | 标签在绿色「在岗」↔ 灰色「请假」之间切换 |

---

## 7. 踩坑清单

### 坑 1：`:style` 里写 CSS 变量名忘加引号

```html
<!-- ❌ 会被当成变量名，报错或静默失效 -->
:style="{ --theme-color: themeColor }"
<!-- ✅ key 必须加引号 -->
:style="{ '--theme-color': themeColor }"
```

### 坑 2：插槽内容样式不生效

忘了 `:deep()`。scoped 样式默认打不到插槽（子组件）内部的元素，必须写 `.card :deep(p)`。

### 坑 3：`.card` 里写了默认 `--theme-color`，但没写在被 `:style` 覆盖的元素上

内联绑定的 `--theme-color` 只作用在 `.profile-card` 及其子元素上。如果 `.card` 是兄弟节点，它拿不到这个值，头像 box-shadow 会变透明。**两个元素都要声明默认值**：

```css
.card,
.profile-card { --theme-color: #2f6fed; }
```

### 坑 4：`transform` 没写 `transition`

hover 效果会突兀地跳一下。给需要动画的属性单独加 `transition`（不要用 `all`，性能差且容易误伤）。

### 坑 5：`color-mix` 兼容性

这是较新的 CSS 特性（Chrome 111+ / Safari 16.2+）。要兼容老浏览器，就得额外手写一份浅色值作为兜底：

```css
background: #eff6ff;                                     /* fallback */
background: color-mix(in srgb, var(--c) 10%, #fff);      /* 支持时用这个 */
```

### 坑 6：多根节点组件接收 class 时报警告

这个文件的 `<template>` 下有两个根元素（`.card` 和 `.profile-card`），这时父组件给 `<ProfileCard class="xxx">` 传 class 会找不到唯一入口，Vue 会提示「Extraneous non-props attributes」。规范做法是在外面套一层唯一的根元素。

---

## 8. 本节小结

- `<slot />` 让外壳和内容解耦，父组件传进来的内容要用 `:deep()` 才能被 scoped 样式命中；
- 主题色用 CSS 变量 + 内联绑定，改一处动全身；
- `color-mix()` 能把一个品牌色派生出背景与描边，大幅减少手写色值。

---

## 参考

- 上一节：[05 · 计算属性与动态样式](./05-props-style.md)
- 下一节：[07 · 用 VitePress 搭建本笔记站](./07-vitepress-site.md)
- 对应代码：[src/ProfileCard.vue](https://github.com/beiiiii-111/frontend-learning/blob/main/vue-project4/src/ProfileCard.vue)
