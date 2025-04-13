<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router' // 新增路由引入
import axios from 'axios'
import { ElMessage, ElButton } from 'element-plus'

const stockpiles = ref([])
const router = useRouter() // 获取路由实例

// 新增计算属性获取用户角色
const role = computed(() => localStorage.getItem('role'))

onMounted(async () => {
  try {
    const res = await axios.get('/api/products')
    const products = res.data.data
    stockpiles.value = await Promise.all(products.map(async product => {
      const res = await axios.get(`/api/products/stockpile/${product.id}`)
      return {
        productId: product.id,
        ...res.data.data
      }
    }))
  } catch (error) {
    ElMessage.error('获取库存失败')
  }
})

// 新增跳转方法
const goToAdminPage = () => {
  router.push('/admin/products')
}
</script>

<template>
  <div class="warehouse-page">
    <div class="page-header">
      <h1>商品库存</h1>
      <el-button
          v-if="role === 'admin'"
          type="primary"
          @click="goToAdminPage"
          class="admin-button"
      >
        库存管理
      </el-button>
    </div>

    <el-table :data="stockpiles">
      <el-table-column prop="productId" label="商品ID"/>
      <el-table-column prop="amount" label="可用库存"/>
      <el-table-column prop="frozen" label="冻结库存"/>
    </el-table>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 20px;
}

.admin-button {
  margin-right: 30px;
}
</style>