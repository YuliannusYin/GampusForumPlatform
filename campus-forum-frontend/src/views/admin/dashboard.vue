<template>
  <div class="dashboard-page" v-loading="overviewLoading">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="card in statCards" :key="card.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-body">
            <el-icon class="stat-icon" :style="{ color: card.color, backgroundColor: card.bg }">
              <component :is="card.icon" />
            </el-icon>
            <div class="stat-meta">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-label">{{ card.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="chart-row">
      <!-- 发帖趋势 -->
      <el-col :xs="24" :lg="14">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">发帖趋势</span>
              <el-radio-group v-model="trendDays" size="small" @change="loadTrend">
                <el-radio-button :value="7">近7天</el-radio-button>
                <el-radio-button :value="30">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <!-- 板块分布 -->
      <el-col :xs="24" :lg="10">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">板块帖子分布</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
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

// 统计卡片配置
const statCards = ref([
  { key: 'totalUsers', label: '总用户数', value: 0, icon: 'User', color: '#409eff', bg: '#ecf5ff' },
  { key: 'totalPosts', label: '总帖子数', value: 0, icon: 'Document', color: '#67c23a', bg: '#f0f9eb' },
  { key: 'totalComments', label: '总评论数', value: 0, icon: 'ChatDotRound', color: '#e6a23c', bg: '#fdf6ec' },
  { key: 'todayNewUsers', label: '今日新增用户', value: 0, icon: 'UserFilled', color: '#f56c6c', bg: '#fef0f0' },
  { key: 'todayNewPosts', label: '今日新增帖子', value: 0, icon: 'EditPen', color: '#909399', bg: '#f4f4f5' }
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
          itemStyle: { color: '#409eff' },
          areaStyle: { color: 'rgba(64, 158, 255, 0.15)' }
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
  gap: 20px;
}

/* 统计卡片 */
.stat-row {
  margin-bottom: 0;
}

.stat-card {
  margin-bottom: 20px;
  border-radius: 6px;
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  font-size: 28px;
  border-radius: 8px;
  flex-shrink: 0;
}

.stat-meta {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 26px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 图表区 */
.chart-row {
  margin-bottom: 0;
}

.chart-card {
  border-radius: 6px;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.chart-box {
  width: 100%;
  height: 360px;
}
</style>
