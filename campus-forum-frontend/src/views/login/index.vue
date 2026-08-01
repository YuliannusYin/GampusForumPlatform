<template>
  <div class="login-page">
    <!-- 返回按钮 -->
    <el-button class="back-button" :icon="ArrowLeft" circle @click="goBack" />

    <!-- 左侧品牌展示区 -->
    <aside class="login-aside">
      <div class="aside-blob aside-blob-1"></div>
      <div class="aside-blob aside-blob-2"></div>
      <div class="aside-inner">
        <div class="aside-brand">
          <div class="brand-logo-lg">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <span class="brand-name-lg">校园论坛</span>
        </div>
        <h2 class="aside-headline">连接每一个<br />校园声音</h2>
        <p class="aside-sub">分享你的校园生活与学习心得，结识志同道合的伙伴，共建温暖有活力的校园社区。</p>
        <ul class="aside-features">
          <li><el-icon><ChatLineRound /></el-icon> 实时交流，畅聊校园趣事</li>
          <li><el-icon><TrendCharts /></el-icon> 热门话题，紧跟校园动态</li>
          <li><el-icon><Medal /></el-icon> 签到成长，解锁更多权益</li>
        </ul>
      </div>
    </aside>

    <!-- 右侧表单区 -->
    <main class="login-main">
      <el-card class="login-card fade-in-up" shadow="always">
        <!-- 品牌标识 -->
        <div class="brand-row">
          <div class="brand-logo">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <span class="brand-name">校园论坛</span>
        </div>

        <!-- 标题 -->
        <div class="login-header">
          <h2 class="login-title">欢迎回来</h2>
          <p class="login-subtitle">登录账号，继续你的校园之旅</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          size="large"
          label-position="top"
          @keyup.enter="handleLogin"
        >
          <!-- 账号 -->
          <el-form-item prop="account" label="账号">
            <el-input
              v-model="loginForm.account"
              placeholder="用户名或邮箱"
              clearable
              :prefix-icon="User"
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password" label="密码">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="密码"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <!-- 登录按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部注册链接 -->
        <div class="login-footer">
          还没有账号？
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </el-card>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 返回上一页；无历史记录则回首页
const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/home')
  }
}

// 表单引用
const loginFormRef = ref(null)
// 提交加载状态
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  account: '',
  password: ''
})

// 表单校验规则
const loginRules = reactive({
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
})

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    // 校验表单
    await loginFormRef.value.validate()
  } catch {
    // 校验不通过，直接返回
    return
  }

  loading.value = true
  try {
    // 调用登录接口
    const data = await login({
      account: loginForm.account,
      password: loginForm.password
    })

    // 存储访问令牌与用户信息
    userStore.setToken(data.accessToken)
    userStore.setUserInfo(data.userInfo)
    // 刷新令牌单独存入 localStorage
    localStorage.setItem('campus_refresh_token', data.refreshToken)

    ElMessage.success('登录成功')

    // 跳转到 redirect 地址或首页
    const redirect = route.query.redirect || '/home'
    router.push(redirect)
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示，此处仅关闭 loading
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg-page);
}

/* ===== 左侧品牌展示区 ===== */
.login-aside {
  position: relative;
  flex: 1 1 48%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-ocean);
  overflow: hidden;
  padding: var(--space-12);
}

.aside-blob {
  position: absolute;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.12);
  pointer-events: none;
}

.aside-blob-1 {
  width: 360px;
  height: 360px;
  top: -120px;
  right: -100px;
}

.aside-blob-2 {
  width: 220px;
  height: 220px;
  bottom: -80px;
  left: -60px;
  background: rgba(255, 255, 255, 0.08);
}

.aside-inner {
  position: relative;
  z-index: 1;
  color: #fff;
  max-width: 420px;
}

.aside-brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-10);
}

.brand-logo-lg {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-logo-lg .el-icon {
  font-size: 26px;
  color: #fff;
}

.brand-name-lg {
  font-family: var(--font-display);
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
}

.aside-headline {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: var(--font-weight-extrabold);
  line-height: 1.2;
  margin: 0 0 var(--space-4);
}

.aside-sub {
  font-size: var(--font-size-body);
  line-height: var(--line-height-relaxed);
  opacity: 0.92;
  margin: 0 0 var(--space-8);
}

.aside-features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.aside-features li {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-body);
  opacity: 0.95;
}

.aside-features .el-icon {
  font-size: 17px;
}

/* ===== 右侧表单区 ===== */
.login-main {
  flex: 1 1 52%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: var(--radius-2xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-3);
}

.login-card :deep(.el-card__body) {
  padding: var(--space-8) var(--space-8) var(--space-6);
}

.brand-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-bottom: var(--space-5);
}

.brand-logo {
  width: 34px;
  height: 34px;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary);
}

.brand-logo .el-icon {
  font-size: 20px;
  color: #fff;
}

.brand-name {
  font-family: var(--font-display);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.login-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0 0 var(--space-2);
}

.login-subtitle {
  font-size: var(--font-size-body);
  color: var(--color-text-3);
  margin: 0;
}

.login-button {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
}

.login-footer {
  text-align: center;
  font-size: var(--font-size-body);
  color: var(--color-text-2);
  margin-top: var(--space-2);
}

.register-link {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  margin-left: var(--space-1);
  transition: color var(--transition-fast);
}

.register-link:hover {
  color: var(--color-primary-hover);
}

/* ===== 返回按钮 ===== */
.back-button {
  position: absolute;
  top: var(--space-5);
  left: var(--space-5);
  z-index: 20;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .login-aside {
    display: none;
  }
  .login-main {
    flex: 1;
    padding: var(--space-5);
    background: var(--gradient-ocean);
  }
  .login-card {
    max-width: 100%;
  }
  .back-button {
    background: rgba(255, 255, 255, 0.9);
  }
}
</style>
