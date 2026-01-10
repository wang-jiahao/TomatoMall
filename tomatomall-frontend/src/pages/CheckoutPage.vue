<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const form = ref({
  shippingAddress: '',
  paymentMethod: 'ALIPAY'
})
const isSubmitting = ref(false)

const ensureLoggedIn = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录后提交订单')
    router.push('/login')
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

// 从路由参数获取购物车项
const cartItemIds = ref([])
const displayTotal = ref('')
onMounted(() => {
  if (!ensureLoggedIn()) {
    return
  }
  const ids = router.currentRoute.value.query.cartItemIds
  if (ids) cartItemIds.value = JSON.parse(ids)
  displayTotal.value = router.currentRoute.value.query.displayTotal || ''
})

// 提交订单
const handleSubmit = async () => {
  try {
    isSubmitting.value = true
    const res = await authAxios.post('/api/cart/checkout', {
      cartItemIds: cartItemIds.value,
      shippingAddress: form.value.shippingAddress,
      paymentMethod: form.value.paymentMethod
    })

    router.push({
      path: `/payment/${res.data.data.orderId}`,
      query: { totalAmount: res.data.data.totalAmount, displayTotal: displayTotal.value }
    })
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '创建订单失败')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="checkout-page page-shell">
    <div class="page-header">
      <div>
        <h1 class="section-title">订单结算</h1>
        <p class="subtle-text">确认地址与支付方式后提交订单。</p>
      </div>
    </div>
    <el-card class="content-card">
      <el-form :model="form" label-position="top">
        <el-form-item label="收货地址" required>
          <el-input
            v-model="form.shippingAddress"
            type="textarea"
            placeholder="请输入详细收货地址"
            rows="4"
          />
        </el-form-item>
        <el-form-item label="支付方式" required>
          <el-select v-model="form.paymentMethod">
            <el-option label="支付宝" value="ALIPAY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="isSubmitting" @click="handleSubmit">提交订单</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
</style>
