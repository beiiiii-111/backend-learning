<template>
  <div style="max-width:1000px;margin:0 auto">
    <div style="display:flex;gap:12px;margin-bottom:16px">
      <el-select v-model="query.category" placeholder="全部分类" clearable style="width:140px" @change="load">
        <el-option label="讲座" value="lecture" /><el-option label="文体" value="sports" />
        <el-option label="志愿" value="volunteer" /><el-option label="竞赛" value="contest" />
      </el-select>
      <el-input v-model="query.keyword" placeholder="搜索活动名称" clearable style="width:240px" @keyup.enter="load" />
      <el-checkbox v-model="query.onlyOpen" label="仅看可报名" @change="load" />
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-row v-if="list.length" :gutter="16">
      <el-col v-for="a in list" :key="a.activityId" :span="8" style="margin-bottom:16px">
        <el-card shadow="hover" @click="$router.push(`/activities/${a.activityId}`)" style="cursor:pointer">
          <h3 style="margin:0 0 8px">{{ a.title }}</h3>
          <p style="color:#888;font-size:13px;margin:4px 0">🕒 {{ a.startTime }}</p>
          <p style="color:#888;font-size:13px;margin:4px 0">📍 {{ a.location }}</p>
          <p style="font-size:13px;margin:4px 0">
            <el-tag size="small">{{ categoryText(a.category) }}</el-tag>
            <span style="margin-left:8px" :style="{ color: a.remaining === 0 ? '#f56c6c' : '#67c23a' }">
              已报 {{ a.signedCount }}/{{ a.capacity }}
              {{ a.remaining === 0 ? '（已满）' : '' }}
            </span>
          </p>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-else description="暂无可报名的活动" />

    <el-pagination layout="prev, pager, next" :total="total" :page-size="query.size"
                   v-model:current-page="query.page" @current-change="load" style="justify-content:center" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import http from '../api'

const query = reactive({ page: 1, size: 10, category: '', keyword: '', onlyOpen: false })
const list = ref([])
const total = ref(0)

const categoryText = (c) => ({ lecture: '讲座', sports: '文体', volunteer: '志愿', contest: '竞赛' }[c] || c)

async function load() {
  const data = await http.get('/activities', { params: query })
  list.value = data.list
  total.value = data.total
}
onMounted(load)
</script>
