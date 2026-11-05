<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api } from '../api'
import type { Task } from '../types/api'
import { useAuth } from '../store/auth'

const auth = useAuth()
const tasks = ref<Task[]>([])
const loading = ref(false)

const title = ref('')
const content = ref('')
const submitting = ref(false)

/** 列表筛选：全部 / 待批改 / 已批改，在前端算，接口不接收查询参数 */
const filter = ref<'all' | 'SUBMITTED' | 'REVIEWED'>('all')
/** 展开查看的详情（走 GET /api/tasks/{id}，本人可见自己的） */
const expanded = ref<Task | null>(null)
const expandedLoading = ref(false)

const reviewedTasks = computed(() => tasks.value.filter((t) => t.status === 'REVIEWED'))

const stats = computed(() => {
  const reviewed = reviewedTasks.value
  const avg =
    reviewed.length > 0
      ? (reviewed.reduce((sum, t) => sum + (t.score ?? 0), 0) / reviewed.length).toFixed(1)
      : null
  return {
    total: tasks.value.length,
    pending: tasks.value.filter((t) => t.status === 'SUBMITTED').length,
    reviewed: reviewed.length,
    avg,
  }
})

const filtered = computed(() =>
  filter.value === 'all' ? tasks.value : tasks.value.filter((t) => t.status === filter.value),
)

const counts = computed(() => ({
  all: tasks.value.length,
  SUBMITTED: stats.value.pending,
  REVIEWED: stats.value.reviewed,
}))

function fmtDate(value: string): string {
  return value.split(' ')[0].replaceAll('-', '/')
}

function fmtTime(value: string): string {
  return value.split(' ')[1] ?? ''
}

function brief(comment: string | null): string {
  if (!comment) return ''
  return comment.length > 14 ? `评语：${comment.slice(0, 14)}…` : `评语：${comment}`
}

async function load() {
  loading.value = true
  try {
    tasks.value = (await api.listMine()) as Task[]
  } finally {
    loading.value = false
  }
}

async function submit() {
  if (!title.value || !content.value || submitting.value) return
  submitting.value = true
  try {
    await api.submitTask({ title: title.value, content: content.value })
    title.value = ''
    content.value = ''
    await load()
  } catch {
    // 30011 重复提交、参数校验错误等提示已由拦截器弹出
  } finally {
    submitting.value = false
  }
}

async function toggleDetail(task: Task) {
  if (expanded.value?.id === task.id) {
    expanded.value = null
    return
  }
  expandedLoading.value = true
  expanded.value = null
  try {
    // 归属判断在后端：本人的 200，别人的 403
    expanded.value = (await api.getTask(task.id)) as Task
  } finally {
    expandedLoading.value = false
  }
}

onMounted(load)
</script>

<template>
  <main class="page">
    <div class="stats">
      <div class="stat">
        <div class="k">已提交</div>
        <div class="v">{{ stats.total }}<small>份</small></div>
      </div>
      <div class="stat">
        <div class="k">待批改</div>
        <div class="v amber">{{ stats.pending }}<small>份</small></div>
      </div>
      <div class="stat">
        <div class="k">已批改</div>
        <div class="v green">{{ stats.reviewed }}<small>份</small></div>
      </div>
      <div class="stat">
        <div class="k">平均分</div>
        <div class="v">{{ stats.avg ?? '—' }}<small v-if="stats.avg">分</small></div>
      </div>
    </div>

    <div class="two-col">
      <div v-if="auth.canSubmit.value" class="card">
        <h2>提交实训任务</h2>
        <p class="desc">提交人由后端按令牌写入，请求里没有用户名字段</p>
        <form @submit.prevent="submit">
          <label>任务标题</label>
          <input v-model="title" maxlength="100" placeholder="实训六：分页查询与条件筛选" />
          <label>完成情况</label>
          <textarea v-model="content" maxlength="2000" placeholder="做了哪些、卡在哪里"></textarea>
          <div class="form-actions">
            <button class="primary" type="submit" :disabled="submitting">
              {{ submitting ? '提交中…' : '提交' }}
            </button>
          </div>
        </form>
      </div>

      <div class="card">
        <h2>我的提交</h2>
        <p class="desc">接口是 /api/tasks/mine，URL 里没有用户名，改也改不出别人的数据</p>

        <div class="toolbar">
          <div class="tabs">
            <button
              v-for="key in ['all', 'SUBMITTED', 'REVIEWED'] as const"
              :key="key"
              :class="{ active: filter === key }"
              @click="filter = key"
            >
              {{ key === 'all' ? '全部' : key === 'SUBMITTED' ? '待批改' : '已批改' }}
              <span class="n">{{ counts[key] }}</span>
            </button>
          </div>
          <span class="count-hint">共 {{ filtered.length }} 条</span>
        </div>

        <table v-if="filtered.length" class="table">
          <thead>
            <tr>
              <th>标题</th>
              <th>状态</th>
              <th>分数</th>
              <th>提交时间</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in filtered" :key="task.id">
              <td>
                <div>{{ task.title }}</div>
                <div v-if="task.comment" class="sub">{{ brief(task.comment) }}</div>
              </td>
              <td>
                <span class="tag" :class="task.status === 'REVIEWED' ? 'green' : 'amber'">
                  {{ task.statusText }}
                </span>
              </td>
              <td>
                <span v-if="task.score != null" class="score-num">{{ task.score }}</span>
                <span v-else class="muted-dash">—</span>
              </td>
              <td class="sub">
                {{ fmtDate(task.submittedAt) }}<br />{{ fmtTime(task.submittedAt) }}
              </td>
              <td>
                <button @click="toggleDetail(task)">
                  {{ expanded?.id === task.id ? '收起' : '查看' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty">{{ loading ? '加载中…' : '还没有提交过任务' }}</div>

        <div v-if="expandedLoading" class="empty">加载详情中…</div>
        <div v-if="expanded" class="detail">
          <h3>{{ expanded.title }}</h3>
          <p class="meta">
            {{ expanded.ownerNickname }} · 提交于 {{ expanded.submittedAt }}
            <template v-if="expanded.reviewedAt"> · 批改于 {{ expanded.reviewedAt }}</template>
          </p>
          <p class="content">{{ expanded.content }}</p>
          <div v-if="expanded.status === 'REVIEWED'" class="score-box">
            <div class="s">得分 {{ expanded.score }}</div>
            <div class="c">评语：{{ expanded.comment ?? '—' }}</div>
          </div>
          <div v-else class="pending-box">教师还没批改</div>
          <button @click="expanded = null">收起</button>
        </div>
      </div>
    </div>
  </main>
</template>
