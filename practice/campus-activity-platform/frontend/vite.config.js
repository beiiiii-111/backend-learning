import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 代理解决跨域：前端请求 /api 转发到后端 8080
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
