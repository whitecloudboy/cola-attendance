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
      { path: 'system/dept', name: 'Dept', component: () => import('../views/system/dept.vue'), meta: { title: '部门管理' } },
      { path: 'system/user', name: 'User', component: () => import('../views/system/user.vue'), meta: { title: '用户管理' } },
      { path: 'system/role', name: 'Role', component: () => import('../views/system/role.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', name: 'Menu', component: () => import('../views/system/menu.vue'), meta: { title: '菜单管理' } }
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
