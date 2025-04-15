<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus'

const userInfo = ref({})
const editMode = ref(false)
const form = ref({})
const router = useRouter();

onMounted(() => {
  // 从 localStorage 获取用户名
  const username = localStorage.getItem('username');

  // 检查用户名是否存在
  if (!username) {
    ElMessage.error('未找到用户信息，请重新登录');
    router.push('/login');
    return;
  }

  // 发送请求时携带 Token
  axios.get(`/api/accounts/${username}`, {
    headers: {
      'token': localStorage.getItem('token') // 确保携带 Token
    }
  })
      .then(response => {
        userInfo.value = response.data.data;
        localStorage.setItem('role', response.data.data.role); // 将角色存储到 localStorage
      })
      .catch(error => {
        if (error.response?.status === 403) {
          ElMessage.error('无权限访问该用户信息');
        } else {
          ElMessage.error('获取用户信息失败');
        }
      });
});

const loadUserInfo = async () => {
  try {
    const username = localStorage.getItem('username'); // 使用 localStorage 中的用户名
    const token = localStorage.getItem('token');

    const res = await axios.get(`/api/accounts/${username}`, {
      headers: { token }
    });

    userInfo.value = res.data.data;
    // 同步表单数据（关键！）
    form.value = { ...res.data.data };
  } catch (error) {
    ElMessage.error('获取用户信息失败');
  }
};

const handleUpdate = async () => {
  try {
    const token = localStorage.getItem('token');
    const res = await axios.put('/api/accounts', form.value, {
      headers: { token }
    });

    userInfo.value = res.data.data;
    ElMessage.success('更新成功');
    editMode.value = false;

    // 新增跳转逻辑
    router.push('/login');  // 跳转到登录页

  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '更新失败');
  }
};


</script>

<template>
  <div class="profile-container">
    <el-card class="profile-box">
      <div class="header">
        <h2>个人信息</h2>
        <el-button
            type="primary"
            @click="editMode = !editMode"
        >
          {{ editMode ? '取消编辑' : '编辑信息' }}
        </el-button>
      </div>

      <el-form :model="userInfo" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.username" :disabled="true" />
        </el-form-item>

        <template v-if="editMode">
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" />
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
            <el-button type="success" @click="handleUpdate">保存修改</el-button>
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
.profile-container {
  padding: 20px;
}
.profile-box {
  max-width: 800px;
  margin: 0 auto;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>