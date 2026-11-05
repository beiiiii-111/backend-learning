import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 三组后端路径前缀都要配代理，漏一条请求就会被 Vite 自己接住返回 index.html，
// 页面上表现为"没有数据"但控制台看不到接口错误
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/auth': 'http://127.0.0.1:8009',
      '/api': 'http://127.0.0.1:8009',
      '/admin': 'http://127.0.0.1:8009',
    },
  },
})
