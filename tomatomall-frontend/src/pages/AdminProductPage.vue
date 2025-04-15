<script setup>
import { ref, onMounted } from 'vue'
import axios from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// 商品列表
const products = ref([])
// 表单对话框可见性
const dialogVisible = ref(false)
// 当前操作模式（create/edit）
const mode = ref('create')
// 表单数据
const form = ref({
  id: null,
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: '',
  specifications: [],
  stockpile: 0
})

// 获取商品列表
const loadProducts = async () => {
  try {
    const res = await axios.get('/api/products')
    products.value = await Promise.all(res.data.map(async product => {
      // 获取每个商品的库存信息
      const stockRes = await axios.get(`/api/products/stockpile/${product.id}`)
      return {
        ...product,
        stockpile: stockRes.data.amount
      }
    }))
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  }
}

// 删除商品
const handleDelete = (id) => {
  ElMessageBox.confirm('确认删除该商品？', '警告', { type: 'warning' })
      .then(async () => {
        await axios.delete(`/api/products/${id}`)
        ElMessage.success('删除成功')
        await loadProducts()
      })
      .catch(() => {})
}

// 打开编辑对话框
const openEditDialog = async (product) => {
  mode.value = 'edit'
  form.value = { ...product }
  try {
    // 获取库存信息
    const res = await axios.get(`/api/products/stockpile/${product.id}`)
    form.value.stockpile = res.data.amount
  } catch (error) {
    form.value.stockpile = 0
  }
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  try {
    let productId
    if (mode.value === 'create') {
      const createRes = await axios.post('/api/products', form.value)
      productId=createRes.data.id
      await axios.patch(`/api/products/stockpile/${productId}`, {
        amount: form.value.stockpile
      })
    } else {
      await axios.put('/api/products', form.value)
      productId=form.value.id
      // 更新库存
      await axios.patch(`/api/products/stockpile/${productId}`, {
        amount: form.value.stockpile})
    }
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error(error.msg || '操作失败')
  }
}

onMounted(loadProducts)
</script>

<template>
  <div class="admin-product">
    <div class="header">
      <h1>商品管理</h1>
      <el-button type="primary" @click="dialogVisible = true; mode = 'create'">
        新增商品
      </el-button>
    </div>

    <el-table :data="products" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="商品名称" />
      <el-table-column prop="price" label="价格" width="120" />
      <el-table-column prop="rate" label="评分" width="120" />
      <el-table-column prop="stockpile" label="库存" width="120" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="mode === 'create' ? '新增商品' : '编辑商品'">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="form.price" :min="0" />
        </el-form-item>
        <el-form-item label="评分" required>
          <el-input-number v-model="form.rate" :min="0" :max="10" />
        </el-form-item>
        <el-form-item label="库存数量" required>
          <el-input-number
              v-model="form.stockpile"
              :min="0"
              :max="9999"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.cover" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="form.detail" type="textarea" rows="4" />
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
.header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>