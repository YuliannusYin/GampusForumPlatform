<template>
  <div class="settings-page">
    <!-- Page header -->
    <div class="page-header fade-in-up">
      <div class="section-title">账号设置</div>
    </div>

    <el-tabs v-model="activeTab" class="settings-tabs fade-in-up delay-1" @tab-change="handleTabChange">
      <!-- 基本资料 -->
      <el-tab-pane label="基本资料" name="profile">
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
      </el-tab-pane>

      <!-- 账号安全 -->
      <el-tab-pane label="账号安全" name="security">
        <el-card shadow="never" class="section-card">
          <template #header>
            <div class="section-title">修改密码</div>
          </template>
          <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="90px"
            class="pwd-form"
          >
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
      </el-tab-pane>

      <!-- 通知偏好 -->
      <el-tab-pane label="通知偏好" name="notification">
        <el-card shadow="never" class="section-card" v-loading="settingsLoading">
          <template #header>
            <div class="section-title">通知偏好</div>
          </template>
          <div class="notify-list">
            <!-- 评论通知 -->
            <div class="notify-item">
              <div class="notify-info">
                <div class="notify-icon notify-icon-primary">
                  <el-icon><ChatDotRound /></el-icon>
                </div>
                <div class="notify-text">
                  <div class="notify-name">评论通知</div>
                  <div class="notify-desc">有人评论你的帖子时通知你</div>
                </div>
              </div>
              <el-switch v-model="settingsForm.commentNotify" />
            </div>

            <!-- 点赞通知 -->
            <div class="notify-item">
              <div class="notify-info">
                <div class="notify-icon notify-icon-danger">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="notify-text">
                  <div class="notify-name">点赞通知</div>
                  <div class="notify-desc">有人点赞你的帖子或评论时通知你</div>
                </div>
              </div>
              <el-switch v-model="settingsForm.likeNotify" />
            </div>

            <!-- 私信通知 -->
            <div class="notify-item">
              <div class="notify-info">
                <div class="notify-icon notify-icon-success">
                  <el-icon><Message /></el-icon>
                </div>
                <div class="notify-text">
                  <div class="notify-name">私信通知</div>
                  <div class="notify-desc">收到私信时通知你</div>
                </div>
              </div>
              <el-switch v-model="settingsForm.messageNotify" />
            </div>
          </div>

          <div class="notify-action">
            <el-button type="primary" :loading="settingsSaving" @click="handleSaveSettings">
              保存设置
            </el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import {
  getProfile,
  updateProfile,
  changePassword,
  uploadFile
} from '@/api/user'
import { getSettings, updateSettings } from '@/api/settings'

const userStore = useUserStore()

// 当前激活标签页
const activeTab = ref('profile')

// 头像首字母占位
const usernameInitial = computed(() => {
  const name = profileForm.nickname || userStore.userInfo?.username || ''
  return name ? name.charAt(0).toUpperCase() : ''
})

// ===== 基本资料 =====
const profileFormRef = ref(null)
const profileSaving = ref(false)
const avatarUploading = ref(false)
const profileForm = reactive({
  nickname: '',
  bio: '',
  avatar: '',
  gender: 0
})

// 头像上传
const handleAvatarUpload = async (options) => {
  const { file } = options
  avatarUploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadFile(formData)
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
    // 同步到 store
    userStore.setUserInfo({ ...userStore.userInfo, ...data })
    ElMessage.success('资料保存成功')
  } catch (e) {
    // 错误已由请求拦截器统一提示
  } finally {
    profileSaving.value = false
  }
}

// 拉取当前用户资料
const loadProfile = async () => {
  try {
    const data = await getProfile()
    profileForm.nickname = data.nickname || ''
    profileForm.bio = data.bio || ''
    profileForm.avatar = data.avatar || ''
    profileForm.gender = data.gender != null ? data.gender : 0
    // 同步到 store
    userStore.setUserInfo({ ...userStore.userInfo, ...data })
  } catch (e) {
    // 接口失败时降级使用 store 中已有信息
    const info = userStore.userInfo || {}
    profileForm.nickname = info.nickname || ''
    profileForm.bio = info.bio || ''
    profileForm.avatar = info.avatar || ''
    profileForm.gender = info.gender != null ? info.gender : 0
  }
}

// ===== 账号安全 =====
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

// ===== 通知偏好 =====
const settingsLoading = ref(false)
const settingsSaving = ref(false)
const settingsLoaded = ref(false)
const settingsForm = reactive({
  commentNotify: false,
  likeNotify: false,
  messageNotify: false
})

const loadSettings = async () => {
  settingsLoading.value = true
  try {
    const data = await getSettings()
    settingsForm.commentNotify = !!data.commentNotify
    settingsForm.likeNotify = !!data.likeNotify
    settingsForm.messageNotify = !!data.messageNotify
    settingsLoaded.value = true
  } catch (e) {
    // 忽略
  } finally {
    settingsLoading.value = false
  }
}

// 保存通知偏好
const handleSaveSettings = async () => {
  settingsSaving.value = true
  try {
    await updateSettings({
      commentNotify: settingsForm.commentNotify,
      likeNotify: settingsForm.likeNotify,
      messageNotify: settingsForm.messageNotify
    })
    ElMessage.success('设置保存成功')
  } catch (e) {
    // 错误已由请求拦截器统一提示
  } finally {
    settingsSaving.value = false
  }
}

// 切换 tab 时懒加载通知偏好
const handleTabChange = (name) => {
  if (name === 'notification' && !settingsLoaded.value) {
    loadSettings()
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.settings-page {
  max-width: 720px;
  margin: 0 auto;
}

/* === Page Header === */
.page-header {
  margin-bottom: var(--space-5);
}

/* === Tabs === */
.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--space-5);
}

.settings-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: var(--color-border-light);
}

.settings-tabs :deep(.el-tabs__item) {
  font-family: var(--font-heading);
  font-weight: var(--font-weight-medium);
  font-size: var(--font-size-body);
  color: var(--color-text-3);
  height: 44px;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
  font-weight: var(--font-weight-semibold);
}

.settings-tabs :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: var(--radius-full);
}

/* === Section Card === */
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

/* === Form Avatar === */
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

/* === Password Form === */
.pwd-form {
  max-width: 460px;
}

/* === Notification Preferences === */
.notify-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.notify-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-xl);
  border: 1px solid var(--color-border-light);
  background: var(--color-bg-subtle);
  transition: all var(--transition-fast);
}

.notify-item:hover {
  border-color: var(--color-border);
  background: var(--color-bg-card);
  box-shadow: var(--shadow-1);
}

.notify-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
  flex: 1;
}

.notify-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-lg);
  font-size: 20px;
  flex-shrink: 0;
}

.notify-icon-primary {
  background: var(--color-primary-tag-bg);
  color: var(--color-primary);
}

.notify-icon-danger {
  background: var(--color-danger-tag-bg);
  color: var(--color-danger);
}

.notify-icon-success {
  background: var(--color-success-tag-bg);
  color: var(--color-success);
}

.notify-text {
  min-width: 0;
}

.notify-name {
  font-family: var(--font-heading);
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  margin-bottom: var(--space-1);
}

.notify-desc {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  line-height: var(--line-height-normal);
}

/* === Switch styling === */
.notify-item :deep(.el-switch.is-checked .el-switch__core) {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.notify-action {
  margin-top: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border-lighter);
}

/* === Responsive === */
@media (max-width: 768px) {
  .notify-item {
    flex-direction: row;
    align-items: center;
  }

  .notify-desc {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
