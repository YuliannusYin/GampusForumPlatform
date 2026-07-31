import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例，统一配置 baseURL 与超时时间
const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 token
service.interceptors.request.use(
  (config) => {
    // 从 localStorage 读取访问令牌
    const token = localStorage.getItem('campus_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    // 请求发送前的错误
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理后端返回结构与 HTTP 异常
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 业务码 200 表示成功，直接返回业务数据
    if (res.code === 200) {
      return res.data
    }
    // 其它业务码：弹出错误提示并拒绝
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(res)
  },
  (error) => {
    // HTTP 层错误处理
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        // 登录已过期，清理本地凭证并跳转登录页
        localStorage.removeItem('campus_token')
        localStorage.removeItem('campus_user')
        ElMessage.error('登录已过期，请重新登录')
        // 简单跳转：避免在拦截器中直接依赖 router 实例
        window.location.href = '/login'
        return Promise.reject(error)
      }
    }
    ElMessage.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service
