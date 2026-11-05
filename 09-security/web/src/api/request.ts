import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { showToast } from '../store/toast'
import type { ApiResponse } from '../types/api'

const request: AxiosInstance = axios.create({ baseURL: '', timeout: 10000 })

function readAuth(): { token: string; tokenType: string } {
  return {
    token: localStorage.getItem('security.token') ?? '',
    tokenType: localStorage.getItem('security.tokenType') ?? 'Bearer',
  }
}

/** 请求拦截器：每个请求带上令牌（有的话） */
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const { token, tokenType } = readAuth()
  if (token) {
    config.headers.Authorization = `${tokenType} ${token}`
  }
  return config
})

/** 响应拦截器：拆外壳 + 分流错误。401 和 403 必须是两个分支，处理完全不同 */
request.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown>
    // 业务失败也走 HTTP 200，靠 code 区分
    if (body.code === 200) {
      return body.data as never
    }
    showToast(body.message || '请求失败', 'error')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    const status: number | undefined = error.response?.status
    const body = error.response?.data as ApiResponse<unknown> | undefined

    if (status === 401) {
      // 未认证 / 令牌失效：清登录态，跳登录页并记住来处
      localStorage.removeItem('security.token')
      localStorage.removeItem('security.tokenType')
      localStorage.removeItem('security.user')
      if (!window.location.pathname.startsWith('/login')) {
        const redirect = encodeURIComponent(
          window.location.pathname + window.location.search,
        )
        showToast('登录已失效，请重新登录', 'error')
        window.location.assign(`/login?redirect=${redirect}`)
      } else {
        showToast(body?.message || '请先登录', 'error')
      }
    } else if (status === 403) {
      // 越权：只提示，人留在原地
      showToast(body?.message || '无权访问', 'error')
    } else {
      showToast(body?.message || '网络错误，请稍后重试', 'error')
    }
    return Promise.reject(error)
  },
)

export default request
