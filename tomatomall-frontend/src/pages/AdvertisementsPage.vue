<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const advertisements = ref([])
const router = useRouter()

// 添加公共请求头配置
const authAxios = axios.create({
  headers: {
    'token': localStorage.getItem('token') // 自动携带token
  }
})

// 获取广告列表
const loadAdvertisements = async () => {
  try {
    const res = await authAxios.get('/api/advertisements')
    advertisements.value = res.data.data
  } catch (error) {
    ElMessage.error('获取广告失败')
  }
}

// 点击广告跳转到商品详情
const goToProduct = (productId) => {
  router.push(`/product/${productId}`)
}

onMounted(loadAdvertisements)
</script>

<template>
  <div class="advertisements-page">
    <h1>广告列表</h1>
    <el-row :gutter="20">
      <el-col
          v-for="ad in advertisements"
          :key="ad.id"
          :span="8"
          class="ad-item"
      >
        <el-card @click="goToProduct(ad.productId)" style="cursor: pointer;">
          <img :src="ad.imgUrl" class="ad-image" />
          <h3>{{ ad.title }}</h3>
          <p class="content">{{ ad.content }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.ad-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}
.content {
  color: #666;
  margin-top: 10px;
}
</style>