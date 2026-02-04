import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'
import Layout from '../views/layout.vue'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/login.vue'), meta: { public: true } },
  {
    path: '/',
    component: Layout,
    redirect: '/system/dept',
    children: [
      { path: 'system/dept', name: 'Dept', component: () => import('../views/system/dept.vue'), meta: { titleKey: 'dept.title' } },
      { path: 'system/user', name: 'User', component: () => import('../views/system/user.vue'), meta: { titleKey: 'user.title' } },
      { path: 'system/role', name: 'Role', component: () => import('../views/system/role.vue'), meta: { titleKey: 'role.title' } },
      { path: 'system/post', name: 'Post', component: () => import('../views/system/post.vue'), meta: { titleKey: 'post.title' } },
      { path: 'system/menu', name: 'Menu', component: () => import('../views/system/menu.vue'), meta: { titleKey: 'menuMgmt.title' } },
      { path: 'schedule/shift', name: 'Shift', component: () => import('../views/schedule/shift.vue'), meta: { titleKey: 'shift.title' } },
      { path: 'schedule/schedule', name: 'Schedule', component: () => import('../views/schedule/schedule.vue'), meta: { titleKey: 'schedulePage.title' } },
      { path: 'attendance/device', name: 'Device', component: () => import('../views/attendance/device.vue'), meta: { titleKey: 'device.title' } },
      { path: 'attendance/record', name: 'Record', component: () => import('../views/attendance/record.vue'), meta: { titleKey: 'record.title' } },
      { path: 'attendance/result', name: 'Result', component: () => import('../views/attendance/result.vue'), meta: { titleKey: 'result.title' } },
      { path: 'attendance/rule-dsl', name: 'RuleDsl', component: () => import('../views/attendance/rule-dsl.vue'), meta: { titleKey: 'ruleDsl.title' } }
    ]
  }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    next()
    return
  }
  if (!userStore.token) {
    next('/login')
    return
  }
  next()
})

export default router
