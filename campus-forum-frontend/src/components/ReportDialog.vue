<template>
  <el-dialog
    :model-value="modelValue"
    title="提交举报"
    width="440px"
    destroy-on-close
    @close="handleClose"
  >
    <p class="report-hint">仅可举报表白墙内容。管理员处理后台可看到真实作者。</p>
    <el-form label-position="top">
      <el-form-item label="举报原因" required>
        <el-radio-group v-model="reason">
          <el-radio :label="1">垃圾广告</el-radio>
          <el-radio :label="2">辱骂骚扰</el-radio>
          <el-radio :label="3">色情低俗</el-radio>
          <el-radio :label="4">人身攻击</el-radio>
          <el-radio :label="5">其他</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="补充说明（可选）">
        <el-input
          v-model="description"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="可以补充具体说明，便于管理员判断"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="danger" :loading="submitting" @click="submit">提交举报</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { createReport } from '@/api/report'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  targetType: { type: Number, required: true },
  targetId: { type: [Number, String], default: null }
})

const emit = defineEmits(['update:modelValue'])

const reason = ref(2)
const description = ref('')
const submitting = ref(false)

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      reason.value = 2
      description.value = ''
    }
  }
)

const handleClose = () => {
  emit('update:modelValue', false)
}

const submit = async () => {
  if (!props.targetId) return
  submitting.value = true
  try {
    await createReport({
      targetType: props.targetType,
      targetId: Number(props.targetId),
      reason: reason.value,
      description: description.value?.trim() || undefined
    })
    ElMessage.success('举报已提交，管理员将尽快处理')
    handleClose()
  } catch (err) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.report-hint {
  margin: 0 0 var(--space-4);
  font-size: var(--font-size-sm);
  color: var(--color-text-3);
  line-height: 1.6;
}

:deep(.el-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-start;
}
</style>
