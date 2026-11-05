/**
 * 与后端出参一一对应的类型定义
 *
 * 可空字段只有三个：score、comment、reviewedAt（待批改时为 null），
 * 写成非空类型会在待批改列表上炸出一片 typecheck 报错。
 */

/** 统一响应外壳 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

/** 登录响应 */
export interface LoginResponse {
  token: string
  tokenType: string
  expiresAt: string
  username: string
  nickname: string
  roles: string[]
}

/** 账号视图（任何响应里都不该出现 password） */
export interface UserProfile {
  username: string
  nickname: string
  roles: string[]
  authorities: string[]
  enabled: boolean
}

export type TaskStatus = 'SUBMITTED' | 'REVIEWED'

/** 任务视图 */
export interface Task {
  id: number
  title: string
  content: string
  ownerId: number
  owner: string
  ownerNickname: string
  status: TaskStatus
  statusText: string
  score: number | null
  comment: string | null
  submittedAt: string
  reviewedAt: string | null
}

export interface SubmitTaskRequest {
  title: string
  content: string
}

export interface ReviewTaskRequest {
  score: number
  comment: string
}

export interface CreateUserRequest {
  username: string
  password: string
  nickname: string
  roles: string[]
}
