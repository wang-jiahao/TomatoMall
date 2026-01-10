<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { bookCatalog } from '@/utils/catalog'

const advertisements = ref([])
const isLoading = ref(true)
const router = useRouter()

const featuredIndex = bookCatalog.findIndex((entry) => entry.title === '三国演义')
const featuredRecommendation = featuredIndex >= 0 ? {
  id: 'featured-sanguo',
  title: bookCatalog[featuredIndex].title,
  content: bookCatalog[featuredIndex].description,
  imgUrl: bookCatalog[featuredIndex].cover,
  productId: `virtual-${featuredIndex}`
} : null

const mergeRecommendations = (items = []) => {
  const list = Array.isArray(items) ? items : []
  if (!featuredRecommendation) {
    return list
  }
  const filtered = list.filter((ad) => ad?.id !== featuredRecommendation.id && ad?.productId !== featuredRecommendation.productId)
  return [featuredRecommendation, ...filtered]
}

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
    const data = res.data?.data || res.data || []
    advertisements.value = mergeRecommendations(data)
  } catch (error) {
    ElMessage.error('获取广告失败')
    advertisements.value = mergeRecommendations([])
  } finally {
    isLoading.value = false
  }
}

// 点击广告跳转到商品详情
const goToProduct = (productId) => {
  if (!productId) {
    return
  }
  router.push(`/product/${productId}`)
}

onMounted(loadAdvertisements)
</script>

<template>
  <div class="advertisements-page page-shell">
    <div class="page-header">
      <div>
        <h1 class="section-title">今日推荐</h1>
        <p class="subtle-text">精选推荐书单与活动信息。</p>
      </div>
    </div>

    <div v-if="isLoading" class="grid-cards">
      <div v-for="n in 6" :key="n" class="content-card product-card skeleton-card"></div>
    </div>

    <div v-else-if="advertisements.length" class="recommendation-list">
      <el-card
        v-for="ad in advertisements"
        :key="ad.id"
        class="card-hover content-card recommendation-card"
        shadow="never"
        @click="goToProduct(ad.productId)"
      >
        <img :src="ad.imgUrl" :alt="ad.title" class="ad-image" />
        <div class="ad-copy">
          <h3 class="ad-title">{{ ad.title }}</h3>
          <p class="content">{{ ad.content }}</p>
        </div>
      </el-card>
    </div>

    <el-empty v-else description="暂无广告信息" />
  </div>
</template>

<style scoped>
.recommendation-list {
  display: grid;
  gap: 20px;
  grid-template-columns: minmax(0, 1fr);
}

.recommendation-card {
  width: 100%;
}

.recommendation-card :deep(.el-card__body) {
  display: grid;
  grid-template-columns: minmax(180px, 230px) minmax(0, 1fr);
  gap: 24px;
  align-items: center;
}

.ad-image {
  width: 100%;
  aspect-ratio: 4 / 5;
  height: auto;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid rgba(31, 26, 22, 0.12);
}

.ad-copy {
  display: grid;
  gap: 10px;
}

.ad-title {
  font-size: clamp(18px, 2vw, 22px);
}

.content {
  color: rgba(31, 26, 22, 0.72);
  margin-top: 0;
}

.skeleton-card {
  height: 260px;
  background: linear-gradient(90deg, rgba(31, 26, 22, 0.08), rgba(31, 26, 22, 0.16), rgba(31, 26, 22, 0.08));
  border-radius: 16px;
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

@media (max-width: 700px) {
  .recommendation-card :deep(.el-card__body) {
    grid-template-columns: minmax(0, 1fr);
  }

  .ad-image {
    max-width: 240px;
    justify-self: center;
  }
}
</style>
