<template>
  <div style="max-width:800px;margin:0 auto">
    <h2>我的报名</h2>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="title" label="活动" min-width="180" />
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="location" label="地点" width="140" />
      <el-table-column prop="signupTime" label="报名时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'signed' ? 'success' : 'info'">
            {{ row.status === 'signed' ? '已报名' : '已取消' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.status === 'signed'" type="danger" size="small" @click="cancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && !list.length" description="还没有报名记录，去活动列表看看吧" />
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
    const data = await http.get('/signups/me')
    list.value = data.list
  } finally {
    loading.value = false
  }
}

async function cancel(row) {
  await ElMessageBox.confirm(`确定取消报名「${row.title}」吗？`, '提示', { type: 'warning' })
  await http.delete(`/signups/${row.signupId}`)
  ElMessage.success('已取消，名额已释放')
  load()
}

onMounted(load)
</script>
