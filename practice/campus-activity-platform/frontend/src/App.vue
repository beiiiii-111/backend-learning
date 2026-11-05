<template>
  <el-container style="min-height: 100vh">
    <el-header style="display:flex;align-items:center;gap:24px;border-bottom:1px solid #eee">
      <b style="font-size:18px">校园活动管理平台</b>
      <el-menu mode="horizontal" :default-active="$route.path" router :ellipsis="false" style="flex:1;border:none">
        <el-menu-item index="/activities">活动列表</el-menu-item>
        <el-menu-item v-if="isOrganizer" index="/publish">发布活动</el-menu-item>
        <el-menu-item v-if="isAdmin" index="/audit">审核中心</el-menu-item>
        <el-menu-item v-if="isLoggedIn" index="/my-signups">我的报名</el-menu-item>
      </el-menu>
      <template v-if="isLoggedIn">
        <span style="color:#666">{{ nickname }}（{{ roleText }}）</span>
        <el-button link type="danger" @click="logout">退出</el-button>
      </template>
      <el-button v-else type="primary" @click="$router.push('/login')">登录</el-button>
    </el-header>
    <el-main><router-view /></el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const role = computed(() => localStorage.getItem('role') || '')
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const isOrganizer = computed(() => role.value === 'organizer')
const isAdmin = computed(() => role.value === 'admin')
const nickname = computed(() => localStorage.getItem('nickname') || '')
const roleText = computed(() => ({ student: '学生', organizer: '组织者', admin: '管理员' }[role.value] || ''))

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>
