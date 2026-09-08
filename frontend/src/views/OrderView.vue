<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { cart, removeFromCart, clearCart, cartTotal } from '@/store/cart'

const router = useRouter()
const note = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')

async function submitOrder() {
  if (cart.items.length === 0) {
    errorMessage.value = 'A kosár üres.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  const orderPayload = {
    note: note.value,
    items: cart.items.map(i => ({
      menuItem: { id: i.menuItemId },
      quantity: i.quantity,
      priceAtOrder: i.price
    }))
  }

  try {
    const token = localStorage.getItem('token')
    await axios.post('http://localhost:8080/api/orders', orderPayload, {
      headers: { Authorization: `Bearer ${token}` }
    })

    clearCart()
    router.push('/')
  } catch (err) {
    errorMessage.value = 'Hiba történt a rendelés leadása közben.'
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="container mt-4">
    <h1>Kosár</h1>

    <div v-if="cart.items.length === 0">
      <p>A kosár üres.</p>
    </div>

    <div v-else>
      <table class="table">
        <thead>
          <tr>
            <th>Termék</th>
            <th>Mennyiség</th>
            <th>Egységár</th>
            <th>Összesen</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in cart.items" :key="item.menuItemId">
            <td>{{ item.name }}</td>
            <td>{{ item.quantity }}</td>
            <td>{{ item.price }} Ft</td>
            <td>{{ item.price * item.quantity }} Ft</td>
            <td>
              <button class="btn btn-sm btn-outline-danger" @click="removeFromCart(item.menuItemId)">
                Törlés
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <h4>Végösszeg: {{ cartTotal() }} Ft</h4>

      <div class="mb-3">
        <label class="form-label">Megjegyzés a rendeléshez</label>
        <textarea class="form-control" v-model="note"></textarea>
      </div>

      <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <button class="btn btn-success" :disabled="isSubmitting" @click="submitOrder">
        {{ isSubmitting ? 'Küldés...' : 'Rendelés leadása' }}
      </button>
    </div>
  </div>
</template>

<style scoped></style>