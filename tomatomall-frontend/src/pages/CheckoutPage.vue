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

// 创建带token的实例 (与cartpage保持一致)
const authAxios = axios.create({
  headers: {
    'token': localStorage.getItem('token')  // 从localStorage获取token
  }
})

// 从路由参数获取购物车项
const cartItemIds = ref([])
onMounted(() => {
  const ids = router.currentRoute.value.query.cartItemIds
  if (ids) cartItemIds.value = JSON.parse(ids)
})

// 提交订单
const handleSubmit = async () => {
  try {
    const res = await authAxios.post('/api/cart/checkout', {
      cartItemIds: cartItemIds.value,
      shippingAddress: form.value.shippingAddress,
      paymentMethod: form.value.paymentMethod
    })

    router.push({
      path: `/payment/${res.data.data.orderId}`,
      query: { totalAmount: res.data.data.totalAmount }
    })
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || '创建订单失败')
  }
}
</script>

<template>
  <div class="checkout-page">
    <h1>订单结算</h1>
    <el-form :model="form" label-width="100px">
      <el-form-item label="收货地址" required>
        <el-input
            v-model="form.shippingAddress"
            type="textarea"
            placeholder="请输入详细收货地址"
        />
      </el-form-item>
      <el-form-item label="支付方式" required>
        <el-select v-model="form.paymentMethod">
          <el-option label="支付宝" value="ALIPAY" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">提交订单</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<style scoped>
.checkout-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}
</style>
