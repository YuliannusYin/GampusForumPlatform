import request from '@/utils/request'

// 点赞 / 取消点赞帖子
export function likePost(postId) {
  return request.post(`/posts/${postId}/like`)
}

// 收藏 / 取消收藏帖子
export function favoritePost(postId) {
  return request.post(`/posts/${postId}/favorite`)
}

// 点赞 / 取消点赞评论
export function likeComment(commentId) {
  return request.post(`/comments/${commentId}/like`)
}

// 获取当前用户对帖子的点赞 / 收藏状态
export function getInteractions(postId) {
  return request.get(`/posts/${postId}/interactions`)
}
