<template>
  <div class="favorites-page">
    <!-- Page header -->
    <div class="page-header fade-in-up">
      <div class="section-title">我的收藏</div>
    </div>

    <div v-loading="loading" class="list-wrap fade-in-up delay-1">
      <div v-for="(post, index) in list" :key="post.id" class="post-item fade-in-up" :style="{ animationDelay: (index * 0.05) + 's' }">
        <PostCard :post="post" />
      </div>
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

/* === Page Header === */
.page-header {
  margin-bottom: var(--space-5);
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
