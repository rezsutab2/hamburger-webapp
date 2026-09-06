<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const email = ref('')
const password = ref('')
const router = useRouter()

const login = async () => {
  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      email: email.value,
      password: password.value
    })
    localStorage.setItem('token', response.data)
    window.location.href = '/'
  } catch (error) {
    alert('Hibás email vagy jelszó!')
  }
}
</script>

<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h4 class="card-title mb-4">Bejelentkezés</h4>
            <div class="mb-3">
              <label class="form-label">Email</label>
              <input type="email" class="form-control" v-model="email" />
            </div>
            <div class="mb-3">
              <label class="form-label">Jelszó</label>
              <input type="password" class="form-control" v-model="password" />
            </div>
            <button class="btn btn-primary w-100" @click="login">Bejelentkezés</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>