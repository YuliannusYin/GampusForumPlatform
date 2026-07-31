import { defineStore } from 'pinia'
import { ref } from 'vue'

// 用户状态管理（占位，后续补充登录、登出、用户信息等逻辑）
export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref({})
  // 访问令牌
  const token = ref('')

  // 设置令牌
  const setToken = (newToken) => {
    token.value = newToken
  }

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
  }

  // 清除用户信息
  const clearUser = () => {
    token.value = ''
    userInfo.value = {}
  }

  return {
    userInfo,
    token,
    setToken,
    setUserInfo,
    clearUser
  }
})
