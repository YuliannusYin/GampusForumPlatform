<template>
  <div class="club-page">
    <!-- 顶部搜索栏 -->
    <div class="search-card fade-in-up">
      <div class="search-bar">
        <div class="search-input-wrap">
          <el-icon class="search-icon"><Search /></el-icon>
          <el-input
            v-model="keyword"
            placeholder="搜索社团名称"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button v-if="userStore.isLoggedIn" type="success" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          创建社团
        </el-button>
      </div>
    </div>

    <!-- 社团列表 -->
    <div class="club-list" v-loading="loading">
      <!-- 空状态 -->
      <div v-if="!loading && clubs.length === 0" class="empty-state fade-in-up">
        <svg width="160" height="160" viewBox="0 0 160 160" fill="none" xmlns="http://www.w3.org/2000/svg">
          <circle cx="80" cy="80" r="72" fill="var(--color-primary-bg)" />
          <rect x="52" y="50" width="56" height="56" rx="14" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="3" />
          <circle cx="80" cy="68" r="12" fill="var(--color-primary)" opacity="0.2" />
          <path d="M64 96 C64 84 70 80 80 80 C90 80 96 84 96 96" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" fill="none" opacity="0.4" />
          <circle cx="44" cy="50" r="6" fill="var(--color-primary)" opacity="0.15" />
          <circle cx="120" cy="110" r="5" fill="var(--color-primary)" opacity="0.1" />
          <rect x="68" y="60" width="24" height="3" rx="1.5" fill="var(--color-primary)" opacity="0.3" />
        </svg>
        <p class="empty-text">暂无社团</p>
        <p class="empty-subtext">快来创建第一个社团吧</p>
      </div>
      <el-row v-else :gutter="20">
        <el-col
          v-for="(club, index) in clubs"
          :key="club.id"
          :xs="24"
          :sm="12"
          :md="8"
        >
          <div
            class="club-card fade-in-up"
            :style="{ animationDelay: `${index * 0.06}s` }"
            @click="goDetail(club.id)"
          >
            <!-- 渐变头部 -->
            <div class="card-gradient-header">
              <div class="header-pattern"></div>
              <el-tag v-if="club.status === 0" type="warning" size="small" class="status-tag" round>待审核</el-tag>
              <el-tag v-else-if="club.status === 2" type="danger" size="small" class="status-tag" round>禁用</el-tag>
              <el-tag v-else type="success" size="small" class="status-tag" round>正常</el-tag>
            </div>
            <!-- 卡片内容 -->
            <div class="card-body">
              <div class="club-name-row">
                <span class="club-name">{{ club.name }}</span>
              </div>
              <div class="club-desc text-ellipsis-2">{{ club.description || '暂无简介' }}</div>
              <div class="club-footer">
                <span class="club-stat">
                  <el-icon><User /></el-icon>
                  {{ club.memberCount || 0 }}
                </span>
                <span class="club-stat">
                  <el-icon><Document /></el-icon>
                  {{ club.postCount || 0 }}
                </span>
                <span class="club-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(club.createTime) }}
                </span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrap" v-if="page.total > 0">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        layout="total, prev, pager, next"
        @current-change="fetchClubs"
      />
    </div>

    <!-- 创建社团弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建社团" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="社团名称" prop="name">
          <el-input
            v-model="form.name"
            placeholder="请输入社团名称"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="社团简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入社团简介（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getClubs, createClub } from '@/api/club'
import { formatTime } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

// 搜索关键词
const keyword = ref('')
// 社团列表
const clubs = ref([])
// 加载状态
const loading = ref(false)
// 分页
const page = reactive({ current: 1, size: 12, total: 0 })

// 拉取社团列表
const fetchClubs = async () => {
  loading.value = true
  try {
    const res = await getClubs({
      page: page.current,
      size: page.size,
      keyword: keyword.value
    })
    clubs.value = res.records || []
    page.total = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    loading.value = false
  }
}

// 搜索：重置到第一页后拉取
const handleSearch = () => {
  page.current = 1
  fetchClubs()
}

// 跳转社团详情
const goDetail = (id) => {
  router.push(`/club/${id}`)
}

// ===== 创建社团 =====
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', description: '' })
const rules = {
  name: [
    { required: true, message: '请输入社团名称', trigger: 'blur' },
    { min: 1, max: 50, message: '名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '简介不能超过 500 个字符', trigger: 'blur' }
  ]
}

// 打开创建弹窗：未登录提示
const openCreateDialog = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再创建社团')
    return
  }
  form.name = ''
  form.description = ''
  dialogVisible.value = true
}

// 提交创建申请
const handleCreate = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    // 校验不通过
    return
  }
  submitting.value = true
  try {
    await createClub({
      name: form.name.trim(),
      description: form.description.trim()
    })
    ElMessage.success('创建申请已提交，等待审核')
    dialogVisible.value = false
    page.current = 1
    fetchClubs()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchClubs()
})
</script>

<style scoped>
.club-page {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* 搜索栏 */
.search-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-4) var(--space-5);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.search-bar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.search-input-wrap {
  position: relative;
  flex: 1;
  max-width: 360px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-3);
  z-index: 1;
  font-size: 16px;
  pointer-events: none;
}

.search-input-wrap :deep(.el-input__wrapper) {
  padding-left: 36px;
  border-radius: var(--radius-full);
}

.search-input {
  width: 100%;
}

/* 社团列表 */
.club-list {
  min-height: 200px;
}

/* 社团卡片 */
.club-card {
  margin-bottom: var(--space-5);
  cursor: pointer;
  border-radius: var(--radius-2xl);
  overflow: hidden;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
  transition: all var(--transition-base);
  opacity: 0;
}

.club-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-4);
  border-color: var(--color-primary-3);
}

/* 渐变头部 */
.card-gradient-header {
  height: 80px;
  background: var(--gradient-primary);
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: var(--space-3);
}

.header-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.15;
  background-image: radial-gradient(circle at 20% 50%, var(--color-white) 1px, transparent 1px),
    radial-gradient(circle at 60% 30%, var(--color-white) 1px, transparent 1px),
    radial-gradient(circle at 80% 70%, var(--color-white) 1.5px, transparent 1.5px);
  background-size: 30px 30px, 25px 25px, 35px 35px;
}

.status-tag {
  position: relative;
  z-index: 1;
  backdrop-filter: blur(4px);
}

/* 卡片内容 */
.card-body {
  padding: var(--space-4);
}

.club-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.club-name {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-family: var(--font-heading);
}

.club-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-2);
  line-height: var(--line-height-relaxed);
  min-height: 42px;
  margin-bottom: var(--space-3);
}

/* 两行省略 */
.text-ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.club-footer {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border-lighter);
}

.club-stat,
.club-time {
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color var(--transition-fast);
}

.club-card:hover .club-stat {
  color: var(--color-primary);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
}

.empty-text {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-2);
  margin: 0 0 var(--space-1) 0;
}

.empty-subtext {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: 0;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-2);
}

/* 响应式 */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input-wrap {
    max-width: 100%;
  }

  .search-bar .el-button {
    width: 100%;
  }

  .club-card {
    margin-bottom: var(--space-3);
  }

  .card-gradient-header {
    height: 60px;
  }
}
</style>
