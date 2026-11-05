import { reactive } from 'vue'

/**
 * 全局轻提示。越权（403）只弹提示、人留在原地，与 401 跳登录是两个分支。
 */
interface ToastState {
  message: string
  visible: boolean
  type: 'info' | 'error'
}

const state = reactive<ToastState>({ message: '', visible: false, type: 'info' })

let timer: ReturnType<typeof setTimeout> | null = null

export function showToast(message: string, type: 'info' | 'error' = 'info'): void {
  state.message = message
  state.type = type
  state.visible = true
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    state.visible = false
  }, 3000)
}

export function useToast() {
  return { state, close: () => (state.visible = false) }
}
