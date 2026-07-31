import request from '@/utils/request'

// 获取板块列表
export function getSections() {
  return request.get('/sections')
}

// 获取帖子列表（支持分页、板块过滤、排序）
export function getPosts(params) {
  return request.get('/posts', { params })
}

// 获取帖子详情
export function getPostDetail(id) {
  return request.get(`/posts/${id}`)
}

// 创建帖子
export function createPost(data) {
  return request.post('/posts', data)
}

// 更新帖子
export function updatePost(id, data) {
  return request.put(`/posts/${id}`, data)
}

// 删除帖子
export function deletePost(id) {
  return request.delete(`/posts/${id}`)
}

// 搜索帖子
export function searchPosts(params) {
  return request.get('/posts/search', { params })
}

// 获取标签列表
export function getTags() {
  return request.get('/tags')
}
