<template>
  <div class="dashboard-page" v-loading="overviewLoading">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">数据概览</h1>
        <p class="page-subtitle">平台运营数据实时监控</p>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div
        v-for="card in statCards"
        :key="card.key"
        class="stat-card fade-in-up"
        :class="'delay-' + (statCards.indexOf(card) + 1)"
      >
        <div class="stat-card-body">
          <div class="stat-icon-wrap" :class="'grad-' + card.key">
            <el-icon class="stat-icon"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-meta">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区 -->
    <div class="chart-grid">
      <!-- 发帖趋势 -->
      <div class="chart-card chart-card--trend">
        <div class="chart-header">
          <span class="chart-title">发帖趋势</span>
          <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="trendChartRef" class="chart-box"></div>
      </div>

      <!-- 板块分布 -->
      <div class="chart-card chart-card--pie">
        <div class="chart-header">
          <span class="chart-title">板块帖子分布</span>
        </div>
        <div ref="pieChartRef" class="chart-box"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getPostTrend, getSectionDistribution } from '@/api/admin'

// 总览数据
const overview = reactive({
  totalUsers: 0,
  totalPosts: 0,
  totalComments: 0,
  todayNewUsers: 0,
  todayNewPosts: 0
})
const overviewLoading = ref(false)

// 统计卡片配置（渐变背景由 CSS 类 grad-<key> 控制）
const statCards = ref([
  { key: 'totalUsers', label: '总用户数', value: 0, icon: 'User' },
  { key: 'totalPosts', label: '总帖子数', value: 0, icon: 'Document' },
  { key: 'totalComments', label: '总评论数', value: 0, icon: 'ChatDotRound' },
  { key: 'todayNewUsers', label: '今日新增用户', value: 0, icon: 'UserFilled' },
  { key: 'todayNewPosts', label: '今日新增帖子', value: 0, icon: 'EditPen' }
])

// 趋势图相关
const trendDays = ref(7)
const trendChartRef = ref(null)
let trendChart = null

// 饼图相关
const pieChartRef = ref(null)
let pieChart = null

// 加载总览数据
const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const data = await getOverview()
    Object.assign(overview, data || {})
    // 同步到卡片
    statCards.value.forEach((card) => {
      card.value = overview[card.key] ?? 0
    })
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    overviewLoading.value = false
  }
}

// 加载发帖趋势并渲染折线图
const loadTrend = async () => {
  try {
    const list = await getPostTrend({ days: trendDays.value })
    const arr = list || []
    const dates = arr.map((item) => item.date)
    const counts = arr.map((item) => item.count)

    await nextTick()
    if (!trendChartRef.value) return
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 30 },
      xAxis: {
        type: 'category',
        data: dates,
        axisLabel: { rotate: dates.length > 10 ? 45 : 0 }
      },
      yAxis: { type: 'value', minInterval: 1 },
      series: [
        {
          name: '发帖数',
          type: 'line',
          smooth: true,
          data: counts,
          itemStyle: { color: '#1664FF' },
          areaStyle: { color: 'rgba(22, 100, 255, 0.15)' }
        }
      ]
    })
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 加载板块分布并渲染饼图
const loadDistribution = async () => {
  try {
    const list = await getSectionDistribution()
    const arr = list || []
    const pieData = arr.map((item) => ({
      name: item.sectionName,
      value: item.count
    }))

    await nextTick()
    if (!pieChartRef.value) return
    if (!pieChart) {
      pieChart = echarts.init(pieChartRef.value)
    }
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'vertical', left: 'left', type: 'scroll' },
      series: [
        {
          name: '板块分布',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          data: pieData,
          label: { formatter: '{b}: {c}' }
        }
      ]
    })
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 窗口尺寸变化时重绘图表
const handleResize = () => {
  trendChart && trendChart.resize()
  pieChart && pieChart.resize()
}

onMounted(async () => {
  await loadOverview()
  await nextTick()
  loadTrend()
  loadDistribution()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  // 销毁 ECharts 实例，避免内存泄漏
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (pieChart) {
    pieChart.dispose()
    pieChart = null
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

/* ===== 页面标题 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0;
  line-height: var(--line-height-tight);
}

.page-subtitle {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: var(--space-1) 0 0;
}

/* ===== 统计卡片 ===== */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-4);
}

.stat-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-5);
  box-shadow: var(--shadow-1);
  transition: transform var(--transition-base), box-shadow var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-3);
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.stat-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  flex-shrink: 0;
}

.stat-icon {
  font-size: 26px;
  color: var(--color-white);
}

/* Gradient backgrounds by card key */
.grad-totalUsers {
  background: var(--gradient-primary);
}

.grad-totalPosts {
  background: var(--gradient-mint);
}

.grad-totalComments {
  background: var(--gradient-gold);
}

.grad-todayNewUsers {
  background: var(--gradient-sunset);
}

.grad-todayNewPosts {
  background: var(--gradient-purple);
}

.stat-meta {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-family: var(--font-display);
  font-size: 26px;
  font-weight: var(--font-weight-extrabold);
  color: var(--color-text-1);
  line-height: 1.2;
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin-top: var(--space-1);
}

/* ===== 图表区 ===== */
.chart-grid {
  display: grid;
  grid-template-columns: 14fr 10fr;
  gap: var(--space-4);
}

.chart-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border-light);
}

.chart-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
}

.chart-box {
  width: 100%;
  height: 360px;
  padding: var(--space-3);
}

/* ===== 响应式 ===== */
@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: var(--space-4);
  }

  .stat-icon-wrap {
    width: 44px;
    height: 44px;
  }

  .stat-icon {
    font-size: 22px;
  }

  .stat-value {
    font-size: 22px;
  }

  .chart-box {
    height: 280px;
  }
}

@media (max-width: 480px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
