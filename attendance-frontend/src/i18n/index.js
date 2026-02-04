import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

const LOCALE_KEY = 'attendance_locale'

function normalizeLocale(input) {
  const v = (input || '').toLowerCase()
  if (v.startsWith('zh')) return 'zh-CN'
  if (v.startsWith('en')) return 'en-US'
  return 'zh-CN'
}

export function getInitialLocale() {
  const saved = localStorage.getItem(LOCALE_KEY)
  if (saved) return normalizeLocale(saved)
  return normalizeLocale(navigator.language)
}

const i18n = createI18n({
  legacy: false,
  locale: getInitialLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

export function setLocale(locale) {
  const next = normalizeLocale(locale)
  i18n.global.locale.value = next
  localStorage.setItem(LOCALE_KEY, next)
}

export default i18n

