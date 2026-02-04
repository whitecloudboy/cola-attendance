<template>
  <el-config-provider :locale="epLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

const { locale } = useI18n()
const epLocale = computed(() => (locale.value?.startsWith('zh') ? zhCn : en))
</script>

<style>
#app { height: 100vh; }
html, body { margin: 0; padding: 0; }
</style>
