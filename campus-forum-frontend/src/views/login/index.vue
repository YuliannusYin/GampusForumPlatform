<template>
  <div class="login-page">
    <!-- 返回按钮 -->
    <el-button class="back-button" :icon="ArrowLeft" circle @click="goBack" />
    <el-card class="login-card" shadow="always">
      <!-- 标题 -->
      <div class="login-header">
        <h2 class="login-title">校园论坛</h2>
        <p class="login-subtitle">欢迎回来，请登录您的账号</p>
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
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.back-button {
  position: absolute;
  top: 24px;
  left: 24px;
  z-index: 10;
}

.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-title {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #909399;
}

.login-button {
  width: 100%;
}

.login-footer {
  text-align: center;
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

.register-link {
  color: #409eff;
  margin-left: 4px;
}

.register-link:hover {
  color: #66b1ff;
}
</style>
