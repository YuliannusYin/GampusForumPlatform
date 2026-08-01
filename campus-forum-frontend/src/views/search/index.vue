<template>
  <div class="search-page">
    <!-- 搜索框 -->
    <div class="search-bar-card fade-in-up">
      <div class="search-bar">
        <div class="search-input-wrap">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="keyword"
            placeholder="请输入关键字搜索帖子"
            clearable
            size="large"
            @keyup.enter="handleSearch"
          />
        </div>
        <el-button type="primary" size="large" :loading="loading" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
      <!-- 当前标签过滤提示 -->
      <div v-if="activeTag" class="tag-filter">
        <span class="filter-label">当前标签：</span>
        <el-tag closable @close="clearTag" round>标签 #{{ activeTag }}</el-tag>
      </div>
    </div>

    <!-- 过滤标签栏 -->
    <div class="filter-tabs fade-in-up delay-1">
      <div class="filter-tab active">
        <el-icon><Document /></el-icon>
        帖子
      </div>
      <div class="filter-tab disabled">
        <el-icon><User /></el-icon>
        用户
      </div>
      <div class="filter-tab disabled">
        <el-icon><PriceTag /></el-icon>
        标签
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="result-card fade-in-up delay-2">
      <div class="result-header">
        <span class="result-title">
          搜索结果<template v-if="total > 0">（共 {{ total }} 条）</template>
        </span>
        <span v-if="keyword" class="result-query">关键词： "{{ keyword }}"</span>
      </div>

      <div v-loading="loading">
        <!-- 结果列表 -->
        <div v-if="resultList.length" class="result-list">
          <PostCard v-for="(post, index) in resultList" :key="post.id" :post="post" class="result-item fade-in-up" :style="{ animationDelay: `${index * 0.04}s` }" />
        </div>

        <!-- 空状态 -->
        <div v-else-if="!loading" class="empty-state">
          <svg width="160" height="160" viewBox="0 0 160 160" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="80" cy="80" r="72" fill="var(--color-primary-bg)" />
            <circle cx="68" cy="68" r="28" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="3" />
            <path d="M88 88 L108 108" stroke="var(--color-primary)" stroke-width="4" stroke-linecap="round" />
            <path d="M58 68 L78 68" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" opacity="0.3" />
            <path d="M58 76 L72 76" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" opacity="0.2" />
            <circle cx="44" cy="120" r="5" fill="var(--color-primary)" opacity="0.15" />
            <circle cx="116" cy="48" r="4" fill="var(--color-primary)" opacity="0.1" />
          </svg>
          <p class="empty-text">暂无搜索结果</p>
          <p class="empty-subtext">试试换个关键词搜索吧</p>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10]"
          layout="prev, pager, next, total"
          background
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PostCard from '@/components/PostCard.vue'
import { searchPosts } from '@/api/post'

const route = useRoute()
const router = useRouter()

// 搜索关键字
const keyword = ref('')
// 当前激活的标签 ID
const activeTag = ref('')
// 当前页码
const currentPage = ref(1)
// 每页条数
const pageSize = ref(10)
// 结果总数
const total = ref(0)
// 结果列表
const resultList = ref([])
// 加载状态
const loading = ref(false)

// 从路由 query 初始化搜索条件
const initFromRoute = () => {
  keyword.value = route.query.keyword || ''
  activeTag.value = route.query.tag || ''
  currentPage.value = 1
  fetchResults()
}

// 执行搜索
const fetchResults = async () => {
  // 关键字与标签都为空时不发起查询
  if (!keyword.value && !activeTag.value) {
    resultList.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (keyword.value) params.keyword = keyword.value
    if (activeTag.value) params.tag = activeTag.value
    const res = await searchPosts(params)
    resultList.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    resultList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 点击搜索按钮 / 回车
const handleSearch = () => {
  currentPage.value = 1
  // 同步到 URL query，便于分享与刷新保持状态
  const query = {}
  if (keyword.value) query.keyword = keyword.value
  if (activeTag.value) query.tag = activeTag.value
  router.replace({ path: '/search', query })
  fetchResults()
}

// 翻页
const handlePageChange = () => {
  fetchResults()
}

// 清除标签过滤
const clearTag = () => {
  activeTag.value = ''
  currentPage.value = 1
  const query = {}
  if (keyword.value) query.keyword = keyword.value
  router.replace({ path: '/search', query })
  fetchResults()
}

// 监听路由 query 变化（例如点击标签跳转、顶部搜索框跳转）
watch(
  () => route.query,
  () => {
    initFromRoute()
  }
)

onMounted(() => {
  initFromRoute()
})
</script>

<style scoped>
.search-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

/* 搜索栏 */
.search-bar-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.search-bar {
  display: flex;
  gap: var(--space-3);
}

.search-input-wrap {
  position: relative;
  flex: 1;
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-3);
  z-index: 1;
  font-size: 18px;
  pointer-events: none;
}

.search-input-wrap :deep(.el-input__wrapper) {
  padding-left: 42px;
  border-radius: var(--radius-full);
}

.tag-filter {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-top: var(--space-3);
  font-size: var(--font-size-sm);
  color: var(--color-text-2);
}

.filter-label {
  font-weight: var(--font-weight-medium);
}

/* 过滤标签栏 */
.filter-tabs {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-2);
}

.filter-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all var(--transition-fast);
  background: var(--color-bg-card);
  color: var(--color-text-3);
  border: 1px solid var(--color-border-light);
}

.filter-tab.active {
  background: var(--color-primary-bg);
  color: var(--color-primary);
  border-color: var(--color-primary-3);
  box-shadow: var(--shadow-1);
}

.filter-tab.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 结果卡片 */
.result-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border-lighter);
  flex-wrap: wrap;
  gap: var(--space-2);
}

.result-title {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  font-family: var(--font-heading);
}

.result-query {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.result-item {
  opacity: 0;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.empty-text {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-2);
  margin: 0 0 var(--space-1) 0;
}

.empty-subtext {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: 0;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-5);
}

/* 响应式 */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
  }

  .search-bar-card {
    padding: var(--space-3);
  }

  .filter-tabs {
    overflow-x: auto;
    padding: 0;
  }

  .filter-tab {
    padding: var(--space-2) var(--space-3);
    font-size: var(--font-size-caption);
    white-space: nowrap;
  }

  .result-card {
    padding: var(--space-3);
  }

  .result-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
