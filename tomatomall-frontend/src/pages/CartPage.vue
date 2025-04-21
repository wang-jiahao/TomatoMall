<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const cartData = ref({
  items: [],
  totalAmount: 0
})

// 添加公共请求头配置
const authAxios = axios.create({
  headers: {
    'token': localStorage.getItem('token') // 自动携带token
  }
})

// 获取购物车数据
const loadCart = async () => {
  try {
    const res = await authAxios.get('/api/cart')
    cartData.value = res.data.data
  } catch (error) {
    ElMessage.error('获取购物车失败')
  }
}

// 删除商品
const handleDelete = async (cartItemId) => {
  try {
    await authAxios.delete(`/api/cart/${cartItemId}`)
    ElMessage.success('删除成功')
    await loadCart()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 修改数量
const handleQuantityChange = async (cartItemId, quantity) => {
  try {
    await authAxios.patch(`/api/cart/${cartItemId}`, { quantity })
    ElMessage.success('修改成功')
    await loadCart()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '修改失败')
  }
}

// 跳转到结算页
const goToCheckout = () => {
  const selectedItems = cartData.value.items.map(item => item.cartItemId)
  router.push({
    path: '/checkout',
    query: { cartItemIds: JSON.stringify(selectedItems) }
  })
}

onMounted(loadCart)
</script>

<template>
  <div class="cart-page">
    <h1>购物车</h1>
    <el-table :data="cartData.items" border>
      <el-table-column prop="title" label="商品名称" />
      <el-table-column label="单价" width="120">
        <template #default="{ row }">¥{{ row.price.toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="数量" width="200">
        <template #default="{ row }">
          <el-input-number
              v-model="row.quantity"
              :min="1"
              @change="(val) => handleQuantityChange(row.cartItemId, val)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button type="danger" @click="handleDelete(row.cartItemId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="total-section">
      <h3>总金额：¥{{ cartData.totalAmount.toFixed(2) }}</h3>
      <el-button type="primary" size="large" @click="goToCheckout">去结算</el-button>
    </div>
  </div>
</template>

<style scoped>
.cart-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.total-section {
  margin-top: 20px;
  text-align: right;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>