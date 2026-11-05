<template>
  <el-card style="max-width:400px;margin:60px auto">
    <h2>登录</h2>
    <el-form :model="form" label-width="70px" @keyup.enter="submit">
      <el-form-item label="用户名"><el-input v-model="form.username" placeholder="admin / organizer / student" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
      <el-button type="primary" style="width:100%" :loading="loading" @click="submit">登 录</el-button>
      <p style="color:#999;font-size:12px;text-align:center">演示账号密码均为 11111111</p>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const router = useRouter()
const form = reactive({ username: 'student', password: '11111111' })
const loading = ref(false)

async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    const data = await http.post('/users/login', form)
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    localStorage.setItem('userId', data.userId)
    const me = await http.get('/users/me')
    localStorage.setItem('nickname', me.nickname)
    ElMessage.success('登录成功')
    router.push('/activities')
  } finally {
    loading.value = false
  }
}
</script>
