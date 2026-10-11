import LoginView from '@/views/LoginView.vue'
import MenuView from '@/views/MenuView.vue'
import RegisterView from '@/views/RegisterView.vue'
import OrderView from '@/views/OrderView.vue'
import ReservationView from '@/views/ReservationView.vue'
import AdminView from '@/views/AdminView.vue'
import { createRouter, createWebHistory } from 'vue-router'
import { jwtDecode } from 'jwt-decode'

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
    ,
    { 
      path: '/reservations',
      name: 'reservations',
      component: ReservationView
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView,
      meta: { requiresAdmin: true }
    }
  ],
})

function isAdminUser() {
  const token = localStorage.getItem('token')
  if (!token) return false
  try {
    return jwtDecode(token).role === 'ADMIN'
  } catch {
    return false
  }
}

router.beforeEach((to, from, next) => {
  if (to.meta.requiresAdmin && !isAdminUser()) {
    next('/')
  } else {
    next()
  }
})

export default router