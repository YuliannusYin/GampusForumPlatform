<template>
  <div class="post-detail" v-loading="loading">
    <template v-if="post">
      <!-- 顶部信息卡片：标题、作者、统计 -->
      <el-card class="post-header" shadow="never">
        <!-- 标题行 -->
        <div class="title-row">
          <el-tag v-if="post.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
          <el-tag v-if="post.isEssence" type="warning" size="small" effect="dark">精华</el-tag>
          <h1 class="post-title">{{ post.title }}</h1>
        </div>

        <!-- 作者信息 -->
        <div class="author-info">
          <el-avatar :size="44" :src="post.userAvatar" class="author-avatar" @click="goAuthorProfile">{{ initialOf(post.username) }}</el-avatar>
          <div class="author-meta">
            <span class="author-name" @click="goAuthorProfile">{{ post.username }}</span>
            <span class="post-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(post.createTime) }}
            </span>
          </div>
          <el-tag v-if="post.sectionName" class="section-tag" effect="plain" size="small">
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
      <el-card class="post-content" shadow="never">
        <MdPreview :model-value="post.content" />
      </el-card>

      <!-- 操作栏 -->
      <el-card class="post-actions" shadow="never">
        <el-button
          :type="liked ? 'primary' : 'default'"
          :loading="likeLoading"
          @click="handleLike"
        >
          <el-icon><Pointer /></el-icon>
          {{ liked ? '已赞' : '点赞' }}
        </el-button>
        <el-button
          :type="favorited ? 'warning' : 'default'"
          :loading="favLoading"
          @click="handleFavorite"
        >
          <el-icon><Star /></el-icon>
          {{ favorited ? '已收藏' : '收藏' }}
        </el-button>
        <el-button v-if="isAuthor" @click="goEdit">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button v-if="isAuthor" type="danger" plain :loading="deleteLoading" @click="handleDelete">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-section" shadow="never">
        <template #header>
          <div class="comment-header">
            <el-icon><ChatDotRound /></el-icon>
            <span>评论 ({{ post.commentCount || 0 }})</span>
          </div>
        </template>
        <CommentList :post-id="postId" />
      </el-card>
    </template>

    <!-- 帖子不存在 -->
    <el-empty v-else-if="!loading" description="帖子不存在或已删除">
      <el-button type="primary" @click="router.push('/home')">返回首页</el-button>
    </el-empty>
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

// 是否为当前用户自己的帖子（作者可见编辑/删除）
const isAuthor = computed(() => {
  return userStore.isLoggedIn && post.value && userStore.userInfo?.id === post.value.userId
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
  gap: 16px;
}

/* 顶部信息 */
.post-header {
  border-radius: 6px;
}

.title-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.post-title {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0;
  line-height: 1.4;
}

/* 作者信息 */
.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.author-avatar {
  cursor: pointer;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  cursor: pointer;
}

.author-name:hover {
  color: #409eff;
}

.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.section-tag {
  flex-shrink: 0;
}

/* 标签 */
.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

/* 统计信息 */
.post-stats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 13px;
  color: #606266;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 正文 */
.post-content {
  border-radius: 6px;
}

/* 操作栏 */
.post-actions {
  border-radius: 6px;
  display: flex;
  gap: 8px;
}

.post-actions :deep(.el-card__body) {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 评论区 */
.comment-section {
  border-radius: 6px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
</style>
