import LoginView from '@/views/LoginView.vue'
import MenuView from '@/views/MenuView.vue'
import RegisterView from '@/views/RegisterView.vue'
import OrderView from '@/views/OrderView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MenuView
    },
    {
      path: '/login',
      component: LoginView
    },
    {
      path: '/register',
      component: RegisterView
    },
    {
      path: '/cart',
      component: OrderView
    }
  ],
})

export default router