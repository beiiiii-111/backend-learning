<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '../store/auth'

const auth = useAuth()
const route = useRoute()
const router = useRouter()

const username = ref('')
const password = ref('')
const formError = ref('')
const submitting = ref(false)

const quick = [
  { label: '学生 student01', username: 'student01', password: 'Student#2026' },
  { label: '教师 teacher01', username: 'teacher01', password: 'Teacher#2026' },
  { label: '管理员 admin', username: 'admin', password: 'Admin#2026' },
  { label: '停用 guest', username: 'guest', password: 'Guest#2026' },
]

function fill(account: { username: string; password: string }) {
  username.value = account.username
  password.value = account.password
  formError.value = ''
}

async function onSubmit() {
  if (!username.value || !password.value || submitting.value) return
  submitting.value = true
  formError.value = ''
  try {
    await auth.login(username.value, password.value)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/tasks'
    router.replace(redirect)
  } catch (e) {
    // 提示语区分 30001 与 30002；顶部 toast 由拦截器弹，这里在卡内再给一个错误框
    formError.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="card login-card">
      <h1>实训任务提交与批改</h1>
      <p class="sub">登录后获取令牌，之后每个请求都会自动带上</p>

      <form @submit.prevent="onSubmit">
        <div v-if="formError" class="form-error">{{ formError }}</div>
        <label>用户名</label>
        <input v-model="username" autocomplete="username" placeholder="student01" />
        <label>密码</label>
        <input
          v-model="password"
          type="password"
          autocomplete="current-password"
          placeholder="••••••••"
        />
        <button class="primary" type="submit" :disabled="submitting">
          {{ submitting ? '登录中…' : '登录' }}
        </button>
      </form>

      <div class="divider">点一下直接填入，不用手打</div>
      <div class="chips">
        <button
          v-for="account in quick"
          :key="account.username"
          type="button"
          class="chip"
          @click="fill(account)"
        >
          {{ account.label }}
        </button>
      </div>
    </div>
  </div>
</template>
