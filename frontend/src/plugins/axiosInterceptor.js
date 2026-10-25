import axios from 'axios'
import { jwtDecode } from 'jwt-decode'

function isTokenExpired(token) {
  try {
    const { exp } = jwtDecode(token)
    return !exp || exp * 1000 < Date.now()
  } catch {
    return true
  }
}

function forceLogout() {
  localStorage.removeItem('token')
  window.location.href = '/login'
}

axios.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status
    const url = error.config?.url || ''
    const token = localStorage.getItem('token')

    const isAuthCall = url.includes('/api/auth/')
    const authFailed = status === 401 || status === 403

    if (token && !isAuthCall && authFailed && isTokenExpired(token)) {
      forceLogout()
    }
    return Promise.reject(error)
  }
)