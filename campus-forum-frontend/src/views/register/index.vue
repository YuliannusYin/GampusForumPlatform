<template>
  <div class="register-page">
    <el-card class="register-card" shadow="always">
      <!-- 标题 -->
      <div class="register-header">
        <h2 class="register-title">注册账号</h2>
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
    // 注册成功后跳转登录页
    router.push('/login')
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示，此处仅关闭 loading
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
}

.register-header {
  text-align: center;
  margin-bottom: 24px;
}

.register-title {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.register-subtitle {
  font-size: 14px;
  color: #909399;
}

.register-button {
  width: 100%;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #606266;
  margin-top: 8px;
}

.login-link {
  color: #409eff;
  margin-left: 4px;
}

.login-link:hover {
  color: #66b1ff;
}
</style>
