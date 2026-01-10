<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { getDisplayPrice, getDisplayTitle, setCatalogMapping } from '@/utils/catalog'

const router = useRouter()
const cartData = ref({
  items: [],
  totalAmount: 0
})
const isLoading = ref(true)
const hasItems = computed(() => cartData.value.items.length > 0)
const displayTotal = computed(() =>
  cartData.value.items.reduce((sum, item, index) => {
    return sum + getDisplayPrice(item, index) * Number(item.quantity || 0)
  }, 0)
)

// 添加公共请求头配置
const authAxios = axios.create({
  headers: {
    'token': localStorage.getItem('token') // 自动携带token
  }
})

// 获取购物车数据
const ensureLoggedIn = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后查看购物车')
    router.push('/login')
    return false
  }
  authAxios.defaults.headers.token = token
  return true
}

const loadCart = async () => {
  if (!ensureLoggedIn()) {
    isLoading.value = false
    return
  }
  try {
    const res = await authAxios.get('/api/cart')
    cartData.value = res.data.data
    setCatalogMapping(cartData.value.items)
  } catch (error) {
    ElMessage.error('获取购物车失败')
  } finally {
    isLoading.value = false
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
    query: { cartItemIds: JSON.stringify(selectedItems), displayTotal: displayTotal.value.toFixed(2) }
  })
}

onMounted(loadCart)
</script>

<template>
  <div class="cart-page page-shell">
    <div class="page-header">
      <div>
        <h1 class="section-title">购物车</h1>
        <p class="subtle-text">随时调整数量，确认后进入结算。</p>
      </div>
    </div>

    <el-card class="table-card content-card">
      <el-skeleton v-if="isLoading" animated>
        <template #template>
          <div class="skeleton-block" style="height: 200px;"></div>
        </template>
      </el-skeleton>
      <el-empty v-else-if="!hasItems" description="购物车还是空的，去挑几本书吧。" />
      <el-table v-else :data="cartData.items" border stripe>
        <el-table-column label="商品名称">
          <template #default="{ row, $index }">{{ getDisplayTitle(row, $index) }}</template>
        </el-table-column>
        <el-table-column label="单价" width="120">
          <template #default="{ row, $index }">￥{{ getDisplayPrice(row, $index).toFixed(2) }}</template>
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

      <div v-if="hasItems" class="total-section">
        <h3>总金额：￥{{ displayTotal.toFixed(2) }}</h3>
        <el-button type="primary" size="large" @click="goToCheckout">去结算</el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.total-section {
  margin-top: 20px;
  text-align: right;
  padding: 16px;
  background: rgba(228, 72, 58, 0.08);
  border-radius: 12px;
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
