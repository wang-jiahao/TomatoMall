<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRoute } from 'vue-router'

const route = useRoute()
const paymentForm = ref('')
const isPaymentLoaded = ref(false)
const isLoading = ref(true)
const displayAmount = computed(() => route.query.displayTotal || route.query.totalAmount)

const ensureLoggedIn = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后完成支付')
    window.location.href = '/login'
    return false
  }
  authAxios.defaults.headers.token = token
  return true
}

// 创建带token的实例 (与cartpage保持一致)
const authAxios = axios.create({
  headers: {
    'token': localStorage.getItem('token')  // 从localStorage获取token
  }
})

// 获取支付表单
const loadPayment = async () => {
  try {
    const res = await authAxios.post(`/api/orders/${route.params.orderId}/pay`)
    paymentForm.value = res.data.data.paymentForm
    isPaymentLoaded.value = true
  } catch (error) {
    ElMessage.error('获取支付信息失败')
    isPaymentLoaded.value = false
  } finally {
    isLoading.value = false
  }
}

const handleConfirmPayment = () => {
  if (!paymentForm.value) {
    ElMessage.error('支付信息未加载成功，请刷新重试')
    return
  }
  const container = document.getElementById('alipay-container')
  container.innerHTML = '' // 清空容器
  const div = document.createElement('div')
  div.innerHTML = paymentForm.value
  container.appendChild(div)
  document.forms[0].submit()
}

onMounted(() => {
  if (!ensureLoggedIn()) {
    isLoading.value = false
    return
  }
  loadPayment()
})
</script>

<template>
  <div class="payment-page page-shell">
    <div class="page-header">
      <div>
        <h1 class="section-title">支付订单</h1>
        <p class="subtle-text">订单金额：￥{{ displayAmount }}</p>
      </div>
    </div>
    <el-card class="content-card">
      <el-skeleton v-if="isLoading" animated>
        <template #template>
          <div class="skeleton-block" style="height: 140px;"></div>
        </template>
      </el-skeleton>
      <div v-else class="payment-actions">
        <el-button
          type="primary"
          @click="handleConfirmPayment"
          :disabled="!isPaymentLoaded"
          class="confirm-btn"
        >
          {{ isPaymentLoaded ? '确认支付' : '加载支付信息中...' }}
        </el-button>
        <p class="subtle-text">确认后将跳转至支付宝完成支付。</p>
      </div>
      <div id="alipay-container"></div>
    </el-card>
  </div>
</template>

<style scoped>
#alipay-container {
  margin: 20px auto;
  max-width: 800px;
}

.payment-actions {
  display: grid;
  gap: 12px;
  text-align: center;
  margin-bottom: 12px;
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
