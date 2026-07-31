import request from '@/utils/request'

// 获取帖子的顶级评论（分页）
export function getComments(postId, params) {
  return request.get(`/posts/${postId}/comments`, { params })
}

// 获取评论的回复列表（分页）
export function getReplies(commentId, params) {
  return request.get(`/comments/${commentId}/replies`, { params })
}

// 发表评论（顶级评论 parentId 传 0 或不传；回复传父评论 id）
export function createComment(postId, data) {
  return request.post(`/posts/${postId}/comments`, data)
}

// 删除评论
export function deleteComment(id) {
  return request.delete(`/comments/${id}`)
}
