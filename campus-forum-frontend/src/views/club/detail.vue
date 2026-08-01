<template>
  <div class="club-detail" v-loading="loading">
    <template v-if="club">
      <!-- 顶部社团信息卡片 -->
      <el-card class="club-header" shadow="never">
        <div class="header-main">
          <div class="header-info">
            <div class="title-row">
              <h2 class="club-name">{{ club.name }}</h2>
              <el-tag v-if="club.status === 1" type="success" size="small">正常</el-tag>
              <el-tag v-else-if="club.status === 0" type="warning" size="small">待审核</el-tag>
              <el-tag v-else-if="club.status === 2" type="danger" size="small">禁用</el-tag>
            </div>
            <p v-if="club.description" class="club-desc">{{ club.description }}</p>
            <div class="club-meta">
              <span>
                <el-icon><User /></el-icon>
                社长：{{ club.creatorName || club.username || '-' }}
              </span>
              <span>
                <el-icon><Clock /></el-icon>
                创建时间：{{ formatTime(club.createTime) }}
              </span>
              <span>成员：{{ club.memberCount || 0 }}</span>
              <span>帖子：{{ club.postCount || 0 }}</span>
            </div>
          </div>
          <div class="header-actions">
            <el-button v-if="canJoin" type="primary" :loading="joinLoading" @click="handleJoin">
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
      </el-card>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="club-tabs">
        <!-- 帖子 -->
        <el-tab-pane label="帖子" name="posts">
          <div v-loading="postsLoading">
            <el-empty v-if="!postsLoading && posts.length === 0" description="暂无帖子" />
            <div v-for="post in posts" :key="post.id" class="post-item">
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
          <el-table :data="members" v-loading="membersLoading" border stripe>
            <el-table-column label="成员" min-width="220">
              <template #default="{ row }">
                <div class="member-cell">
                  <el-avatar :size="36" :src="row.avatar">
                    {{ initialOf(row.nickname || row.username) }}
                  </el-avatar>
                  <span class="member-name">{{ row.nickname || row.username }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="角色" width="120" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.role === 1" type="danger" size="small">社长</el-tag>
                <el-tag v-else type="info" size="small">成员</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="加入时间" width="180">
              <template #default="{ row }">{{ formatTime(row.joinTime) }}</template>
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
    </template>

    <!-- 社团不存在 -->
    <el-empty v-else-if="!loading" description="社团不存在或已禁用">
      <el-button type="primary" @click="router.push('/club')">返回社团列表</el-button>
    </el-empty>

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
  gap: 16px;
}

/* 顶部信息卡片 */
.club-header {
  border-radius: 6px;
}

.header-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.header-info {
  flex: 1;
  min-width: 0;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.club-name {
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  margin: 0;
}

.club-desc {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 10px;
}

.club-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.club-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
}

/* 标签页 */
.club-tabs {
  background-color: transparent;
}

/* 帖子项 */
.post-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-card-flex {
  flex: 1;
  min-width: 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* 成员单元格 */
.member-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-name {
  font-size: 14px;
  color: #303133;
}

/* 小屏适配：操作按钮换到下方 */
@media (max-width: 768px) {
  .header-main {
    flex-direction: column;
  }

  .header-actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
}
</style>
