<template>
  <div class="post-edit" v-loading="pageLoading">
    <el-alert
      v-if="clubId"
      title="正在社团内发帖"
      type="info"
      :closable="false"
      show-icon
      class="club-alert"
    />

    <div class="edit-wrap fade-in-up">
      <!-- 页面头部 -->
      <div class="edit-head">
        <div class="edit-head-icon">
          <el-icon><EditPen /></el-icon>
        </div>
        <div class="edit-head-text">
          <span class="edit-title">{{ isEdit ? '编辑帖子' : '发布新帖' }}</span>
          <span class="edit-sub">{{ isEdit ? '修改并更新你的内容' : '分享你的想法，让更多人看到' }}</span>
        </div>
      </div>

      <el-card class="edit-card" shadow="never">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="edit-form"
        >
          <!-- 标题 -->
          <el-form-item label="标题" prop="title">
            <el-input
              v-model="form.title"
              placeholder="请输入标题（最多 100 字）"
              maxlength="100"
              show-word-limit
              clearable
            />
          </el-form-item>

          <!-- 板块 -->
          <el-form-item label="板块" prop="sectionId">
            <el-select
              v-model="form.sectionId"
              placeholder="请选择板块"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="item in sectionOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-alert
            v-if="isConfession"
            title="发布后前台将隐藏你的身份，评论区仍为实名。违规内容可被举报，管理员后台可追溯。"
            type="warning"
            :closable="false"
            show-icon
            class="anon-alert"
          />

          <!-- 标签 -->
          <el-form-item label="标签" prop="tagIds">
            <el-select
              v-model="form.tagIds"
              placeholder="请选择标签（可多选）"
              multiple
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="item in tagOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <!-- 摘要 -->
          <el-form-item label="摘要（可选，不填将自动从正文截取）" prop="summary">
            <el-input
              v-model="form.summary"
              type="textarea"
              :rows="2"
              placeholder="一句话概括帖子内容"
              maxlength="200"
              show-word-limit
            />
          </el-form-item>

          <!-- 正文 -->
          <el-form-item label="正文（支持 Markdown）" prop="content">
            <MdEditor
              v-model="form.content"
              :style="{ width: '100%' }"
              :toolbars-exclude="['github', 'save', 'pageFullscreen', 'htmlPreview']"
              placeholder="开始书写你的帖子内容…"
              :on-upload-img="onUploadImg"
            />
          </el-form-item>

          <!-- 操作按钮 -->
          <el-form-item>
            <el-button type="primary" class="submit-btn" :loading="submitting" @click="handleSubmit">
              {{ isEdit ? '保存修改' : '发布帖子' }}
            </el-button>
            <el-button class="cancel-btn" @click="handleCancel">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getSections, getTags, getPostDetail, createPost, updatePost } from '@/api/post'
import { createClubPost } from '@/api/club'
import { uploadFile } from '@/api/user'

const route = useRoute()
const router = useRouter()

// 是否为编辑模式（路由名 PostEdit 即编辑，否则新建）
const isEdit = computed(() => route.name === 'PostEdit')
// 编辑模式下的帖子 ID
const editId = computed(() => route.params.id)
// 社团发帖模式：来自路由 query 的 clubId
const clubId = computed(() => route.query.clubId)
const isConfession = computed(() => {
  const selected = sectionOptions.value.find((item) => item.id === form.sectionId)
  return selected?.code === 'confession'
})

// 表单引用
const formRef = ref(null)
// 页面加载状态（编辑模式回填）
const pageLoading = ref(false)
// 提交状态
const submitting = ref(false)

// 表单数据
const form = reactive({
  title: '',
  sectionId: '',
  tagIds: [],
  summary: '',
  content: ''
})

// 校验规则
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  sectionId: [{ required: true, message: '请选择板块', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文内容', trigger: 'blur' }]
}

// 板块选项
const sectionOptions = ref([])
// 标签选项
const tagOptions = ref([])

// 拉取板块选项
const fetchSections = async () => {
  try {
    const list = await getSections()
    sectionOptions.value = list || []
    if (!isEdit.value && route.query.anonymous === '1') {
      const wall = sectionOptions.value.find((item) => item.code === 'confession')
      if (wall) {
        form.sectionId = wall.id
      }
    }
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 拉取标签选项
const fetchTags = async () => {
  try {
    const list = await getTags()
    tagOptions.value = list || []
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  }
}

// 编辑模式：回填表单
const fetchPostForEdit = async () => {
  if (!isEdit.value) return
  pageLoading.value = true
  try {
    const data = await getPostDetail(editId.value)
    form.title = data.title || ''
    form.sectionId = data.sectionId
    form.tagIds = (data.tags || []).map((t) => t.id)
    form.summary = data.summary || ''
    form.content = data.content || ''
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    pageLoading.value = false
  }
}

// Markdown 正文图片上传：调用 /api/files/upload，返回图片 URL 数组
const onUploadImg = async (files, callback) => {
  const urls = []
  for (const file of files) {
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await uploadFile(formData)
      if (res && res.url) {
        urls.push(res.url)
      }
    } catch (err) {
      // 错误已由 request.js 拦截器统一提示
    }
  }
  callback(urls)
}

// 从正文截取摘要（去除 Markdown 标记后取前 120 字）
const excerptFromContent = (content) => {
  const text = (content || '')
    .replace(/[#*`>\-\[\]()!_~]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
  return text.slice(0, 120)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    // 校验不通过
    return
  }
  submitting.value = true
  try {
    // 摘要为空时自动从正文截取
    const summary = form.summary && form.summary.trim()
      ? form.summary.trim()
      : excerptFromContent(form.content)
    const payload = {
      title: form.title.trim(),
      content: form.content,
      summary,
      sectionId: form.sectionId,
      tagIds: form.tagIds
    }
    let result
    if (isEdit.value) {
      // 编辑：调 updatePost
      result = await updatePost(editId.value, payload)
      ElMessage.success('修改成功')
    } else if (clubId.value) {
      // 社团发帖：调 createClubPost
      result = await createClubPost(clubId.value, payload)
      ElMessage.success('发布成功')
    } else {
      // 新建：调 createPost
      result = await createPost(payload)
      ElMessage.success('发布成功')
    }
    // 社团发帖成功后跳回社团详情，其余跳帖子详情
    if (!isEdit.value && clubId.value) {
      router.push(`/club/${clubId.value}`)
    } else {
      // 跳转帖子详情：优先用返回的 id（PostDetailVO 或纯 id）
      const targetId = (result && (result.id || result)) || editId.value
      router.push(`/post/${targetId}`)
    }
  } catch (err) {
    // 错误已由 request.js 拦截器统一提示
  } finally {
    submitting.value = false
  }
}

// 取消：返回上一页
const handleCancel = () => {
  router.back()
}

onMounted(() => {
  fetchSections()
  fetchTags()
  fetchPostForEdit()
})
</script>

<style scoped>
.post-edit {
  max-width: 900px;
  margin: 0 auto;
}

/* ===== 社团提示 ===== */
.club-alert,
.anon-alert {
  margin-bottom: var(--space-4);
  border-radius: var(--radius-lg);
}

/* ===== 页面头部 ===== */
.edit-wrap {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.edit-head {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.edit-head-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-xl);
  background: var(--gradient-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary);
  flex-shrink: 0;
}

.edit-head-icon .el-icon {
  font-size: 22px;
  color: #fff;
}

.edit-head-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.edit-title {
  font-family: var(--font-heading);
  font-size: var(--font-size-h2);
  font-weight: var(--font-weight-bold);
  color: var(--color-text-1);
}

.edit-sub {
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
}

/* ===== 卡片 ===== */
.edit-card {
  border-radius: var(--radius-2xl);
  border: 1px solid var(--color-border-light);
  box-shadow: var(--shadow-1);
}

.edit-card :deep(.el-card__body) {
  padding: var(--space-6) var(--space-6) var(--space-5);
}

.edit-form {
  padding: 2px 0;
}

/* 表单项标签 */
.edit-form :deep(.el-form-item__label) {
  font-size: var(--font-size-body);
  font-weight: var(--font-weight-semibold);
  color: var(--color-text-1);
  padding-bottom: var(--space-2);
}

.edit-form :deep(.el-form-item) {
  margin-bottom: var(--space-5);
}

/* 输入框圆角 */
.edit-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-lg);
}

.edit-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-lg);
}

/* ===== 标签多选 chips 风格 ===== */
.edit-form :deep(.el-select .el-tag) {
  border-radius: var(--radius-full);
  border: none;
  background: var(--color-primary-tag-bg);
  color: var(--color-primary);
  font-weight: var(--font-weight-medium);
  padding: 0 10px;
}

.edit-form :deep(.el-select .el-tag .el-tag__close) {
  color: var(--color-primary);
}

.edit-form :deep(.el-select .el-tag .el-tag__close:hover) {
  background: var(--color-primary);
  color: #fff;
}

/* 下拉面板圆角 */
.edit-form :deep(.el-select__popper.el-popper) {
  border-radius: var(--radius-lg);
}

/* ===== Markdown 编辑器圆角 ===== */
.edit-form :deep(.md-editor) {
  width: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

/* ===== 操作按钮 ===== */
.edit-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.submit-btn {
  min-width: 120px;
  height: 42px;
  border-radius: var(--radius-lg);
  font-weight: var(--font-weight-semibold);
}

.cancel-btn {
  min-width: 88px;
  height: 42px;
  border-radius: var(--radius-lg);
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .edit-card :deep(.el-card__body) {
    padding: var(--space-4);
  }
  .submit-btn,
  .cancel-btn {
    width: 100%;
    min-width: 0;
  }
}
</style>
