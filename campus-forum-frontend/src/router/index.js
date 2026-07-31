import { createRouter, createWebHistory } from 'vue-router'

// 路由表（占位，后续按业务模块补充）
const routes = []

// 创建路由实例，使用 history 模式
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

export default router
