<template>
  <div style="max-width:700px;margin:0 auto" v-if="a">
    <el-page-header @back="$router.back()" :content="a.title" style="margin-bottom:16px" />
    <el-card>
      <p>🕒 活动时间：{{ a.startTime }}</p>
      <p>📍 活动地点：{{ a.location }}</p>
      <p>⏰ 报名截止：{{ a.signupDeadline }}</p>
      <p>👥 名额：已报 <b>{{ a.signedCount }}</b> / {{ a.capacity }}
        <span v-if="a.remaining === 0" style="color:#f56c6c">（名额已满）</span></p>
      <p style="white-space:pre-wrap;border-top:1px solid #eee;padding-top:12px">{{ a.description }}</p>
      <el-button v-if="isStudent" type="primary" size="large" style="width:100%"
                 :disabled="a.remaining === 0" :loading="signing" @click="signup">
        {{ a.remaining === 0 ? '名额已满' : '立即报名' }}
      </el-button>
      <p v-if="!isLoggedIn" style="color:#999;text-align:center;font-size:13px">
        未登录，<router-link to="/login">去登录</router-link>后可报名
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import http, { onUnauthorized } from '../api'

const route = useRoute()
const a = ref(null)
const signing = ref(false)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const isStudent = computed(() => localStorage.getItem('role') === 'student')

onMounted(async () => {
  a.value = await http.get(`/activities/${route.params.id}`)
})

async function signup() {
  signing.value = true
  try {
    await http.post(`/activities/${route.params.id}/signups`)
    ElMessage.success('报名成功！可在「我的报名」中查看')
    a.value = await http.get(`/activities/${route.params.id}`)   // 刷新名额
  } catch (e) {
    // 业务错误（已满/重复/截止）已由拦截器 toast，无需处理
  } finally {
    signing.value = false
  }
}
</script>
