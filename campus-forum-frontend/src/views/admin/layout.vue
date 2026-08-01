<template>
  <el-container class="admin-layout">
    <!-- 侧边菜单 -->
    <el-aside width="210px" class="admin-aside">
      <div class="admin-logo">
        <el-icon class="logo-icon"><Setting /></el-icon>
        <span class="logo-text">论坛后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="admin-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/posts">
          <el-icon><Document /></el-icon>
          <span>帖子管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/sections">
          <el-icon><Files /></el-icon>
          <span>板块管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon><PriceTag /></el-icon>
          <span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/clubs">
          <el-icon><User /></el-icon>
          <span>社团审核</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="admin-main-container">
      <!-- 顶部 header -->
      <el-header class="admin-header">
        <div class="header-title">管理后台</div>
        <div class="header-actions">
          <el-link type="primary" :underline="false" @click="goHome">
            <el-icon><Back /></el-icon>
            返回前台
          </el-link>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

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
}

/* 侧边栏 */
.admin-aside {
  background-color: #304156;
  overflow-y: auto;
}

.admin-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 60px;
  color: #fff;
  background-color: #2b3a4d;
}

.logo-icon {
  font-size: 22px;
  color: #409eff;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 菜单：暗色主题 */
.admin-menu {
  border-right: none;
  background-color: #304156;
}

.admin-menu .el-menu-item {
  color: #bfcbd9;
}

.admin-menu .el-menu-item:hover {
  background-color: #263445;
  color: #fff;
}

.admin-menu .el-menu-item.is-active {
  background-color: #1f2d3d;
  color: #409eff;
}

/* 主体容器 */
.admin-main-container {
  display: flex;
  flex-direction: column;
}

/* 顶部 header */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 主内容区 */
.admin-main {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}
</style>
