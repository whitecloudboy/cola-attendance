<template>
  <div class="login-wrap">
    <div class="login-left">
      <div class="login-left-overlay" />
      <div class="login-left-bg">
        <img
          src="https://images.unsplash.com/photo-1677442136019-21780ecad995?w=1200&q=85"
          alt="AI"
          class="bg-img main"
        />
        <img
          src="https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=1200&q=85"
          alt="Tech"
          class="bg-img second"
        />
      </div>
      <div class="login-left-content">
        <div class="brand-block">
          <h1 class="brand">{{ t('app.shortTitle') }}</h1>
          <p class="slogan">{{ t('login.slogan') }}</p>
          <p class="sub">{{ t('login.sub') }}</p>
        </div>
      </div>
    </div>
    <div class="login-right">
      <div class="login-form-wrap">
        <div class="form-header">
          <h2>{{ t('login.title') }}</h2>
          <p class="form-desc">{{ t('login.formDesc') }}</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" class="login-form" @submit.prevent="onSubmit">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              :placeholder="t('login.usernamePlaceholder')"
              size="large"
              clearable
              class="input-lg"
            >
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              :placeholder="t('login.passwordPlaceholder')"
              size="large"
              show-password
              clearable
              class="input-lg"
              @keyup.enter="onSubmit"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="onSubmit">
              {{ t('login.submit') }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <div class="login-right-grid" aria-hidden="true" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '../store/user'
import { login } from '../api/auth'

const router = useRouter()
const userStore = useUserStore()
const { t } = useI18n()

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: () => t('login.usernameRequired'), trigger: 'blur' }],
  password: [{ required: true, message: () => t('login.passwordRequired'), trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value?.validate().catch(() => {})
  loading.value = true
  try {
    const res = await login(form)
    userStore.setLogin(res)
    ElMessage.success(t('login.success'))
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: flex;
  background: #0a0e1a;
}

/* Left: AI imagery */
.login-left {
  flex: 1;
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.login-left-bg {
  position: absolute;
  inset: 0;
}

.login-left-bg .bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.9;
  transition: opacity 0.8s ease;
}

.login-left-bg .bg-img.second {
  opacity: 0;
}

.login-left:hover .bg-img.main {
  opacity: 0.5;
}

.login-left:hover .bg-img.second {
  opacity: 0.7;
}

.login-left-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(21, 101, 192, 0.75) 0%, rgba(15, 23, 42, 0.85) 50%, rgba(0, 0, 0, 0.7) 100%);
  z-index: 1;
}

.login-left-content {
  position: relative;
  z-index: 2;
  height: 100%;
  padding: 48px 56px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-block {
  position: relative;
  max-width: 380px;
  padding: 32px 0 32px 28px;
  border-left: 3px solid rgba(21, 101, 192, 0.9);
  background: linear-gradient(90deg, rgba(0, 0, 0, 0.25) 0%, transparent 100%);
  border-radius: 0 12px 12px 0;
}

.brand {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.04em;
  margin: 0 0 14px 0;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
  line-height: 1.2;
}

.slogan {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.92);
  margin: 0 0 10px 0;
  font-weight: 500;
  letter-spacing: 0.02em;
  line-height: 1.5;
}

.sub {
  font-size: 0.9rem;
  color: rgba(255, 255, 255, 0.65);
  margin: 0;
  line-height: 1.5;
}

/* Right: form */
.login-right {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  position: relative;
  background: linear-gradient(180deg, #0f172a 0%, #0a0e1a 100%);
}

.login-right-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(21, 101, 192, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(21, 101, 192, 0.03) 1px, transparent 1px);
  background-size: 24px 24px;
  pointer-events: none;
}

.login-form-wrap {
  width: 100%;
  max-width: 360px;
  position: relative;
  z-index: 1;
  padding: 40px 36px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(21, 101, 192, 0.25);
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.05);
}

.form-header {
  margin-bottom: 32px;
  text-align: center;
}

.form-header h2 {
  font-size: 1.5rem;
  font-weight: 600;
  color: #f1f5f9;
  margin: 0 0 8px 0;
}

.form-desc {
  font-size: 0.875rem;
  color: #94a3b8;
  margin: 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.login-form :deep(.input-lg .el-input__wrapper) {
  background: rgba(30, 41, 59, 0.8);
  border: 1px solid rgba(71, 85, 105, 0.5);
  border-radius: 10px;
  box-shadow: none;
  padding: 4px 16px 4px 40px;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.login-form :deep(.input-lg .el-input__wrapper:hover),
.login-form :deep(.input-lg .el-input__wrapper.is-focus) {
  border-color: rgba(21, 101, 192, 0.6);
  box-shadow: 0 0 0 2px rgba(21, 101, 192, 0.15);
}

.login-form :deep(.input-icon) {
  color: #64748b;
  font-size: 18px;
}

.login-form :deep(.el-input__inner) {
  color: #f1f5f9;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #64748b;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 1rem;
  font-weight: 500;
  border-radius: 10px;
  background: linear-gradient(135deg, #1565c0 0%, #0d47a1 100%);
  border: none;
}

.submit-btn:hover {
  background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
  opacity: 0.95;
}

@media (max-width: 900px) {
  .login-wrap {
    flex-direction: column;
  }

  .login-left {
    min-height: 280px;
  }

  .login-left-content {
    padding: 24px;
  }

  .brand-block { padding: 24px 0 24px 20px; max-width: 100%; }
  .brand { font-size: 1.75rem; }
  .slogan { font-size: 1rem; }
  .sub { display: none; }

  .login-right {
    width: 100%;
    flex: 1;
  }
}
</style>
