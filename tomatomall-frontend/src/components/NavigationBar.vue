<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}
</script>

<template>
  <div class="nav-bar-container">
    <el-menu
        class="nav-bar"
        mode="horizontal"
        :router="true"
    >
      <el-menu-item index="/">番茄书城</el-menu-item>
      <el-menu-item index="/cart">购物车</el-menu-item>
      <el-menu-item index="/warehouse">库存管理</el-menu-item>

      <div class="auth-buttons">
        <el-menu-item v-if="!isLoggedIn" index="/login">登录</el-menu-item>
        <el-menu-item v-if="!isLoggedIn" index="/register">注册</el-menu-item>
        <el-submenu v-if="isLoggedIn" index="profile">
          <template #title>个人中心</template>
          <el-menu-item index="/profile">个人信息</el-menu-item>
          <el-menu-item @click="handleLogout">退出登录</el-menu-item>
        </el-submenu>
      </div>
    </el-menu>
  </div>
</template>

<style scoped>
.nav-bar-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  height: 60px;
}

.nav-bar {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin: 0 auto;
  height: 100%;
}

.auth-buttons {
  display: flex;
  margin-right: 20px;
}
</style>