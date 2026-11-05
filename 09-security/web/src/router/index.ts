import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '../store/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/tasks' },
    { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
    { path: '/tasks', name: 'tasks', component: () => import('../views/TaskList.vue') },
    {
      path: '/review',
      name: 'review',
      component: () => import('../views/ReviewList.vue'),
      meta: { requiresAuth: true, authorities: ['task:review'] },
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('../views/Users.vue'),
      meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    { path: '/:pathMatch(.*)*', redirect: '/tasks' },
  ],
})

/** 路由守卫：未登录进不去业务页，权限不够进不去别人那页 */
router.beforeEach((to) => {
  const auth = useAuth()

  if (to.path === '/login') {
    return true
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn.value) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  const needRoles = to.meta.roles as string[] | undefined
  if (needRoles && !needRoles.some((r) => auth.state.user?.roles.includes(r))) {
    return { path: '/tasks' }
  }
  const needAuthorities = to.meta.authorities as string[] | undefined
  if (needAuthorities && !needAuthorities.some((a) => auth.state.user?.authorities.includes(a))) {
    return { path: '/tasks' }
  }
  return true
})

export default router
