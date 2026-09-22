# 08 · 条件渲染综合练习：学生名单多视图

> - 难度：入门
> - 前置：[03 · Vue 基础指令合集](./03-vue-directives.md)
> - 预计时长：约 45 分钟
> - 对应代码：[Student.vue](https://github.com/beiiiii-111/frontend-learning/blob/main/03-vue-basic/vue-basic-demo/src/components/Student.vue)

## 1. 本节导读

Vue 基础 · 条件渲染综合练习。**一份数据，多种显示方式**，用 `v-if / v-else-if / v-else` 决定当前显示哪一种。

| 项目 | 说明 |
| --- | --- |
| 练习文件 | `src/components/Student.vue` |
| 技术栈 | Vue 3 `<script setup>` + Element Plus |
| 核心知识点 | `v-if / v-else-if / v-else`、`v-for`、`ref`、事件绑定 |

### 练习要求

页面顶部放一组按钮，在**表格、卡片、专业分组、名单**四种显示方式之间切换，显示的是同一批学生数据：

- 表格模式：用 `el-table` 逐行显示学号、姓名、班级、方向、成绩、状态，**成绩可排序**
- 卡片模式：用 `el-card` 块状显示，每张卡片一个人，**窄屏自动换列**
- 专业分组模式：按 `direction` 把同一方向的人归到一起（延伸练习）
- 名单模式：一行一人，紧凑排列，人多了也能快速扫读
- **切换视图不影响数据本身，只改变显示方式**

---

## 2. 前置知识与边界

| 已经学过（本练习会用到） | 本练习不用（还没学到） |
| --- | --- |
| `ref` 定义响应式数据 | `computed` 计算属性 |
| `v-for` 遍历数组、`:key` | `watch` 侦听器 |
| `v-if` / `v-else-if` / `v-else` | 组件拆分与 `props` / `emit` |
| `v-model` 双向绑定 | 路由与状态管理 |
| 事件绑定 `@click` | 数组高阶方法 `filter` / `reduce` |
| 动态绑定 `:style`、`:class` | |
| Element Plus 组件标签用法 | |

这一版只做视图切换，不做搜索和数据筛选。等学完 `computed` 之后再回来加搜索，能把「输入框内容一变，列表就跟着变」写得省事得多，也更容易看出计算属性和普通函数的区别。

---

## 3. 数据设计

数组里每个对象是一条学生记录。字段设计的原则是：**字段本身存「事实」，显示成什么样交给模板决定**。

| 字段 | 类型 | 说明 | 出现在 |
| --- | --- | --- | --- |
| `id` | string | 学号，唯一，同时用作 `:key` | 所有视图 |
| `name` | string | 姓名，卡片和名单里取首字做头像 | 所有视图 |
| `className` | string | 班级 | 所有视图 |
| `direction` | string | 专业方向 | 所有视图 |
| `score` | number | 成绩，决定颜色和进度条长度 | 所有视图 |
| `status` | string | 状态字典的键，不是给用户看的文案 | 所有视图 |

数据里只存 `active`、`intern`、`leave` 这样的短标识，文案和颜色在字典里集中维护。**以后要改叫法，只改字典一处**，不用去模板里一个个翻。

| 键 | 显示文案 | 标签类型 | 效果 |
| --- | --- | --- | --- |
| `active` | 在读 | `success` | 绿色标签 |
| `intern` | 实习中 | `warning` | 橙色标签 |
| `leave` | 休学 | `info` | 灰色标签 |

---

## 4. 实现步骤

下面按实际动手顺序展开。每一步都先想清楚「数据怎么放」，再写模板。

### 4.1 准备数据数组

数据从头到尾不会被修改，所以**用普通数组就够了，不需要 `ref`**。需要响应式的，是「当前看哪个视图」这类会变的东西。

```js
// 这份数据不会被修改，普通数组即可
const students = [
  {
    id: "20230101",
    name: "林一鸣",
    className: "软件 2301",
    direction: "前端开发",
    score: 92,
    status: "active",
  },
  // ... 其余 7 条
]
```

### 4.2 定义一个会变的状态

整个页面只有一处会变：当前看哪个视图。它用一个字符串记录，所有视图都从它派生。

```js
const viewMode = ref("table")   // 当前视图：table / card / group / list
```

思路是：**数据是死的，视图是活的**。把「活」的部分放到一个 `ref` 里，切换视图就只是改一个字符串，和数据处理完全分开，后面要加功能也不会互相牵扯。

### 4.3 先搭条件渲染骨架，再往里填内容

先把几种情况按顺序排成一列，用最少的标签占位，确认切换逻辑对了，再逐个把具体组件填进去：

```html
<el-table v-if="viewMode === 'table'" :data="students">
  <!-- 表格列 -->
</el-table>

<el-row v-else-if="viewMode === 'card'">
  <!-- 卡片 -->
</el-row>

<div v-else-if="viewMode === 'group'">
  <!-- 专业分组 -->
</div>

<ul v-else class="name-list">
  <!-- 名单 -->
</ul>
```

三个决定顺序的规则：

- 互斥的分支用 `v-else-if` 串成一条链，**不要写成多个独立的 `v-if`**。写成独立 `v-if` 时，若两个条件同时成立，两块内容会一起显示；
- 链尾用 `v-else` 兜底，不再写条件，含义是「前面都不成立时轮到我」；
- 链上的元素必须**紧挨着写**，中间只能放注释。

### 4.4 填表格视图

能用 `prop` 直接显示的列就写 `prop`；需要加工的列（成绩带颜色、状态变标签、加操作按钮）用默认插槽，通过 `scope.row` 拿到当前行的数据。

```html
<el-table v-if="viewMode === 'table'" :data="students" stripe border>
  <el-table-column prop="id" label="学号" width="120" />
  <el-table-column prop="name" label="姓名" width="110" />
  <el-table-column prop="className" label="班级" width="120" />
  <el-table-column prop="direction" label="方向" />

  <!-- 成绩：按分数上色，可点击表头排序 -->
  <el-table-column prop="score" label="成绩" width="100" sortable>
    <template #default="scope">
      <span :style="{ color: scoreColor(scope.row.score) }">{{ scope.row.score }}</span>
    </template>
  </el-table-column>

  <!-- 状态：键转文案 + 转颜色 -->
  <el-table-column label="状态" width="110">
    <template #default="scope">
      <el-tag :type="STATUS_MAP[scope.row.status].type" effect="light">
        {{ STATUS_MAP[scope.row.status].text }}
      </el-tag>
    </template>
  </el-table-column>
</el-table>
```

注意 `:data="students"`：**表格自己会遍历数组，不需要再写 `v-for`**。这是组件和原生标签的一个区别。

### 4.5 填卡片视图

卡片由两层组成：外层 `el-row` 排布多列，内层 `el-col` 控制每个卡片占几列。这里才需要写 `v-for`，因为卡片是我们自己排的。

```html
<el-row v-else-if="viewMode === 'card'" :gutter="16">
  <el-col v-for="item in students" :key="item.id" :xs="24" :sm="12" :md="8">
    <el-card shadow="hover">
      <div class="stu-card__top">
        <el-avatar :size="48">{{ item.name.charAt(0) }}</el-avatar>
        <div>
          <p>{{ item.name }}</p>
          <p>{{ item.id }} · {{ item.className }}</p>
        </div>
      </div>

      <el-progress :percentage="item.score" :color="scoreColor(item.score)" />
    </el-card>
  </el-col>
</el-row>
```

`:xs` / `:sm` / `:md` 是三种屏幕宽度下的占比，总共 24 格：窄屏 24（一行一个）、中屏 12（一行两个）、宽屏 8（一行三个），**窄屏自动换列不用自己写媒体查询**。

### 4.6 填专业分组视图（在判断链上加一个 `v-else-if`）

分组这件事本身不需要 `computed`，因为分组结果也是死的。用最普通的 `for` 循环先把人归好堆，模板里两层 `v-for` 直接渲染：

```js
// 分组结果同样不会变，还是普通数组；只用 for 循环，不用 filter / reduce
const DIRECTIONS = ["前端开发", "后端开发", "数据开发", "测试开发"];

const groupedStudents = [];
for (const direction of DIRECTIONS) {
  const members = [];
  for (const item of students) {
    if (item.direction === direction) members.push(item);
  }
  groupedStudents.push({ direction, members });
}
```

```html
<div v-else-if="viewMode === 'group'">
  <section v-for="group in groupedStudents" :key="group.direction">
    <h3>{{ group.direction }} <span>{{ group.members.length }} 人</span></h3>

    <ul>
      <li v-for="item in group.members" :key="item.id">
        <el-avatar :size="32">{{ item.name.charAt(0) }}</el-avatar>
        <span>{{ item.name }}</span>
        <span>{{ item.id }} · {{ item.className }}</span>
        <span :style="{ color: scoreColor(item.score) }">{{ item.score }}</span>
        <el-tag size="small" :type="STATUS_MAP[item.status].type" effect="light">
          {{ STATUS_MAP[item.status].text }}
        </el-tag>
      </li>
    </ul>
  </section>
</div>
```

外层 `v-for` 遍历「分组」，内层 `v-for` 遍历「组内成员」，两层都要给 `:key`：外层用方向名（唯一），内层用学号。

### 4.7 填名单视图（`v-else` 兜底分支）

这一块不需要组件，用普通 `ul` + `li` 加 flex 就够了。**视图容器不必是组件，用最简单的标签反而更好控制**。

```html
<ul v-else class="name-list">
  <li v-for="item in students" :key="item.id" class="name-list__item">
    <el-avatar :size="32">{{ item.name.charAt(0) }}</el-avatar>
    <span class="name-list__name">{{ item.name }}</span>
    <span class="name-list__meta">{{ item.id }} · {{ item.className }} · {{ item.direction }}</span>
    <span :style="{ color: scoreColor(item.score) }">{{ item.score }}</span>
    <el-tag size="small" :type="STATUS_MAP[item.status].type" effect="light">
      {{ STATUS_MAP[item.status].text }}
    </el-tag>
  </li>
</ul>
```

### 4.8 收尾：抽公共函数、显示人数

根据成绩「上色」这件事，在几个视图里都要用，所以抽成函数放在 `script` 里共用，模板只管调用：

```js
function scoreColor(score) {
  if (score >= 85) return "#0f9d58"
  if (score >= 70) return "#e6a23c"
  return "#f56c6c"
}
```

人数用数组自带的 `length` 属性，不需要额外定义变量：

```html
<p class="page__desc">
  共 {{ students.length }} 人 · 切换视图用的是 v-if / v-else-if / v-else
</p>
```

---

## 5. 条件渲染是怎么判断的

几块内容写在同一个位置，**从上往下依次判断，谁的条件成立就渲染谁，后面的分支直接跳过**：

| 顺序 | 指令 | 条件 | 渲染结果 |
| --- | --- | --- | --- |
| 1 | `v-if` | `viewMode === 'table'` | `el-table` 表格，逐行看字段、比成绩 |
| 2 | `v-else-if` | `viewMode === 'card'` | `el-card` 栅格卡片，看单个学生的全貌 |
| 3 | `v-else-if` | `viewMode === 'group'` | 按专业方向分组，看同一方向有哪些人 |
| 4 | `v-else` | 不写条件，前面的都不成立时才轮到它 | `ul` + `li` 名单，一行一人，最紧凑 |

点「表格 / 卡片 / 专业分组 / 名单」按钮只是改了 `viewMode` 这一个字符串，模板会重新判断一次，**把不成立的分支销毁、把成立的分支渲染出来**。

### v-if 与 v-show 的区别

| 对比项 | `v-if` | `v-show` |
| --- | --- | --- |
| 做法 | 条件为假时元素被销毁，为真时重新创建 | 元素一直在，只改 `display` 样式 |
| 初次渲染开销 | 条件为假时不做任何渲染，更省 | 无论条件真假都会渲染一次 |
| 反复切换开销 | 每次都要创建或销毁 | 只改样式，几乎无开销 |
| 支持 `v-else` | 支持，可以串成判断链 | 不支持，只能单独用 |
| 适合场景 | 切换不频繁：视图模式、权限区块、空状态 | 切换频繁：折叠面板、提示文字 |

`v-else` 和 `v-else-if` 必须紧跟在 `v-if` 后面，**中间只能放注释，插别的元素就断链**。单独使用 `v-else` 会报 `v-else/v-else-if has no adjacent v-if`。

---

## 6. 四种视图怎么选

| 视图 | 容器 | 一屏能看多少 | 适合的场合 |
| --- | --- | --- | --- |
| 表格 | `el-table` | 8 行以上，字段对齐 | 对比数据、按成绩排序、扫一遍所有人的分数 |
| 卡片 | `el-row` + `el-card` | 宽屏 3 张，每张 2 至 3 行信息 | 关注单个学生、配头像和进度条做展示 |
| 专业分组 | `div` + `section` | 按方向分块，块内一行一人 | 想知道某个方向有多少人、都是谁 |
| 名单 | `ul` + `li` | 一行一人，最密 | 快速点名、人数多、只需要扫姓名和关键字段 |

---

## 7. 容易写错的地方

### 坑 1：加了分支却忘了改条件（本次真实踩到）

复制「卡片」那一整块出来改成分组视图，却忘了把 `v-else-if="viewMode === 'card'"` 改成 `'group'`。结果是：**专业分组按钮永远点不出分组视图**，页面看起来和卡片模式一模一样，控制台还不报错——因为语法完全合法，只是条件永远不成立。

排查方法：点了没反应时，先看分支条件里写的是不是这个视图自己的值。

### 坑 2：组件样式不加 `scoped` 会污染全局

写组件样式时 `<style>` 要加 `scoped`，否则里面的选择器会作用到整个页面。裸标签选择器最容易出事，例如某个组件里写了 `p { width: 200px; height: 80px; line-height: 80px }`，看起来只想管自己那两个 `<p>`，实际会把页面里所有段落都改成宽 200px、行高 80px。**别的组件即使没被渲染，它的样式也会被打包并生效。**

### 其余容易漏的点

| 类别 | 注意点 |
| --- | --- |
| 模板 | `v-else` 必须紧跟 `v-if`；互斥分支用 `v-else-if`，别写成多个独立 `v-if`；顺序即优先级，从特殊到一般 |
| 数据 | 不会被改的数据不用 `ref`，会变的（视图模式）才用；`:key` 用学号这类唯一值 |
| 组件 | `el-table` 自己遍历数据，不再写 `v-for`；自定义列用 `scope.row` 取当前行；`el-radio-button` 用 `value` 传值 |
| 分组 | 两层 `v-for` 都要给 `:key`：外层用方向名，内层用学号 |

---

## 8. 验收清单

- [x] 四种视图都能正常显示，内容一致
- [x] 点击按钮后视图立即切换，数据不丢、顺序不变
- [x] 表格模式：成绩可点表头排序，颜色随分数变化
- [x] 卡片模式：窄窗口下自动变成一行一张，进度条长度与分数一致
- [x] 专业分组模式：同一方向的人归到一起，标题显示该方向人数
- [x] 名单模式：一行一人，鼠标移上去有高亮反馈
- [x] 状态标签类型正确，在读绿、实习中橙、休学灰

---

## 9. 延伸练习

| 题目 | 要改的地方 | 难度 |
| --- | --- | --- |
| 加「只看及格」开关 | `el-switch` + 一个 `ref`，再改成对应的数据数组 | 较易 |
| 点「查看详情」弹出对话框展示完整信息 | `el-dialog` + 一个 `ref` 保存选中的学生 | 中等 |
| 加搜索框，按姓名过滤（等学过 `computed` 再做） | 新增关键字 `ref` 和筛选逻辑，注意关键字清空时要恢复全部 | 中等 |
| 把各个视图拆成子组件 | 需要先学 `props` 与组件通信 | 较难 |

---

## 10. 本节小结

- 一份数据多种展示，关键是把「当前视图」抽成一个 `ref`，模板用 `v-if / v-else-if / v-else` 链去分派；
- 不变的用普通数组，会变的才用 `ref`；字典把「事实」和「文案样式」分开维护；
- `el-table` 自带遍历不需要 `v-for`，自己排的布局（卡片 / 分组 / 名单）才需要；
- 复制分支改视图时，记得同步改它的判断条件。

---

## 附录：Student.vue 完整代码

<details>
<summary>展开查看完整代码（约 370 行）</summary>

```vue
<script setup>
import { ref } from "vue";
import { ElMessage } from "element-plus";

// 一、数据源：学生数组。视图切换只改变怎么显示，不改变这份数据
// 这份数据从头到尾不会被修改，所以用普通数组就够了，不用 ref
const students = [
  { id: "20230101", name: "林一鸣", className: "软件 2301", direction: "前端开发", score: 92, status: "active" },
  { id: "20230102", name: "周予安", className: "软件 2301", direction: "后端开发", score: 85, status: "active" },
  { id: "20230103", name: "苏晚晴", className: "软件 2302", direction: "前端开发", score: 78, status: "intern" },
  { id: "20230104", name: "陈砚舟", className: "软件 2302", direction: "数据开发", score: 64, status: "leave" },
  { id: "20230105", name: "顾星野", className: "软件 2302", direction: "后端开发", score: 88, status: "active" },
  { id: "20230106", name: "许知微", className: "软件 2303", direction: "测试开发", score: 71, status: "intern" },
  { id: "20230107", name: "沈墨白", className: "软件 2303", direction: "前端开发", score: 95, status: "active" },
  { id: "20230108", name: "叶清和", className: "软件 2303", direction: "数据开发", score: 59, status: "leave" },
];

// 二、视图模式：table / card / group / list，模板里按它做 v-if 分支
const viewMode = ref("table");

// 三、状态字典：数据和文案分开写，模板里只做映射
const STATUS_MAP = {
  active: { text: "在读", type: "success" },
  intern: { text: "实习中", type: "warning" },
  leave: { text: "休学", type: "info" },
};

// 四、成绩颜色：普通函数，传入分数返回颜色
function scoreColor(score) {
  if (score >= 85) return "#0f9d58";
  if (score >= 70) return "#e6a23c";
  return "#f56c6c";
}

// 五、专业分组：把同一方向的人归到一起。
// 分组结果同样不会变，所以还是普通数组；这里只用最普通的 for 循环，
// 不用还没学到的 filter / reduce，也不需要用 computed
const DIRECTIONS = ["前端开发", "后端开发", "数据开发", "测试开发"];

const groupedStudents = [];
for (const direction of DIRECTIONS) {
  const members = [];
  for (const item of students) {
    if (item.direction === direction) members.push(item);
  }
  groupedStudents.push({ direction, members });
}

function showDetail(student) {
  ElMessage.success(
    `${student.name}（${student.id}）：${student.direction}，成绩 ${student.score}`,
  );
}
</script>

<template>
  <div class="page">
    <header class="page__head">
      <div>
        <h2 class="page__title">学生名单</h2>
        <p class="page__desc">
          共 {{ students.length }} 人 · 切换视图用的是 v-if / v-else-if / v-else
        </p>
      </div>

      <!-- 视图切换：viewMode 一变，模板自动在几个分支之间换 -->
      <el-radio-group v-model="viewMode">
        <el-radio-button value="table">表格</el-radio-button>
        <el-radio-button value="card">卡片</el-radio-button>
        <el-radio-button value="group">专业分组</el-radio-button>
        <el-radio-button value="list">名单</el-radio-button>
      </el-radio-group>
    </header>

    <!-- ===== 条件渲染：下面四块同一时间只会出现一块 ===== -->

    <!-- 第一块：表格模式 -->
    <el-table v-if="viewMode === 'table'" :data="students" stripe border>
      <el-table-column prop="id" label="学号" width="120" />
      <el-table-column prop="name" label="姓名" width="110" />
      <el-table-column prop="className" label="班级" width="120" />
      <el-table-column prop="direction" label="方向" />
      <el-table-column prop="score" label="成绩" width="100" sortable>
        <template #default="scope">
          <span class="score" :style="{ color: scoreColor(scope.row.score) }">{{
            scope.row.score
          }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <el-tag :type="STATUS_MAP[scope.row.status].type" effect="light">
            {{ STATUS_MAP[scope.row.status].text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="90">
        <template #default="scope">
          <el-button link type="primary" @click="showDetail(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 第二块：卡片模式 -->
    <el-row v-else-if="viewMode === 'card'" :gutter="16">
      <el-col v-for="item in students" :key="item.id" :xs="24" :sm="12" :md="8" class="card-col">
        <el-card shadow="hover" class="stu-card">
          <div class="stu-card__top">
            <el-avatar :size="48" class="avatar">{{ item.name.charAt(0) }}</el-avatar>
            <div>
              <p class="stu-card__name">{{ item.name }}</p>
              <p class="stu-card__id">{{ item.id }} · {{ item.className }}</p>
            </div>
          </div>

          <div class="stu-card__tags">
            <el-tag size="small" effect="plain">{{ item.direction }}</el-tag>
            <el-tag size="small" :type="STATUS_MAP[item.status].type" effect="light">
              {{ STATUS_MAP[item.status].text }}
            </el-tag>
          </div>

          <p class="stu-card__score">成绩 {{ item.score }}</p>
          <el-progress
            :percentage="item.score"
            :color="scoreColor(item.score)"
            :stroke-width="8"
            :show-text="false"
          />

          <template #footer>
            <el-button link type="primary" @click="showDetail(item)">查看详情</el-button>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三块：专业分组模式（延伸练习：在判断链上再加一个 v-else-if 分支） -->
    <div v-else-if="viewMode === 'group'" class="group-wrap">
      <section v-for="group in groupedStudents" :key="group.direction" class="group">
        <h3 class="group__title">
          {{ group.direction }}
          <span class="group__count">{{ group.members.length }} 人</span>
        </h3>

        <ul class="group__list">
          <li v-for="item in group.members" :key="item.id" class="group__item">
            <el-avatar :size="32" class="avatar">{{ item.name.charAt(0) }}</el-avatar>
            <span class="group__name">{{ item.name }}</span>
            <span class="group__meta">{{ item.id }} · {{ item.className }}</span>
            <span class="score" :style="{ color: scoreColor(item.score) }">{{ item.score }}</span>
            <el-tag size="small" :type="STATUS_MAP[item.status].type" effect="light">
              {{ STATUS_MAP[item.status].text }}
            </el-tag>
          </li>
        </ul>
      </section>
    </div>

    <!-- 第四块：名单模式，一行一人，人多了用这个（v-else 兜底，不写条件） -->
    <ul v-else class="name-list">
      <li v-for="item in students" :key="item.id" class="name-list__item">
        <el-avatar :size="32" class="avatar">{{ item.name.charAt(0) }}</el-avatar>
        <span class="name-list__name">{{ item.name }}</span>
        <span class="name-list__meta">{{ item.id }} · {{ item.className }} · {{ item.direction }}</span>
        <span class="score" :style="{ color: scoreColor(item.score) }">{{ item.score }}</span>
        <el-tag size="small" :type="STATUS_MAP[item.status].type" effect="light">
          {{ STATUS_MAP[item.status].text }}
        </el-tag>
      </li>
    </ul>
  </div>
</template>
```

</details>

---

## 参考

- 上一节：[03 · Vue 基础指令合集](./03-vue-directives.md)
- 源码：[Student.vue](https://github.com/beiiiii-111/frontend-learning/blob/main/03-vue-basic/vue-basic-demo/src/components/Student.vue)
- 对应代码仓库：[beiiiii-111/frontend-learning](https://github.com/beiiiii-111/frontend-learning)
- 官方文档：[Vue 3 · 条件渲染](https://cn.vuejs.org/guide/essentials/conditional.html)
