<template>
  <div class="home-page">
    <!-- 顶部公告轮播 -->
    <el-card class="banner-card fade-in-up" shadow="never" :body-style="{ padding: '0' }">
      <el-carousel height="200px" indicator-position="outside" arrow="hover">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-item" :class="'banner-' + index">
            <div class="banner-deco banner-deco-1"></div>
            <div class="banner-deco banner-deco-2"></div>
            <div class="banner-text">
              <span class="banner-badge">校园动态</span>
              <h3 class="banner-title">{{ banner.title }}</h3>
              <p class="banner-desc">{{ banner.desc }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 主体两栏布局 -->
    <el-row :gutter="24" class="home-main">
      <!-- 左侧主内容区：帖子列表 -->
      <el-col :xs="24" :sm="24" :md="17">
        <div class="main-col fade-in-up delay-1">
          <PostList sort="latest" />
        </div>
      </el-col>

      <!-- 右侧侧边栏 -->
      <el-col :xs="24" :sm="24" :md="7">
        <div class="side-col">
          <!-- 签到卡片 -->
          <el-card class="side-card sign-card fade-in-up delay-2" shadow="never">
            <div class="sign-glow"></div>
            <template #header>
              <div class="card-header">
                <el-icon><Calendar /></el-icon>
                <span>每日签到</span>
              </div>
            </template>

            <div class="sign-content">
              <div class="sign-status">
                <span class="sign-label">今日状态</span>
                <el-tag
                  :type="signInStatus.signedToday ? 'success' : 'info'"
                  effect="dark"
                  round
                  size="small"
                >
                  {{ signInStatus.signedToday ? '已签到' : '未签到' }}
                </el-tag>
              </div>
              <div class="sign-stats">
                <div class="stat-block">
                  <div class="stat-value gradient-text">{{ signInStatus.continuousDays || 0 }}</div>
                  <div class="stat-name">连续天数</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-block">
                  <div class="stat-value gradient-text">{{ signInStatus.totalPoints || 0 }}</div>
                  <div class="stat-name">总积分</div>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-block">
                  <div class="stat-value gradient-text">Lv.{{ signInStatus.level || 0 }}</div>
                  <div class="stat-name">等级</div>
                </div>
              </div>
              <el-button
                v-if="!signInStatus.signedToday"
                type="primary"
                class="sign-btn"
                :loading="signLoading"
                @click="handleSignIn"
              >
                立即签到
              </el-button>
              <el-button v-else type="success" class="sign-btn" disabled>今日已签到</el-button>
            </div>
          </el-card>

          <!-- 热门标签卡片 -->
          <el-card class="side-card tags-card fade-in-up delay-3" shadow="never">
            <template #header>
              <div class="card-header">
                <el-icon><PriceTag /></el-icon>
                <span>热门标签</span>
              </div>
            </template>
            <div class="tags-wrap">
              <el-tag
                v-for="(tag, i) in hotTags"
                :key="tag.id"
                class="hot-tag"
                :class="'chip-' + (i % 5)"
                effect="plain"
                @click="goTagSearch(tag.id)"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PostList from '@/components/PostList.vue'
import { useUserStore } from '@/store/user'
import { getSignInStatus, signIn } from '@/api/signin'

const router = useRouter()
const userStore = useUserStore()

// 顶部轮播公告位（静态，渐变背景由 CSS 类 banner-0/1/2 控制）
const banners = [
  { title: '欢迎来到校园论坛', desc: '在这里分享你的校园生活与学习心得' },
  { title: '遵守社区规范', desc: '友善发言，共建良好交流氛围' },
  { title: '每日签到领积分', desc: '坚持签到，提升等级解锁更多权益' }
]

// 热门标签（静态常用标签）
const hotTags = [
  { id: 1, name: '学习交流' },
  { id: 2, name: '校园生活' },
  { id: 3, name: '求职招聘' },
  { id: 4, name: '情感吐槽' },
  { id: 5, name: '二手交易' },
  { id: 6, name: '活动预告' },
  { id: 7, name: '求助问答' },
  { id: 8, name: '技术分享' }
]

// 签到状态
const signInStatus = reactive({
  signedToday: false,
  continuousDays: 0,
  totalPoints: 0,
  level: 0
})
// 签到按钮加载状态
const signLoading = ref(false)

// 获取签到状态
const fetchSignInStatus = async () => {
  try {
    const data = await getSignInStatus()
    Object.assign(signInStatus, {
      signedToday: data.signedToday || false,
      continuousDays: data.continuousDays || 0,
      totalPoints: data.totalPoints || 0,
      level: data.level || 0
    })
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 点击签到
const handleSignIn = async () => {
  signLoading.value = true
  try {
    const data = await signIn()
    // 签到成功后更新本地状态
    signInStatus.signedToday = true
    signInStatus.continuousDays = data.continuousDays ?? signInStatus.continuousDays
    signInStatus.totalPoints = data.totalPoints ?? signInStatus.totalPoints
    signInStatus.level = data.level ?? signInStatus.level
    ElMessage.success(`签到成功，获得 ${data.points || 0} 积分`)
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    signLoading.value = false
  }
}

// 点击标签跳转搜索
const goTagSearch = (tagId) => {
  router.push({ path: '/search', query: { tag: tagId } })
}

onMounted(() => {
  // 仅登录用户拉取签到状态，游客跳过避免触发 401
  if (userStore.isLoggedIn) {
    fetchSignInStatus()
  }
})
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ===== 轮播公告 ===== */
.banner-card {
  border-radius: var(--radius-2xl);
  overflow: hidden;
  border: none;
  box-shadow: var(--shadow-2);
}

.banner-item {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 var(--space-8);
  overflow: hidden;
}

.banner-0 { background: var(--gradient-ocean); }
.banner-1 { background: var(--gradient-sunset); }
.banner-2 { background: var(--gradient-primary); }

.banner-deco {
  position: absolute;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.12);
  pointer-events: none;
}

.banner-deco-1 {
  width: 220px;
  height: 220px;
  right: -60px;
  top: -80px;
}

.banner-deco-2 {
  width: 140px;
  height: 140px;
  right: 80px;
  bottom: -70px;
  background: rgba(255, 255, 255, 0.08);
}

.banner-text {
  position: relative;
  z-index: 1;
  color: #fff;
  max-width: 70%;
}

.banner-badge {
  display: inline-block;
  padding: 3px 12px;
  border-radius: var(--radius-full);
  background: rgba(255, 255, 255, 0.22);
  backdrop-filter: blur(4px);
  font-size: var(--font-size-caption);
  font-weight: var(--font-weight-medium);
  margin-bottom: var(--space-3);
}

.banner-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  margin: 0 0 var(--space-2);
  line-height: var(--line-height-tight);
}

.banner-desc {
  font-size: var(--font-size-body);
  opacity: 0.92;
  margin: 0;
}

/* ===== 主体布局 ===== */
.home-main {
  align-items: flex-start;
}

.main-col {
  min-width: 0;
}

.side-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* ===== 侧边栏卡片通用 ===== */
.side-card {
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.side-card :deep(.el-card__header) {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border-lighter);
}

.side-card :deep(.el-card__body) {
  padding: var(--space-5);
}

.card-header {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
}

.card-header .el-icon {
  color: var(--color-primary);
  font-size: 18px;
}

/* ===== 签到卡片 ===== */
.sign-card {
  position: relative;
}

.sign-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: var(--gradient-primary);
}

.sign-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.sign-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sign-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.sign-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  text-align: center;
  padding: var(--space-3) 0;
  background: var(--color-bg-subtle);
  border-radius: var(--radius-lg);
}

.stat-block {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-extrabold);
  line-height: 1;
}

.stat-name {
  font-size: var(--font-size-caption);
  color: var(--color-text-3);
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: var(--color-border-light);
}

.sign-btn {
  width: 100%;
  height: 40px;
  border-radius: var(--radius-lg);
  font-weight: var(--font-weight-semibold);
}

/* ===== 热门标签 ===== */
.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.hot-tag {
  cursor: pointer;
  border: none;
  border-radius: var(--radius-full);
  padding: 5px 14px;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.hot-tag:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-2);
}

.chip-0 { background: var(--color-primary-tag-bg); color: var(--color-primary); }
.chip-1 { background: var(--color-success-tag-bg); color: var(--color-success); }
.chip-2 { background: var(--color-warning-tag-bg); color: var(--color-warning); }
.chip-3 { background: var(--color-violet-tag-bg); color: var(--color-violet); }
.chip-4 { background: var(--color-teal-tag-bg); color: var(--color-teal); }

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .banner-item {
    padding: 0 var(--space-5);
  }
  .banner-text {
    max-width: 100%;
  }
  .banner-title {
    font-size: var(--font-size-h1);
  }
  .home-main {
    gap: var(--space-4);
  }
  .sign-stats {
    flex-direction: row;
  }
}
</style>
