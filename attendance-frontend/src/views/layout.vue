<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">考勤排班</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1e3a5f"
        text-color="#b0c4de"
        active-text-color="#fff"
      >
        <el-sub-menu index="system" popper-class="sidebar-pop">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/dept">部门管理</el-menu-item>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ currentTitle }}</span>
        <div class="user">
          <span>{{ userStore.userInfo?.displayName || userStore.userInfo?.username }}</span>
          <el-button type="danger" link @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Setting } from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '考勤与排班')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside {
  background-color: #1e3a5f;
  overflow-x: hidden;
}
.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-weight: bold;
  font-size: 16px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
  padding: 0 20px;
}
.title { font-size: 16px; }
.user {
  display: flex;
  align-items: center;
  gap: 12px;
}
.main {
  background: #f0f2f5;
  padding: 16px;
  overflow: auto;
}
</style>
