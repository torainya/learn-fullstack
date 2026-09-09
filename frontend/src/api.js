import axios from 'axios'

// 统一的 API 客户端：所有请求都走这里，便于加拦截器（token、错误处理等）
const api = axios.create({ baseURL: '/api' })

/** 提交一条消息 */
export function createMessage(content) {
  return api.post('/messages', { content })
}

/** 查询最新消息 */
export function fetchMessages() {
  return api.get('/messages')
}

/** 查询已消费消息数 */
export function fetchStats() {
  return api.get('/messages/stats')
}
