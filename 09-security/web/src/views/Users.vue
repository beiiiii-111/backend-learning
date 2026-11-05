<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api } from '../api'
import { useAuth } from '../store/auth'
import type { UserProfile } from '../types/api'

const { state: authState } = useAuth()

const users = ref<UserProfile[]>([])
const loading = ref(false)
const toggling = ref('')

const filter = ref<'all' | 'STUDENT' | 'TEACHER' | 'ADMIN'>('all')
const keyword = ref('')

const username = ref('')
const password = ref('')
const nickname = ref('')
const role = ref('STUDENT')
const creating = ref(false)

const stats = computed(() => ({
  total: users.value.length,
  students: users.value.filter((u) => u.roles.includes('STUDENT')).length,
  teachers: users.value.filter((u) => u.roles.includes('TEACHER')).length,
  disabled: users.value.filter((u) => !u.enabled).length,
}))

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  return users.value
    .filter((u) => (filter.value === 'all' ? true : u.roles.includes(filter.value)))
    .filter((u) => {
      if (!kw) return true
      return u.username.toLowerCase().includes(kw) || u.nickname.toLowerCase().includes(kw)
    })
})

function startCreate() {
  if (!username.value || !password.value || !nickname.value || creating.value) return
  creating.value = true
  void doCreate()
}

async function doCreate() {
  try {
    await api.createUser({
      username: username.value,
      password: password.value,
      nickname: nickname.value,
      roles: [role.value],
    })
    username.value = ''
    password.value = ''
    nickname.value = ''
    role.value = 'STUDENT'
    await load()
  } catch {
    // 30004 用户名已存在、参数校验等提示已由拦截器弹出
  } finally {
    creating.value = false
  }
}

async function remove(user: UserProfile) {
  if (!window.confirm(`确定删除账号 ${user.username} 吗？`)) return
  try {
    await api.deleteUser(user.username)
    await load()
  } catch {
    // 30005 不能删除当前登录账号等提示已由拦截器弹出
  }
}

async function toggleStatus(user: UserProfile) {
  if (toggling.value) return
  // 自己停自己 = 把自己锁在门外，前端直接拦，后端还有 30006 兜底
  if (user.username === authState.user?.username) return
  const next = !user.enabled
  if (!next && !window.confirm(`确定停用 ${user.username} 吗？停用后该账号无法登录，已有登录态立即失效。`)) {
    return
  }
  toggling.value = user.username
  try {
    await api.setUserStatus(user.username, next)
    await load()
  } catch {
    // 30006 不能停用自己、404 等提示已由拦截器弹出
  } finally {
    toggling.value = ''
  }
}

async function load() {
  loading.value = true
  try {
    users.value = (await api.listUsers()) as UserProfile[]
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <main class="page">
    <div class="stats">
      <div class="stat">
        <div class="k">账号总数</div>
        <div class="v">{{ stats.total }}<small>个</small></div>
      </div>
      <div class="stat">
        <div class="k">学生</div>
        <div class="v">{{ stats.students }}<small>个</small></div>
      </div>
      <div class="stat">
        <div class="k">教师</div>
        <div class="v">{{ stats.teachers }}<small>个</small></div>
      </div>
      <div class="stat">
        <div class="k">停用</div>
        <div class="v amber">{{ stats.disabled }}<small>个</small></div>
      </div>
    </div>

    <div class="two-col">
      <div class="card">
        <h2>新增账号</h2>
        <p class="desc">只有 ADMIN 能调，教师访问 /admin/users 会拿到 403</p>
        <form @submit.prevent="startCreate">
          <label>用户名</label>
          <input v-model="username" maxlength="32" placeholder="student11" />
          <label>姓名</label>
          <input v-model="nickname" maxlength="50" placeholder="马嘉航" />
          <label>初始密码</label>
          <input v-model="password" type="password" maxlength="64" placeholder="Student#2026" />
          <label>角色</label>
          <select v-model="role">
            <option value="STUDENT">学生 STUDENT</option>
            <option value="TEACHER">教师 TEACHER</option>
            <option value="ADMIN">管理员 ADMIN</option>
          </select>
          <div class="form-actions">
            <button class="primary" type="submit" :disabled="creating">
              {{ creating ? '创建中…' : '创建' }}
            </button>
          </div>
        </form>
      </div>

      <div class="card">
        <h2>账号列表</h2>
        <p class="desc">响应里没有密码字段 · 点状态标签可切换 启用/停用，停用立即生效</p>

        <div class="toolbar">
          <div class="tabs">
            <button
              v-for="key in ['all', 'STUDENT', 'TEACHER', 'ADMIN'] as const"
              :key="key"
              :class="{ active: filter === key }"
              @click="filter = key"
            >
              {{ key === 'all' ? '全部' : key === 'STUDENT' ? '学生' : key === 'TEACHER' ? '教师' : '管理员' }}
            </button>
          </div>
          <input v-model="keyword" class="search" placeholder="按用户名或姓名搜" />
          <span class="count-hint">筛出 {{ filtered.length }} 个</span>
        </div>

        <table v-if="filtered.length" class="table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>姓名</th>
              <th>角色</th>
              <th>状态</th>
              <th style="width: 80px"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filtered" :key="user.username">
              <td>{{ user.username }}</td>
              <td>{{ user.nickname }}</td>
              <td>{{ user.roles.join('、') }}</td>
              <td>
                <span
                  v-if="user.username !== authState.user?.username"
                  class="tag status-toggle"
                  :class="[user.enabled ? 'green' : 'amber', { busy: toggling === user.username }]"
                  :title="user.enabled ? '点击停用该账号' : '点击启用该账号'"
                  @click="toggleStatus(user)"
                >
                  {{ user.enabled ? '启用' : '停用' }}
                </span>
                <span v-else class="tag green" title="不能停用当前登录账号">启用</span>
              </td>
              <td>
                <button class="danger" @click="remove(user)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty">{{ loading ? '加载中…' : '没有符合条件的账号' }}</div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.status-toggle {
  cursor: pointer;
  user-select: none;
  transition: opacity 0.15s, transform 0.15s;
}
.status-toggle:hover {
  opacity: 0.75;
  transform: scale(1.05);
}
.status-toggle.busy {
  opacity: 0.45;
  pointer-events: none;
}
</style>
