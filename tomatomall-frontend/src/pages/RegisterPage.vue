<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const form = ref({
  username: '',
  password: '',
  name: '',
  role: 'user',
  telephone: '',
  email: '',
  location: ''
})
const isSubmitting = ref(false)

const router = useRouter()

const handleRegister = async () => {
  try {
    isSubmitting.value = true
    await axios.post('/api/accounts', form.value)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    // 显示具体错误信息
    const errorMsg = error.response?.data?.msg || '注册失败';
    ElMessage.error(errorMsg);
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="auth-layout page-shell">
    <div class="auth-aside">
      <span class="hero-badge">新用户指引</span>
      <h1 class="auth-title">创建你的阅读档案</h1>
      <p class="subtle-text">注册后即可收藏、下单并查看物流进度。</p>
    </div>
    <el-card class="auth-card glass-card">
      <h2>用户注册</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" clearable autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" clearable />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.telephone" clearable />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" clearable />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.location" clearable />
        </el-form-item>
        <el-form-item>
          <div class="auth-actions">
            <el-button type="primary" :loading="isSubmitting" @click="handleRegister">注册</el-button>
            <el-button @click="router.push('/login')">去登录</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.auth-card {
  width: min(480px, 100%);
}
</style>
