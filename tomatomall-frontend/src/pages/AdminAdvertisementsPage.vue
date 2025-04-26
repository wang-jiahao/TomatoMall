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
  <div class="admin-advertisements">
    <div class="header">
      <h1>广告管理</h1>
      <el-button type="primary" @click="dialogVisible = true; mode = 'create'; form = { id: null, title: '', content: '', imgUrl: '', productId: '' }">
        新增广告
      </el-button>
    </div>

    <el-table :data="advertisements" border>
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

    <!-- 编辑/新增对话框 -->
    <el-dialog v-model="dialogVisible" :title="mode === 'create' ? '新增广告' : '编辑广告'">
      <el-form :model="form" label-width="80px">
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