<template>
  <article class="post-card" @click="goDetail">
    <div class="post-main">
      <!-- Tags row -->
      <div class="post-tags-row" v-if="post.isTop || post.isEssence">
        <span v-if="post.isTop" class="badge badge-top">
          <el-icon><Top /></el-icon>
          置顶
        </span>
        <span v-if="post.isEssence" class="badge badge-essence">
          <el-icon><StarFilled /></el-icon>
          精华
        </span>
      </div>

      <!-- Title -->
      <h3 class="post-title" @click.stop="goDetail">{{ post.title }}</h3>

      <!-- Summary -->
      <p class="post-summary text-ellipsis-2">{{ post.summary || excerpt }}</p>

      <!-- Footer -->
      <div class="post-footer">
        <div class="footer-left">
          <span v-if="isAnonymous" class="post-author anonymous" @click.stop>
            <el-avatar :size="22" class="author-avatar ghost">匿</el-avatar>
            {{ anonymousLabel }}
          </span>
          <span v-else-if="post.username" class="post-author" @click.stop="goAuthorProfile">
            <el-avatar :size="22" class="author-avatar">{{ authorInitial }}</el-avatar>
            {{ post.username }}
          </span>
          <span class="post-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(post.createTime) }}
          </span>
        </div>
        <div class="footer-right">
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
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Top, StarFilled, Clock, View, Pointer, ChatDotRound, Star } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'

const props = defineProps({
  post: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const excerpt = computed(() => {
  const content = props.post.content || ''
  const text = content
    .replace(/[#*`>\-\[\]()!_~]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return text.slice(0, 120)
})

const authorInitial = computed(() => {
  const name = props.post.username || ''
  return name ? name.charAt(0).toUpperCase() : ''
})

const isAnonymous = computed(() => Number(props.post.isAnonymous) === 1)
const anonymousLabel = computed(() => (props.post.isAuthor ? '匿名（我）' : '匿名墙友'))

const goDetail = () => {
  if (!props.post.id) return
  router.push(`/post/${props.post.id}`)
}

const goAuthorProfile = () => {
  if (isAnonymous.value) return
  if (props.post.userId) router.push(`/user/${props.post.userId}`)
}
</script>

<style scoped>
.post-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-3);
  cursor: pointer;
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.post-card::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.post-card:hover {
  border-color: var(--color-primary-3);
  box-shadow: var(--shadow-2);
  transform: translateY(-1px);
}

.post-card:hover::before {
  opacity: 1;
}

.post-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

/* Badges */
.post-tags-row {
  display: flex;
  gap: var(--space-2);
  margin-bottom: -2px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: var(--font-size-mini);
  font-weight: var(--font-weight-semibold);
  line-height: 1.5;
}

.badge .el-icon {
  font-size: 11px;
}

.badge-top {
  background: var(--color-danger-tag-bg);
  color: var(--color-danger);
}

.badge-essence {
  background: var(--color-warning-tag-bg);
  color: var(--color-warning);
}

/* Title */
.post-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  line-height: var(--line-height-tight);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.post-title:hover {
  color: var(--color-primary);
}

/* Summary */
.post-summary {
  font-size: var(--font-size-sm);
  color: var(--color-text-2);
  line-height: var(--line-height-relaxed);
}

/* Footer */
.post-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-top: var(--space-1);
  padding-top: var(--space-2);
  border-top: 1px solid var(--color-border-lighter);
}

.footer-left,
.footer-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.post-author {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--font-size-sm);
  color: var(--color-text-2);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.post-author:hover {
  color: var(--color-primary);
}

.post-author.anonymous {
  cursor: default;
  color: var(--color-text-3);
}

.post-author.anonymous:hover {
  color: var(--color-text-3);
}

.author-avatar {
  background: var(--gradient-primary);
  color: #fff;
  font-size: 10px;
  font-weight: var(--font-weight-semibold);
}

.author-avatar.ghost {
  background: #c4b6a6;
}

.post-time {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: var(--font-size-caption);
  color: var(--color-text-3);
}

.post-stat {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: var(--font-size-caption);
  color: var(--color-text-3);
  transition: color var(--transition-fast);
}

.post-stat .el-icon {
  font-size: 14px;
}

@media (max-width: 768px) {
  .post-card {
    padding: var(--space-3) var(--space-4);
  }

  .post-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }

  .footer-right {
    gap: var(--space-3);
  }
}
</style>
