<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from '@/utils/request'
import { ElMessage } from 'element-plus'

const product = ref({})
const route = useRoute()

const addToCart = async (productId) => {
  try {
    await axios.post('/api/cart', { productId, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(async () => {
  try {
    const res = await axios.get(`/api/products/${route.params.id}`)
    product.value = res.data
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  }
})
</script>

<template>
  <div class="product-detail">
    <el-card>
      <div class="cover">
        <img :src="product.cover" alt="商品封面">
      </div>
      <h1>{{ product.title }}</h1>
      <div class="meta">
        <span class="price">¥{{ product.price }}</span>
        <el-rate :model-value="product.rate" disabled show-score />
      </div>
      <p class="description">{{ product.description }}</p>
      <div class="detail" v-html="product.detail"></div>
      <el-button
          type="primary"
          @click="addToCart(product.id)"
      >加入购物车</el-button>
      <el-divider />
      <h3>商品规格</h3>
      <el-table :data="product.specifications" border>
        <el-table-column prop="item" label="规格项" width="120" />
        <el-table-column prop="value" label="规格内容" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.cover img {
  max-width: 300px;
  max-height: 400px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin: 20px 0;
}
.price {
  font-size: 24px;
  color: #f56c6c;
}
</style>