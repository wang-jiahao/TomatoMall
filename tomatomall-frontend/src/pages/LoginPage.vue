<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const form = ref({
  username: '',
  password: ''
})

const router = useRouter()

const handleLogin = async () => {
  try {
    const res = await axios.post('/api/accounts/login', form.value);
    const token = res.data.data;

    // 存储 Token 和用户名
    localStorage.setItem('token', token);
    localStorage.setItem('username', form.value.username); // 确保此处正确存储

    ElMessage.success('登录成功');
    router.push('/profile');
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '登录失败');
  }
};
</script>

<template>
  <div class="login-container">
    <el-card class="login-box">
      <h2>用户登录</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin">登录</el-button>
          <el-button @click="router.push('/register')">去注册</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  padding-top: 100px;
}
.login-box {
  width: 500px;
  padding: 20px;
}
</style>