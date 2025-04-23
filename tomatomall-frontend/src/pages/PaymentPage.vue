<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRoute } from 'vue-router'

const route = useRoute()
const paymentForm = ref('')
const isPaymentLoaded = ref(false)

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
  loadPayment()
})
</script>

<template>
  <div class="payment-page">
    <h1>支付订单</h1>
    <p>订单金额：¥{{ route.query.totalAmount }}</p>
    <el-button
        type="primary"
        @click="handleConfirmPayment"
        :disabled="!isPaymentLoaded"
        class="confirm-btn"
    >
      {{ isPaymentLoaded ? '确认支付' : '加载支付信息中...' }}
    </el-button>
    <div id="alipay-container"></div>
  </div>
</template>

<style scoped>
.payment-page {
  padding: 20px;
  text-align: center;
}
#alipay-container {
  margin: 20px auto;
  max-width: 800px;
}
</style>
