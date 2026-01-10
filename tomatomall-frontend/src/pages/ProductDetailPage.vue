<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from '@/utils/request'
import { ElMessage } from 'element-plus'
import {
  getDisplayCover,
  getDisplayTitle,
  getFallbackCover,
  getDisplayPrice,
  getDisplayRate,
  getDisplayStock,
  getDisplayDescription,
  getDisplayDetail,
  getDisplaySpecs,
  getCatalogEntryByIndex,
  getCatalogIndex,
  setCatalogMappingEntry
} from '@/utils/catalog'

const product = ref({})
const route = useRoute()
const router = useRouter()
const isLoading = ref(true)

const displayDescription = computed(() => getDisplayDescription(product.value) || product.value.description || '')
const displayDetail = computed(() => getDisplayDetail(product.value) || product.value.detail || '')
const displaySpecs = computed(() => getDisplaySpecs(product.value))
const hasSpecs = computed(() => displaySpecs.value.length > 0)
const displayTitle = computed(() => getDisplayTitle(product.value))
const displayCover = computed(() => getDisplayCover(product.value))
const displayPrice = computed(() => getDisplayPrice(product.value))
const displayRate = computed(() => getDisplayRate(product.value))
const displayStock = computed(() => getDisplayStock(product.value))

const parseProductId = (response) => {
  if (!response) {
    return null
  }
  return (
    response.data?.id ||
    response.data?.data?.id ||
    response.id ||
    response.data?.productId ||
    response.data?.data?.productId
  )
}

const buildSpecs = (entry) => ([
  { item: '作者', value: entry.author || '—' },
  { item: '出版社', value: entry.publisher || '—' },
  { item: '出版日期', value: entry.publishedAt || '—' }
])

const ensureStockpile = async (productId, amount) => {
  const safeAmount = Number(amount) > 0 ? Number(amount) : 20
  try {
    const res = await axios.get(`/api/products/stockpile/${productId}`)
    const currentAmount =
      res?.data?.amount ??
      res?.data?.data?.amount ??
      res?.amount ??
      res?.data?.amount
    if (Number(currentAmount) > 0) {
      return true
    }
  } catch (error) {
    // Ignore and attempt to patch.
  }
  try {
    await axios.patch(`/api/products/stockpile/${productId}`, { amount: safeAmount })
    return true
  } catch (error) {
    return false
  }
}

const ensureProductExists = async () => {
  const catalogIndex = Number.isFinite(Number(product.value.catalogIndex))
    ? Number(product.value.catalogIndex)
    : getCatalogIndex(product.value)
  const entry = getCatalogEntryByIndex(catalogIndex)
  if (!entry) {
    return null
  }
  try {
    const payload = {
      title: entry.title,
      price: entry.price,
      rate: entry.rate,
      description: entry.description,
      cover: entry.cover,
      detail: entry.detail,
      specifications: buildSpecs(entry)
    }
    const res = await axios.post('/api/products', payload)
    const productId = parseProductId(res)
    if (!productId) {
      return null
    }
    await ensureStockpile(productId, entry.stock)
    setCatalogMappingEntry(productId, catalogIndex)
    product.value = {
      ...product.value,
      id: productId,
      catalogIndex
    }
    return productId
  } catch (error) {
    return null
  }
}

const addToCart = async (productId) => {
  let actualProductId = productId || product.value?.id || product.value?.productId
  if (String(actualProductId || '').startsWith('virtual-')) {
    actualProductId = await ensureProductExists()
    if (!actualProductId) {
      ElMessage.error('商品尚未创建，请使用管理员账号补充库存')
      return
    }
  }
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后再加入购物车')
    router.push('/login')
    return
  }
  try {
    axios.defaults.headers.token = token
    await ensureStockpile(actualProductId, displayStock.value)
    await axios.post('/api/cart', { productId: actualProductId, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (error) {
    const message = error.response?.data?.msg || '操作失败'
    if (message.includes('库存')) {
      const ensured = await ensureStockpile(actualProductId, displayStock.value)
      if (ensured) {
        try {
          await axios.post('/api/cart', { productId: actualProductId, quantity: 1 })
          ElMessage.success('已加入购物车')
          return
        } catch (retryError) {
          ElMessage.error(retryError.response?.data?.msg || message)
          return
        }
      }
    }
    ElMessage.error(message)
  }
}

const handleCoverError = (event) => {
  if (event.target.dataset.fallback === 'true') {
    return
  }
  event.target.dataset.fallback = 'true'
  event.target.src = getFallbackCover(product.value)
}

onMounted(async () => {
  const rawId = String(route.params.id || '')
  if (rawId.startsWith('virtual-')) {
    const index = Number(rawId.replace('virtual-', ''))
    const entry = getCatalogEntryByIndex(index)
    if (entry) {
      product.value = {
        id: rawId,
        catalogIndex: index,
        ...entry
      }
      isLoading.value = false
      return
    }
  }
  try {
    const res = await axios.get(`/api/products/${route.params.id}`)
    product.value = res.data?.data || res.data || {}
  } catch (error) {
    ElMessage.error('获取商品详情失败')
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="product-detail page-shell">
    <el-card class="content-card">
      <el-skeleton v-if="isLoading" animated>
        <template #template>
          <div class="skeleton-block" style="height: 260px;"></div>
        </template>
      </el-skeleton>
      <div v-else class="detail-grid">
        <div class="cover">
          <img :src="displayCover" alt="商品封面" @error="handleCoverError">
        </div>
        <div class="detail-info">
          <h1>{{ displayTitle }}</h1>
          <div class="meta">
            <span class="price-tag">￥{{ displayPrice.toFixed(2) }}</span>
            <el-rate :model-value="displayRate" disabled show-score />
          </div>
          <p class="description">{{ displayDescription }}</p>
          <div class="actions">
            <el-button type="primary" @click="addToCart(product.id)">加入购物车</el-button>
            <el-button @click="router.push('/')">继续浏览</el-button>
          </div>
        </div>
      </div>
      <el-divider />
      <div class="detail-content" v-html="displayDetail"></div>
      <el-divider />
      <h3>商品规格</h3>
      <el-empty v-if="!hasSpecs" description="暂无规格信息" />
      <el-table v-else :data="displaySpecs" border stripe>
        <el-table-column prop="item" label="规格项" width="120" />
        <el-table-column prop="value" label="规格内容" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.5fr) minmax(0, 1fr);
  gap: 24px;
}

.cover img {
  width: 100%;
  border-radius: 16px;
  border: 1px solid rgba(31, 26, 22, 0.12);
  object-fit: cover;
}

.meta {
  display: flex;
  align-items: center;
  gap: 20px;
  margin: 16px 0;
}

.description {
  color: rgba(31, 26, 22, 0.72);
  margin-bottom: 16px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.detail-content {
  color: rgba(31, 26, 22, 0.78);
  line-height: 1.8;
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

@media (max-width: 960px) {
  .detail-grid {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
