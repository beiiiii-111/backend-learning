<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api } from '../api'
import type { Task } from '../types/api'

const tasks = ref<Task[]>([])
const loading = ref(false)

const filter = ref<'all' | 'SUBMITTED' | 'REVIEWED'>('all')
const keyword = ref('')
/** 正在批改的任务（同一时刻只开一个面板） */
const editingId = ref<number | null>(null)
const score = ref('')
const comment = ref('')
const saving = ref(false)

const reviewedTasks = computed(() => tasks.value.filter((t) => t.status === 'REVIEWED'))

const stats = computed(() => {
  const reviewed = reviewedTasks.value
  const owners = new Set(tasks.value.map((t) => t.owner))
  const avg =
    reviewed.length > 0
      ? (reviewed.reduce((sum, t) => sum + (t.score ?? 0), 0) / reviewed.length).toFixed(1)
      : null
  return {
    total: tasks.value.length,
    pending: tasks.value.filter((t) => t.status === 'SUBMITTED').length,
    reviewed: reviewed.length,
    people: owners.size,
    avg,
  }
})

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return tasks.value
    .filter((t) => (filter.value === 'all' ? true : t.status === filter.value))
    .filter((t) => {
      if (!kw) return true
      return (
        t.owner.toLowerCase().includes(kw) ||
        t.ownerNickname.toLowerCase().includes(kw) ||
        t.title.toLowerCase().includes(kw)
      )
    })
})

const counts = computed(() => ({
  all: tasks.value.length,
  SUBMITTED: stats.value.pending,
  REVIEWED: stats.value.reviewed,
}))

const editingTask = computed(() => tasks.value.find((t) => t.id === editingId.value) ?? null)

function fmtDate(value: string): string {
  return value.split(' ')[0].replaceAll('-', '/')
}

function fmtTime(value: string): string {
  return value.split(' ')[1] ?? ''
}

function startEdit(task: Task) {
  editingId.value = task.id
  score.value = ''
  comment.value = ''
}

function cancelEdit() {
  editingId.value = null
}

async function load() {
  loading.value = true
  try {
    tasks.value = (await api.listAllTasks()) as Task[]
  } finally {
    loading.value = false
  }
}

async function save() {
  if (editingId.value == null || score.value === '' || saving.value) return
  const value = Number(score.value)
  if (Number.isNaN(value)) return
  saving.value = true
  try {
    await api.reviewTask(editingId.value, { score: value, comment: comment.value })
    editingId.value = null
    await load()
  } catch {
    // 30012 重复批改、分数超界等提示已由拦截器弹出
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <main class="page">
    <div class="stats">
      <div class="stat">
        <div class="k">全部提交</div>
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
        <div class="k">平均分 · {{ stats.people }} 人提交</div>
        <div class="v">{{ stats.avg ?? '—' }}<small v-if="stats.avg">分</small></div>
      </div>
    </div>

    <div class="card">
      <h2>全部提交</h2>
      <p class="desc">接口 /api/tasks，需要 task:review 权限点，学生调这个接口会拿到 403</p>

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
        <input
          v-model="keyword"
          class="search"
          placeholder="按姓名、账号或标题搜"
        />
        <span class="count-hint">筛出 {{ filtered.length }} 条</span>
      </div>

      <table v-if="filtered.length" class="table">
        <thead>
          <tr>
            <th style="width: 48px">#</th>
            <th>标题</th>
            <th>提交人</th>
            <th>状态</th>
            <th>分数</th>
            <th>提交时间</th>
            <th style="width: 90px"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="task in filtered" :key="task.id">
            <td class="sub">{{ task.id }}</td>
            <td>
              <div>{{ task.title }}</div>
              <div v-if="task.comment" class="sub">评语：{{ task.comment }}</div>
            </td>
            <td>
              <div>{{ task.ownerNickname }}</div>
              <div class="sub">{{ task.owner }}</div>
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
              <button
                v-if="task.status === 'SUBMITTED' && editingId !== task.id"
                @click="startEdit(task)"
              >
                批改
              </button>
              <span v-else-if="task.status === 'REVIEWED'" class="sub">已批</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty">{{ loading ? '加载中…' : '没有符合条件的提交' }}</div>

      <div v-if="editingTask" class="detail">
        <h3>批改：{{ editingTask.title }}</h3>
        <p class="meta">
          {{ editingTask.ownerNickname }}（{{ editingTask.owner }}）提交于
          {{ editingTask.submittedAt }} · 同一份不能批改两次，后端会返回业务码 30012
        </p>
        <div class="content-box">{{ editingTask.content }}</div>
        <label>分数（0 到 100）</label>
        <input v-model="score" type="number" min="0" max="100" style="max-width: 220px" />
        <label>评语</label>
        <textarea v-model="comment" maxlength="500" placeholder="写清楚哪对、哪里要改"></textarea>
        <div class="form-actions">
          <button class="primary" :disabled="saving" @click="save">
            {{ saving ? '保存中…' : '保存批改' }}
          </button>
          <button @click="cancelEdit">取消</button>
        </div>
      </div>
    </div>
  </main>
</template>
