<template>
  <div class="comment-list">
    <!-- Comment list -->
    <div v-if="comments.length" class="comment-items">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <!-- Comment main -->
        <div class="comment-main">
          <el-avatar :size="38" :src="comment.userAvatar" class="comment-avatar" @click="goUserProfile(comment.userId)">{{ initialOf(comment.username) }}</el-avatar>
          <div class="comment-body">
            <!-- Head -->
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
            <!-- Content -->
            <MdPreview :model-value="comment.content" class="comment-content" :preview-only="true" />

            <!-- Actions -->
            <div class="comment-actions">
              <button class="action-chip" :class="{ liked: comment.liked }" @click="handleLikeComment(comment)">
                <el-icon><Pointer /></el-icon>
                {{ comment.likeCount || 0 }}
              </button>
              <button class="action-chip" @click="toggleReply(comment)">
                <el-icon><ChatLineRound /></el-icon>
                回复
              </button>
              <button
                v-if="comment.replyCount > 0"
                class="action-chip"
                @click="toggleReplies(comment)"
              >
                {{ comment.repliesExpanded ? '收起回复' : `展开 ${comment.replyCount} 条回复` }}
              </button>
            </div>

            <!-- Reply input -->
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

            <!-- Sub-replies -->
            <div v-if="comment.repliesExpanded" class="reply-list">
              <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                <el-avatar :size="28" :src="reply.userAvatar" class="reply-avatar" @click="goUserProfile(reply.userId)">{{ initialOf(reply.username) }}</el-avatar>
                <div class="reply-body">
                  <div class="reply-head">
                    <span class="reply-user" @click="goUserProfile(reply.userId)">{{ reply.username }}</span>
                    <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                  </div>
                  <MdPreview :model-value="reply.content" class="reply-content" :preview-only="true" />
                </div>
              </div>
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

    <!-- Empty -->
    <div v-else-if="!loading" class="empty-comments">
      <div class="empty-icon">
        <svg viewBox="0 0 120 120" width="80" height="80" fill="none">
          <circle cx="60" cy="60" r="50" fill="#EBF1FF"/>
          <path d="M40 50c0-3.3 2.7-6 6-6h28c3.3 0 6 2.7 6 6v16c0 3.3-2.7 6-6 6H52l-8 7v-7h-2c-1.1 0-2-.9-2-2V50z" fill="#fff" stroke="#1664FF" stroke-width="2"/>
          <circle cx="52" cy="58" r="2.5" fill="#97BCFF"/>
          <circle cx="62" cy="58" r="2.5" fill="#97BCFF"/>
          <circle cx="72" cy="58" r="2.5" fill="#97BCFF"/>
        </svg>
      </div>
      <p class="empty-text">暂无评论，快来抢沙发吧</p>
    </div>

    <!-- Pagination -->
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

    <!-- Post comment -->
    <div class="comment-post">
      <div class="post-title">
        <el-icon><EditPen /></el-icon>
        发表评论
      </div>
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
import { Pointer, ChatLineRound, EditPen } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { formatTime } from '@/utils/format'
import { getComments, getReplies, createComment, deleteComment } from '@/api/comment'
import { likeComment } from '@/api/interaction'

const props = defineProps({
  postId: {
    type: [Number, String],
    required: true
  }
})

const router = useRouter()
const userStore = useUserStore()

const comments = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const newComment = ref('')
const submitting = ref(false)

const replyPageSize = 5

const initialOf = (name) => {
  if (!name) return ''
  return name.charAt(0).toUpperCase()
}

const isOwnComment = (comment) => {
  return userStore.isLoggedIn && userStore.userInfo?.id === comment.userId
}

const goUserProfile = (userId) => {
  if (userId) router.push(`/user/${userId}`)
}

const fetchComments = async () => {
  loading.value = true
  try {
    const res = await getComments(props.postId, {
      page: currentPage.value,
      size: pageSize.value
    })
    const list = res.records || []
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
    comments.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handlePageChange = () => {
  fetchComments()
}

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
    // handled by interceptor
  }
}

const toggleReply = (comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再回复')
    return
  }
  comment.replying = !comment.replying
  if (comment.replying && !comment.replyContent) comment.replyContent = ''
}

const cancelReply = (comment) => {
  comment.replying = false
  comment.replyContent = ''
}

const submitReply = async (comment) => {
  const content = comment.replyContent?.trim()
  if (!content) return
  comment.submitting = true
  try {
    const data = await createComment(props.postId, {
      content,
      parentId: comment.id
    })
    comment.replyCount = (comment.replyCount || 0) + 1
    comment.replyTotal = (comment.replyTotal || 0) + 1
    if (comment.repliesExpanded) {
      comment.replies.push(data)
    }
    comment.replyContent = ''
    comment.replying = false
    ElMessage.success('回复成功')
  } catch (err) {
    // handled by interceptor
  } finally {
    comment.submitting = false
  }
}

const toggleReplies = async (comment) => {
  if (comment.repliesExpanded) {
    comment.repliesExpanded = false
    return
  }
  if (comment.replies.length === 0) {
    await loadReplies(comment, 1)
  }
  comment.repliesExpanded = true
}

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
    // handled by interceptor
  } finally {
    comment.loadingMore = false
  }
}

const loadMoreReplies = (comment) => {
  loadReplies(comment, comment.replyPage + 1)
}

const handleDeleteComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await deleteComment(comment.id)
    ElMessage.success('删除成功')
    comments.value = comments.value.filter((c) => c.id !== comment.id)
    total.value = Math.max(0, total.value - 1)
  } catch (err) {
    // handled by interceptor
  }
}

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
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

watch(() => props.postId, () => {
  currentPage.value = 1
  fetchComments()
})

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

/* Comment item */
.comment-item {
  padding: var(--space-3) 0;
  border-bottom: 1px solid var(--color-border-lighter);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-main {
  display: flex;
  gap: var(--space-3);
}

.comment-avatar {
  flex-shrink: 0;
  cursor: pointer;
  background: var(--gradient-primary);
  color: #fff;
  font-weight: var(--font-weight-semibold);
}

.comment-body {
  flex: 1;
  min-width: 0;
}

.comment-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-1);
}

.comment-user {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.comment-user:hover {
  color: var(--color-primary);
}

.comment-time {
  font-size: var(--font-size-caption);
  color: var(--color-text-3);
}

.comment-del {
  margin-left: auto;
}

.comment-content :deep(.md-editor-preview) {
  padding: var(--space-1) 0;
  font-size: var(--font-size-body);
  color: var(--color-text-2);
}

/* Action chips */
.comment-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

.action-chip {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border: none;
  background: var(--color-bg-page);
  border-radius: var(--radius-full);
  font-size: var(--font-size-caption);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-3);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: var(--font-body);
}

.action-chip:hover {
  background: var(--color-bg-hover);
  color: var(--color-text-1);
}

.action-chip.liked {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.action-chip .el-icon {
  font-size: 14px;
}

/* Reply input */
.reply-input {
  margin-top: var(--space-2);
  padding: var(--space-3);
  background: var(--color-bg-page);
  border-radius: var(--radius-lg);
}

.reply-input-btns {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
  margin-top: var(--space-2);
}

/* Sub-replies */
.reply-list {
  margin-top: var(--space-3);
  padding: var(--space-3);
  background: var(--color-bg-subtle);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-lighter);
}

.reply-item {
  display: flex;
  gap: var(--space-2);
  padding: var(--space-2) 0;
}

.reply-body {
  flex: 1;
  min-width: 0;
}

.reply-head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: 2px;
}

.reply-user {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.reply-user:hover {
  color: var(--color-primary);
}

.reply-avatar {
  cursor: pointer;
  background: var(--gradient-primary);
  color: #fff;
  font-size: 10px;
  font-weight: var(--font-weight-semibold);
}

.reply-time {
  font-size: var(--font-size-caption);
  color: var(--color-text-3);
}

.reply-content :deep(.md-editor-preview) {
  padding: 2px 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-2);
}

/* Empty */
.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-8) var(--space-4);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--space-3);
}

.empty-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-4);
}

/* Post comment */
.comment-post {
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border-lighter);
}

.post-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  margin-bottom: var(--space-3);
}

.post-btn-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-2);
}
</style>
