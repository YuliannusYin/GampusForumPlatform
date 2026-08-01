<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="layout-header">
      <div class="header-left" @click="goHome">
        <el-icon class="logo-icon"><Promotion /></el-icon>
        <span class="logo-text">校园论坛</span>
      </div>

      <div class="header-center">
        <el-input
          v-model="keyword"
          placeholder="搜索帖子"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="header-right">
        <!-- 未读消息 -->
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="header-icon">
          <el-icon :size="20" @click="goNotification"><Bell /></el-icon>
        </el-badge>

        <!-- 发帖按钮 -->
        <el-button type="primary" :icon="EditPen" @click="goCreatePost">发帖</el-button>

        <!-- 用户下拉菜单 -->
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="avatar">{{ usernameInitial }}</el-avatar>
            <span class="username">{{ displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="posts">我的发帖</el-dropdown-item>
              <el-dropdown-item command="favorites">我的收藏</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
              <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>后台管理</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="layout-body">
      <!-- 侧边栏：板块导航 -->
      <el-aside width="200px" class="layout-aside">
        <el-menu :default-active="activeSection" @select="handleSectionSelect">
          <el-menu-item index="all">
            <el-icon><Menu /></el-icon>
            <span>全部板块</span>
          </el-menu-item>
          <el-menu-item index="clubs" @click="goClubs">
            <el-icon><User /></el-icon>
            <span>社团</span>
          </el-menu-item>
          <el-menu-item
            v-for="section in sections"
            :key="section.id"
            :index="String(section.id)"
          >
            <el-icon><Files /></el-icon>
            <span>{{ section.name }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getSections } from '@/api/post'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 搜索关键字
const keyword = ref('')
// 未读消息数（占位，后续接入消息接口）
const unreadCount = ref(0)
// 板块列表
const sections = ref([])
// 当前选中板块：在 /section/:id 下高亮对应板块，其余情况高亮"全部"
const activeSection = computed(() => {
  if (route.name === 'Section' && route.params.id) {
    return String(route.params.id)
  }
  return 'all'
})

// 用户昵称/用户名
const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '游客')
// 头像地址
const avatar = computed(() => userStore.userInfo?.avatar || '')
// 头像首字母占位
const usernameInitial = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  return name ? name.charAt(0).toUpperCase() : ''
})

// 返回首页
const goHome = () => {
  router.push('/home')
}

// 跳转搜索页
const handleSearch = () => {
  router.push({ path: '/search', query: { keyword: keyword.value } })
}

// 跳转通知页
const goNotification = () => {
  router.push('/message/notification')
}

// 跳转发帖页
const goCreatePost = () => {
  router.push('/post/create')
}

// 跳转社团列表页
const goClubs = () => {
  router.push('/club')
}

// 板块选择：跳转对应板块页或首页
const handleSectionSelect = (index) => {
  // 社团菜单项由 @click=goClubs 单独处理，这里直接跳过
  if (index === 'clubs') return
  if (index === 'all') {
    router.push('/home')
  } else {
    router.push(`/section/${index}`)
  }
}

// 获取板块列表
const fetchSections = async () => {
  try {
    const list = await getSections()
    sections.value = list || []
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    sections.value = []
  }
}

onMounted(() => {
  fetchSections()
})

// 下拉菜单命令处理
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/user')
      break
    case 'posts':
      router.push('/user/posts')
      break
    case 'favorites':
      router.push('/user/favorites')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'admin':
      router.push('/admin')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

/* 顶部导航 */
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.logo-icon {
  font-size: 24px;
  color: #409eff;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-center {
  flex: 1;
  max-width: 400px;
  margin: 0 24px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  outline: none;
}

.username {
  font-size: 14px;
  color: #303133;
}

/* 侧边栏 */
.layout-aside {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
  overflow-y: auto;
}

/* 菜单铺满侧边栏，移除默认右边框避免与 aside 边框重叠 */
.layout-aside .el-menu {
  border-right: none;
  height: 100%;
}

/* 主内容区 */
.layout-main {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

/* 路由切换过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式：小屏隐藏用户名 */
@media (max-width: 768px) {
  .username {
    display: none;
  }
  .header-center {
    margin: 0 12px;
  }
}
</style>
