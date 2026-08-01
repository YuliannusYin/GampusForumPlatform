<template>
  <div class="comment-list">
    <!-- 顶级评论列表 -->
    <div v-if="comments.length" class="comment-items">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <!-- 评论主体 -->
        <div class="comment-main">
          <el-avatar :size="40" :src="comment.userAvatar" class="comment-avatar" @click="goUserProfile(comment.userId)">{{ initialOf(comment.username) }}</el-avatar>
          <div class="comment-body">
            <!-- 顶部：用户名 + 时间 + 删除 -->
            <div class="comment-head">
              <span class="comment-user" @click="goUserProfile(comment.userId)">{{ comment.username }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              <el-button
                v-if="isOwnComment(comment)"
                link
                type="danger"
                size="small"
                class="comment-del"
                @click="handleDeleteComment(comment)"
              >
                删除
              </el-button>
            </div>
            <!-- 内容：用 MdPreview 渲染 -->
            <MdPreview :model-value="comment.content" class="comment-content" :preview-only="true" />

            <!-- 操作行 -->
            <div class="comment-actions">
              <el-button
                link
                :type="comment.liked ? 'primary' : ''"
                size="small"
                @click="handleLikeComment(comment)"
              >
                <el-icon><Pointer /></el-icon>
                {{ comment.likeCount || 0 }}
              </el-button>
              <el-button link size="small" @click="toggleReply(comment)">
                <el-icon><ChatLineRound /></el-icon>
                回复
              </el-button>
              <el-button
                v-if="comment.replyCount > 0"
                link
                size="small"
                @click="toggleReplies(comment)"
              >
                {{ comment.repliesExpanded ? '收起回复' : `展开回复(${comment.replyCount})` }}
              </el-button>
            </div>

            <!-- 回复输入框 -->
            <div v-if="comment.replying" class="reply-input">
              <el-input
                v-model="comment.replyContent"
                type="textarea"
                :rows="3"
                placeholder="写下你的回复…"
                maxlength="500"
                show-word-limit
              />
              <div class="reply-input-btns">
                <el-button size="small" @click="cancelReply(comment)">取消</el-button>
                <el-button
                  type="primary"
                  size="small"
                  :loading="comment.submitting"
                  :disabled="!comment.replyContent || !comment.replyContent.trim()"
                  @click="submitReply(comment)"
                >
                  回复
                </el-button>
              </div>
            </div>

            <!-- 子回复列表 -->
            <div v-if="comment.repliesExpanded" class="reply-list">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <el-avatar :size="32" :src="reply.userAvatar" class="reply-avatar" @click="goUserProfile(reply.userId)">{{ initialOf(reply.username) }}</el-avatar>
                <div class="reply-body">
                  <div class="reply-head">
                    <span class="reply-user" @click="goUserProfile(reply.userId)">{{ reply.username }}</span>
                    <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                  </div>
                  <MdPreview :model-value="reply.content" class="reply-content" :preview-only="true" />
                </div>
              </div>
              <!-- 加载更多回复 -->
              <el-button
                v-if="comment.replies.length < comment.replyTotal"
                link
                type="primary"
                size="small"
                :loading="comment.loadingMore"
                @click="loadMoreReplies(comment)"
              >
                加载更多回复
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="!loading" description="暂无评论，快来抢沙发吧" />

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, total"
        background
        @current-change="handlePageChange"
      />
    </div>

    <!-- 底部发表顶级评论 -->
    <div class="comment-post">
      <div class="post-title">发表评论</div>
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="4"
        placeholder="支持 Markdown 语法，友善发言…"
        maxlength="1000"
        show-word-limit
      />
      <div class="post-btn-wrap">
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="!newComment || !newComment.trim()"
          @click="submitComment"
        >
          发表
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { useUserStore } from '@/store/user'
import { formatTime } from '@/utils/format'
import { getComments, getReplies, createComment, deleteComment } from '@/api/comment'
import { likeComment } from '@/api/interaction'

const props = defineProps({
  // 帖子 ID
  postId: {
    type: [Number, String],
    required: true
  }
})

const router = useRouter()
const userStore = useUserStore()

// 顶级评论列表
const comments = ref([])
// 分页状态
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
// 列表加载状态
const loading = ref(false)

// 顶级评论输入
const newComment = ref('')
const submitting = ref(false)

// 每页回复加载条数
const replyPageSize = 5

// 获取用户名首字母（头像占位）
const initialOf = (name) => {
  if (!name) return ''
  return name.charAt(0).toUpperCase()
}

// 是否为当前用户自己的评论（用于显示删除按钮）
const isOwnComment = (comment) => {
  return userStore.isLoggedIn && userStore.userInfo?.id === comment.userId
}

// 跳转用户公开主页
const goUserProfile = (userId) => {
  if (userId) router.push(`/user/${userId}`)
}

// 拉取顶级评论
const fetchComments = async () => {
  loading.value = true
  try {
    const res = await getComments(props.postId, {
      page: currentPage.value,
      size: pageSize.value
    })
    // 兼容 { records, total, page, size }
    const list = res.records || []
    // 为每条评论附加本地交互状态
    comments.value = list.map((c) => ({
      ...c,
      liked: false,
      replying: false,
      replyContent: '',
      submitting: false,
      repliesExpanded: false,
      replies: [],
      replyTotal: 0,
      replyPage: 1,
      loadingMore: false
    }))
    total.value = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    comments.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 翻页
const handlePageChange = () => {
  fetchComments()
}

// 顶级评论点赞
const handleLikeComment = async (comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  try {
    const res = await likeComment(comment.id)
    comment.liked = !!res.liked
    comment.likeCount = res.likeCount != null ? res.likeCount : comment.likeCount
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 切换回复输入框显示
const toggleReply = (comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再回复')
    return
  }
  comment.replying = !comment.replying
  if (comment.replying && !comment.replyContent) comment.replyContent = ''
}

// 取消回复
const cancelReply = (comment) => {
  comment.replying = false
  comment.replyContent = ''
}

// 提交回复
const submitReply = async (comment) => {
  const content = comment.replyContent?.trim()
  if (!content) return
  comment.submitting = true
  try {
    const data = await createComment(props.postId, {
      content,
      parentId: comment.id
    })
    // 回复成功：回复数 +1；若已展开则追加到列表
    comment.replyCount = (comment.replyCount || 0) + 1
    comment.replyTotal = (comment.replyTotal || 0) + 1
    if (comment.repliesExpanded) {
      comment.replies.push(data)
    }
    comment.replyContent = ''
    comment.replying = false
    ElMessage.success('回复成功')
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    comment.submitting = false
  }
}

// 展开 / 收起子回复
const toggleReplies = async (comment) => {
  if (comment.repliesExpanded) {
    comment.repliesExpanded = false
    return
  }
  // 首次展开：加载第一页
  if (comment.replies.length === 0) {
    await loadReplies(comment, 1)
  }
  comment.repliesExpanded = true
}

// 加载子回复（指定页码）
const loadReplies = async (comment, page) => {
  comment.loadingMore = true
  try {
    const res = await getReplies(comment.id, { page, size: replyPageSize })
    const list = res.records || []
    if (page === 1) {
      comment.replies = list
    } else {
      comment.replies = comment.replies.concat(list)
    }
    comment.replyTotal = res.total || 0
    comment.replyPage = page
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    comment.loadingMore = false
  }
}

// 加载更多回复
const loadMoreReplies = (comment) => {
  loadReplies(comment, comment.replyPage + 1)
}

// 删除评论
const handleDeleteComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    // 用户取消
    return
  }
  try {
    await deleteComment(comment.id)
    ElMessage.success('删除成功')
    // 从列表中移除
    comments.value = comments.value.filter((c) => c.id !== comment.id)
    total.value = Math.max(0, total.value - 1)
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 发表顶级评论
const submitComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发表评论')
    return
  }
  const content = newComment.value?.trim()
  if (!content) return
  submitting.value = true
  try {
    const data = await createComment(props.postId, {
      content,
      parentId: 0
    })
    // 新评论前置插入并附带本地状态
    comments.value.unshift({
      ...data,
      liked: false,
      replying: false,
      replyContent: '',
      submitting: false,
      repliesExpanded: false,
      replies: [],
      replyTotal: 0,
      replyPage: 1,
      loadingMore: false
    })
    total.value += 1
    newComment.value = ''
    ElMessage.success('评论成功')
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    submitting.value = false
  }
}

// 帖子 ID 变化时重新加载
watch(
  () => props.postId,
  () => {
    currentPage.value = 1
    fetchComments()
  }
)

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 评论项 */
.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.comment-user {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
}

.comment-user:hover {
  color: #409eff;
}

.comment-avatar {
  cursor: pointer;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-del {
  margin-left: auto;
}

/* 评论内容：限制 MdPreview 内边距 */
.comment-content :deep(.md-editor-preview) {
  padding: 4px 0;
  font-size: 14px;
}

/* 操作行 */
.comment-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

/* 回复输入框 */
.reply-input {
  margin-top: 10px;
}

.reply-input-btns {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

/* 子回复列表 */
.reply-list {
  margin-top: 12px;
  padding: 10px 12px;
  background-color: #f9fafb;
  border-radius: 6px;
}

.reply-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.reply-user {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
}

.reply-user:hover {
  color: #409eff;
}

.reply-avatar {
  cursor: pointer;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-content :deep(.md-editor-preview) {
  padding: 2px 0;
  font-size: 13px;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* 底部发表评论 */
.comment-post {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.post-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.post-btn-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
