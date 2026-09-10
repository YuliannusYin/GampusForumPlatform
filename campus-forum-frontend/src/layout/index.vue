<template>
  <div class="layout-container">
    <!-- 顶部导航 -->
    <header class="layout-header">
      <div class="header-left" @click="goHome">
        <div class="logo-mark">
          <svg viewBox="0 0 32 32" width="28" height="28" fill="none">
            <rect width="32" height="32" rx="8" fill="url(#logo-grad)" />
            <path d="M9 11.5C9 10.1 10.1 9 11.5 9h9C21.9 9 23 10.1 23 11.5v5c0 1.4-1.1 2.5-2.5 2.5H16l-4 3.5V19h-.5C10.1 19 9 17.9 9 16.5v-5z" fill="white" fill-opacity="0.95"/>
            <defs>
              <linearGradient id="logo-grad" x1="0" y1="0" x2="32" y2="32">
                <stop stop-color="#1664FF"/>
                <stop offset="1" stop-color="#0055FF"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <span class="logo-text">校园论坛</span>
      </div>

      <div class="header-center">
        <div class="search-wrapper">
          <el-icon class="search-icon"><Search /></el-icon>
          <input
            v-model="keyword"
            class="search-input"
            placeholder="搜索帖子、话题、用户…"
            @keyup.enter="handleSearch"
          />
          <kbd class="search-kbd" v-if="!keyword">Enter</kbd>
        </div>
      </div>

      <div class="header-right">
        <template v-if="!userStore.isLoggedIn">
          <el-button text @click="goLogin">登录</el-button>
          <el-button type="primary" @click="goRegister">注册</el-button>
        </template>

        <template v-else>
          <div class="header-action" @click="goNotification">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0">
              <div class="action-btn">
                <el-icon :size="20"><Bell /></el-icon>
              </div>
            </el-badge>
          </div>

          <el-button type="primary" :icon="EditPen" class="create-btn" @click="goCreatePost">发帖</el-button>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar :size="34" :src="avatar" class="user-avatar">{{ usernameInitial }}</el-avatar>
              <span class="username hide-mobile">{{ displayName }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="posts">
                  <el-icon><Document /></el-icon>
                  我的发帖
                </el-dropdown-item>
                <el-dropdown-item command="favorites">
                  <el-icon><Star /></el-icon>
                  我的收藏
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin" command="admin" divided>
                  <el-icon><Monitor /></el-icon>
                  后台管理
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </div>
    </header>

    <div class="layout-body">
      <!-- 侧边栏 -->
      <aside class="layout-aside" :class="{ collapsed: sidebarCollapsed }">
        <nav class="sidebar-nav">
          <div class="nav-group">
            <div class="nav-group-label">导航</div>
            <div
              class="nav-item"
              :class="{ active: activeSection === 'all' }"
              @click="goAllSections"
            >
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </div>
            <div
              class="nav-item"
              :class="{ active: route.name === 'ClubList' || route.name === 'ClubDetail' }"
              @click="goClubs"
            >
              <el-icon><UserFilled /></el-icon>
              <span>社团</span>
            </div>
            <div
              class="nav-item"
              :class="{ active: route.name === 'Wall' }"
              @click="goWall"
            >
              <el-icon><ChatDotRound /></el-icon>
              <span>表白墙</span>
            </div>
          </div>

          <div class="nav-group" v-if="sections.length">
            <div class="nav-group-label">板块</div>
            <div
              v-for="section in sections"
              :key="section.id"
              class="nav-item"
              :class="{ active: String(section.id) === activeSection }"
              @click="handleSectionSelect(section)"
            >
              <span class="nav-dot" :style="{ background: getSectionColor(section.id) }"></span>
              <span class="nav-text">{{ section.name }}</span>
            </div>
          </div>
        </nav>
      </aside>

      <!-- 主内容区 -->
      <main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  Search, Bell, EditPen, ArrowDown, User, Document, Star,
  Setting, Monitor, SwitchButton, HomeFilled, UserFilled, ChatDotRound
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { getSections } from '@/api/post'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const keyword = ref('')
const unreadCount = ref(0)
const sections = ref([])
const sidebarCollapsed = ref(false)

const activeSection = computed(() => {
  if (route.name === 'Wall') {
    const wall = sections.value.find((item) => item.code === 'confession')
    return wall ? String(wall.id) : 'wall'
  }
  if (route.name === 'Section' && route.params.id) {
    return String(route.params.id)
  }
  return 'all'
})

const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '游客')
const avatar = computed(() => userStore.userInfo?.avatar || '')
const usernameInitial = computed(() => {
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || ''
  return name ? name.charAt(0).toUpperCase() : ''
})

// 板块配色
const sectionColors = ['#1664FF', '#2A814B', '#BD7E00', '#D7312A', '#6E4C9F', '#24758E', '#E8624E', '#387BFF']
const getSectionColor = (id) => sectionColors[(id - 1) % sectionColors.length] || '#1664FF'

const goHome = () => router.push('/home')
const goAllSections = () => router.push('/home')
const handleSearch = () => {
  if (keyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: keyword.value } })
  }
}
const goNotification = () => router.push('/message/notification')
const goCreatePost = () => router.push('/post/create')
const goClubs = () => router.push('/club')
const goWall = () => router.push('/wall')
const goLogin = () => router.push('/login')
const goRegister = () => router.push('/register')

const handleSectionSelect = (section) => {
  if (section?.code === 'confession') {
    router.push('/wall')
    return
  }
  router.push(`/section/${section.id}`)
}

const fetchSections = async () => {
  try {
    const list = await getSections()
    sections.value = list || []
  } catch (err) {
    sections.value = []
  }
}

onMounted(() => {
  fetchSections()
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/user'); break
    case 'posts': router.push('/user/posts'); break
    case 'favorites': router.push('/user/favorites'); break
    case 'settings': router.push('/user/settings'); break
    case 'admin': router.push('/admin'); break
    case 'logout': handleLogout(); break
  }
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/home')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-page);
}

/* ===== Header ===== */
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  padding: 0 var(--space-6);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
  position: sticky;
  top: 0;
  z-index: 100;
  gap: var(--space-4);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.logo-mark {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--transition-base);
}

.header-left:hover .logo-mark {
  transform: scale(1.05) rotate(-3deg);
}

.logo-text {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  letter-spacing: -0.3px;
}

/* Search */
.header-center {
  flex: 1;
  max-width: 440px;
}

.search-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-3);
  height: 38px;
  background: var(--color-bg-page);
  border-radius: var(--radius-full);
  border: 1px solid transparent;
  transition: all var(--transition-base);
}

.search-wrapper:focus-within {
  background: #fff;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(22, 100, 255, 0.1);
}

.search-icon {
  color: var(--color-text-3);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: var(--font-size-sm);
  color: var(--color-text-1);
  font-family: var(--font-body);
}

.search-input::placeholder {
  color: var(--color-text-3);
}

.search-kbd {
  font-size: 10px;
  font-family: var(--font-mono);
  color: var(--color-text-4);
  background: var(--color-bg-card);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

/* Right actions */
.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.header-action {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  color: var(--color-text-2);
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.create-btn {
  height: 36px;
  padding: 0 var(--space-4);
  border-radius: var(--radius-full);
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  cursor: pointer;
  padding: 3px var(--space-2) 3px 3px;
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
  outline: none;
}

.user-dropdown:hover {
  background: var(--color-bg-hover);
}

.user-avatar {
  flex-shrink: 0;
  font-weight: var(--font-weight-semibold);
}

.username {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-1);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-arrow {
  color: var(--color-text-3);
  font-size: 12px;
}

/* ===== Body Layout ===== */
.layout-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* Sidebar */
.layout-aside {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: var(--color-bg-card);
  border-right: 1px solid var(--color-border-light);
  overflow-y: auto;
  padding: var(--space-4) 0;
  transition: width var(--transition-base);
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: 0 var(--space-2);
}

.nav-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-group-label {
  font-size: var(--font-size-mini);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-3);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  padding: var(--space-2) var(--space-3);
  margin-bottom: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: 0 var(--space-3);
  height: 38px;
  border-radius: var(--radius-lg);
  color: var(--color-text-2);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
  position: relative;
}

.nav-item:hover {
  background: var(--color-bg-hover);
  color: var(--color-text-1);
}

.nav-item.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.nav-item.active::before {
  content: "";
  position: absolute;
  left: -2px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
}

.nav-item .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
  margin-left: 5px;
  margin-right: 5px;
}

.nav-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Main */
.layout-main {
  flex: 1;
  padding: var(--space-6);
  overflow-y: auto;
  background: var(--color-bg-page);
}

/* Route transition */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}

/* Responsive */
@media (max-width: 768px) {
  .layout-header {
    padding: 0 var(--space-3);
  }

  .header-center {
    max-width: none;
  }

  .logo-text {
    display: none;
  }

  .layout-aside {
    display: none;
  }

  .layout-main {
    padding: var(--space-3);
  }

  .create-btn :deep(span) {
    display: none;
  }
}
</style>
