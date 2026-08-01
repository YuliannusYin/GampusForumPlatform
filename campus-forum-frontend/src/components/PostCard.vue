<template>
  <el-card class="post-card" shadow="hover" @click="goDetail">
    <div class="post-main">
      <!-- 标题行 -->
      <div class="post-title">
        <el-tag v-if="post.isTop" type="danger" size="small" effect="dark">置顶</el-tag>
        <el-tag v-if="post.isEssence" type="warning" size="small" effect="dark">精华</el-tag>
        <span class="title-text" @click.stop="goDetail">{{ post.title }}</span>
      </div>

      <!-- 摘要 -->
      <div class="post-summary text-ellipsis-2">{{ post.summary || excerpt }}</div>

      <!-- 底部统计信息 -->
      <div class="post-footer">
        <span v-if="post.username" class="post-author" @click.stop="goAuthorProfile">
          <el-icon><User /></el-icon>
          {{ post.username }}
        </span>
        <span class="post-time">
          <el-icon><Clock /></el-icon>
          {{ formatTime(post.createTime) }}
        </span>
        <span class="post-stat">
          <el-icon><View /></el-icon>
          {{ post.viewCount || 0 }}
        </span>
        <span class="post-stat">
          <el-icon><Pointer /></el-icon>
          {{ post.likeCount || 0 }}
        </span>
        <span class="post-stat">
          <el-icon><ChatDotRound /></el-icon>
          {{ post.commentCount || 0 }}
        </span>
        <span v-if="post.favoriteCount != null" class="post-stat">
          <el-icon><Star /></el-icon>
          {{ post.favoriteCount || 0 }}
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { formatTime } from '@/utils/format'

const props = defineProps({
  // 帖子对象
  post: {
    type: Object,
    required: true
  }
})

const router = useRouter()

// 摘要：无 summary 时截取 content 纯文本
const excerpt = computed(() => {
  const content = props.post.content || ''
  const text = content
    .replace(/[#*`>\-\[\]()!_~]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return text.slice(0, 120)
})

// 跳转帖子详情
const goDetail = () => {
  if (!props.post.id) return
  router.push(`/post/${props.post.id}`)
}

// 跳转作者用户主页
const goAuthorProfile = () => {
  if (props.post.userId) router.push(`/user/${props.post.userId}`)
}
</script>

<style scoped>
.post-card {
  margin-bottom: 12px;
  cursor: pointer;
  transition: transform 0.15s ease;
}

.post-card:hover {
  transform: translateY(-2px);
}

.post-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.post-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.title-text:hover {
  color: #409eff;
}

.post-summary {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

/* 两行省略 */
.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
  color: #909399;
}

.post-time,
.post-stat,
.post-author {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-author {
  cursor: pointer;
  color: #606266;
}

.post-author:hover {
  color: #409eff;
}
</style>
