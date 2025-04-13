<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const products = ref([])
const showAddDialog = ref(false)
const showStockDialog = ref(false)
const currentProduct = ref({})
const adjustAmount = ref(0)

// 商品表单
const form = ref({
  title: '',
  price: 0,
  rate: 0,
  description: '',
  cover: '',
  detail: '',
  specifications: []
})

onMounted(() => loadProducts())

const loadProducts = async () => {
  try {
    const res = await axios.get('/api/products')
    products.value = res.data.data
  } catch (error) {
    ElMessage.error('获取商品失败')
  }
}

const deleteProduct = async (id) => {
  try {
    await axios.delete(`/api/products/${id}`)
    ElMessage.success('删除成功')
    loadProducts()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const submitForm = async () => {
  try {
    await axios.post('/api/products', form.value)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const adjustStock = async () => {
  try {
    await axios.patch(`/api/products/stockpile/${currentProduct.value.id}`, {
      amount: adjustAmount.value
    })
    ElMessage.success('调整成功')
    showStockDialog.value = false
    loadProducts()
  } catch (error) {
    ElMessage.error('调整失败')
  }
}
</script>

<template>
  <div class="admin-page">
    <h1>商品管理</h1>

    <el-button
        type="primary"
        @click="showAddDialog = true"
    >
      添加商品
    </el-button>

    <el-table :data="products" class="product-table">
      <el-table-column prop="id" label="ID"/>
      <el-table-column prop="title" label="商品名称"/>
      <el-table-column prop="price" label="价格"/>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button
              type="danger"
              @click="deleteProduct(scope.row.id)"
          >
            删除
          </el-button>
          <el-button
              @click="currentProduct = scope.row; showStockDialog = true"
          >
            调库存
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加商品对话框 -->
    <el-dialog v-model="showAddDialog" title="添加商品">
      <el-form :model="form">
        <el-form-item label="商品名称">
          <el-input v-model="form.title"/>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0"/>
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.cover"/>
        </el-form-item>
        <el-button type="primary" @click="submitForm">提交</el-button>
      </el-form>
    </el-dialog>

    <!-- 调整库存对话框 -->
    <el-dialog v-model="showStockDialog" title="调整库存">
      <el-input-number
          v-model="adjustAmount"
          :min="0"
          label="调整数量"
      />
      <el-button @click="adjustStock">确认调整</el-button>
    </el-dialog>
  </div>
</template>

<style scoped>
.product-table {
  margin-top: 20px;
}
</style>