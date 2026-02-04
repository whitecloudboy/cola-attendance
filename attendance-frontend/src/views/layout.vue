<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">{{ t('app.shortTitle') }}</div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#1565c0"
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
          <el-menu-item index="/attendance/rule-dsl">{{ t('menu.attendance.ruleDsl') }}</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="title">{{ currentTitle }}</span>
          <div class="tabs-wrap">
            <div
              v-for="tab in tabs"
              :key="tab.path"
              class="tab-item"
              :class="{ active: route.path === tab.path }"
              @click="goTab(tab)"
            >
              <span class="tab-title">{{ tabTitle(tab) }}</span>
              <el-icon class="tab-close" @click.stop="closeTab(tab)"><Close /></el-icon>
            </div>
          </div>
        </div>
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
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, Calendar, Close, Setting, Stamp } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../store/user'
import { setLocale } from '../i18n'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const { t, locale } = useI18n()

const tabs = ref([])

watch(
  () => route.path,
  (path) => {
    if (path === '/login' || path === '/') return
    const exists = tabs.value.some((tab) => tab.path === path)
    if (!exists) {
      tabs.value.push({
        path: route.path,
        fullPath: route.fullPath,
        titleKey: route.meta?.titleKey,
        name: route.name
      })
    }
  },
  { immediate: true }
)

function tabTitle(tab) {
  return tab.titleKey ? t(tab.titleKey) : (tab.name || tab.path)
}

function goTab(tab) {
  if (route.path !== tab.path) router.push(tab.fullPath || tab.path)
}

function closeTab(tab) {
  const list = tabs.value.filter((t) => t.path !== tab.path)
  if (route.path === tab.path && list.length) {
    const next = list[list.length - 1]
    router.push(next.fullPath || next.path)
  }
  if (list.length === 0) {
    list.push({
      path: route.path,
      fullPath: route.fullPath,
      titleKey: route.meta?.titleKey,
      name: route.name
    })
  }
  tabs.value = list
}

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
  background-color: #1565c0;
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
  padding: 0 16px 0 20px;
  min-height: 56px;
}
.header-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}
.title {
  font-size: 16px;
  flex-shrink: 0;
  margin-right: 16px;
}
.tabs-wrap {
  display: flex;
  align-items: center;
  gap: 4px;
  overflow-x: auto;
  padding: 4px 0;
  flex: 1;
  min-width: 0;
}
.tabs-wrap::-webkit-scrollbar { height: 4px; }
.tab-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  font-size: 13px;
  color: #606266;
  background: #f0f2f5;
}
.tab-item:hover { background: #e4e7ed; }
.tab-item.active {
  background: #1565c0;
  color: #fff;
}
.tab-item .tab-close {
  font-size: 12px;
  opacity: 0.7;
}
.tab-item .tab-close:hover { opacity: 1; }
.tab-item.active .tab-close:hover { color: #fff; }
.user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
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
