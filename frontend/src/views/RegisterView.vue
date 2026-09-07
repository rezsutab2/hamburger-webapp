<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const name = ref('')
const email = ref('')
const password = ref('')
const router = useRouter()

const register = async () => {
  try {
    await axios.post('http://localhost:8080/api/auth/register', {
      name: name.value,
      email: email.value,
      password: password.value
    })
    alert('Sikeres regisztráció!')
    router.push('/login')
  } catch (error) {
    alert('Hiba történt a regisztráció során!')
  }
}
</script>


<template>
  <div class="container mt-5">
    <div class="row justify-content-center">
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h4 class="card-title mb-4">Regisztráció</h4>
            <div class="mb-3">
              <label class="form-label">Név</label>
              <input type="text" class="form-control" v-model="name" />
            </div>
            <div class="mb-3">
              <label class="form-label">Email</label>
              <input type="email" class="form-control" v-model="email" />
            </div>
            <div class="mb-3">
              <label class="form-label">Jelszó</label>
              <input type="password" class="form-control" v-model="password" />
            </div>
            <button class="btn btn-success w-100" @click="register">Regisztráció</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>