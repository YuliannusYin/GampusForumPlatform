<template>
  <div class="section-page" v-loading="loading">
    <!-- Section header banner -->
    <div v-if="sectionInfo" class="section-banner fade-in-up">
      <div class="banner-inner">
        <div class="banner-left">
          <div class="banner-icon-wrap">
            <el-icon class="banner-icon"><Files /></el-icon>
          </div>
          <div class="banner-meta">
            <h2 class="banner-name">{{ sectionInfo.name }}</h2>
            <p v-if="sectionInfo.description" class="banner-desc">{{ sectionInfo.description }}</p>
          </div>
        </div>
        <div class="banner-stat">
          <span class="stat-number banner-stat-value">{{ sectionInfo.postCount || 0 }}</span>
          <span class="banner-stat-label">帖子数</span>
        </div>
      </div>
    </div>

    <!-- Posts list -->
    <div class="section-posts fade-in-up delay-1">
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
  gap: var(--space-4);
}

/* === Section Banner === */
.section-banner {
  position: relative;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary);
}

.section-banner::before {
  content: "";
  position: absolute;
  top: -50%;
  right: -10%;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  pointer-events: none;
}

.banner-inner {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-6);
  padding: var(--space-8) var(--space-6);
}

.banner-left {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  min-width: 0;
  flex: 1;
}

.banner-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.banner-icon {
  font-size: 32px;
  color: var(--color-white);
}

.banner-meta {
  min-width: 0;
  flex: 1;
}

.banner-name {
  font-family: var(--font-heading);
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-extrabold);
  color: var(--color-white);
  line-height: var(--line-height-tight);
  margin-bottom: var(--space-2);
}

.banner-desc {
  font-size: var(--font-size-body);
  color: rgba(255, 255, 255, 0.85);
  line-height: var(--line-height-normal);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.banner-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
  padding-left: var(--space-6);
  border-left: 1px solid rgba(255, 255, 255, 0.25);
  flex-shrink: 0;
}

.banner-stat-value {
  font-size: var(--font-size-display);
  color: var(--color-white);
  line-height: 1;
}

.banner-stat-label {
  font-size: var(--font-size-sm);
  color: rgba(255, 255, 255, 0.8);
  font-weight: var(--font-weight-medium);
}

/* === Posts === */
.section-posts {
  display: flex;
  flex-direction: column;
}

/* === Responsive === */
@media (max-width: 768px) {
  .banner-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: var(--space-6) var(--space-4);
  }

  .banner-left {
    width: 100%;
  }

  .banner-icon-wrap {
    width: 52px;
    height: 52px;
  }

  .banner-icon {
    font-size: 26px;
  }

  .banner-stat {
    flex-direction: row;
    gap: var(--space-2);
    padding-left: 0;
    border-left: none;
    border-top: 1px solid rgba(255, 255, 255, 0.25);
    padding-top: var(--space-4);
    width: 100%;
    align-items: center;
  }

  .banner-stat-value {
    font-size: var(--font-size-h2);
  }
}
</style>
