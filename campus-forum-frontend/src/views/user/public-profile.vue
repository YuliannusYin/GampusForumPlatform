<template>
  <div class="public-profile-page" v-loading="profileLoading">
    <!-- Profile header with gradient cover -->
    <div v-if="profile" class="profile-header fade-in-up">
      <div class="header-cover">
        <div class="cover-decoration"></div>
      </div>
      <div class="header-body">
        <el-avatar :size="96" :src="profile.avatar" class="user-avatar">
          {{ usernameInitial }}
        </el-avatar>

        <div class="user-meta">
          <div class="user-name-row">
            <span class="user-nickname">{{ profile.nickname || profile.username || '匿名用户' }}</span>
            <el-tag type="primary" size="small" effect="light">Lv.{{ profile.level || 1 }}</el-tag>
          </div>
          <div class="user-bio">{{ profile.bio || '这个人很懒，什么都没留下' }}</div>
          <div class="user-extra">
            <span v-if="profile.createTime" class="extra-item">
              <el-icon><Clock /></el-icon>
              注册于 {{ formatTime(profile.createTime) }}
            </span>
            <span class="extra-item">
              <el-icon><GoldMedal /></el-icon>
              积分：{{ profile.points || 0 }}
            </span>
          </div>
        </div>

        <!-- Stats -->
        <div class="user-stats">
          <div class="stat-item">
            <div class="stat-number stat-value">{{ profile.followingCount || 0 }}</div>
            <div class="stat-label">关注</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number stat-value">{{ profile.followerCount || 0 }}</div>
            <div class="stat-label">粉丝</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-number stat-value">{{ profile.postCount || 0 }}</div>
            <div class="stat-label">发帖</div>
          </div>
        </div>

        <!-- Follow button -->
        <div v-if="showFollowBtn" class="follow-action">
          <el-button
            :type="isFollowed ? 'default' : 'primary'"
            :loading="followLoading"
            @click="handleToggleFollow"
            round
          >
            {{ isFollowed ? '已关注' : '+ 关注' }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- Main content: tabs -->
    <el-tabs v-model="activeTab" class="profile-tabs fade-in-up delay-1" @tab-change="handleTabChange">
      <!-- 发帖 -->
      <el-tab-pane label="发帖" name="posts">
        <div v-loading="postsLoading" class="list-wrap">
          <div
            v-for="(post, index) in postsList"
            :key="post.id"
            class="item-card fade-in-up"
            :style="{ animationDelay: (index * 0.05) + 's' }"
            @click="goPost(post.id)"
          >
            <div class="post-title">{{ post.title }}</div>
            <div class="post-summary text-ellipsis-2">{{ post.summary || excerpt(post.content) }}</div>
            <div class="item-footer">
              <span class="footer-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(post.createTime) }}
              </span>
              <span class="footer-item">
                <el-icon><View /></el-icon>
                {{ post.viewCount || 0 }}
              </span>
              <span class="footer-item">
                <el-icon><ChatDotRound /></el-icon>
                {{ post.commentCount || 0 }}
              </span>
            </div>
          </div>
          <el-empty v-if="!postsLoading && postsList.length === 0" description="暂无发帖" />
        </div>
        <div class="pagination-wrap" v-if="postsPage.total > 0">
          <el-pagination
            v-model:current-page="postsPage.current"
            v-model:page-size="postsPage.size"
            :total="postsPage.total"
            layout="total, prev, pager, next"
            @current-change="loadPosts"
          />
        </div>
      </el-tab-pane>

      <!-- 评论 -->
      <el-tab-pane label="评论" name="comments">
        <div v-loading="commentsLoading" class="list-wrap">
          <div
            v-for="(comment, index) in commentsList"
            :key="comment.id"
            class="item-card fade-in-up"
            :style="{ animationDelay: (index * 0.05) + 's' }"
            @click="goPost(comment.postId)"
          >
            <div class="comment-content text-ellipsis-2">{{ comment.content }}</div>
            <div class="item-footer">
              <span class="footer-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(comment.createTime) }}
              </span>
            </div>
          </div>
          <el-empty v-if="!commentsLoading && commentsList.length === 0" description="暂无评论" />
        </div>
        <div class="pagination-wrap" v-if="commentsPage.total > 0">
          <el-pagination
            v-model:current-page="commentsPage.current"
            v-model:page-size="commentsPage.size"
            :total="commentsPage.total"
            layout="total, prev, pager, next"
            @current-change="loadComments"
          />
        </div>
      </el-tab-pane>

      <!-- 社团 -->
      <el-tab-pane label="社团" name="clubs">
        <div v-loading="clubsLoading" class="list-wrap">
          <div
            v-for="(club, index) in clubsList"
            :key="club.id"
            class="item-card fade-in-up"
            :style="{ animationDelay: (index * 0.05) + 's' }"
            @click="goClub(club.id)"
          >
            <div class="club-title">
              <el-icon><User /></el-icon>
              <span>{{ club.name }}</span>
            </div>
            <div class="club-desc text-ellipsis-2">{{ club.description || '暂无简介' }}</div>
            <div class="item-footer">
              <span class="footer-item">
                <el-icon><UserFilled /></el-icon>
                成员数：{{ club.memberCount || 0 }}
              </span>
            </div>
          </div>
          <el-empty v-if="!clubsLoading && clubsList.length === 0" description="暂未加入社团" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  getUserProfile,
  getUserPosts,
  getUserComments,
  getUserClubs,
  isFollowing,
  followUser,
  unfollowUser
} from '@/api/follow'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前查看的用户 ID
const userId = computed(() => route.params.id)

// 用户公开信息
const profile = ref(null)
const profileLoading = ref(false)

// 头像首字母占位
const usernameInitial = computed(() => {
  const name = profile.value?.nickname || profile.value?.username || ''
  return name ? name.charAt(0).toUpperCase() : ''
})

// 是否登录且不是自己：控制关注按钮显示
const showFollowBtn = computed(() => {
  if (!userStore.isLoggedIn) return false
  const currentId = userStore.userInfo?.id
  return currentId != null && String(currentId) !== String(userId.value)
})

// 关注状态
const isFollowed = ref(false)
const followLoading = ref(false)

// 当前激活标签页
const activeTab = ref('posts')

// ===== 发帖 =====
const postsLoading = ref(false)
const postsList = ref([])
const postsPage = reactive({ current: 1, size: 10, total: 0 })

const loadPosts = async () => {
  postsLoading.value = true
  try {
    const res = await getUserPosts(userId.value, { page: postsPage.current, size: postsPage.size })
    postsList.value = res.records || []
    postsPage.total = res.total || 0
  } catch (e) {
    // 忽略
  } finally {
    postsLoading.value = false
  }
}

// ===== 评论 =====
const commentsLoading = ref(false)
const commentsList = ref([])
const commentsPage = reactive({ current: 1, size: 10, total: 0 })

const loadComments = async () => {
  commentsLoading.value = true
  try {
    const res = await getUserComments(userId.value, { page: commentsPage.current, size: commentsPage.size })
    commentsList.value = res.records || []
    commentsPage.total = res.total || 0
  } catch (e) {
    // 忽略
  } finally {
    commentsLoading.value = false
  }
}

// ===== 社团 =====
const clubsLoading = ref(false)
const clubsList = ref([])

const loadClubs = async () => {
  clubsLoading.value = true
  try {
    const res = await getUserClubs(userId.value)
    clubsList.value = Array.isArray(res) ? res : (res?.records || [])
  } catch (e) {
    // 忽略
  } finally {
    clubsLoading.value = false
  }
}

// 摘要：无 summary 时截取 content 纯文本
const excerpt = (content) => {
  const text = (content || '')
    .replace(/[#*`>\-\[\]()!_~]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return text.slice(0, 120)
}

// 切换 tab 时懒加载
const handleTabChange = (name) => {
  if (name === 'comments' && commentsList.value.length === 0) loadComments()
  if (name === 'clubs' && clubsList.value.length === 0) loadClubs()
}

// 关注 / 取关切换
const handleToggleFollow = async () => {
  followLoading.value = true
  try {
    if (isFollowed.value) {
      await unfollowUser(userId.value)
      isFollowed.value = false
      // 本地粉丝数 -1
      if (profile.value) {
        profile.value.followerCount = Math.max(0, (profile.value.followerCount || 0) - 1)
      }
      ElMessage.success('已取消关注')
    } else {
      await followUser(userId.value)
      isFollowed.value = true
      // 本地粉丝数 +1
      if (profile.value) {
        profile.value.followerCount = (profile.value.followerCount || 0) + 1
      }
      ElMessage.success('关注成功')
    }
  } catch (e) {
    // 错误已由请求拦截器统一提示
  } finally {
    followLoading.value = false
  }
}

// 跳转帖子详情
const goPost = (postId) => {
  if (!postId) return
  router.push(`/post/${postId}`)
}

// 跳转社团详情
const goClub = (clubId) => {
  if (!clubId) return
  router.push(`/club/${clubId}`)
}

// 拉取用户公开信息
const loadProfile = async () => {
  profileLoading.value = true
  try {
    profile.value = await getUserProfile(userId.value)
    // 已登录且非自己：拉取关注状态
    if (showFollowBtn.value) {
      try {
        const res = await isFollowing(userId.value)
        // 兼容 boolean 或 { following: true } 两种返回
        isFollowed.value = typeof res === 'boolean' ? res : !!res?.following
      } catch (e) {
        // 忽略
      }
    }
  } catch (e) {
    // 错误已由请求拦截器统一提示
    profile.value = null
  } finally {
    profileLoading.value = false
  }
}

// 路由参数变化时重置并重新加载
watch(
  () => userId.value,
  () => {
    activeTab.value = 'posts'
    postsPage.current = 1
    postsPage.total = 0
    commentsPage.current = 1
    commentsPage.total = 0
    commentsList.value = []
    clubsList.value = []
    isFollowed.value = false
    loadProfile()
    loadPosts()
  }
)

onMounted(() => {
  loadProfile()
  loadPosts()
})
</script>

<style scoped>
.public-profile-page {
  max-width: 900px;
  margin: 0 auto;
}

/* === Profile Header === */
.profile-header {
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-2);
  margin-bottom: var(--space-5);
}

.header-cover {
  position: relative;
  height: 120px;
  background: var(--gradient-ocean);
  overflow: hidden;
}

.cover-decoration {
  position: absolute;
  top: -60%;
  right: 5%;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18) 0%, transparent 65%);
  border-radius: 50%;
  pointer-events: none;
}

.header-body {
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: var(--space-5);
  padding: 0 var(--space-6) var(--space-5);
}

.user-avatar {
  flex-shrink: 0;
  margin-top: -48px;
  border: 4px solid var(--color-bg-card);
  box-shadow: var(--shadow-2);
  --el-avatar-bg-color: transparent;
  background: var(--gradient-primary);
  color: var(--color-white);
  font-family: var(--font-display);
  font-weight: var(--font-weight-extrabold);
  font-size: var(--font-size-h1);
}

.user-meta {
  flex: 1;
  min-width: 200px;
  padding-bottom: var(--space-1);
}

.user-name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.user-nickname {
  font-family: var(--font-heading);
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  line-height: var(--line-height-tight);
}

.user-bio {
  font-size: var(--font-size-body);
  color: var(--color-text-2);
  line-height: var(--line-height-relaxed);
  margin-bottom: var(--space-2);
}

.user-extra {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.extra-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

/* === Stats === */
.user-stats {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-2) 0;
  flex-shrink: 0;
}

.stat-item {
  text-align: center;
  padding: 0 var(--space-2);
}

.stat-value {
  font-size: var(--font-size-h1);
  color: var(--color-text-1);
  line-height: 1;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin-top: var(--space-1);
  font-weight: var(--font-weight-medium);
}

.stat-divider {
  width: 1px;
  height: 32px;
  background: var(--color-border-light);
}

/* === Follow Button === */
.follow-action {
  flex-shrink: 0;
  padding-bottom: var(--space-1);
}

/* === Tabs === */
.profile-tabs {
  background-color: transparent;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--space-5);
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--color-border-light);
}

.profile-tabs :deep(.el-tabs__item) {
  font-family: var(--font-heading);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-body);
  color: var(--color-text-3);
  height: 44px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: var(--radius-full);
}

/* === List === */
.list-wrap {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

/* === Item Card === */
.item-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  cursor: pointer;
  transition: all var(--transition-base);
  opacity: 0;
}

.item-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-2);
  transform: translateY(-2px);
}

.post-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  margin-bottom: var(--space-2);
  transition: color var(--transition-fast);
}

.item-card:hover .post-title {
  color: var(--color-primary);
}

.post-summary,
.comment-content,
.club-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-2);
  line-height: var(--line-height-relaxed);
  margin-bottom: var(--space-3);
}

.club-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  margin-bottom: var(--space-2);
  transition: color var(--transition-fast);
}

.item-card:hover .club-title {
  color: var(--color-primary);
}

.item-footer {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.footer-item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

/* === Text ellipsis === */
.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* === Pagination === */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-5);
}

/* === Responsive === */
@media (max-width: 768px) {
  .header-body {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: var(--space-3);
    padding: 0 var(--space-4) var(--space-5);
  }

  .user-avatar {
    margin-top: -48px;
  }

  .user-meta {
    align-items: center;
    padding-bottom: 0;
  }

  .user-name-row {
    justify-content: center;
  }

  .user-extra {
    justify-content: center;
  }

  .user-stats {
    justify-content: center;
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
