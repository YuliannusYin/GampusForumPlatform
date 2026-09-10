import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

// 路由表
const routes = [
  // 登录
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  // 注册
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/register/index.vue'),
    meta: { title: '注册' }
  },
  // 主站点布局
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/home/index.vue'), meta: { title: '首页' } },
      { path: 'wall', name: 'Wall', component: () => import('@/views/wall/index.vue'), meta: { title: '表白墙' } },
      { path: 'section/:id', name: 'Section', component: () => import('@/views/section/index.vue'), meta: { title: '板块' } },
      { path: 'post/:id', name: 'PostDetail', component: () => import('@/views/post/detail.vue'), meta: { title: '帖子详情' } },
      { path: 'post/create', name: 'PostCreate', component: () => import('@/views/post/edit.vue'), meta: { title: '发帖', requiresAuth: true } },
      { path: 'post/edit/:id', name: 'PostEdit', component: () => import('@/views/post/edit.vue'), meta: { title: '编辑帖子', requiresAuth: true } },
      { path: 'user', name: 'UserProfile', component: () => import('@/views/user/profile.vue'), meta: { title: '个人中心', requiresAuth: true } },
      { path: 'user/posts', name: 'UserPosts', component: () => import('@/views/user/posts.vue'), meta: { title: '我的发帖', requiresAuth: true } },
      { path: 'user/favorites', name: 'UserFavorites', component: () => import('@/views/user/favorites.vue'), meta: { title: '我的收藏', requiresAuth: true } },
      { path: 'message/notification', name: 'Notification', component: () => import('@/views/message/notification.vue'), meta: { title: '通知', requiresAuth: true } },
      { path: 'message/chat', name: 'Chat', component: () => import('@/views/message/chat.vue'), meta: { title: '私信', requiresAuth: true } },
      { path: 'club', name: 'ClubList', component: () => import('@/views/club/index.vue'), meta: { title: '社团' } },
      { path: 'club/:id', name: 'ClubDetail', component: () => import('@/views/club/detail.vue'), meta: { title: '社团详情' } },
      { path: 'user/settings', name: 'UserSettings', component: () => import('@/views/user/settings.vue'), meta: { title: '设置', requiresAuth: true } },
      { path: 'user/:id', name: 'PublicProfile', component: () => import('@/views/user/public-profile.vue'), meta: { title: '用户主页' } },
      { path: 'search', name: 'Search', component: () => import('@/views/search/index.vue'), meta: { title: '搜索' } }
    ]
  },
  // 后台管理布局
  {
    path: '/admin',
    component: () => import('@/views/admin/layout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/dashboard.vue'), meta: { title: '数据看板', requiresAdmin: true } },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/users.vue'), meta: { title: '用户管理', requiresAdmin: true } },
      { path: 'posts', name: 'AdminPosts', component: () => import('@/views/admin/posts.vue'), meta: { title: '帖子管理', requiresAdmin: true } },
      { path: 'sections', name: 'AdminSections', component: () => import('@/views/admin/sections.vue'), meta: { title: '板块管理', requiresAdmin: true } },
      { path: 'tags', name: 'AdminTags', component: () => import('@/views/admin/tags.vue'), meta: { title: '标签管理', requiresAdmin: true } },
      { path: 'clubs', name: 'AdminClubs', component: () => import('@/views/admin/clubs.vue'), meta: { title: '社团审核', requiresAdmin: true } },
      { path: 'reports', name: 'AdminReports', component: () => import('@/views/admin/reports.vue'), meta: { title: '举报处理', requiresAdmin: true } },
      { path: 'test-data', name: 'AdminTestData', component: () => import('@/views/admin/test-data.vue'), meta: { title: '测试数据', requiresAdmin: true, requiresSuperAdmin: true } }
    ]
  },
  // 404 兜底
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' }
  }
]

// 创建路由实例，使用 history 模式
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    // 路由切换时回到顶部
    return { top: 0 }
  }
})

// 全局前置守卫：处理标题、登录校验、权限校验
router.beforeEach((to, from, next) => {
  // 设置文档标题
  document.title = to.meta.title ? `${to.meta.title} - 校园论坛` : '校园论坛'

  // 在守卫内部调用 useUserStore，确保 pinia 已初始化
  const userStore = useUserStore()

  // 需要登录但未登录：提示并回到首页（不主动跳转登录页）
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    ElMessage.warning('该页面需要登录后才能访问')
    next('/')
    return
  }

  // 需要管理员权限但非管理员：提示并回到首页
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage.error('无权限访问')
    next('/')
    return
  }

  // 需要超级管理员权限但非超级管理员：提示并回到首页
  if (to.meta.requiresSuperAdmin && !userStore.isSuperAdmin) {
    ElMessage.error('无权限访问')
    next('/')
    return
  }

  next()
})

export default router
