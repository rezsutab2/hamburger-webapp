<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const menuItems = ref([])

onMounted(async () => {
  const response = await axios.get('http://localhost:8080/api/menu')
  menuItems.value = response.data
})
</script>

<template>
  <div class="container mt-4">
    <h1>Étlapunk</h1>
    <div class="row">
      <div class="col-md-4" v-for="item in menuItems" :key="item.id">
        <div class="card mb-4">
          <div class="card-body">
            <h5 class="card-title">{{ item.name }}</h5>
            <p class="card-text">{{ item.description }}</p>
            <p class="card-text"><strong>{{ item.price }} Ft</strong></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped></style>