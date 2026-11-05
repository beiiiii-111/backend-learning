<template>
  <div style="max-width:900px;margin:0 auto">
    <h2>审核中心（管理员）</h2>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="activityId" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="location" label="地点" width="140" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="audit(row, 'approve')">通过</el-button>
          <el-button type="danger" size="small" @click="reject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && !list.length" description="暂无待审核的活动" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '../api'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await http.get('/activities', { params: { status: 'pending', page: 1, size: 50 } })
    list.value = data.list
  } finally {
    loading.value = false
  }
}

async function audit(row, action) {
  await http.put(`/activities/${row.activityId}/audit`, { action })
  ElMessage.success(action === 'approve' ? '已通过' : '已驳回')
  load()
}

async function reject(row) {
  const { value } = await ElMessageBox.prompt('请填写驳回原因（必填）', `驳回「${row.title}」`, {
    inputValidator: (v) => (v && v.trim() ? true : '驳回原因不能为空')
  })
  await http.put(`/activities/${row.activityId}/audit`, { action: 'reject', reason: value })
  ElMessage.success('已驳回')
  load()
}

onMounted(load)
</script>
