# 05 · 计算属性与动态样式 —— 活动卡片轮播

> - 难度：进阶
> - 前置：[04 · 组件拆分与事件](./04-component-event.md)
> - 预计时长：约 45 分钟
> - 对应代码：[vue-project2](https://github.com/beiiiii-111/frontend-learning/tree/main/vue-project2)

## 1. 本节导读

这一节做一个「活动海报卡片」，练的是三件更贴近实战的事：

1. **用下标 + computed 表达「当前是第几个」**，而不是复制多份数据；
2. **动态拼接 class 名**，让状态标签自动换色；
3. **动态绑定 style**，把背景图这种「写死的数据」交给 JS。

---

## 2. 核心思路：用下标驱动，而不是复制数据

需求是「两张海报轮播」。新手常见做法是写两套 DOM、用 `v-if` 切换谁显示；这个项目的做法是——**维护一个下标 `index`，用 computed 算出当前该显示的那一条**。

```js
const list = ref([
  {
    title: '2026 春季校园歌手大赛',
    status: 'signing',
    cover: 'https://.../singer.jpg',
    offline: false
  },
  {
    title: '秋日校园辩论赛',
    status: 'closed',
    cover: 'https://.../debate.jpg',
    offline: true
  }
])

// 当前显示第几个
const index = ref(0)

// 由下标计算出的当前活动
const activity = computed(() => list.value[index.value])
```

**好处**：模板里永远只写 `activity.xxx`，不需要关心它在数组第几个；以后加到 10 张海报，这段逻辑一行都不用改。

### 2.1 循环切换的取模写法

```js
function prev() {
  index.value = (index.value - 1 + list.value.length) % list.value.length
}
function next() {
  index.value = (index.value + 1) % list.value.length
}
```

- `next`：`+1` 后取模，到最后一个自然回到 0；
- `prev`：**先加上数组长度再减 1**，是为了避免 `-1 % 2 = -1` 得到负数下标。

这是 JS 里写循环数组的通用套路，可以记下来。

---

## 3. 动态 class：状态自动换色

```html
<span class="tag" :class="'tag--' + activity.status">
  {{ statusText[activity.status] }}
</span>
```

配套数据 + CSS：

```js
const statusText = {
  draft: '草稿',
  signing: '报名中',
  closed: '报名截止',
  finished: '已结束'
}
```

```css
.tag--draft    { background: #909399; }  /* 灰 */
.tag--signing  { background: #10b981; }  /* 绿 */
.tag--closed   { background: #f59e0b; }  /* 橙 */
```

`:class` 支持字符串、对象、数组三种写法，这里用的是**字符串拼接**——把数据里的 `status` 直接变成 CSS 类名。好处是新增一个状态只要加一行 CSS，JS 完全不用动。

### 3.1 另一种写法：对象语法

同一个需求也可以写对象形式，语义更清楚：

```html
<span :class="{ 'tag--signing': activity.status === 'signing' }">
```

**怎么选**：选项是「互斥的几个值」用拼接；选项是「多个独立的布尔开关」用对象。

---

## 4. 动态 style：背景图来自数据

```html
<div class="poster" :style="{ backgroundImage: 'url(' + activity.cover + ')' }">
```

**为什么不用 `<img>`？** 因为背景图要做「铺满裁切」，而这要靠 CSS 的 `background-size: cover`：

```css
.poster {
  position: relative;
  height: 320px;
  background-size: cover;      /* 等比放大覆盖容器，多余部分裁掉 */
  background-position: center; /* 居中显示 */
}
```

两个细节：

- `:style` 的值是一个 **JS 对象**，CSS 属性要写成**驼峰**（`backgroundImage`，不是 `background-image`）；
- 千万别用 `<img>` + `object-fit` 去硬凑，用背景图 + cover 是海报类需求的标准答案。

### 4.1 按钮禁用

```html
<button class="off" :disabled="activity.status !== 'draft'">下架</button>
```

只有「草稿」状态才能下架，其余状态按钮自动置灰不可点。这类**权限/可用性判断直接写在模板里**很常见，不用专门写方法。

---

## 5. 运行效果

| 操作 | 现象 |
| --- | --- |
| 点「下一张」 | 海报、标题、状态标签整体切换；角标从 `1 / 2` 变 `2 / 2` |
| 点「上一张」 | 反向切换；在第一个时再点会绕回最后一个 |
| 状态为 `signing` | 左上角标签绿色底「报名中」 |
| 状态为 `closed` | 标签橙色「报名截止」，且「下架」按钮置灰不可点 |

---

## 6. 踩坑清单

### 坑 1：`prev` 算出负数下标

写成 `(index.value - 1) % list.value.length` 时，index 为 0 会得到 `-1`，`list[-1]` 是 `undefined`，模板立刻报 `Cannot read properties of undefined`。**记得先加长度再取模。**

### 坑 2：`:style` 里 key 写成了短横线

```html
<!-- ❌ -->
:style="{ 'background-image': 'url(...)' }"
<!-- ✅ 推荐驼峰 -->
:style="{ backgroundImage: 'url(...)' }"
```

字符串 key 也能跑，但项目里混用容易出错，统一用驼峰。

### 坑 3：图片 URL 里有中文或空格

拼接 `url(...)` 时如果路径带空格会被截断，稳妥做法是给 URL 加引号：

```js
:style="{ backgroundImage: `url('${activity.cover}')` }"
```

### 坑 4：`computed` 返回的是对象，模板里要多写一层

用了 `computed(() => list.value[index.value])` 后，模板必须写 `activity.title`，而不是直接 `title`。控制台报 `undefined` 时先检查是不是漏写了这层。

### 坑 5：忘了 `.activity-card { overflow: hidden }`

海报是圆角卡片里的第一块内容，父容器不加 `overflow: hidden`，海报会把圆角「顶出去」，视觉上四个角是方的。

---

## 7. 本节小结

- 「当前选中第几个」应该建模成**下标 + computed**，而不是复制多份 DOM；
- `:class` 拼接适合互斥状态，`:class` 对象适合布尔开关；
- `:style` 用驼峰 key，背景图铺满用 `background-size: cover`。

---

## 参考

- 上一节：[04 · 组件拆分与事件](./04-component-event.md)
- 下一节：[06 · 插槽与主题切换](./06-slot-theme.md)
- 对应代码：[src/ActivityCard.vue](https://github.com/beiiiii-111/frontend-learning/blob/main/vue-project2/src/ActivityCard.vue)
