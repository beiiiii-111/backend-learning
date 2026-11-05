import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/activities' },
    { path: '/login', component: () => import('./views/Login.vue') },
    { path: '/activities', component: () => import('./views/ActivityList.vue') },
    { path: '/activities/:id', component: () => import('./views/ActivityDetail.vue') },
    { path: '/publish', component: () => import('./views/ActivityPublish.vue') },
    { path: '/audit', component: () => import('./views/AuditCenter.vue') },
    { path: '/my-signups', component: () => import('./views/MySignups.vue') }
  ]
})
