<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const advertisements = ref([])
const dialogVisible = ref(false)
const mode = ref('create')
const form = ref({
  id: null,
  title: '',
  content: '',
  imgUrl: '',
  productId: ''
})
const isLoading = ref(true)

// 添加公共请求头配置
const authAxios = axios.create({
  headers: {
    'token': localStorage.getItem('token') // 自动携带token
  }
})

// 加载广告列表
const loadAdvertisements = async () => {
  try {
    const res = await authAxios.get('/api/advertisements')
    advertisements.value = res.data.data
  } catch (error) {
    ElMessage.error('获取广告失败')
  } finally {
    isLoading.value = false
  }
}

// 删除广告
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该广告？', '警告', { type: 'warning' })
    .then(async () => {
      await authAxios.delete(`/api/advertisements/${id}`)
      ElMessage.success('删除成功')
      loadAdvertisements()
    })
    .catch(() => {})
}

// 打开编辑对话框
const openEditDialog = (ad) => {
  mode.value = 'edit'
  form.value = { ...ad }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    if (mode.value === 'create') {
      await authAxios.post('/api/advertisements', form.value)
    } else {
      await authAxios.put('/api/advertisements', form.value)
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadAdvertisements()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '操作失败')
  }
}

onMounted(loadAdvertisements)
</script>

<template>
  <div class="admin-advertisements page-shell">
    <div class="page-header">
      <div>
        <h1 class="section-title">广告管理</h1>
        <p class="subtle-text">维护首页与活动推广广告。</p>
      </div>
      <el-button
        type="primary"
        @click="dialogVisible = true; mode = 'create'; form = { id: null, title: '', content: '', imgUrl: '', productId: '' }"
      >
        新增广告
      </el-button>
    </div>

    <el-card class="content-card table-card">
      <el-skeleton v-if="isLoading" animated>
        <template #template>
          <div class="skeleton-block" style="height: 200px;"></div>
        </template>
      </el-skeleton>
      <el-table v-else :data="advertisements" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="content" label="内容" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="mode === 'create' ? '新增广告' : '编辑广告'">
      <el-form :model="form" label-position="top">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" />
        </el-form-item>
        <el-form-item label="图片URL" required>
          <el-input v-model="form.imgUrl" />
        </el-form-item>
        <el-form-item label="商品ID" required>
          <el-input v-model="form.productId" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
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
