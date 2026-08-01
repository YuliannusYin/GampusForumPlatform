<template>
  <div class="club-detail" v-loading="loading">
    <template v-if="club">
      <!-- 顶部社团信息卡片 -->
      <div class="club-header fade-in-up">
        <!-- 渐变横幅 -->
        <div class="banner">
          <div class="banner-pattern"></div>
          <div class="banner-content">
            <el-tag v-if="club.status === 1" type="success" size="small" round>正常</el-tag>
            <el-tag v-else-if="club.status === 0" type="warning" size="small" round>待审核</el-tag>
            <el-tag v-else-if="club.status === 2" type="danger" size="small" round>禁用</el-tag>
          </div>
        </div>
        <!-- 社团信息主体 -->
        <div class="header-main">
          <div class="club-logo">
            <span class="logo-text">{{ (club.name || '社').charAt(0) }}</span>
          </div>
          <div class="header-info">
            <div class="title-row">
              <h2 class="club-name">{{ club.name }}</h2>
            </div>
            <p v-if="club.description" class="club-desc">{{ club.description }}</p>
            <div class="club-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                社长：{{ club.creatorName || club.username || '-' }}
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(club.createTime) }}
              </span>
              <span class="meta-stat">
                <span class="stat-value">{{ club.memberCount || 0 }}</span>
                <span class="stat-label">成员</span>
              </span>
              <span class="meta-stat">
                <span class="stat-value">{{ club.postCount || 0 }}</span>
                <span class="stat-label">帖子</span>
              </span>
            </div>
          </div>
          <div class="header-actions">
            <el-button v-if="canJoin" type="primary" :loading="joinLoading" @click="handleJoin">
              <el-icon><Plus /></el-icon>
              加入社团
            </el-button>
            <el-button v-if="canLeave" type="danger" plain :loading="leaveLoading" @click="handleLeave">
              退出社团
            </el-button>
            <el-button v-if="canPost" type="success" @click="goCreatePost">
              <el-icon><EditPen /></el-icon>
              在社团发帖
            </el-button>
            <el-button v-if="isLeader" @click="openEditDialog">
              <el-icon><Edit /></el-icon>
              编辑社团
            </el-button>
          </div>
        </div>
      </div>

      <!-- 标签页 -->
      <div class="club-tabs fade-in-up delay-1">
        <el-tabs v-model="activeTab">
          <!-- 帖子 -->
          <el-tab-pane label="帖子" name="posts">
            <div v-loading="postsLoading">
              <!-- 帖子空状态 -->
              <div v-if="!postsLoading && posts.length === 0" class="empty-state">
                <svg width="140" height="140" viewBox="0 0 140 140" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="70" cy="70" r="63" fill="var(--color-primary-bg)" />
                  <rect x="42" y="40" width="56" height="64" rx="12" fill="var(--color-bg-card)" stroke="var(--color-primary)" stroke-width="3" />
                  <path d="M52 56 L88 56" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" opacity="0.4" />
                  <path d="M52 68 L80 68" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" opacity="0.3" />
                  <path d="M52 80 L72 80" stroke="var(--color-primary)" stroke-width="3" stroke-linecap="round" opacity="0.2" />
                  <circle cx="104" cy="44" r="10" fill="var(--color-primary)" opacity="0.15" />
                </svg>
                <p class="empty-text">暂无帖子</p>
                <p class="empty-subtext">社团成员可以在这里发布帖子</p>
              </div>
              <div v-for="(post, index) in posts" :key="post.id" class="post-item fade-in-up" :style="{ animationDelay: `${index * 0.05}s` }">
                <PostCard :post="post" class="post-card-flex" />
                <el-button
                  v-if="isLeader"
                  type="danger"
                  plain
                  size="small"
                  :loading="deletingPostId === post.id"
                  @click="handleDeletePost(post)"
                >
                  删除
                </el-button>
              </div>
              <div class="pagination-wrap" v-if="postPage.total > 0">
                <el-pagination
                  v-model:current-page="postPage.current"
                  v-model:page-size="postPage.size"
                  :total="postPage.total"
                  layout="total, prev, pager, next"
                  @current-change="fetchPosts"
                />
              </div>
            </div>
          </el-tab-pane>

          <!-- 成员 -->
          <el-tab-pane :label="`成员 (${members.length})`" name="members">
            <el-table :data="members" v-loading="membersLoading" stripe class="member-table">
              <el-table-column label="成员" min-width="220">
                <template #default="{ row }">
                  <div class="member-cell">
                    <el-avatar :size="40" :src="row.avatar" class="member-avatar">
                      {{ initialOf(row.nickname || row.username) }}
                    </el-avatar>
                    <div class="member-info">
                      <span class="member-name">{{ row.nickname || row.username }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="角色" width="120" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.role === 1" type="danger" size="small" round>社长</el-tag>
                  <el-tag v-else type="info" size="small" round>成员</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="加入时间" width="180">
                <template #default="{ row }">
                  <span class="join-time">{{ formatTime(row.joinTime) }}</span>
                </template>
              </el-table-column>
              <el-table-column v-if="isLeader" label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button
                    v-if="!isSelf(row)"
                    type="danger"
                    plain
                    size="small"
                    :loading="removingMemberId === row.id"
                    @click="handleRemoveMember(row)"
                  >
                    移除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </template>

    <!-- 社团不存在 -->
    <div v-else-if="!loading" class="empty-state fade-in-up">
      <svg width="160" height="160" viewBox="0 0 160 160" fill="none" xmlns="http://www.w3.org/2000/svg">
        <circle cx="80" cy="80" r="72" fill="var(--color-primary-bg)" />
        <circle cx="80" cy="62" r="28" fill="var(--color-primary)" opacity="0.15" />
        <path d="M56 106 C56 88 66 80 80 80 C94 80 104 88 104 106" stroke="var(--color-primary)" stroke-width="4" stroke-linecap="round" fill="none" opacity="0.3" />
        <path d="M68 52 L92 72 M92 52 L68 72" stroke="var(--color-danger)" stroke-width="4" stroke-linecap="round" opacity="0.5" />
      </svg>
      <p class="empty-text">社团不存在或已禁用</p>
      <el-button type="primary" @click="router.push('/club')">返回社团列表</el-button>
    </div>

    <!-- 编辑社团弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑社团" width="500px">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-position="top">
        <el-form-item label="社团名称" prop="name">
          <el-input
            v-model="editForm.name"
            placeholder="请输入社团名称"
            maxlength="50"
            show-word-limit
            clearable
          />
        </el-form-item>
        <el-form-item label="社团简介" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入社团简介（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  getClubDetail,
  joinClub,
  leaveClub,
  getClubPosts,
  removeClubPost,
  getClubMembers,
  removeClubMember,
  updateClub
} from '@/api/club'
import PostCard from '@/components/PostCard.vue'
import { formatTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 社团 ID（来自路由参数）
const clubId = computed(() => route.params.id)

// 社团详情
const club = ref(null)
const loading = ref(false)

// 成员列表
const members = ref([])
const membersLoading = ref(false)

// 帖子列表
const posts = ref([])
const postsLoading = ref(false)
const postPage = reactive({ current: 1, size: 10, total: 0 })

// 当前激活的标签页
const activeTab = ref('posts')

// 当前用户在成员列表中的记录（用于判断是否为成员 / 社长）
const currentMember = computed(() => {
  if (!userStore.isLoggedIn) return null
  const uid = userStore.userInfo.id
  return members.value.find((m) => m.userId === uid || m.id === uid) || null
})
const isMember = computed(() => !!currentMember.value)
const isLeader = computed(() => isMember.value && currentMember.value.role === 1)

// 操作按钮可见性
const canJoin = computed(() => !!club.value && userStore.isLoggedIn && !isMember.value && club.value.status === 1)
const canLeave = computed(() => !!club.value && userStore.isLoggedIn && isMember.value && !isLeader.value)
const canPost = computed(() => !!club.value && userStore.isLoggedIn && isMember.value && club.value.status === 1)

// 用户名首字母（头像占位）
const initialOf = (name) => {
  if (!name) return ''
  return name.charAt(0).toUpperCase()
}

// 是否为当前登录用户自己
const isSelf = (row) => {
  const uid = userStore.userInfo.id
  return row.userId === uid || row.id === uid
}

// 拉取社团详情
const fetchDetail = async () => {
  loading.value = true
  try {
    club.value = await getClubDetail(clubId.value)
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    club.value = null
  } finally {
    loading.value = false
  }
}

// 拉取成员列表
const fetchMembers = async () => {
  membersLoading.value = true
  try {
    const list = await getClubMembers(clubId.value)
    members.value = list || []
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    members.value = []
  } finally {
    membersLoading.value = false
  }
}

// 拉取帖子列表
const fetchPosts = async () => {
  postsLoading.value = true
  try {
    const res = await getClubPosts(clubId.value, {
      page: postPage.current,
      size: postPage.size
    })
    posts.value = res.records || []
    postPage.total = res.total || 0
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
    posts.value = []
  } finally {
    postsLoading.value = false
  }
}

// 路由参数变化时重新加载全部数据
const loadAll = () => {
  fetchDetail()
  fetchMembers()
  postPage.current = 1
  fetchPosts()
}

watch(
  () => clubId.value,
  () => {
    loadAll()
  }
)

// ===== 加入 / 退出 =====
const joinLoading = ref(false)
const handleJoin = async () => {
  joinLoading.value = true
  try {
    await joinClub(clubId.value)
    ElMessage.success('加入成功')
    fetchMembers()
    fetchDetail()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    joinLoading.value = false
  }
}

const leaveLoading = ref(false)
const handleLeave = async () => {
  try {
    await ElMessageBox.confirm('确定要退出该社团吗？', '退出确认', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消'
    })
  } catch {
    // 用户取消
    return
  }
  leaveLoading.value = true
  try {
    await leaveClub(clubId.value)
    ElMessage.success('已退出社团')
    fetchMembers()
    fetchDetail()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    leaveLoading.value = false
  }
}

// 跳转社团发帖
const goCreatePost = () => {
  router.push({ path: '/post/create', query: { clubId: clubId.value } })
}

// ===== 删除社团帖子（仅社长） =====
const deletingPostId = ref(null)
const handleDeletePost = async (post) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇帖子吗？删除后不可恢复。', '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    // 用户取消
    return
  }
  deletingPostId.value = post.id
  try {
    await removeClubPost(clubId.value, post.id)
    ElMessage.success('删除成功')
    fetchPosts()
    fetchDetail()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    deletingPostId.value = null
  }
}

// ===== 移除成员（仅社长） =====
const removingMemberId = ref(null)
const handleRemoveMember = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要移除成员「${row.nickname || row.username}」吗？`,
      '移除确认',
      { type: 'warning', confirmButtonText: '移除', cancelButtonText: '取消' }
    )
  } catch {
    // 用户取消
    return
  }
  removingMemberId.value = row.id
  try {
    await removeClubMember(clubId.value, row.id)
    ElMessage.success('已移除')
    fetchMembers()
    fetchDetail()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    removingMemberId.value = null
  }
}

// ===== 编辑社团（仅社长） =====
const editDialogVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref(null)
const editForm = reactive({ name: '', description: '' })
const editRules = {
  name: [
    { required: true, message: '请输入社团名称', trigger: 'blur' },
    { min: 1, max: 50, message: '名称长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '简介不能超过 500 个字符', trigger: 'blur' }
  ]
}

const openEditDialog = () => {
  editForm.name = club.value?.name || ''
  editForm.description = club.value?.description || ''
  editDialogVisible.value = true
}

const handleEdit = async () => {
  if (!editFormRef.value) return
  try {
    await editFormRef.value.validate()
  } catch {
    // 校验不通过
    return
  }
  editSubmitting.value = true
  try {
    await updateClub(clubId.value, {
      name: editForm.name.trim(),
      description: editForm.description.trim()
    })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    fetchDetail()
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    editSubmitting.value = false
  }
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.club-detail {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

/* 顶部信息卡片 */
.club-header {
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-2);
}

/* 渐变横幅 */
.banner {
  height: 120px;
  background: var(--gradient-primary);
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: flex-end;
  padding: var(--space-4);
}

.banner-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background-image: radial-gradient(circle at 20% 50%, var(--color-white) 1px, transparent 1px),
    radial-gradient(circle at 60% 30%, var(--color-white) 1px, transparent 1px),
    radial-gradient(circle at 80% 70%, var(--color-white) 1.5px, transparent 1.5px);
  background-size: 30px 30px, 25px 25px, 35px 35px;
}

.banner-content {
  position: relative;
  z-index: 1;
}

/* 主体信息 */
.header-main {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: 0 var(--space-5) var(--space-5);
  flex-wrap: wrap;
  position: relative;
}

.club-logo {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-2xl);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: -40px;
  box-shadow: var(--shadow-primary);
  border: 4px solid var(--color-bg-card);
  flex-shrink: 0;
}

.logo-text {
  font-size: 32px;
  font-weight: var(--font-weight-extrabold);
  color: var(--color-white);
  font-family: var(--font-display);
}

.header-info {
  flex: 1;
  min-width: 0;
  padding-top: var(--space-2);
}

.title-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.club-name {
  font-size: var(--font-size-display);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  margin: 0;
  font-family: var(--font-heading);
}

.club-desc {
  font-size: var(--font-size-body);
  color: var(--color-text-2);
  line-height: var(--line-height-relaxed);
  margin: 0 0 var(--space-3);
}

.club-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-5);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.meta-stat {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.stat-value {
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
  color: var(--color-primary);
  font-family: var(--font-display);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

.header-actions {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  flex-shrink: 0;
  padding-top: var(--space-2);
}

/* 标签页 */
.club-tabs {
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  padding: 0 var(--space-5) var(--space-5);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.club-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--space-5);
}

.club-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.club-tabs :deep(.el-tabs__item) {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-medium);
  padding: 0 var(--space-4);
  height: 52px;
  line-height: 52px;
}

.club-tabs :deep(.el-tabs__active-bar) {
  height: 3px;
  border-radius: var(--radius-full);
  background: var(--gradient-primary);
}

/* 帖子项 */
.post-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
  opacity: 0;
}

.post-card-flex {
  flex: 1;
  min-width: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-5);
}

/* 成员表格 */
.member-table {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.member-cell {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.member-avatar {
  background: var(--gradient-primary);
  color: var(--color-white);
  font-weight: var(--font-weight-semibold);
  flex-shrink: 0;
}

.member-name {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-1);
}

.join-time {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12) var(--space-4);
  text-align: center;
  gap: var(--space-3);
}

.empty-text {
  font-size: var(--font-size-h3);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-2);
  margin: 0;
}

.empty-subtext {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin: 0 0 var(--space-2) 0;
}

/* 小屏适配：操作按钮换到下方 */
@media (max-width: 768px) {
  .club-detail {
    padding: 0 var(--space-2);
  }

  .banner {
    height: 80px;
  }

  .header-main {
    flex-direction: column;
    padding: 0 var(--space-3) var(--space-4);
  }

  .club-logo {
    width: 64px;
    height: 64px;
    margin-top: -32px;
  }

  .logo-text {
    font-size: 26px;
  }

  .header-info {
    padding-top: 0;
  }

  .header-actions {
    flex-direction: row;
    flex-wrap: wrap;
    padding-top: 0;
  }

  .club-meta {
    gap: var(--space-3);
  }

  .club-tabs {
    padding: 0 var(--space-2) var(--space-4);
  }
}
</style>
