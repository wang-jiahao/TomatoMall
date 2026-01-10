<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const form = ref({
  username: '',
  password: ''
})
const isSubmitting = ref(false)

const router = useRouter()

const handleLogin = async () => {
  try {
    isSubmitting.value = true
    const res = await axios.post('/api/accounts/login', form.value);
    const token = res.data.data;

    // 存储 Token 和用户名
    localStorage.setItem('token', token);
    localStorage.setItem('username', form.value.username); // 确保此处正确存储

    ElMessage.success('登录成功');
    router.push('/profile');
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '登录失败');
  } finally {
    isSubmitting.value = false
  }
};
</script>

<template>
  <div class="auth-layout page-shell">
    <div class="auth-aside">
      <span class="hero-badge">欢迎回来</span>
      <h1 class="auth-title">继续你的阅读清单</h1>
      <p class="subtle-text">登录后查看购物车、订单进度与收藏书单。</p>
    </div>
    <el-card class="auth-card glass-card">
      <h2>用户登录</h2>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" clearable autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <div class="auth-actions">
            <el-button type="primary" :loading="isSubmitting" @click="handleLogin">登录</el-button>
            <el-button @click="router.push('/register')">去注册</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.auth-card {
  width: min(420px, 100%);
}
</style>
