<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import {
  bookCatalog,
  getCatalogIndex,
  getDisplayCover,
  getDisplayTitle,
  getFallbackCover,
  getDisplayPrice,
  getDisplayRate,
  getDisplayDescription,
  setCatalogMapping
} from '@/utils/catalog'

const router = useRouter()
const products = ref([])
const isLoading = ref(true)
const query = ref('')
const sortKey = ref('default')

onMounted(async () => {
  try {
    const res = await axios.get('/api/products')
    products.value = res.data?.data || res.data || []
    setCatalogMapping(products.value, { overwrite: true })
  } catch (error) {
    ElMessage.error('获取商品失败')
  } finally {
    isLoading.value = false
  }
})

const viewDetail = (product) => {
  const id = product?.id ?? product?.productId
  if (!id) {
    return
  }
  router.push(`/product/${id}`)
}

const handleCoverError = (event, product, index) => {
  if (event.target.dataset.fallback === 'true') {
    return
  }
  event.target.dataset.fallback = 'true'
  event.target.src = getFallbackCover(product, index)
}

const displayProducts = computed(() => {
  const base = products.value.slice()
  const mapped = new Map()
  base.forEach((product, index) => {
    const catalogIndex = getCatalogIndex(product, index)
    if (!mapped.has(catalogIndex)) {
      mapped.set(catalogIndex, {
        ...product,
        catalogIndex
      })
    }
  })
  return bookCatalog.map((entry, catalogIndex) => {
    return (
      mapped.get(catalogIndex) || {
        id: `virtual-${catalogIndex}`,
        catalogIndex,
        isVirtual: true
      }
    )
  })
})

const filteredProducts = computed(() => {
  const term = query.value.trim().toLowerCase()
  let list = displayProducts.value.slice()
  if (term) {
    list = list.filter((product, index) => {
      const title = getDisplayTitle(product, index).toLowerCase()
      const description = String(getDisplayDescription(product, index) || product.description || '').toLowerCase()
      return title.includes(term) || description.includes(term)
    })
  }

  if (sortKey.value === 'price-asc') {
    list.sort((a, b) => getDisplayPrice(a) - getDisplayPrice(b))
  } else if (sortKey.value === 'price-desc') {
    list.sort((a, b) => getDisplayPrice(b) - getDisplayPrice(a))
  } else if (sortKey.value === 'rate-desc') {
    list.sort((a, b) => getDisplayRate(b) - getDisplayRate(a))
  }

  return list
})
</script>

<template>
  <div class="home-page page-shell">
    <section class="hero hero-solo">
      <div>
        <span class="hero-badge">BlancBooks精选</span>
        <h1 class="hero-title">发现下一本爱不释手的好书</h1>
        <p class="subtle-text">
          精选榜单、口碑评分与折扣推荐，让阅读更简单。
        </p>
      </div>
    </section>

    <div class="page-header">
      <div>
        <h2 class="section-title">精选好书</h2>
        <p class="subtle-text">支持关键词搜索和排序。</p>
      </div>
      <div class="input-row">
        <el-input
          v-model="query"
          clearable
          placeholder="搜索书名或描述"
          style="min-width: 220px"
        />
        <el-select v-model="sortKey" style="min-width: 160px">
          <el-option label="默认排序" value="default" />
          <el-option label="价格从低到高" value="price-asc" />
          <el-option label="价格从高到低" value="price-desc" />
          <el-option label="评分从高到低" value="rate-desc" />
        </el-select>
      </div>
    </div>

    <div v-if="isLoading" class="grid-cards">
      <div v-for="n in 8" :key="n" class="content-card product-card skeleton-card">
        <div class="skeleton-block cover"></div>
        <div class="skeleton-block line"></div>
        <div class="skeleton-block line short"></div>
        <div class="skeleton-block line tiny"></div>
      </div>
    </div>

    <div v-else-if="filteredProducts.length" class="grid-cards">
      <el-card
        v-for="(product, index) in filteredProducts"
        :key="product.id || `catalog-${product.catalogIndex || index}`"
        class="product-card card-hover content-card"
        shadow="never"
      >
        <img
          :src="getDisplayCover(product, index)"
          class="product-cover"
          @error="(event) => handleCoverError(event, product, index)"
        />
        <h3>{{ getDisplayTitle(product, index) }}</h3>
        <div class="price-tag">￥{{ getDisplayPrice(product, index).toFixed(2) }}</div>
        <div class="muted-chip">评分 {{ getDisplayRate(product, index).toFixed(1) }}</div>
        <el-button type="primary" size="small" @click="viewDetail(product)">
          查看详情
        </el-button>
      </el-card>
    </div>

    <el-empty v-else description="未找到相关商品，请尝试更换关键词。"/>
  </div>
</template>

<style scoped>
.skeleton-card {
  display: grid;
  gap: 12px;
}

.skeleton-block {
  background: linear-gradient(90deg, rgba(31, 26, 22, 0.08), rgba(31, 26, 22, 0.16), rgba(31, 26, 22, 0.08));
  border-radius: 10px;
  animation: shimmer 1.4s ease infinite;
  height: 14px;
}

.skeleton-block.cover {
  height: 220px;
}

.skeleton-block.short {
  width: 70%;
}

.skeleton-block.tiny {
  width: 45%;
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
