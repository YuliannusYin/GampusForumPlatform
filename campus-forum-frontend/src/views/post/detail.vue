<template>
  <div class="post-detail" v-loading="loading">
    <template v-if="post">
      <!-- 顶部信息卡片：标题、作者、统计 -->
      <el-card class="post-header fade-in-up" shadow="never">
        <!-- 标签行：置顶 / 精华 -->
        <div v-if="post.isTop || post.isEssence" class="badge-row">
          <span v-if="post.isTop" class="badge-chip badge-top">
            <el-icon><Top /></el-icon> 置顶
          </span>
          <span v-if="post.isEssence" class="badge-chip badge-essence">
            <el-icon><Medal /></el-icon> 精华
          </span>
        </div>

        <!-- 标题 -->
        <h1 class="post-title">{{ post.title }}</h1>

        <!-- 作者信息 -->
        <div class="author-info">
          <el-avatar
            :size="44"
            :src="isAnonymous ? '' : post.userAvatar"
            class="author-avatar"
            :class="{ ghost: isAnonymous, clickable: !isAnonymous }"
            @click="goAuthorProfile"
          >
            {{ isAnonymous ? '匿' : initialOf(post.username) }}
          </el-avatar>
          <div class="author-meta">
            <span
              class="author-name"
              :class="{ anonymous: isAnonymous }"
              @click="goAuthorProfile"
            >{{ authorDisplayName }}</span>
            <span class="post-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(post.createTime) }}
            </span>
          </div>
          <el-tag v-if="post.sectionName" class="section-tag" effect="plain" size="small" round>
            {{ post.sectionName }}
          </el-tag>
        </div>

        <!-- 标签 -->
        <div v-if="post.tags && post.tags.length" class="post-tags">
          <el-tag
            v-for="tag in post.tags"
            :key="tag.id"
            size="small"
            effect="plain"
            class="post-tag"
          >
            {{ tag.name }}
          </el-tag>
        </div>

        <!-- 统计信息 -->
        <div class="post-stats">
          <span class="stat-item">
            <el-icon><View /></el-icon>
            {{ post.viewCount || 0 }} 浏览
          </span>
          <span class="stat-item">
            <el-icon><Pointer /></el-icon>
            {{ post.likeCount || 0 }} 点赞
          </span>
          <span class="stat-item">
            <el-icon><ChatDotRound /></el-icon>
            {{ post.commentCount || 0 }} 评论
          </span>
          <span class="stat-item">
            <el-icon><Star /></el-icon>
            {{ post.favoriteCount || 0 }} 收藏
          </span>
        </div>
      </el-card>

      <!-- 正文：Markdown 渲染 -->
      <el-card class="post-content fade-in-up delay-1" shadow="never">
        <MdPreview :model-value="post.content" />
      </el-card>

      <!-- 操作栏 -->
      <div class="post-actions fade-in-up delay-2">
        <el-button
          :type="liked ? 'primary' : 'default'"
          class="action-chip"
          round
          :loading="likeLoading"
          @click="handleLike"
        >
          <el-icon><Pointer /></el-icon>
          <span>{{ liked ? '已赞' : '点赞' }}</span>
        </el-button>
        <el-button
          :type="favorited ? 'warning' : 'default'"
          class="action-chip"
          round
          :loading="favLoading"
          @click="handleFavorite"
        >
          <el-icon><Star /></el-icon>
          <span>{{ favorited ? '已收藏' : '收藏' }}</span>
        </el-button>
        <el-button v-if="isAuthor" class="action-chip" round @click="goEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button v-if="isAuthor" type="danger" plain class="action-chip" round :loading="deleteLoading" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
        <el-button v-if="isAnonymous && !isAuthor" class="action-chip" round @click="reportVisible = true">
          举报
        </el-button>
      </div>

      <!-- 评论区 -->
      <el-card class="comment-section fade-in-up delay-3" shadow="never">
        <template #header>
          <div class="comment-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论</span>
            <span class="comment-count">{{ post.commentCount || 0 }}</span>
          </div>
        </template>
        <CommentList :post-id="postId" :allow-report="isAnonymous" />
      </el-card>
    </template>

    <!-- 帖子不存在 -->
    <el-empty v-else-if="!loading" description="帖子不存在或已删除">
      <el-button type="primary" @click="router.push('/home')">返回首页</el-button>
    </el-empty>

    <ReportDialog v-model="reportVisible" :target-type="1" :target-id="postId" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { useUserStore } from '@/store/user'
import { formatTime } from '@/utils/format'
import { getPostDetail, deletePost } from '@/api/post'
import { likePost, favoritePost, getInteractions } from '@/api/interaction'
import CommentList from '@/components/CommentList.vue'
import ReportDialog from '@/components/ReportDialog.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 帖子 ID（来自路由参数）
const postId = computed(() => route.params.id)

// 帖子详情
const post = ref(null)
// 加载状态
const loading = ref(false)

// 点赞 / 收藏状态
const liked = ref(false)
const favorited = ref(false)
const likeLoading = ref(false)
const favLoading = ref(false)
const deleteLoading = ref(false)
const reportVisible = ref(false)

// 是否为当前用户自己的帖子（匿名帖由接口下发 isAuthor，不再比较 userId）
const isAnonymous = computed(() => Number(post.value?.isAnonymous) === 1)
const isAuthor = computed(() => !!post.value?.isAuthor)
const authorDisplayName = computed(() => {
  if (isAnonymous.value) {
    return isAuthor.value ? '匿名（我）' : '匿名墙友'
  }
  return post.value?.username || '用户'
})

// 用户名首字母（头像占位）
const initialOf = (name) => {
  if (!name) return ''
  return name.charAt(0).toUpperCase()
}

// 拉取帖子详情
const fetchDetail = async () => {
  loading.value = true
  try {
    post.value = await getPostDetail(postId.value)
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    post.value = null
  } finally {
    loading.value = false
  }
}

// 拉取当前用户的点赞 / 收藏状态
const fetchInteractions = async () => {
  if (!userStore.isLoggedIn) return
  try {
    const res = await getInteractions(postId.value)
    liked.value = !!res.liked
    favorited.value = !!res.favorited
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 点赞 / 取消点赞
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  likeLoading.value = true
  try {
    const res = await likePost(postId.value)
    liked.value = !!res.liked
    if (res.likeCount != null && post.value) {
      post.value.likeCount = res.likeCount
    }
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    likeLoading.value = false
  }
}

// 收藏 / 取消收藏
const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再收藏')
    return
  }
  favLoading.value = true
  try {
    const res = await favoritePost(postId.value)
    favorited.value = !!res.favorited
    if (res.favoriteCount != null && post.value) {
      post.value.favoriteCount = res.favoriteCount
    }
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    favLoading.value = false
  }
}

// 跳转编辑页
const goEdit = () => {
  router.push(`/post/edit/${postId.value}`)
}

// 跳转作者用户主页
const goAuthorProfile = () => {
  if (isAnonymous.value) return
  if (post.value && post.value.userId) {
    router.push(`/user/${post.value.userId}`)
  }
}

// 删除帖子（确认后调用）
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后不可恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    // 用户取消
    return
  }
  deleteLoading.value = true
  try {
    await deletePost(postId.value)
    ElMessage.success('删除成功')
    router.push('/home')
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    deleteLoading.value = false
  }
}

onMounted(() => {
  fetchDetail()
  fetchInteractions()
})
</script>

<style scoped>
.post-detail {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* ===== 顶部信息卡片 ===== */
.post-header {
  border-radius: var(--radius-2xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.post-header :deep(.el-card__body) {
  padding: var(--space-6);
}

.badge-row {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.badge-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-caption);
  font-weight: var(--font-weight-semibold);
  color: #fff;
}

.badge-top { background: var(--color-danger); }
.badge-essence { background: var(--gradient-gold); }

.post-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0 0 var(--space-4);
  line-height: var(--line-height-tight);
}

/* 作者信息 */
.author-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}

.author-avatar {
  flex-shrink: 0;
  background: var(--gradient-primary);
  color: #fff;
  font-weight: var(--font-weight-semibold);
}

.author-avatar.clickable {
  cursor: pointer;
}

.author-avatar.ghost {
  cursor: default;
  background: #c4b6a6;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  flex: 1;
  min-width: 0;
}

.author-name {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.author-name:hover {
  color: var(--color-primary);
}

.author-name.anonymous {
  cursor: default;
}

.author-name.anonymous:hover {
  color: var(--color-text-1);
}

.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.section-tag {
  flex-shrink: 0;
  background: var(--color-primary-tag-bg);
  color: var(--color-primary);
}

/* 标签 */
.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.post-tag {
  border: none;
  border-radius: var(--radius-full);
  padding: 3px 12px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  background: var(--color-bg-page);
  color: var(--color-text-2);
}

/* 统计信息 */
.post-stats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border-lighter);
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-item .el-icon {
  font-size: 15px;
}

/* ===== 正文卡片 ===== */
.post-content {
  border-radius: var(--radius-2xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.post-content :deep(.el-card__body) {
  padding: var(--space-6);
}

.post-content :deep(.md-editor-preview) {
  font-size: var(--font-size-body);
  line-height: var(--line-height-relaxed);
  color: var(--color-text-1);
}

/* ===== 操作栏（chip 风格） ===== */
.post-actions {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
  padding: var(--space-2) var(--space-1);
}

.action-chip {
  border-radius: var(--radius-full);
  padding: 10px 22px;
  font-weight: var(--font-weight-medium);
  transition: all var(--transition-fast);
}

.action-chip :deep(.el-icon) {
  font-size: 16px;
  margin-right: 4px;
}

/* 未激活的默认 chip */
.action-chip.el-button--default {
  border-color: var(--color-border);
  color: var(--color-text-2);
  background: var(--color-bg-card);
}

.action-chip.el-button--default:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: var(--color-primary-bg);
  transform: translateY(-1px);
  box-shadow: var(--shadow-primary);
}

/* ===== 评论区 ===== */
.comment-section {
  border-radius: var(--radius-2xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.comment-section :deep(.el-card__header) {
  padding: var(--space-4) var(--space-6);
  border-bottom: 1px solid var(--color-border-lighter);
}

.comment-section :deep(.el-card__body) {
  padding: var(--space-5) var(--space-6);
}

.comment-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
}

.comment-header .el-icon {
  color: var(--color-primary);
}

.comment-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 7px;
  border-radius: var(--radius-full);
  background: var(--color-primary-tag-bg);
  color: var(--color-primary);
  font-size: var(--font-size-caption);
  font-weight: var(--font-weight-semibold);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .post-header :deep(.el-card__body),
  .post-content :deep(.el-card__body),
  .comment-section :deep(.el-card__body) {
    padding: var(--space-4);
  }
  .post-title {
    font-size: var(--font-size-h2);
  }
  .post-stats {
    gap: var(--space-3);
  }
  .action-chip {
    padding: 7px 14px;
  }
}
</style>
