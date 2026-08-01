<template>
  <div class="club-page">
    <!-- 顶部搜索栏 -->
    <el-card class="search-card" shadow="never">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索社团名称"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button v-if="userStore.isLoggedIn" type="success" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          创建社团
        </el-button>
      </div>
    </el-card>

    <!-- 社团列表 -->
    <div class="club-list" v-loading="loading">
      <el-empty v-if="!loading && clubs.length === 0" description="暂无社团" />
      <el-row v-else :gutter="16">
        <el-col
          v-for="club in clubs"
          :key="club.id"
          :xs="24"
          :sm="12"
          :md="8"
        >
          <el-card class="club-card" shadow="hover" @click="goDetail(club.id)">
            <div class="club-name-row">
              <span class="club-name">{{ club.name }}</span>
              <el-tag v-if="club.status === 0" type="warning" size="small">待审核</el-tag>
              <el-tag v-else-if="club.status === 2" type="danger" size="small">禁用</el-tag>
            </div>
            <div class="club-desc text-ellipsis-2">{{ club.description || '暂无简介' }}</div>
            <div class="club-footer">
              <span class="club-stat">
                <el-icon><User /></el-icon>
                {{ club.memberCount || 0 }} 成员
              </span>
              <span class="club-stat">
                <el-icon><Document /></el-icon>
                {{ club.postCount || 0 }} 帖子
              </span>
              <span class="club-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(club.createTime) }}
              </span>
            </div>
          </el-card>
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
  gap: 16px;
}

/* 搜索栏 */
.search-card {
  border-radius: 6px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  max-width: 320px;
}

/* 社团卡片 */
.club-list {
  min-height: 200px;
}

.club-card {
  margin-bottom: 16px;
  cursor: pointer;
  border-radius: 6px;
  transition: transform 0.15s ease;
}

.club-card:hover {
  transform: translateY(-2px);
}

.club-name-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.club-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.club-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  min-height: 42px;
  margin-bottom: 10px;
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
  gap: 16px;
  font-size: 13px;
  color: #909399;
}

.club-stat,
.club-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 分页 */
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}
</style>
