<template>
  <div class="favorites-page">
    <div class="page-header">
      <h3 class="page-title">我的收藏</h3>
    </div>

    <div v-loading="loading" class="list-wrap">
      <PostCard v-for="post in list" :key="post.id" :post="post" />
      <el-empty v-if="!loading && list.length === 0" description="还没有收藏任何帖子" />
    </div>

    <div class="pagination-wrap" v-if="page.total > 0">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import PostCard from '@/components/PostCard.vue'
import { getMyFavorites } from '@/api/user'

const loading = ref(false)
const list = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

// 拉取收藏列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyFavorites({ page: page.current, size: page.size })
    list.value = res.records || []
    page.total = res.total || 0
  } catch (e) {
    // 忽略
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.favorites-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 16px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.list-wrap {
  min-height: 200px;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
