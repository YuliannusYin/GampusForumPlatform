<template>
  <div class="not-found">
    <div class="error-content fade-in-up">
      <!-- 友好插图 -->
      <svg class="error-illustration" width="200" height="200" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
        <!-- 背景圆 -->
        <circle cx="100" cy="100" r="90" fill="var(--color-primary-bg)" />
        <circle cx="100" cy="100" r="70" fill="var(--color-bg-card)" opacity="0.6" />
        <!-- 地球/星球图案 -->
        <ellipse cx="100" cy="100" rx="45" ry="45" fill="var(--color-primary)" opacity="0.1" />
        <ellipse cx="100" cy="100" rx="45" ry="20" fill="none" stroke="var(--color-primary)" stroke-width="2" opacity="0.2" />
        <ellipse cx="100" cy="100" rx="20" ry="45" fill="none" stroke="var(--color-primary)" stroke-width="2" opacity="0.2" />
        <!-- 迷路的小人 -->
        <circle cx="100" cy="78" r="14" fill="var(--color-primary)" opacity="0.15" />
        <path d="M86 110 C86 96 92 92 100 92 C108 92 114 96 114 110" stroke="var(--color-primary)" stroke-width="3.5" stroke-linecap="round" fill="none" opacity="0.3" />
        <!-- 问号气泡 -->
        <path d="M140 55 C140 48 145 44 151 44 L170 44 C176 44 181 48 181 55 L181 68 C181 75 176 79 170 79 L160 79 L152 86 L152 79 C145 79 140 75 140 68 Z" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="2.5" />
        <circle cx="155" cy="60" r="2.5" fill="var(--color-primary)" />
        <path d="M163 60 L163 64 M163 67 L163 67.5" stroke="var(--color-primary)" stroke-width="2.5" stroke-linecap="round" />
        <!-- 装饰小圆点 -->
        <circle cx="40" cy="50" r="6" fill="var(--color-primary)" opacity="0.15" />
        <circle cx="170" cy="140" r="5" fill="var(--color-primary)" opacity="0.1" />
        <circle cx="35" cy="150" r="4" fill="var(--color-primary)" opacity="0.1" />
        <circle cx="155" cy="165" r="3" fill="var(--color-primary)" opacity="0.15" />
      </svg>

      <!-- 大号渐变 404 文字 -->
      <h1 class="error-code">404</h1>

      <!-- 提示文案 -->
      <h2 class="error-title">页面走丢了</h2>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移动</p>

      <!-- 返回首页按钮 -->
      <el-button type="primary" size="large" round @click="goHome" class="home-btn">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回首页
const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--color-bg-page);
  padding: var(--space-6);
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 480px;
}

.error-illustration {
  margin-bottom: var(--space-2);
}

/* 大号渐变 404 */
.error-code {
  font-size: 120px;
  font-weight: var(--font-weight-extrabold);
  line-height: 1;
  margin: 0 0 var(--space-4) 0;
  font-family: var(--font-display);
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: -4px;
  text-shadow: none;
  filter: drop-shadow(0 8px 24px rgba(22, 100, 255, 0.2));
}

.error-title {
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0 0 var(--space-2) 0;
  font-family: var(--font-heading);
}

.error-desc {
  font-size: var(--font-size-body);
  color: var(--color-text-3);
  margin: 0 0 var(--space-8) 0;
  line-height: var(--line-height-normal);
}

.home-btn {
  padding: 0 var(--space-8);
  height: 48px;
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semibold);
}

/* 响应式 */
@media (max-width: 768px) {
  .error-code {
    font-size: 80px;
    letter-spacing: -2px;
  }

  .error-title {
    font-size: var(--font-size-h1);
  }

  .error-desc {
    font-size: var(--font-size-sm);
  }

  .error-illustration {
    width: 150px;
    height: 150px;
  }
}
</style>
