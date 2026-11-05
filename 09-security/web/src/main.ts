import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { useAuth } from './store/auth'
import './style.css'

// 先恢复登录态再挂载，避免按未登录渲染一闪
const app = createApp(App)
app.use(router)
useAuth().restore()
app.mount('#app')
