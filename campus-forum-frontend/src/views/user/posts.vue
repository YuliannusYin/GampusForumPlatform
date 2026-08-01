<template>
  <div class="posts-page">
    <!-- Page header -->
    <div class="page-header fade-in-up">
      <div class="section-title">我的内容</div>
    </div>

    <el-tabs v-model="activeTab" class="posts-tabs fade-in-up delay-1" @tab-change="handleTabChange">
      <!-- 我的发帖 -->
      <el-tab-pane label="我的发帖" name="posts">
        <div v-loading="postsLoading" class="list-wrap">
          <div v-for="(post, index) in postsList" :key="post.id" class="post-item fade-in-up" :style="{ animationDelay: (index * 0.05) + 's' }">
            <PostCard :post="post" />
          </div>
          <el-empty v-if="!postsLoading && postsList.length === 0" description="还没有发过帖子" />
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

      <!-- 我的点赞 -->
      <el-tab-pane label="我的点赞" name="likes">
        <div v-loading="likesLoading" class="list-wrap">
          <div v-for="(post, index) in likesList" :key="post.id" class="post-item fade-in-up" :style="{ animationDelay: (index * 0.05) + 's' }">
            <PostCard :post="post" />
          </div>
          <el-empty v-if="!likesLoading && likesList.length === 0" description="还没有点赞过帖子" />
        </div>
        <div class="pagination-wrap" v-if="likesPage.total > 0">
          <el-pagination
            v-model:current-page="likesPage.current"
            v-model:page-size="likesPage.size"
            :total="likesPage.total"
            layout="total, prev, pager, next"
            @current-change="loadLikes"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PostCard from '@/components/PostCard.vue'
import { getMyPosts, getMyLikes } from '@/api/user'

const activeTab = ref('posts')

// ===== 我的发帖 =====
const postsLoading = ref(false)
const postsList = ref([])
const postsPage = reactive({ current: 1, size: 10, total: 0 })

const loadPosts = async () => {
  postsLoading.value = true
  try {
    const res = await getMyPosts({ page: postsPage.current, size: postsPage.size })
    postsList.value = res.records || []
    postsPage.total = res.total || 0
  } catch (e) {
    // 忽略
  } finally {
    postsLoading.value = false
  }
}

// ===== 我的点赞 =====
const likesLoading = ref(false)
const likesList = ref([])
const likesPage = reactive({ current: 1, size: 10, total: 0 })

const loadLikes = async () => {
  likesLoading.value = true
  try {
    const res = await getMyLikes({ page: likesPage.current, size: likesPage.size })
    likesList.value = res.records || []
    likesPage.total = res.total || 0
  } catch (e) {
    // 忽略
  } finally {
    likesLoading.value = false
  }
}

// 切换 tab 时懒加载点赞列表
const handleTabChange = (name) => {
  if (name === 'likes' && likesList.value.length === 0) {
    loadLikes()
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.posts-page {
  max-width: 900px;
  margin: 0 auto;
}

/* === Page Header === */
.page-header {
  margin-bottom: var(--space-5);
}

/* === Tabs === */
.posts-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--space-5);
}

.posts-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--color-border-light);
}

.posts-tabs :deep(.el-tabs__item) {
  font-family: var(--font-heading);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-body);
  color: var(--color-text-3);
  height: 44px;
}

.posts-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.posts-tabs :deep(.el-tabs__active-bar) {
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

.post-item {
  opacity: 0;
}

/* === Pagination === */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-5);
}

/* === Responsive === */
@media (max-width: 768px) {
  .pagination-wrap {
    justify-content: center;
  }
}
</style>
