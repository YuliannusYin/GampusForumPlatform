<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="admin-aside">
      <div class="admin-logo" @click="goHome">
        <div class="logo-badge">
          <svg viewBox="0 0 32 32" width="22" height="22" fill="none">
            <rect width="32" height="32" rx="8" fill="url(#admin-logo-grad)" />
            <path d="M9 11.5C9 10.1 10.1 9 11.5 9h9C21.9 9 23 10.1 23 11.5v5c0 1.4-1.1 2.5-2.5 2.5H16l-4 3.5V19h-.5C10.1 19 9 17.9 9 16.5v-5z" fill="white" fill-opacity="0.95"/>
            <defs>
              <linearGradient id="admin-logo-grad" x1="0" y1="0" x2="32" y2="32">
                <stop stop-color="#1664FF"/>
                <stop offset="1" stop-color="#0055FF"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <span class="logo-text">论坛后台</span>
      </div>

      <nav class="admin-nav">
        <div class="nav-group">
          <div class="nav-group-label">概览</div>
          <router-link to="/admin/dashboard" class="nav-item" :class="{ active: activeMenu === '/admin/dashboard' }">
            <el-icon><Odometer /></el-icon>
            <span class="nav-text">数据看板</span>
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-label">内容管理</div>
          <router-link to="/admin/users" class="nav-item" :class="{ active: activeMenu === '/admin/users' }">
            <el-icon><User /></el-icon>
            <span class="nav-text">用户管理</span>
          </router-link>
          <router-link to="/admin/posts" class="nav-item" :class="{ active: activeMenu === '/admin/posts' }">
            <el-icon><Document /></el-icon>
            <span class="nav-text">帖子管理</span>
          </router-link>
          <router-link to="/admin/sections" class="nav-item" :class="{ active: activeMenu === '/admin/sections' }">
            <el-icon><Files /></el-icon>
            <span class="nav-text">板块管理</span>
          </router-link>
          <router-link to="/admin/tags" class="nav-item" :class="{ active: activeMenu === '/admin/tags' }">
            <el-icon><PriceTag /></el-icon>
            <span class="nav-text">标签管理</span>
          </router-link>
        </div>

        <div class="nav-group">
          <div class="nav-group-label">审核与数据</div>
          <router-link to="/admin/clubs" class="nav-item" :class="{ active: activeMenu === '/admin/clubs' }">
            <el-icon><User /></el-icon>
            <span class="nav-text">社团审核</span>
          </router-link>
          <router-link
            v-if="userStore.isSuperAdmin"
            to="/admin/test-data"
            class="nav-item"
            :class="{ active: activeMenu === '/admin/test-data' }"
          >
            <el-icon><MagicStick /></el-icon>
            <span class="nav-text">测试数据</span>
          </router-link>
        </div>
      </nav>
    </aside>

    <div class="admin-main-container">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <div class="header-title">
            <el-icon class="title-icon"><Setting /></el-icon>
            <span>管理后台</span>
          </div>
        </div>
        <div class="header-right">
          <div class="back-link" @click="goHome">
            <el-icon><Back /></el-icon>
            <span>返回前台</span>
          </div>
          <div class="header-divider"></div>
          <div class="admin-user">
            <el-avatar :size="32" :src="userStore.userInfo?.avatar">
              {{ (userStore.userInfo?.nickname || userStore.userInfo?.username || 'A').charAt(0).toUpperCase() }}
            </el-avatar>
            <span class="admin-username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员' }}</span>
          </div>
        </div>
      </header>

      <!-- 主内容区 -->
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { MagicStick } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 当前激活菜单：使用路由路径高亮
const activeMenu = computed(() => route.path)

// 返回前台首页
const goHome = () => {
  router.push('/home')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.admin-aside {
  width: var(--sidebar-width);
  flex-shrink: 0;
  background: var(--color-bg-card);
  border-right: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  transition: width var(--transition-base);
}

.admin-logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-5);
  height: var(--header-height);
  border-bottom: 1px solid var(--color-border-light);
  cursor: pointer;
  user-select: none;
  flex-shrink: 0;
}

.logo-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--transition-base);
}

.admin-logo:hover .logo-badge {
  transform: scale(1.05) rotate(-3deg);
}

.logo-text {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  letter-spacing: -0.3px;
}

/* ===== 导航 ===== */
.admin-nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: var(--space-4) var(--space-2);
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
  text-decoration: none;
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

.nav-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 主容器 ===== */
.admin-main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ===== 顶部栏 ===== */
.admin-header {
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
  flex-shrink: 0;
  gap: var(--space-4);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-heading);
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
}

.title-icon {
  color: var(--color-primary);
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-shrink: 0;
}

.back-link {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.back-link:hover {
  background: var(--color-primary-bg);
}

.back-link .el-icon {
  font-size: 14px;
}

.header-divider {
  width: 1px;
  height: 24px;
  background: var(--color-border-light);
}

.admin-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 3px var(--space-2) 3px 3px;
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
  cursor: default;
}

.admin-user:hover {
  background: var(--color-bg-hover);
}

.admin-username {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-1);
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 主内容区 ===== */
.admin-main {
  flex: 1;
  padding: var(--space-6);
  background: var(--color-bg-page);
  overflow-y: auto;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .admin-aside {
    width: 64px;
  }

  .logo-text,
  .nav-text,
  .nav-group-label {
    display: none;
  }

  .nav-item {
    justify-content: center;
  }

  .admin-logo {
    justify-content: center;
    padding: 0;
  }

  .admin-header {
    padding: 0 var(--space-3);
  }

  .admin-username {
    display: none;
  }

  .back-link span {
    display: none;
  }

  .admin-main {
    padding: var(--space-3);
  }
}
</style>
