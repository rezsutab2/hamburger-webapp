<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { jwtDecode } from 'jwt-decode'
import axios from 'axios'

const router = useRouter()
const token = localStorage.getItem('token')
const isLoggedIn = ref(!!token)
const userName = ref('')

if (token) {
  axios.get('http://localhost:8080/api/user/me', {
    headers: { Authorization: `Bearer ${token}` }
  }).then(response => {
    userName.value = response.data.name
  })
}

const logout = () => {
  localStorage.removeItem('token')
  isLoggedIn.value = false
  window.location.href = '/'
}
</script>

<template>
  <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
      <a class="navbar-brand" href="/">Ne0n Burger</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav ms-auto">
          <li class="nav-item">
            <RouterLink class="nav-link" to="/">Étlap</RouterLink>
          </li>
        <template v-if="isLoggedIn">
            <li class="nav-item">
                <span class="nav-link text-light">{{ userName }}</span>
            </li>
            <li class="nav-item">
                <button class="btn btn-outline-light ms-2" @click="logout">Kijelentkezés</button>
            </li>
        </template>
          <template v-else>
            <li class="nav-item">
              <RouterLink class="nav-link" to="/login">Bejelentkezés</RouterLink>
            </li>
            <li class="nav-item">
              <RouterLink class="nav-link" to="/register">Regisztráció</RouterLink>
            </li>
          </template>
        </ul>
      </div>
    </div>
  </nav>
</template>

<style scoped></style>