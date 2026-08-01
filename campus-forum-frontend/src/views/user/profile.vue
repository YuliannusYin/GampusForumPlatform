<template>
  <div class="profile-page">
    <!-- Profile header card with gradient banner -->
    <div class="profile-header fade-in-up">
      <div class="header-banner">
        <div class="banner-decoration"></div>
      </div>
      <div class="header-body">
        <el-upload
          :show-file-list="false"
          :http-request="handleAvatarUpload"
          accept="image/*"
          class="avatar-uploader"
        >
          <div class="avatar-wrap">
            <el-avatar :size="96" :src="userInfo.avatar" class="user-avatar">
              {{ usernameInitial }}
            </el-avatar>
            <div class="avatar-mask">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
        </el-upload>

        <div class="user-meta">
          <div class="user-name-row">
            <span class="user-nickname">{{ userInfo.nickname || userInfo.username }}</span>
            <el-tag type="primary" size="small" effect="light">Lv.{{ userInfo.level || 1 }}</el-tag>
            <el-tag
              v-for="role in userInfo.roles"
              :key="role"
              :type="roleType(role)"
              size="small"
              effect="light"
            >
              {{ roleText(role) }}
            </el-tag>
          </div>
          <div class="user-detail-row">
            <span class="user-username">@{{ userInfo.username }}</span>
            <span v-if="userInfo.email" class="user-email">
              <el-icon><Message /></el-icon>
              {{ userInfo.email }}
            </span>
          </div>
        </div>

        <div class="header-stats">
          <div class="header-stat">
            <span class="stat-number header-stat-value">{{ userInfo.points || 0 }}</span>
            <span class="header-stat-label">积分</span>
          </div>
          <div class="header-stat">
            <span class="stat-number header-stat-value">Lv.{{ userInfo.level || 1 }}</span>
            <span class="header-stat-label">等级</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Main content: tabs -->
    <el-tabs v-model="activeTab" class="profile-tabs fade-in-up delay-1">
      <!-- 我的资料 -->
      <el-tab-pane label="我的资料" name="profile">
        <el-row :gutter="20">
          <!-- 资料编辑 -->
          <el-col :xs="24" :md="14">
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="section-title">编辑资料</div>
              </template>
              <el-form ref="profileFormRef" :model="profileForm" label-width="80px">
                <el-form-item label="头像">
                  <div class="form-avatar">
                    <el-avatar :size="64" :src="profileForm.avatar" class="form-avatar-img">
                      {{ usernameInitial }}
                    </el-avatar>
                    <el-upload
                      :show-file-list="false"
                      :http-request="handleAvatarUpload"
                      accept="image/*"
                    >
                      <el-button type="primary" plain :loading="avatarUploading">
                        上传头像
                      </el-button>
                    </el-upload>
                  </div>
                </el-form-item>
                <el-form-item label="昵称" prop="nickname">
                  <el-input
                    v-model="profileForm.nickname"
                    placeholder="请输入昵称"
                    maxlength="20"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="个人简介" prop="bio">
                  <el-input
                    v-model="profileForm.bio"
                    type="textarea"
                    :rows="4"
                    placeholder="介绍一下自己吧"
                    maxlength="200"
                    show-word-limit
                  />
                </el-form-item>
                <el-form-item label="性别" prop="gender">
                  <el-select v-model="profileForm.gender" placeholder="请选择性别">
                    <el-option label="未知" :value="0" />
                    <el-option label="男" :value="1" />
                    <el-option label="女" :value="2" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile">
                    保存资料
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <!-- 修改密码 -->
          <el-col :xs="24" :md="10">
            <el-card shadow="never" class="section-card">
              <template #header>
                <div class="section-title">修改密码</div>
              </template>
              <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
                <el-form-item label="旧密码" prop="oldPassword">
                  <el-input
                    v-model="pwdForm.oldPassword"
                    type="password"
                    show-password
                    placeholder="请输入旧密码"
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="pwdForm.newPassword"
                    type="password"
                    show-password
                    placeholder="请输入新密码"
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input
                    v-model="pwdForm.confirmPassword"
                    type="password"
                    show-password
                    placeholder="请再次输入新密码"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">
                    修改密码
                  </el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 积分记录 -->
      <el-tab-pane label="积分记录" name="points">
        <el-card shadow="never" class="section-card">
          <!-- 积分概览 -->
          <div class="points-overview">
            <div class="overview-item overview-points">
              <div class="overview-icon">
                <el-icon><GoldMedal /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">当前积分</div>
                <div class="stat-number overview-value">{{ userInfo.points || 0 }}</div>
              </div>
            </div>
            <div class="overview-item overview-level">
              <div class="overview-icon">
                <el-icon><Medal /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-label">当前等级</div>
                <div class="stat-number overview-value">Lv.{{ userInfo.level || 1 }}</div>
              </div>
            </div>
          </div>

          <el-table :data="pointsRecords" v-loading="pointsLoading" stripe>
            <el-table-column label="变更值" prop="changeValue" width="100" align="center">
              <template #default="{ row }">
                <span :class="row.changeValue >= 0 ? 'points-up' : 'points-down'">
                  {{ row.changeValue >= 0 ? '+' : '' }}{{ row.changeValue }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="类型" prop="type" width="140" />
            <el-table-column label="描述" prop="description" min-width="200" show-overflow-tooltip />
            <el-table-column label="时间" prop="createTime" width="180">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap" v-if="pointsPage.total > 0">
            <el-pagination
              v-model:current-page="pointsPage.current"
              v-model:page-size="pointsPage.size"
              :total="pointsPage.total"
              layout="total, prev, pager, next"
              @current-change="loadPointsRecords"
            />
          </div>
        </el-card>
      </el-tab-pane>

      <!-- 签到记录 -->
      <el-tab-pane label="签到记录" name="signin">
        <el-card shadow="never" class="section-card">
          <el-table :data="signinRecords" v-loading="signinLoading" stripe>
            <el-table-column label="签到日期" prop="signinDate" width="160">
              <template #default="{ row }">{{ formatDate(row.signinDate) }}</template>
            </el-table-column>
            <el-table-column label="连续天数" prop="continuousDays" width="120" align="center" />
            <el-table-column label="获得积分" prop="earnedPoints" width="120" align="center">
              <template #default="{ row }">
                <span class="points-up">+{{ row.earnedPoints }}</span>
              </template>
            </el-table-column>
            <el-table-column label="签到时间" prop="createTime" min-width="180">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
          </el-table>

          <div class="pagination-wrap" v-if="signinPage.total > 0">
            <el-pagination
              v-model:current-page="signinPage.current"
              v-model:page-size="signinPage.size"
              :total="signinPage.total"
              layout="total, prev, pager, next"
              @current-change="loadSigninRecords"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import {
  getProfile,
  updateProfile,
  changePassword,
  getPointsRecords,
  getSigninRecords,
  uploadFile
} from '@/api/user'
import { formatTime, formatDate } from '@/utils/format'

const userStore = useUserStore()

// 用户信息（从接口拉取最新，降级使用 store 数据）
const userInfo = ref({
  id: null,
  username: '',
  nickname: '',
  avatar: '',
  email: '',
  points: 0,
  level: 1,
  roles: []
})

// 当前激活的标签页
const activeTab = ref('profile')

// 头像首字母占位
const usernameInitial = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || ''
  return name ? name.charAt(0).toUpperCase() : ''
})

// 角色文本转换
const roleText = (role) => {
  const map = { ROLE_ADMIN: '管理员', ROLE_USER: '用户', ROLE_MODERATOR: '版主' }
  return map[role] || role
}
// 角色 tag 颜色
const roleType = (role) => {
  if (role === 'ROLE_ADMIN') return 'danger'
  if (role === 'ROLE_MODERATOR') return 'warning'
  return 'info'
}

// ===== 资料编辑 =====
const profileFormRef = ref(null)
const profileSaving = ref(false)
const avatarUploading = ref(false)
const profileForm = reactive({
  nickname: '',
  bio: '',
  avatar: '',
  gender: 0
})

// 头像上传：成功后同步到顶部头像与表单 avatar 字段
const handleAvatarUpload = async (options) => {
  const { file } = options
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadFile(formData)
    userInfo.value.avatar = res.url
    profileForm.avatar = res.url
    ElMessage.success('头像上传成功')
  } catch (e) {
    // 错误已由请求拦截器统一提示
  } finally {
    avatarUploading.value = false
  }
}

// 保存资料
const handleSaveProfile = async () => {
  profileSaving.value = true
  try {
    const data = await updateProfile({
      nickname: profileForm.nickname,
      bio: profileForm.bio,
      avatar: profileForm.avatar,
      gender: profileForm.gender
    })
    // 合并返回数据并更新本地视图与 store
    const merged = { ...userInfo.value, ...data }
    userInfo.value = merged
    userStore.setUserInfo(merged)
    ElMessage.success('资料保存成功')
  } catch (e) {
    // 错误已由请求拦截器统一提示
  } finally {
    profileSaving.value = false
  }
}

// ===== 修改密码 =====
const pwdFormRef = ref(null)
const pwdSaving = ref(false)
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 确认密码校验
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = reactive({
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
})

// 提交修改密码
const handleChangePassword = async () => {
  if (!pwdFormRef.value) return
  try {
    await pwdFormRef.value.validate()
  } catch {
    // 校验不通过
    return
  }
  pwdSaving.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e) {
    // 错误已由请求拦截器统一提示
  } finally {
    pwdSaving.value = false
  }
}

// ===== 积分明细 =====
const pointsLoading = ref(false)
const pointsRecords = ref([])
const pointsPage = reactive({ current: 1, size: 10, total: 0 })

const loadPointsRecords = async () => {
  pointsLoading.value = true
  try {
    const res = await getPointsRecords({ page: pointsPage.current, size: pointsPage.size })
    pointsRecords.value = res.records || []
    pointsPage.total = res.total || 0
  } catch (e) {
    // 忽略
  } finally {
    pointsLoading.value = false
  }
}

// ===== 签到记录 =====
const signinLoading = ref(false)
const signinRecords = ref([])
const signinPage = reactive({ current: 1, size: 10, total: 0 })

const loadSigninRecords = async () => {
  signinLoading.value = true
  try {
    const res = await getSigninRecords({ page: signinPage.current, size: signinPage.size })
    signinRecords.value = res.records || []
    signinPage.total = res.total || 0
  } catch (e) {
    // 忽略
  } finally {
    signinLoading.value = false
  }
}

// 拉取个人资料
const loadProfile = async () => {
  try {
    const data = await getProfile()
    userInfo.value = { ...userInfo.value, ...data }
    // 同步到编辑表单
    profileForm.nickname = data.nickname || ''
    profileForm.bio = data.bio || ''
    profileForm.avatar = data.avatar || ''
    profileForm.gender = data.gender != null ? data.gender : 0
    // 同步到 store
    userStore.setUserInfo({ ...userStore.userInfo, ...data })
  } catch (e) {
    // 接口失败时降级使用 store 中已有信息
    userInfo.value = { ...userInfo.value, ...userStore.userInfo }
    profileForm.nickname = userStore.userInfo.nickname || ''
    profileForm.bio = userStore.userInfo.bio || ''
    profileForm.avatar = userStore.userInfo.avatar || ''
    profileForm.gender = userStore.userInfo.gender != null ? userStore.userInfo.gender : 0
  }
}

// 切换到积分/签到 tab 时懒加载
watch(activeTab, (val) => {
  if (val === 'points' && pointsRecords.value.length === 0) loadPointsRecords()
  if (val === 'signin' && signinRecords.value.length === 0) loadSigninRecords()
})

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
}

/* === Profile Header === */
.profile-header {
  background: var(--color-bg-card);
  border-radius: var(--radius-2xl);
  overflow: hidden;
  box-shadow: var(--shadow-2);
  margin-bottom: var(--space-5);
}

.header-banner {
  position: relative;
  height: 120px;
  background: var(--gradient-primary);
  overflow: hidden;
}

.banner-decoration {
  position: absolute;
  top: -60%;
  right: 5%;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18) 0%, transparent 65%);
  border-radius: 50%;
  pointer-events: none;
}

.header-body {
  display: flex;
  align-items: flex-end;
  gap: var(--space-6);
  padding: 0 var(--space-6) var(--space-5);
}

.avatar-uploader {
  display: inline-block;
  flex-shrink: 0;
  margin-top: -48px;
}

.avatar-wrap {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: var(--radius-full);
  overflow: hidden;
  cursor: pointer;
  border: 4px solid var(--color-bg-card);
  box-shadow: var(--shadow-2);
}

.user-avatar {
  display: block;
  --el-avatar-bg-color: transparent;
  background: var(--gradient-primary);
  color: var(--color-white);
  font-family: var(--font-display);
  font-weight: var(--font-weight-extrabold);
  font-size: var(--font-size-h1);
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: var(--font-size-caption);
  color: var(--color-white);
  background-color: rgba(12, 13, 14, 0.5);
  opacity: 0;
  transition: opacity var(--transition-fast);
  border-radius: var(--radius-full);
}

.avatar-wrap:hover .avatar-mask {
  opacity: 1;
}

.user-meta {
  flex: 1;
  min-width: 0;
  padding-bottom: var(--space-1);
}

.user-name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.user-nickname {
  font-family: var(--font-heading);
  font-size: var(--font-size-h1);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
  line-height: var(--line-height-tight);
}

.user-detail-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-4);
  font-size: var(--font-size-body);
  color: var(--color-text-3);
}

.user-email {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.header-stats {
  display: flex;
  gap: var(--space-6);
  flex-shrink: 0;
  padding-bottom: var(--space-1);
}

.header-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-1);
}

.header-stat-value {
  font-size: var(--font-size-h1);
  color: var(--color-primary);
  line-height: 1;
}

.header-stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  font-weight: var(--font-weight-medium);
}

/* === Tabs === */
.profile-tabs {
  background-color: transparent;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--space-5);
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--color-border-light);
}

.profile-tabs :deep(.el-tabs__item) {
  font-family: var(--font-heading);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-body);
  color: var(--color-text-3);
  height: 44px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: var(--radius-full);
}

.section-card {
  margin-bottom: var(--space-4);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
}

.section-card :deep(.el-card__header) {
  padding: var(--space-4) var(--space-5);
  border-bottom: 1px solid var(--color-border-lighter);
}

.section-card :deep(.el-card__body) {
  padding: var(--space-5);
}

.form-avatar {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.form-avatar-img {
  --el-avatar-bg-color: transparent;
  background: var(--gradient-primary);
  color: var(--color-white);
  font-family: var(--font-display);
  font-weight: var(--font-weight-extrabold);
}

/* === Points Overview === */
.points-overview {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-5);
  flex-wrap: wrap;
}

.overview-item {
  flex: 1 1 200px;
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-5);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
}

.overview-points {
  background: var(--color-primary-bg);
}

.overview-level {
  background: var(--color-warning-bg);
}

.overview-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: var(--radius-lg);
  font-size: 22px;
  flex-shrink: 0;
}

.overview-points .overview-icon {
  background: var(--color-primary-tag-bg);
  color: var(--color-primary);
}

.overview-level .overview-icon {
  background: var(--color-warning-tag-bg);
  color: var(--color-warning);
}

.overview-info {
  min-width: 0;
}

.overview-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  margin-bottom: var(--space-1);
}

.overview-value {
  font-size: var(--font-size-h1);
  color: var(--color-text-1);
}

/* === Points colors === */
.points-up {
  color: var(--color-success);
  font-weight: var(--font-weight-semibold);
}

.points-down {
  color: var(--color-danger);
  font-weight: var(--font-weight-semibold);
}

/* === Pagination === */
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-4);
}

/* === Responsive === */
@media (max-width: 768px) {
  .header-body {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: var(--space-3);
    padding: 0 var(--space-4) var(--space-5);
  }

  .avatar-uploader {
    margin-top: -48px;
  }

  .user-meta {
    align-items: center;
    padding-bottom: 0;
  }

  .user-name-row {
    justify-content: center;
  }

  .user-detail-row {
    justify-content: center;
  }

  .header-stats {
    justify-content: center;
    padding-bottom: 0;
  }

  .overview-item {
    flex: 1 1 100%;
  }
}
</style>
