import request from './request'
import type {
  ApiResponse,
  CreateUserRequest,
  LoginResponse,
  ReviewTaskRequest,
  SubmitTaskRequest,
  Task,
  UserProfile,
} from '../types/api'

/** 十一个接口一一对应，路径与 contract.md 保持一致 */
export const api = {
  // 认证
  login(payload: { username: string; password: string }): Promise<LoginResponse> {
    return request.post('/auth/login', payload)
  },
  me(): Promise<UserProfile> {
    return request.get('/auth/me')
  },

  // 公开
  ping(): Promise<{ status: string }> {
    return request.get('/public/ping')
  },

  // 任务
  submitTask(payload: SubmitTaskRequest): Promise<Task> {
    return request.post('/api/tasks', payload)
  },
  listMine(): Promise<Task[]> {
    return request.get('/api/tasks/mine')
  },
  getTask(id: number): Promise<Task> {
    return request.get(`/api/tasks/${id}`)
  },
  listAllTasks(): Promise<Task[]> {
    return request.get('/api/tasks')
  },
  reviewTask(id: number, payload: ReviewTaskRequest): Promise<Task> {
    return request.post(`/api/tasks/${id}/review`, payload)
  },

  // 账号管理
  listUsers(): Promise<UserProfile[]> {
    return request.get('/admin/users')
  },
  createUser(payload: CreateUserRequest): Promise<UserProfile> {
    return request.post('/admin/users', payload)
  },
  deleteUser(username: string): Promise<void> {
    return request.delete(`/admin/users/${username}`) as Promise<void>
  },
  setUserStatus(username: string, enabled: boolean): Promise<UserProfile> {
    return request.patch(`/admin/users/${username}/status`, { enabled })
  },
}

export type { ApiResponse }
