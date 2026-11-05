<template>
  <el-card style="max-width:640px;margin:0 auto">
    <h2>发布活动</h2>
    <el-form :model="form" label-width="110px">
      <el-form-item label="活动标题"><el-input v-model="form.title" maxlength="50" show-word-limit /></el-form-item>
      <el-form-item label="封面图地址"><el-input v-model="form.coverUrl" placeholder="https://..." /></el-form-item>
      <el-form-item label="活动分类">
        <el-select v-model="form.category" style="width:200px">
          <el-option label="讲座" value="lecture" /><el-option label="文体" value="sports" />
          <el-option label="志愿" value="volunteer" /><el-option label="竞赛" value="contest" />
        </el-select>
      </el-form-item>
      <el-form-item label="活动时间">
        <el-input v-model="form.startTime" placeholder="2026-11-20 14:00:00" style="width:220px" />
        <span style="margin:0 8px">至</span>
        <el-input v-model="form.endTime" placeholder="2026-11-20 17:00:00" style="width:220px" />
      </el-form-item>
      <el-form-item label="报名截止"><el-input v-model="form.signupDeadline" placeholder="须早于活动开始时间" style="width:220px" /></el-form-item>
      <el-form-item label="活动地点"><el-input v-model="form.location" /></el-form-item>
      <el-form-item label="人数上限"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
      <el-form-item label="活动详情">
        <el-input v-model="form.description" type="textarea" :rows="5" maxlength="2000" show-word-limit />
      </el-form-item>
      <el-button type="primary" style="width:100%" :loading="submitting" @click="submit">提交审核</el-button>
      <p style="color:#999;font-size:12px;text-align:center;margin-top:8px">提交后进入待审核状态，管理员审核通过才会展示</p>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '../api'

const router = useRouter()
const submitting = ref(false)
const form = reactive({
  title: '', coverUrl: '', category: '', startTime: '', endTime: '',
  signupDeadline: '', location: '', capacity: 100, description: ''
})

async function submit() {
  for (const [k, label] of [['title', '标题'], ['category', '分类'], ['startTime', '开始时间'],
        ['endTime', '结束时间'], ['signupDeadline', '报名截止'], ['location', '地点'], ['description', '详情']]) {
    if (!form[k]) return ElMessage.warning(`请填写${label}`)
  }
  submitting.value = true
  try {
    await http.post('/activities', form)
    ElMessage.success('提交成功，等待管理员审核')
    router.push('/activities')
  } finally {
    submitting.value = false
  }
}
</script>
