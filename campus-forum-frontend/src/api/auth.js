import request from '@/utils/request'

// 用户注册
export function register(data) {
  return request.post('/auth/register', data)
}

// 用户登录（account 为用户名或邮箱）
export function login(data) {
  return request.post('/auth/login', data)
}

// 刷新令牌
export function refreshToken(data) {
  return request.post('/auth/refresh', data)
}

// 退出登录
export function logout() {
  return request.post('/auth/logout')
}
