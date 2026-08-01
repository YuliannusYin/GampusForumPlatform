<template>
  <div class="post-list" v-loading="loading">
    <!-- Sort bar -->
    <div class="sort-bar">
      <button
        v-for="opt in sortOptions"
        :key="opt.value"
        class="sort-btn"
        :class="{ active: currentSort === opt.value }"
        @click="changeSort(opt.value)"
      >
        <el-icon><component :is="opt.icon" /></el-icon>
        {{ opt.label }}
      </button>
    </div>

    <!-- Post list -->
    <div v-if="postList.length" class="list-wrap">
      <PostCard
        v-for="(post, index) in postList"
        :key="post.id"
        :post="post"
        class="fade-in-up"
        :style="{ animationDelay: `${index * 0.04}s` }"
      />
    </div>

    <!-- Empty -->
    <div v-else-if="!loading" class="empty-state">
      <div class="empty-icon">
        <svg viewBox="0 0 120 120" width="100" height="100" fill="none">
          <circle cx="60" cy="60" r="50" fill="#EBF1FF"/>
          <rect x="35" y="40" width="50" height="40" rx="6" fill="#fff" stroke="#1664FF" stroke-width="2"/>
          <line x1="42" y1="52" x2="68" y2="52" stroke="#97BCFF" stroke-width="3" stroke-linecap="round"/>
          <line x1="42" y1="60" x2="78" y2="60" stroke="#97BCFF" stroke-width="3" stroke-linecap="round"/>
          <line x1="42" y1="68" x2="60" y2="68" stroke="#97BCFF" stroke-width="3" stroke-linecap="round"/>
          <circle cx="88" cy="78" r="14" fill="#1664FF"/>
          <path d="M83 78l3.5 3.5L93 74" stroke="#fff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <p class="empty-text">暂无帖子，快来发布第一篇吧</p>
    </div>

    <!-- Pagination -->
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
import { ref, watch, onMounted, markRaw } from 'vue'
import { Clock, TrendCharts } from '@element-plus/icons-vue'
import { getPosts } from '@/api/post'
import PostCard from '@/components/PostCard.vue'

const props = defineProps({
  sectionId: {
    type: [Number, String],
    default: ''
  },
  sort: {
    type: String,
    default: 'latest'
  }
})

const sortOptions = [
  { value: 'latest', label: '最新', icon: markRaw(Clock) },
  { value: 'hot', label: '最热', icon: markRaw(TrendCharts) }
]

const currentSort = ref(props.sort)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const postList = ref([])
const loading = ref(false)

const fetchPosts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      sort: currentSort.value
    }
    if (props.sectionId !== '' && props.sectionId !== undefined && props.sectionId !== null) {
      params.sectionId = props.sectionId
    }
    const res = await getPosts(params)
    postList.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    postList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const changeSort = (val) => {
  if (val === currentSort.value) return
  currentSort.value = val
  currentPage.value = 1
  fetchPosts()
}

const handlePageChange = () => {
  fetchPosts()
}

watch(() => props.sectionId, () => {
  currentPage.value = 1
  fetchPosts()
})

watch(() => props.sort, (val) => {
  if (val !== currentSort.value) {
    currentSort.value = val
    currentPage.value = 1
    fetchPosts()
  }
})

onMounted(() => {
  fetchPosts()
})
</script>

<style scoped>
.post-list {
  min-height: 200px;
}

/* Sort bar */
.sort-bar {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  margin-bottom: var(--space-4);
  padding: var(--space-1);
  background: var(--color-bg-card);
  border-radius: var(--radius-full);
  border: 1px solid var(--color-border-light);
  width: fit-content;
}

.sort-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px var(--space-4);
  border: none;
  background: transparent;
  border-radius: var(--radius-full);
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-3);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: var(--font-body);
}

.sort-btn:hover {
  color: var(--color-text-1);
  background: var(--color-bg-hover);
}

.sort-btn.active {
  background: var(--gradient-primary);
  color: #fff;
  box-shadow: var(--shadow-primary);
}

.sort-btn .el-icon {
  font-size: 15px;
}

/* List */
.list-wrap {
  display: flex;
  flex-direction: column;
}

/* Empty */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.empty-icon {
  margin-bottom: var(--space-4);
}

.empty-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

/* Pagination */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-6);
}

@media (max-width: 768px) {
  .sort-bar {
    width: 100%;
  }

  .sort-btn {
    flex: 1;
    justify-content: center;
  }
}
</style>
