<template>
  <div class="posts-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div>
        <h1 class="page-title">帖子管理</h1>
        <p class="page-subtitle">管理平台帖子、置顶与加精</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="filter-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="关键词">
          <el-input
            v-model="query.keyword"
            placeholder="标题关键词"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="板块">
          <el-select
            v-model="query.sectionId"
            placeholder="全部板块"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="sec in sections"
              :key="sec.id"
              :label="sec.name"
              :value="sec.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="正常" :value="0" />
            <el-option label="隐藏" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 帖子表格 -->
    <div class="table-card">
      <el-table :data="list" v-loading="loading" class="admin-table">
        <el-table-column label="标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="openPost(row)">
              {{ row.title }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="sectionName" label="板块" width="120" show-overflow-tooltip />
        <el-table-column prop="viewCount" label="浏览" width="80" align="center" />
        <el-table-column prop="likeCount" label="点赞" width="80" align="center" />
        <el-table-column prop="commentCount" label="评论" width="80" align="center" />
        <el-table-column label="置顶" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isTop" type="danger" size="small" class="status-chip">是</el-tag>
            <span v-else class="text-muted">否</span>
          </template>
        </el-table-column>
        <el-table-column label="精华" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isEssence" type="warning" size="small" class="status-chip">是</el-tag>
            <span v-else class="text-muted">否</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="success" size="small" class="status-chip">正常</el-tag>
            <el-tag v-else type="info" size="small" class="status-chip">隐藏</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              :type="row.isTop ? 'warning' : 'primary'"
              size="small"
              link
              @click="handleToggleTop(row)"
            >
              {{ row.isTop ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button
              :type="row.isEssence ? 'warning' : 'primary'"
              size="small"
              link
              @click="handleToggleEssence(row)"
            >
              {{ row.isEssence ? '取消加精' : '加精' }}
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="loadList"
          @size-change="handleSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { formatTime } from '@/utils/format'
import { getSections } from '@/api/post'
import {
  getAdminPosts,
  deleteAdminPost,
  updatePostTop,
  updatePostEssence
} from '@/api/admin'

// 查询条件
const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  sectionId: undefined,
  status: undefined
})
// 列表数据
const list = ref([])
const total = ref(0)
const loading = ref(false)
// 板块列表
const sections = ref([])

// 加载板块下拉
const loadSections = async () => {
  try {
    const data = await getSections()
    sections.value = data || []
  } catch (err) {
    sections.value = []
  }
}

// 加载帖子列表
const loadList = async () => {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      sectionId: query.sectionId || undefined,
      status: query.status === '' ? undefined : query.status
    }
    const res = await getAdminPosts(params)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  query.page = 1
  loadList()
}

// 重置
const handleReset = () => {
  query.keyword = ''
  query.sectionId = undefined
  query.status = undefined
  query.page = 1
  loadList()
}

// 每页条数变化
const handleSizeChange = () => {
  query.page = 1
  loadList()
}

// 新窗口打开帖子详情
const openPost = (row) => {
  const url = `${window.location.origin}/post/${row.id}`
  window.open(url, '_blank')
}

// 置顶/取消置顶
const handleToggleTop = async (row) => {
  const target = row.isTop ? 0 : 1
  try {
    await updatePostTop(row.id, { isTop: target })
    ElMessage.success(target === 1 ? '置顶成功' : '取消置顶成功')
    loadList()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 加精/取消加精
const handleToggleEssence = async (row) => {
  const target = row.isEssence ? 0 : 1
  try {
    await updatePostEssence(row.id, { isEssence: target })
    ElMessage.success(target === 1 ? '加精成功' : '取消加精成功')
    loadList()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 删除帖子
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除帖子「${row.title}」吗？此操作不可恢复。`, '提示', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteAdminPost(row.id)
        ElMessage.success('删除成功')
        loadList()
      } catch (err) {
        // 错误已由 request.js 拦截器统一提示
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadSections()
  loadList()
})
</script>

<style scoped>
.posts-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
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

/* ===== 筛选卡片 ===== */
.filter-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  box-shadow: var(--shadow-1);
}

.filter-card :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: var(--space-4);
}

.filter-card :deep(.el-form-item__label) {
  color: var(--color-text-2);
  font-weight: var(--font-weight-medium);
}

/* ===== 表格卡片 ===== */
.table-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-1);
  overflow: hidden;
}

.admin-table {
  border-radius: var(--radius-xl);
}

.text-muted {
  color: var(--color-text-4);
}

.status-chip {
  border-radius: var(--radius-full);
}

/* ===== 分页 ===== */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding: var(--space-4) var(--space-5);
  border-top: 1px solid var(--color-border-light);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .filter-card :deep(.el-form-item) {
    margin-right: 0;
    margin-bottom: var(--space-2);
  }

  .pagination-wrap {
    justify-content: center;
  }
}
</style>
