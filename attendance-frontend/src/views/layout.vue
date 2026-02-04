<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">{{ t('app.shortTitle') }}</div>
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
            <span>{{ t('menu.system.title') }}</span>
          </template>
          <el-menu-item index="/system/dept">{{ t('menu.system.dept') }}</el-menu-item>
          <el-menu-item index="/system/user">{{ t('menu.system.user') }}</el-menu-item>
          <el-menu-item index="/system/role">{{ t('menu.system.role') }}</el-menu-item>
          <el-menu-item index="/system/post">{{ t('menu.system.post') }}</el-menu-item>
          <el-menu-item index="/system/menu">{{ t('menu.system.menu') }}</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="schedule" popper-class="sidebar-pop">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>{{ t('menu.schedule.title') }}</span>
          </template>
          <el-menu-item index="/schedule/shift">{{ t('menu.schedule.shift') }}</el-menu-item>
          <el-menu-item index="/schedule/schedule">{{ t('menu.schedule.schedule') }}</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="attendance" popper-class="sidebar-pop">
          <template #title>
            <el-icon><Stamp /></el-icon>
            <span>{{ t('menu.attendance.title') }}</span>
          </template>
          <el-menu-item index="/attendance/device">{{ t('menu.attendance.device') }}</el-menu-item>
          <el-menu-item index="/attendance/record">{{ t('menu.attendance.record') }}</el-menu-item>
          <el-menu-item index="/attendance/result">{{ t('menu.attendance.result') }}</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ currentTitle }}</span>
        <div class="user">
          <el-dropdown trigger="click" @command="handleLang">
            <span class="lang">
              {{ locale?.startsWith('zh') ? t('lang.zh') : t('lang.en') }}
              <el-icon style="margin-left:6px"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="zh-CN">{{ t('lang.zh') }}</el-dropdown-item>
                <el-dropdown-item command="en-US">{{ t('lang.en') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <span>{{ userStore.userInfo?.displayName || userStore.userInfo?.username }}</span>
          <el-button type="danger" link @click="handleLogout">{{ t('action.logout') }}</el-button>
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
import { ArrowDown, Calendar, Setting, Stamp } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../store/user'
import { setLocale } from '../i18n'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t, locale } = useI18n()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => {
  if (route.meta?.titleKey) return t(route.meta.titleKey)
  return route.meta?.title || t('app.title')
})

function handleLang(command) {
  setLocale(command)
}

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
.lang {
  cursor: pointer;
  color: #303133;
  display: inline-flex;
  align-items: center;
}
.main {
  background: #f0f2f5;
  padding: 16px;
  overflow: auto;
}
</style>
