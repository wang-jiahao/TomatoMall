<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const role = computed(() => localStorage.getItem('role'))
const activePath = computed(() => route.path)

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/login')
}

const handleAuthCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    handleLogout()
  }
}
</script>

<template>
  <div class="nav-bar-container">
    <div class="nav-bar-inner">
      <el-menu
        class="nav-bar"
        mode="horizontal"
        :router="true"
        :default-active="activePath"
        :ellipsis="false"
      >
        <el-menu-item index="/" class="brand-item">
          <span class="brand-title">BlancBooks</span>
          <span class="brand-tag">新书热读</span>
        </el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
        <el-menu-item index="/advertisements">今日推荐</el-menu-item>
      </el-menu>

      <div class="auth-buttons">
        <template v-if="!isLoggedIn">
          <el-button class="ghost-button" @click="router.push('/login')">登录</el-button>
          <el-button type="primary" @click="router.push('/register')">注册</el-button>
        </template>
        <el-dropdown v-else @command="handleAuthCommand">
          <el-button class="ghost-button">
            个人中心
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nav-bar-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  height: var(--nav-height);
  background: linear-gradient(120deg, rgba(255, 250, 246, 0.96), rgba(255, 236, 222, 0.92));
  box-shadow: 0 12px 28px rgba(31, 26, 22, 0.12);
  backdrop-filter: blur(12px);
}

.nav-bar-inner {
  display: flex;
  align-items: center;
  height: 100%;
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: 0 var(--page-padding);
  gap: 12px;
}

.nav-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  height: 100%;
  border-bottom: none;
  background: transparent;
  flex: 1;
  min-width: 0;
}

.brand-item {
  gap: 10px;
}

.brand-title {
  font-family: var(--font-serif);
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.brand-tag {
  font-size: 12px;
  color: var(--color-accent-strong);
  background: rgba(228, 72, 58, 0.12);
  padding: 3px 8px;
  border-radius: 999px;
}

.auth-buttons {
  display: flex;
  margin-left: auto;
  gap: 10px;
  align-items: center;
}

:deep(.el-menu-item) {
  border-radius: 999px;
  margin: 0 4px;
}

:deep(.el-menu-item.is-active) {
  color: var(--color-accent-strong);
  background: rgba(228, 72, 58, 0.12);
}

:deep(.el-submenu__title) {
  border-radius: 999px;
}

.ghost-button {
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--color-border);
  color: var(--color-ink);
}

.ghost-button:hover {
  background: rgba(255, 255, 255, 0.85);
}
</style>
