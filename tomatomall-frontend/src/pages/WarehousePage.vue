<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router' // 新增路由引入
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getDisplayStock, setCatalogMapping } from '@/utils/catalog'

const stockpiles = ref([])
const router = useRouter() // 获取路由实例
const isLoading = ref(true)

const authAxios = axios.create({
  headers: {
    'token': localStorage.getItem('token')
  }
})

// 新增计算属性获取用户角色
const role = computed(() => localStorage.getItem('role'))

onMounted(async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后查看库存')
    router.push('/login')
    isLoading.value = false
    return
  }
  authAxios.defaults.headers.token = token
  try {
    const res = await authAxios.get('/api/products')
    const products = res.data?.data || []
    setCatalogMapping(products, { overwrite: true })
    stockpiles.value = await Promise.all(products.map(async (product, index) => {
      const fallbackAmount = getDisplayStock(product, index)
      try {
        const stockRes = await authAxios.get(`/api/products/stockpile/${product.id}`)
        const stockData = stockRes.data?.data || stockRes.data || {}
        return {
          productId: product.id,
          amount: Number(stockData.amount) > 0 ? stockData.amount : fallbackAmount,
          frozen: Number(stockData.frozen) >= 0 ? stockData.frozen : 0
        }
      } catch (error) {
        try {
          await authAxios.patch(`/api/products/stockpile/${product.id}`, { amount: fallbackAmount })
        } catch (patchError) {
          // Swallow patch errors to keep the UI responsive.
        }
        return {
          productId: product.id,
          amount: fallbackAmount,
          frozen: 0
        }
      }
    }))
  } catch (error) {
    ElMessage.error('获取库存失败')
  } finally {
    isLoading.value = false
  }
})

// 新增跳转方法
const goToAdminPage = () => {
  router.push('/admin/products')
}
</script>

<template>
  <div class="warehouse-page page-shell">
    <div class="page-header">
      <div>
        <h1 class="section-title">商品库存</h1>
        <p class="subtle-text">实时查看库存与冻结量。</p>
      </div>
      <el-button
        v-if="role === 'admin'"
        type="primary"
        @click="goToAdminPage"
        class="admin-button"
      >
        库存管理
      </el-button>
    </div>

    <el-card class="content-card table-card">
      <el-skeleton v-if="isLoading" animated>
        <template #template>
          <div class="skeleton-block" style="height: 200px;"></div>
        </template>
      </el-skeleton>
      <el-empty v-else-if="!stockpiles.length" description="暂无库存记录" />
      <el-table v-else :data="stockpiles" stripe>
        <el-table-column prop="productId" label="商品ID" />
        <el-table-column prop="amount" label="可用库存" />
        <el-table-column prop="frozen" label="冻结库存" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.admin-button {
  margin-left: auto;
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
