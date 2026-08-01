<template>
  <div class="register-page">
    <!-- 左侧品牌展示区 -->
    <aside class="reg-aside">
      <div class="aside-blob aside-blob-1"></div>
      <div class="aside-blob aside-blob-2"></div>
      <div class="aside-inner">
        <div class="aside-brand">
          <div class="brand-logo-lg">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <span class="brand-name-lg">校园论坛</span>
        </div>
        <h2 class="aside-headline">开启你的<br />校园新旅程</h2>
        <p class="aside-sub">注册账号，记录校园点滴，加入兴趣圈子，和同学们一起分享、交流、成长。</p>
        <ul class="aside-features">
          <li><el-icon><EditPen /></el-icon> 自由发帖，记录校园生活</li>
          <li><el-icon><Connection /></el-icon> 兴趣圈子，结识同好伙伴</li>
          <li><el-icon><Medal /></el-icon> 签到成长，积累专属积分</li>
        </ul>
      </div>
    </aside>

    <!-- 右侧表单区 -->
    <main class="reg-main">
      <el-card class="register-card fade-in-up" shadow="always">
        <!-- 品牌标识 -->
        <div class="brand-row">
          <div class="brand-logo">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <span class="brand-name">校园论坛</span>
        </div>

        <!-- 标题 -->
        <div class="register-header">
          <h2 class="register-title">创建账号</h2>
          <p class="register-subtitle">加入校园论坛，分享你的精彩</p>
        </div>

        <!-- 注册表单 -->
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          size="large"
          label-position="top"
          @keyup.enter="handleRegister"
        >
          <!-- 用户名 -->
          <el-form-item prop="username" label="用户名">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              clearable
              :prefix-icon="User"
            />
          </el-form-item>

          <!-- 邮箱 -->
          <el-form-item prop="email" label="邮箱">
            <el-input
              v-model="registerForm.email"
              placeholder="请输入邮箱"
              clearable
              :prefix-icon="Message"
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item prop="password" label="密码">
            <el-input
              v-model="registerForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item prop="confirmPassword" label="确认密码">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <!-- 注册按钮 -->
          <el-form-item>
            <el-button
              type="primary"
              class="register-button"
              :loading="loading"
              @click="handleRegister"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部登录链接 -->
        <div class="register-footer">
          已有账号？
          <router-link to="/login" class="login-link">去登录</router-link>
        </div>
      </el-card>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { register } from '@/api/auth'

const router = useRouter()

// 表单引用
const registerFormRef = ref(null)
// 提交加载状态
const loading = ref(false)

// 注册表单数据
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

// 确认密码自定义校验
const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 表单校验规则
const registerRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需为 3-20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: ['blur', 'change'] }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度需为 6-20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    // 校验表单
    await registerFormRef.value.validate()
  } catch {
    // 校验不通过，直接返回
    return
  }

  loading.value = true
  try {
    // 调用注册接口
    await register({
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password
    })

    ElMessage.success('注册成功')
    // 注册成功后回到首页
    router.push('/home')
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示，此处仅关闭 loading
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg-page);
}

/* ===== 左侧品牌展示区 ===== */
.reg-aside {
  position: relative;
  flex: 1 1 48%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
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
  left: -100px;
}

.aside-blob-2 {
  width: 220px;
  height: 220px;
  bottom: -80px;
  right: -60px;
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
.reg-main {
  flex: 1 1 52%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}

.register-card {
  width: 100%;
  max-width: 420px;
  border-radius: var(--radius-2xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-3);
}

.register-card :deep(.el-card__body) {
  padding: var(--space-8) var(--space-8) var(--space-6);
}

.brand-row {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
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

.register-header {
  text-align: center;
  margin-bottom: var(--space-5);
}

.register-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0 0 var(--space-2);
}

.register-subtitle {
  font-size: var(--font-size-body);
  color: var(--color-text-3);
  margin: 0;
}

.register-button {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-lg);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
}

.register-footer {
  text-align: center;
  font-size: var(--font-size-body);
  color: var(--color-text-2);
  margin-top: var(--space-2);
}

.login-link {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
  margin-left: var(--space-1);
  transition: color var(--transition-fast);
}

.login-link:hover {
  color: var(--color-primary-hover);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .reg-aside {
    display: none;
  }
  .reg-main {
    flex: 1;
    padding: var(--space-5);
    background: var(--gradient-primary);
  }
  .register-card {
    max-width: 100%;
  }
}
</style>
