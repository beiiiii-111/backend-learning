import axios from 'axios'
import { ElMessage } from 'element-plus'
import { router } from './router'

// 统一请求封装：自动携带 token、401 跳登录、错误统一提示
const http = axios.create({ baseURL: '/api/v1', timeout: 10000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body.code !== 0) {
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(body)
    }
    return body.data
  },
  (err) => {
    if (err.response && err.response.status === 500) {
      ElMessage.error('服务器内部错误')
    }
    return Promise.reject(err)
  }
)

// 401 统一跳登录（在业务层判断，避免依赖响应状态码）
export function onUnauthorized() {
  localStorage.removeItem('token')
  ElMessage.warning('请先登录')
  router.push('/login')
}

export default http
