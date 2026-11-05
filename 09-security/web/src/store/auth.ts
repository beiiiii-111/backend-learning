import { reactive, computed } from 'vue'
import type { UserProfile } from '../types/api'
import { api } from '../api'
import type { LoginResponse } from '../types/api'

const TOKEN_KEY = 'security.token'
const TOKEN_TYPE_KEY = 'security.tokenType'
const USER_KEY = 'security.user'

interface AuthState {
  token: string
  tokenType: string
  user: UserProfile | null
}

/**
 * 登录态。令牌必须存 localStorage：只存内存的话刷新页面就退出登录。
 * 应用启动时（main.ts）先 restore 再挂载，避免按未登录渲染一闪。
 */
const state = reactive<AuthState>({
  token: localStorage.getItem(TOKEN_KEY) ?? '',
  tokenType: localStorage.getItem(TOKEN_TYPE_KEY) ?? 'Bearer',
  user: null,
})

try {
  const cached = localStorage.getItem(USER_KEY)
  if (cached) state.user = JSON.parse(cached) as UserProfile
} catch {
  state.user = null
}

function persist() {
  if (state.token) {
    localStorage.setItem(TOKEN_KEY, state.token)
    localStorage.setItem(TOKEN_TYPE_KEY, state.tokenType)
  } else {
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(TOKEN_TYPE_KEY)
  }
  if (state.user) {
    localStorage.setItem(USER_KEY, JSON.stringify(state.user))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

export function useAuth() {
  const isLoggedIn = computed(() => state.token.length > 0)

  /** 有批改权限才显示批改入口，按权限点控制而不是写死角色 */
  const canReview = computed(() => state.user?.authorities.includes('task:review') ?? false)
  const isAdmin = computed(() => state.user?.roles.includes('ADMIN') ?? false)
  const canSubmit = computed(() => state.user?.authorities.includes('task:submit') ?? false)

  async function login(username: string, password: string): Promise<void> {
    const data = await api.login({ username, password })
    const resp = data as LoginResponse
    state.token = resp.token
    state.tokenType = resp.tokenType
    state.user = {
      username: resp.username,
      nickname: resp.nickname,
      roles: resp.roles,
      authorities: [],
      enabled: true,
    }
    persist()
    // 角色与权限点以服务端 /auth/me 为准，登录页拿到的只是摘要
    await refresh()
  }

  async function refresh(): Promise<void> {
    if (!state.token) return
    try {
      state.user = (await api.me()) as UserProfile
      persist()
    } catch {
      // 401 由拦截器统一处理并跳登录
    }
  }

  function clear(): void {
    state.token = ''
    state.user = null
    persist()
  }

  /** 启动时恢复登录态；令牌被手改过的话，下一次请求会 401 并清空 */
  function restore(): void {
    if (state.token) void refresh()
  }

  return { state, isLoggedIn, canReview, isAdmin, canSubmit, login, refresh, clear, restore }
}
