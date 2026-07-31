<template>
  <div class="search-page">
    <!-- 搜索框 -->
    <el-card class="search-bar-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="请输入关键字搜索帖子"
          clearable
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" :loading="loading" @click="handleSearch">搜索</el-button>
      </div>
      <!-- 当前标签过滤提示 -->
      <div v-if="activeTag" class="tag-filter">
        <span>当前标签：</span>
        <el-tag closable @close="clearTag">标签 #{{ activeTag }}</el-tag>
      </div>
    </el-card>

    <!-- 搜索结果 -->
    <el-card class="result-card" shadow="never">
      <div class="result-header">
        <span class="result-title">
          搜索结果<template v-if="total > 0">（共 {{ total }} 条）</template>
        </span>
      </div>

      <div v-loading="loading">
        <!-- 结果列表 -->
        <div v-if="resultList.length" class="result-list">
          <PostCard v-for="post in resultList" :key="post.id" :post="post" />
        </div>

        <!-- 空状态 -->
        <el-empty v-else-if="!loading" description="暂无搜索结果" />
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
    </el-card>
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
  gap: 16px;
}

/* 搜索栏 */
.search-bar-card {
  border-radius: 6px;
}

.search-bar {
  display: flex;
  gap: 12px;
}

.search-bar .el-input {
  flex: 1;
}

.tag-filter {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
}

/* 结果卡片 */
.result-card {
  border-radius: 6px;
}

.result-header {
  margin-bottom: 12px;
}

.result-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.result-list {
  display: flex;
  flex-direction: column;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
