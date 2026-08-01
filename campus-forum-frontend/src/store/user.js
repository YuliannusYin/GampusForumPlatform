import { defineStore } from 'pinia'

// 用户状态管理（选项式写法）
export const useUserStore = defineStore('user', {
  state: () => ({
    // 访问令牌：初始从 localStorage 读取
    token: localStorage.getItem('campus_token') || '',
    // 用户信息：初始从 localStorage 读取并解析
    userInfo: JSON.parse(localStorage.getItem('campus_user') || '{}')
  }),

  getters: {
    // 是否已登录（token 非空即视为已登录）
    isLoggedIn: (state) => !!state.token,
    // 是否为管理员（roles 中包含 ROLE_ADMIN 或 ROLE_SUPER_ADMIN）
    isAdmin: (state) => {
      const roles = state.userInfo?.roles || []
      return Array.isArray(roles) ? (roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SUPER_ADMIN')) : false
    },
    // 是否为超级管理员（roles 中包含 ROLE_SUPER_ADMIN）
    isSuperAdmin: (state) => {
      const roles = state.userInfo?.roles || []
      return Array.isArray(roles) ? roles.includes('ROLE_SUPER_ADMIN') : false
    }
  },

  actions: {
    // 设置令牌并持久化
    setToken(token) {
      this.token = token
      if (token) {
        localStorage.setItem('campus_token', token)
      } else {
        localStorage.removeItem('campus_token')
      }
    },

    // 设置用户信息并持久化
    setUserInfo(info) {
      this.userInfo = info || {}
      if (info) {
        localStorage.setItem('campus_user', JSON.stringify(info))
      } else {
        localStorage.removeItem('campus_user')
      }
    },

    // 退出登录：清空状态与本地存储
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('campus_token')
      localStorage.removeItem('campus_user')
    }
  }
})
