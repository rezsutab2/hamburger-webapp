<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

function authHeaders() {
  const token = localStorage.getItem('token')
  return { Authorization: `Bearer ${token}` }
}

const activeTab = ref('menu')

const menuItems = ref([])
const editingItem = ref(null)
const menuForm = ref({ name: '', description: '', price: 0, available: true })
const menuError = ref('')

async function loadMenu() {
  const response = await axios.get('http://localhost:8080/api/menu')
  menuItems.value = response.data
}

function startEdit(item) {
  editingItem.value = item.id
  menuForm.value = { name: item.name, description: item.description, price: item.price, available: item.available }
}

function startNew() {
  editingItem.value = 'new'
  menuForm.value = { name: '', description: '', price: 0, available: true }
}

function cancelEdit() {
  editingItem.value = null
  menuError.value = ''
}

async function saveMenuItem() {
  menuError.value = ''
  try {
    if (editingItem.value === 'new') {
      await axios.post('http://localhost:8080/api/menu', menuForm.value, { headers: authHeaders() })
    } else {
      await axios.put(`http://localhost:8080/api/menu/${editingItem.value}`, menuForm.value, { headers: authHeaders() })
    }
    editingItem.value = null
    await loadMenu()
  } catch (err) {
    menuError.value = 'Hiba történt a mentés közben.'
    console.error(err)
  }
}

async function deleteMenuItem(id) {
  if (!confirm('Biztosan törlöd ezt a tételt?')) return
  try {
    await axios.delete(`http://localhost:8080/api/menu/${id}`, { headers: authHeaders() })
    await loadMenu()
  } catch (err) {
    menuError.value = 'Hiba történt a törlés közben.'
    console.error(err)
  }
}

const orders = ref([])
const orderStatuses = ['PENDING', 'CONFIRMED', 'READY', 'COMPLETED', 'CANCELLED']

async function loadOrders() {
  const response = await axios.get('http://localhost:8080/api/orders/all', { headers: authHeaders() })
  orders.value = response.data
}

async function updateOrderStatus(order, newStatus) {
  try {
    await axios.put(`http://localhost:8080/api/orders/${order.id}/status`, { status: newStatus }, { headers: authHeaders() })
    order.status = newStatus
  } catch (err) {
    console.error(err)
  }
}

const reservations = ref([])
const reservationStatuses = ['PENDING', 'CONFIRMED', 'CANCELLED']

async function loadReservations() {
  const response = await axios.get('http://localhost:8080/api/reservations/all', { headers: authHeaders() })
  reservations.value = response.data
}

async function updateReservationStatus(reservation, newStatus) {
  try {
    await axios.put(`http://localhost:8080/api/reservations/${reservation.id}/status`, { status: newStatus }, { headers: authHeaders() })
    reservation.status = newStatus
  } catch (err) {
    console.error(err)
  }
}

onMounted(() => {
  loadMenu()
  loadOrders()
  loadReservations()
})
</script>

<template>
  <div class="container mt-4">
    <h2 class="mb-4">Admin felület</h2>

    <ul class="nav nav-tabs mb-4">
      <li class="nav-item">
        <button class="nav-link" :class="{ active: activeTab === 'menu' }" @click="activeTab = 'menu'">Menü</button>
      </li>
      <li class="nav-item">
        <button class="nav-link" :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'">Rendelések</button>
      </li>
      <li class="nav-item">
        <button class="nav-link" :class="{ active: activeTab === 'reservations' }" @click="activeTab = 'reservations'">Foglalások</button>
      </li>
    </ul>

    <!-- MENÜ -->
    <div v-if="activeTab === 'menu'">
      <button class="btn btn-primary mb-3" @click="startNew">Új tétel</button>
      <div v-if="menuError" class="alert alert-danger">{{ menuError }}</div>

      <div v-if="editingItem" class="card mb-3 p-3">
        <div class="row g-2">
          <div class="col-md-3">
            <input v-model="menuForm.name" class="form-control" placeholder="Név" />
          </div>
          <div class="col-md-4">
            <input v-model="menuForm.description" class="form-control" placeholder="Leírás" />
          </div>
          <div class="col-md-2">
            <input v-model.number="menuForm.price" type="number" class="form-control" placeholder="Ár" />
          </div>
          <div class="col-md-2 d-flex align-items-center">
            <div class="form-check">
              <input v-model="menuForm.available" class="form-check-input" type="checkbox" id="availableCheck" />
              <label class="form-check-label" for="availableCheck">Elérhető</label>
            </div>
          </div>
          <div class="col-md-1 d-flex gap-1">
            <button class="btn btn-success btn-sm" @click="saveMenuItem">Mentés</button>
          </div>
        </div>
        <button class="btn btn-outline-secondary btn-sm mt-2" style="width: fit-content" @click="cancelEdit">Mégse</button>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>Név</th><th>Leírás</th><th>Ár</th><th>Elérhető</th><th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in menuItems" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.description }}</td>
            <td>{{ item.price }} Ft</td>
            <td>{{ item.available ? 'Igen' : 'Nem' }}</td>
            <td class="d-flex gap-2">
              <button class="btn btn-outline-primary btn-sm" @click="startEdit(item)">Szerkesztés</button>
              <button class="btn btn-outline-danger btn-sm" @click="deleteMenuItem(item.id)">Törlés</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- RENDELÉSEK -->
    <div v-if="activeTab === 'orders'">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>Felhasználó</th><th>Megjegyzés</th><th>Állapot</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id">
            <td>{{ order.id }}</td>
            <td>{{ order.user?.name }}</td>
            <td>{{ order.note }}</td>
            <td>
              <select class="form-select form-select-sm" :value="order.status" @change="updateOrderStatus(order, $event.target.value)">
                <option v-for="status in orderStatuses" :key="status" :value="status">{{ status }}</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- FOGLALÁSOK -->
    <div v-if="activeTab === 'reservations'">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th><th>Felhasználó</th><th>Dátum</th><th>Időpont</th><th>Asztal</th><th>Létszám</th><th>Állapot</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="reservation in reservations" :key="reservation.id">
            <td>{{ reservation.id }}</td>
            <td>{{ reservation.user?.name }}</td>
            <td>{{ reservation.date }}</td>
            <td>{{ reservation.startTime?.slice(0, 5) }}</td>
            <td>{{ reservation.table?.tableNumber }}</td>
            <td>{{ reservation.guestCount }}</td>
            <td>
              <select class="form-select form-select-sm" :value="reservation.status" @change="updateReservationStatus(reservation, $event.target.value)">
                <option v-for="status in reservationStatuses" :key="status" :value="status">{{ status }}</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>