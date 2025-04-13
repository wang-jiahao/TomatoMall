<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const products = ref([])

onMounted(async () => {
  try {
    const res = await axios.get('/api/products')
    products.value = res.data.data
  } catch (error) {
    ElMessage.error('获取商品失败')
  }
})

const viewDetail = (id) => {
  router.push(`/product/${id}`)
}
</script>

<template>
  <div class="home-page">
    <h1>番茄书城</h1>

    <el-row :gutter="20" class="product-list">
      <el-col
          v-for="product in products"
          :key="product.id"
          :span="6"
          class="product-item"
      >
        <el-card class="product-card">
          <img :src="product.cover" class="product-cover"/>
          <h3>{{ product.title }}</h3>
          <p class="price">¥ {{ product.price.toFixed(2) }}</p>
          <p class="rate">评分: {{ product.rate }}</p>
          <el-button
              type="primary"
              @click="viewDetail(product.id)"
          >
            查看详情
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.product-list {
  margin: 20px;
}

.product-card {
  margin-bottom: 20px;
  text-align: center;
}

.product-cover {
  width: 200px;
  height: 250px;
  object-fit: cover;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: bold;
}

.rate {
  color: #e6a23c;
}
</style>
