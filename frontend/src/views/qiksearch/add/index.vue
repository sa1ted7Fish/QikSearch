<template>
  <div class="app-container">
    <!-- 页面标题 - 居中显示 -->
    <div class="page-header">
      <h2>新增题目</h2>
    </div>

    <!-- 表单卡片 - 居中布局 -->
    <div class="form-container">
      <el-card class="form-card">
        <el-form ref="questionFormRef" :model="questionForm" :rules="rules">
          <el-form-item prop="fullText">
            <el-input
                v-model="questionForm.fullText"
                type="textarea"
                rows="6"
                placeholder="请输入题目内容..."
                maxlength="2000"
                show-word-limit
                class="full-width-input"
            />
          </el-form-item>

          <!-- 操作按钮区 - 居中显示 -->
          <el-form-item class="button-group">
            <el-button type="primary" @click="handleSubmit" size="default">提交</el-button>
            <el-button @click="handleReset" size="default" class="ml-2">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { addQuestion } from '@/api/qiksearch/add'

// 表单数据
const questionForm = reactive({
  fullText: ''  // 确保初始值为字符串
})

// 表单验证规则
const rules = {
  fullText: [
    { required: true, message: '请输入题目内容', trigger: 'blur' },
    { min: 5, message: '题目内容长度不能少于5个字符', trigger: 'blur' }
  ]
}

// 表单引用
const questionFormRef = ref(null)

// 提交表单
const handleSubmit = async () => {
  if (!questionFormRef.value) return
  try {
    await questionFormRef.value.validate()

    const response = await addQuestion(questionForm.fullText)
    if (response.code === 200) {
      ElMessage.success('题目新增成功')
      questionFormRef.value.resetFields()
    } else {
      ElMessage.error(response.msg || '题目新增失败')
    }
  } catch (error) {
    if (error.name !== 'Error') {
      ElMessage.error('操作失败，请稍后重试')
      console.error('新增题目错误:', error)
    }
  }
}

// 重置表单
const handleReset = () => {
  if (questionFormRef.value) {
    questionFormRef.value.resetFields()
  }
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  padding: 20px;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  align-items: center; /* 整体内容居中 */
}

.page-header {
  margin: 40px 0 30px;
  text-align: center; /* 标题文本居中 */
  width: 100%;
  max-width: 800px;
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.form-container {
  width: 100%;
  max-width: 800px; /* 限制最大宽度，类似搜索引擎风格 */
  width: 90%; /* 响应式宽度 */
}

.form-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border: none;
}

.full-width-input {
  width: 100%; /* 输入框占满容器 */
  font-size: 16px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  padding: 12px 15px;
  min-height: 120px;
  resize: vertical;
}

.button-group {
  display: flex;
  justify-content: center; /* 按钮组居中 */
  margin-top: 20px;
}

:deep(.el-button) {
  padding: 10px 20px;
  font-size: 15px;
  border-radius: 6px;
}

:deep(.el-form-item__label) {
  font-size: 16px;
  color: #606266;
}
</style>

