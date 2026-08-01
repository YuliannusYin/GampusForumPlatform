<template>
  <div class="home-page">
    <!-- 顶部公告位 -->
    <el-card class="banner-card" shadow="never" :body-style="{ padding: '0' }">
      <el-carousel height="160px" indicator-position="outside">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="banner-item" :style="{ background: banner.bg }">
            <h3 class="banner-title">{{ banner.title }}</h3>
            <p class="banner-desc">{{ banner.desc }}</p>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 主体两栏布局 -->
    <el-row :gutter="20" class="home-main">
      <!-- 左侧主内容区：帖子列表 -->
      <el-col :xs="24" :sm="24" :md="18">
        <PostList sort="latest" />
      </el-col>

      <!-- 右侧侧边栏 -->
      <el-col :xs="24" :sm="24" :md="6">
        <!-- 签到卡片 -->
        <el-card class="side-card sign-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><Calendar /></el-icon>
              <span>每日签到</span>
            </div>
          </template>

          <div class="sign-content">
            <div class="sign-status">
              <span class="sign-label">今日状态</span>
              <el-tag :type="signInStatus.signedToday ? 'success' : 'info'" effect="dark">
                {{ signInStatus.signedToday ? '已签到' : '未签到' }}
              </el-tag>
            </div>
            <div class="sign-stats">
              <div class="stat-block">
                <div class="stat-value">{{ signInStatus.continuousDays || 0 }}</div>
                <div class="stat-name">连续天数</div>
              </div>
              <div class="stat-block">
                <div class="stat-value">{{ signInStatus.totalPoints || 0 }}</div>
                <div class="stat-name">总积分</div>
              </div>
              <div class="stat-block">
                <div class="stat-value">Lv.{{ signInStatus.level || 0 }}</div>
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
              签到
            </el-button>
            <el-button v-else type="success" class="sign-btn" disabled>已签到</el-button>
          </div>
        </el-card>

        <!-- 热门标签卡片 -->
        <el-card class="side-card tags-card" shadow="never">
          <template #header>
            <div class="card-header">
              <el-icon><PriceTag /></el-icon>
              <span>热门标签</span>
            </div>
          </template>
          <div class="tags-wrap">
            <el-tag
              v-for="tag in hotTags"
              :key="tag.id"
              class="hot-tag"
              effect="plain"
              @click="goTagSearch(tag.id)"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </el-card>
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

// 顶部轮播公告位（静态）
const banners = [
  {
    title: '欢迎来到校园论坛',
    desc: '在这里分享你的校园生活与学习心得',
    bg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    title: '遵守社区规范',
    desc: '友善发言，共建良好交流氛围',
    bg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    title: '每日签到领积分',
    desc: '坚持签到，提升等级解锁更多权益',
    bg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
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
  gap: 16px;
}

/* 轮播公告 */
.banner-card {
  border-radius: 6px;
  overflow: hidden;
}

.banner-item {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  text-align: center;
}

.banner-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.banner-desc {
  font-size: 14px;
  opacity: 0.9;
}

/* 主体布局 */
.home-main {
  align-items: flex-start;
}

/* 侧边栏卡片 */
.side-card {
  margin-bottom: 16px;
  border-radius: 6px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

/* 签到卡片 */
.sign-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.sign-status {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sign-label {
  font-size: 13px;
  color: #909399;
}

.sign-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.stat-name {
  font-size: 12px;
  color: #909399;
}

.sign-btn {
  width: 100%;
}

/* 热门标签 */
.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  cursor: pointer;
}

.hot-tag:hover {
  color: #409eff;
  border-color: #409eff;
}
</style>
