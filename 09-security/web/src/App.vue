<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuth } from './store/auth'
import { useToast } from './store/toast'

const auth = useAuth()
const router = useRouter()
const { state: toast, close } = useToast()

function logout() {
  auth.clear()
  router.push('/login')
}
</script>

<template>
  <header v-if="auth.isLoggedIn.value" class="topbar">
    <div class="brand">
      <b>实训任务提交与批改</b>
      <span>09 · 认证与授权</span>
    </div>
    <nav>
      <router-link to="/tasks">我的提交</router-link>
      <router-link v-if="auth.canReview.value" to="/review">待批改</router-link>
      <router-link v-if="auth.isAdmin.value" to="/users">账号管理</router-link>
    </nav>
    <div class="spacer"></div>
    <span class="whoami">
      {{ auth.state.user?.nickname }}
      <em class="role-badge">{{ auth.state.user?.roles[0] }}</em>
    </span>
    <button class="link" @click="logout">退出</button>
  </header>

  <router-view />

  <div v-if="toast.visible" class="toast" :class="toast.type" @click="close">
    {{ toast.message }}
  </div>
</template>
