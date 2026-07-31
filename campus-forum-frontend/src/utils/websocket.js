import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

// STOMP 客户端实例
let stompClient = null
// 当前订阅，用于断开时清理
let subscription = null

/**
 * 根据环境构造 SockJS 握手地址
 * 开发环境：直连后端 8080 端口，路径 /api/ws/chat
 * 生产环境：走当前域名经 nginx 代理，路径 /api/ws/chat
 */
function buildSocketUrl() {
  if (import.meta.env.DEV) {
    return `http://${window.location.hostname}:8080/api/ws/chat`
  }
  return `${window.location.protocol}//${window.location.host}/api/ws/chat`
}

/**
 * 建立 STOMP 连接并订阅私信队列
 * @param {string} token 登录令牌，用于 CONNECT 帧 Authorization 头
 * @param {(message: object) => void} onMessage 收到新消息时的回调
 */
export function connect(token, onMessage) {
  // 已存在连接则先断开，避免重复连接
  if (stompClient && stompClient.active) {
    disconnect()
  }

  stompClient = new Client({
    // 使用 SockJS 作为底层传输
    webSocketFactory: () => new SockJS(buildSocketUrl()),
    // CONNECT 帧携带鉴权头
    connectHeaders: {
      Authorization: token ? `Bearer ${token}` : ''
    },
    // 调试日志：开发环境打印，生产环境关闭
    debug: (msg) => {
      if (import.meta.env.DEV) {
        // eslint-disable-next-line no-console
        console.log('[STOMP]', msg)
      }
    },
    reconnectDelay: 5000, // 断线自动重连间隔
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000
  })

  // 连接成功后订阅用户私信队列
  stompClient.onConnect = () => {
    subscription = stompClient.subscribe('/user/queue/chat', (frame) => {
      try {
        const payload = JSON.parse(frame.body)
        onMessage && onMessage(payload)
      } catch (e) {
        // 解析失败时原样回传，避免阻塞
        onMessage && onMessage(frame.body)
      }
    })
  }

  // 连接异常处理
  stompClient.onStompError = (frame) => {
    // eslint-disable-next-line no-console
    console.error('[STOMP] 协议错误:', frame.headers['message'] || frame)
  }
  stompClient.onWebSocketError = (event) => {
    // eslint-disable-next-line no-console
    console.error('[STOMP] WebSocket 错误:', event)
  }

  // 激活连接
  stompClient.activate()
}

/**
 * 断开 STOMP 连接并清理订阅
 */
export function disconnect() {
  if (subscription) {
    try {
      subscription.unsubscribe()
    } catch (e) {
      // 忽略取消订阅异常
    }
    subscription = null
  }
  if (stompClient) {
    try {
      stompClient.deactivate()
    } catch (e) {
      // 忽略断开异常
    }
    stompClient = null
  }
}
