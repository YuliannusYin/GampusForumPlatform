<template>
  <div class="profile-page">
    <!-- 顶部用户信息卡片 -->
    <el-card class="user-card" shadow="never">
      <div class="user-info">
        <!-- 头像：点击可上传新头像 -->
        <el-upload
          :show-file-list="false"
          :http-request="handleAvatarUpload"
          accept="image/*"
          class="avatar-uploader"
        >
          <div class="avatar-wrap">
            <el-avatar :size="100" :src="userInfo.avatar" class="user-avatar">
              {{ usernameInitial }}
            </el-avatar>
            <div class="avatar-mask">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
        </el-upload>

        <!-- 基本信息 -->
        <div class="user-meta">
          <div class="user-name-row">
            <span class="user-nickname">{{ userInfo.nickname || userInfo.username }}</span>
            <el-tag type="primary" size="small">Lv.{{ userInfo.level || 1 }}</el-tag>
            <el-tag
              v-for="role in userInfo.roles"
              :key="role"
              :type="roleType(role)"
              size="small"
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
          <div class="user-points">
            <el-icon><GoldMedal /></el-icon>
            <span>积分：{{ userInfo.points || 0 }}</span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 主体内容：标签页 -->
    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 我的资料 -->
      <el-tab-pane label="我的资料" name="profile">
        <el-row :gutter="20">
          <!-- 资料编辑 -->
          <el-col :xs="24" :md="14">
            <el-card shadow="never" class="section-card">
              <template #header>
                <span class="card-title">编辑资料</span>
              </template>
              <el-form ref="profileFormRef" :model="profileForm" label-width="80px">
                <el-form-item label="头像">
                  <div class="form-avatar">
                    <el-avatar :size="80" :src="profileForm.avatar">
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
                <span class="card-title">修改密码</span>
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
            <div class="overview-item">
              <div class="overview-label">当前积分</div>
              <div class="overview-value">{{ userInfo.points || 0 }}</div>
            </div>
            <div class="overview-item">
              <div class="overview-label">当前等级</div>
              <div class="overview-value">Lv.{{ userInfo.level || 1 }}</div>
            </div>
          </div>

          <el-table :data="pointsRecords" v-loading="pointsLoading" border stripe>
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
          <el-table :data="signinRecords" v-loading="signinLoading" border stripe>
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

/* 用户信息卡片 */
.user-card {
  margin-bottom: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-uploader {
  display: inline-block;
}

.avatar-wrap {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.user-avatar {
  display: block;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 12px;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.avatar-wrap:hover .avatar-mask {
  opacity: 1;
}

.user-meta {
  flex: 1;
}

.user-name-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.user-nickname {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.user-detail-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}

.user-email {
  display: flex;
  align-items: center;
  gap: 4px;
}

.user-points {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #e6a23c;
  font-weight: 600;
}

/* 标签页 */
.profile-tabs {
  background-color: transparent;
}

.section-card {
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-avatar {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 积分概览 */
.points-overview {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}

.overview-item {
  flex: 0 0 auto;
  padding: 16px 24px;
  background-color: #f5f7fa;
  border-radius: 8px;
  text-align: center;
}

.overview-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}

.overview-value {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
}

/* 积分变更颜色 */
.points-up {
  color: #67c23a;
  font-weight: 600;
}

.points-down {
  color: #f56c6c;
  font-weight: 600;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* 小屏适配 */
@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    text-align: center;
  }
}
</style>
