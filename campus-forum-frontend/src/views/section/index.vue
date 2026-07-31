<template>
  <div class="section-page" v-loading="loading">
    <!-- 板块信息头部 -->
    <el-card v-if="sectionInfo" class="section-header" shadow="never">
      <div class="section-info">
        <el-icon class="section-icon"><Files /></el-icon>
        <div class="section-meta">
          <h2 class="section-name">{{ sectionInfo.name }}</h2>
          <p v-if="sectionInfo.description" class="section-desc">{{ sectionInfo.description }}</p>
        </div>
        <div class="section-stat">
          <span class="stat-value">{{ sectionInfo.postCount || 0 }}</span>
          <span class="stat-label">帖子数</span>
        </div>
      </div>
    </el-card>

    <!-- 帖子列表 -->
    <div class="section-posts">
      <PostList :section-id="sectionId" sort="latest" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import PostList from '@/components/PostList.vue'
import { getSections } from '@/api/post'

const route = useRoute()

// 从路由参数获取板块 ID
const sectionId = computed(() => route.params.id)

// 板块信息
const sectionInfo = ref(null)
// 加载状态
const loading = ref(false)

// 获取板块信息：拉取全部板块后筛选当前板块
const fetchSectionInfo = async () => {
  loading.value = true
  try {
    const list = await getSections()
    const found = (list || []).find((item) => String(item.id) === String(sectionId.value))
    sectionInfo.value = found || null
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    sectionInfo.value = null
  } finally {
    loading.value = false
  }
}

// 路由参数变化时重新加载板块信息
watch(
  () => sectionId.value,
  () => {
    fetchSectionInfo()
  }
)

onMounted(() => {
  fetchSectionInfo()
})
</script>

<style scoped>
.section-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 板块头部 */
.section-header {
  border-radius: 6px;
}

.section-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.section-icon {
  font-size: 40px;
  color: #409eff;
  flex-shrink: 0;
}

.section-meta {
  flex: 1;
  min-width: 0;
}

.section-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.section-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}

.section-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-left: 16px;
  border-left: 1px solid #ebeef5;
}

.section-stat .stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #409eff;
}

.section-stat .stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
