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

const router = useRouter()

const handleRegister = async () => {
  try {
    await axios.post('/api/accounts', form.value)
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    // 显示具体错误信息
    const errorMsg = error.response?.data?.msg || '注册失败';
    ElMessage.error(errorMsg);
  }
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-box">
      <h2>用户注册</h2>
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户名" required>
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="form.role">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.telephone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister">注册</el-button>
          <el-button @click="router.push('/login')">去登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  padding-top: 50px;
}
.register-box {
  width: 600px;
  padding: 20px;
}
</style>