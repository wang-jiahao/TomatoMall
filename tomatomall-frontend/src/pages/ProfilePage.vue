<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const userInfo = ref({})
const editMode = ref(false)
const form = ref({})
const isLoading = ref(true)
const isSaving = ref(false)
const router = useRouter()

onMounted(() => {
  // 从 localStorage 获取用户名
  const username = localStorage.getItem('username')

  // 检查用户名是否存在
  if (!username) {
    ElMessage.error('未找到用户信息，请重新登录')
    router.push('/login')
    return
  }

  // 发送请求时携带 Token
  axios.get(`/api/accounts/${username}`, {
    headers: {
      'token': localStorage.getItem('token') // 确保携带 Token
    }
  })
    .then(response => {
      userInfo.value = response.data.data
      form.value = { ...response.data.data }
      localStorage.setItem('role', response.data.data.role) // 将角色存储到 localStorage
    })
    .catch(error => {
      if (error.response?.status === 403) {
        ElMessage.error('无权限访问该用户信息')
      } else {
        ElMessage.error('获取用户信息失败')
      }
    })
    .finally(() => {
      isLoading.value = false
    })
})

const handleUpdate = async () => {
  try {
    isSaving.value = true
    const token = localStorage.getItem('token')
    const res = await axios.put('/api/accounts', form.value, {
      headers: { token }
    })

    userInfo.value = res.data.data
    ElMessage.success('更新成功')
    editMode.value = false

    // 新增跳转逻辑
    router.push('/login')  // 跳转到登录页

  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '更新失败')
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div class="profile-container page-shell">
    <el-card class="profile-box content-card">
      <div class="header">
        <div>
          <h2 class="section-title">个人信息</h2>
          <p class="subtle-text">完善资料有助于更快完成订单。</p>
        </div>
        <el-button
          type="primary"
          @click="editMode = !editMode"
          :disabled="isLoading"
        >
          {{ editMode ? '取消编辑' : '编辑信息' }}
        </el-button>
      </div>

      <el-skeleton v-if="isLoading" animated>
        <template #template>
          <div class="skeleton-block" style="height: 200px;"></div>
        </template>
      </el-skeleton>

      <el-form v-else :model="userInfo" label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" :disabled="true" />
        </el-form-item>

        <template v-if="editMode">
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" show-password />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="form.name" />
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
            <el-button type="success" :loading="isSaving" @click="handleUpdate">保存修改</el-button>
          </el-form-item>
        </template>

        <template v-else>
          <el-form-item label="姓名">
            <span>{{ userInfo.name }}</span>
          </el-form-item>
          <el-form-item label="角色">
            <span>{{ userInfo.role }}</span>
          </el-form-item>
          <el-form-item label="手机号">
            <span>{{ userInfo.telephone || '未填写' }}</span>
          </el-form-item>
          <el-form-item label="邮箱">
            <span>{{ userInfo.email || '未填写' }}</span>
          </el-form-item>
          <el-form-item label="地址">
            <span>{{ userInfo.location || '未填写' }}</span>
          </el-form-item>
        </template>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.profile-box {
  max-width: 900px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.skeleton-block {
  background: linear-gradient(90deg, rgba(31, 26, 22, 0.08), rgba(31, 26, 22, 0.16), rgba(31, 26, 22, 0.08));
  border-radius: 10px;
  animation: shimmer 1.4s ease infinite;
}

@keyframes shimmer {
  0% {
    background-position: -200px 0;
  }
  100% {
    background-position: 200px 0;
  }
}
</style>
