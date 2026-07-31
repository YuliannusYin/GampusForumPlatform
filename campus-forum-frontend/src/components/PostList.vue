<template>
  <div class="post-list" v-loading="loading">
    <!-- 排序切换 -->
    <div class="sort-bar">
      <el-radio-group v-model="currentSort" size="default" @change="handleSortChange">
        <el-radio-button value="latest">最新</el-radio-button>
        <el-radio-button value="hot">最热</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 帖子列表 -->
    <div v-if="postList.length" class="list-wrap">
      <PostCard v-for="post in postList" :key="post.id" :post="post" />
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="!loading" description="暂无帖子" />

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
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { getPosts } from '@/api/post'
import PostCard from '@/components/PostCard.vue'

const props = defineProps({
  // 板块 ID（可选，传入则只查该板块的帖子）
  sectionId: {
    type: [Number, String],
    default: ''
  },
  // 排序方式：latest 最新 / hot 最热
  sort: {
    type: String,
    default: 'latest'
  }
})

// 当前排序
const currentSort = ref(props.sort)
// 当前页码
const currentPage = ref(1)
// 每页条数
const pageSize = ref(10)
// 帖子总数
const total = ref(0)
// 帖子列表
const postList = ref([])
// 加载状态
const loading = ref(false)

// 拉取帖子列表
const fetchPosts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: currentSort.value
    }
    // 板块过滤
    if (props.sectionId !== '' && props.sectionId !== undefined && props.sectionId !== null) {
      params.sectionId = props.sectionId
    }
    const res = await getPosts(params)
    // 兼容后端返回结构：{ records, total, page, size }
    postList.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    postList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 排序切换
const handleSortChange = () => {
  currentPage.value = 1
  fetchPosts()
}

// 翻页
const handlePageChange = () => {
  fetchPosts()
}

// 监听 sectionId 变化（板块切换时重置并重新加载）
watch(
  () => props.sectionId,
  () => {
    currentPage.value = 1
    fetchPosts()
  }
)

// 监听外部 sort 变化，同步内部状态
watch(
  () => props.sort,
  (val) => {
    if (val !== currentSort.value) {
      currentSort.value = val
      currentPage.value = 1
      fetchPosts()
    }
  }
)

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.post-list {
  min-height: 200px;
}

/* 排序栏 */
.sort-bar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 12px;
  padding: 10px 12px;
  background-color: #fff;
  border-radius: 4px;
}

/* 列表区域 */
.list-wrap {
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
