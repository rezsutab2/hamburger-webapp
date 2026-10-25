<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const orders = ref([])
const isLoading = ref(true)
const errorMessage = ref('')

const statusLabels = {
  PENDING: 'Feldolgozásra vár',
  CONFIRMED: 'Visszaigazolva',
  READY: 'Elkészült',
  COMPLETED: 'Átvéve',
  CANCELLED: 'Lemondva'
}

const statusClasses = {
  PENDING: 'bg-secondary',
  CONFIRMED: 'bg-primary',
  READY: 'bg-success',
  COMPLETED: 'bg-dark',
  CANCELLED: 'bg-danger'
}

function orderTotal(order) {
  return (order.items || []).reduce((sum, i) => sum + i.priceAtOrder * i.quantity, 0)
}

function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleString('hu-HU', { dateStyle: 'medium', timeStyle: 'short' })
}

onMounted(async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await axios.get('http://localhost:8080/api/orders', {
      headers: { Authorization: `Bearer ${token}` }
    })
    orders.value = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  } catch (err) {
    errorMessage.value = 'Hiba történt a rendelések betöltése közben.'
    console.error(err)
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="container mt-4">
    <h1>Rendeléseim</h1>

    <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>
    <div v-if="isLoading" class="text-muted">Betöltés...</div>
    <p v-else-if="orders.length === 0 && !errorMessage">Még nincs leadott rendelésed.</p>

    <div v-for="order in orders" :key="order.id" class="card mb-3">
      <div class="card-header d-flex justify-content-between align-items-center">
        <span>#{{ order.id }} · {{ formatDate(order.createdAt) }}</span>
        <span class="badge" :class="statusClasses[order.status]">
          {{ statusLabels[order.status] || order.status }}
        </span>
      </div>
      <div class="card-body">
        <table class="table table-sm mb-2">
          <tbody>
            <tr v-for="item in order.items" :key="item.id">
              <td>{{ item.menuItem?.name }}</td>
              <td class="text-end">{{ item.quantity }} db</td>
              <td class="text-end">{{ item.priceAtOrder * item.quantity }} Ft</td>
            </tr>
          </tbody>
        </table>
        <p v-if="order.note" class="text-muted mb-2">Megjegyzés: {{ order.note }}</p>
        <strong>Végösszeg: {{ orderTotal(order) }} Ft</strong>
      </div>
    </div>
  </div>
</template>