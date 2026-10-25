<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

function authHeaders() {
  const token = localStorage.getItem('token')
  return { Authorization: `Bearer ${token}` }
}

function errorText(err, fallback) {
  const data = err.response?.data
  return (typeof data === 'string' ? data : data?.message) || fallback
}

const activeTab = ref('menu')

// Menü
const menuItems = ref([])
const ingredients = ref([])
const editingItem = ref(null)
const menuForm = ref({ name: '', description: '', price: 0, available: true, menuItemIngredient: [] })
const menuError = ref('')

async function loadMenu() {
  const response = await axios.get('http://localhost:8080/api/menu')
  menuItems.value = response.data
}

async function loadIngredients() {
  const response = await axios.get('http://localhost:8080/api/ingredients', { headers: authHeaders() })
  ingredients.value = response.data
}

function startEdit(item) {
  editingItem.value = item.id
  menuForm.value = {
    name: item.name,
    description: item.description,
    price: item.price,
    available: item.available,
    menuItemIngredient: (item.menuItemIngredient || []).map(mii => ({
      ingredientId: mii.ingredient.id,
      quantity: mii.quantity
    }))
  }
}

function startNew() {
  editingItem.value = 'new'
  menuForm.value = { name: '', description: '', price: 0, available: true, menuItemIngredient: [] }
}

function cancelEdit() {
  editingItem.value = null
  menuError.value = ''
}

function addIngredientRow() {
  menuForm.value.menuItemIngredient.push({ ingredientId: null, quantity: 0 })
}

function removeIngredientRow(index) {
  menuForm.value.menuItemIngredient.splice(index, 1)
}

function ingredientName(id) {
  const ing = ingredients.value.find(i => i.id === id)
  return ing ? `${ing.name} (${ing.unit})` : ''
}

function hasUnit(ingredientId) {
  const ing = ingredients.value.find(i => i.id === ingredientId)
  return !!ing?.unit
}

async function saveMenuItem() {
  menuError.value = ''

  const payload = {
    name: menuForm.value.name,
    description: menuForm.value.description,
    price: menuForm.value.price,
    available: menuForm.value.available,
    menuItemIngredient: menuForm.value.menuItemIngredient
      .filter(row => row.ingredientId)
      .map(row => ({
        ingredient: { id: row.ingredientId },
        quantity: row.quantity
      }))
  }

  try {
    if (editingItem.value === 'new') {
      await axios.post('http://localhost:8080/api/menu', payload, { headers: authHeaders() })
    } else {
      await axios.put(`http://localhost:8080/api/menu/${editingItem.value}`, payload, { headers: authHeaders() })
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

// Összetevők / allergének
const allergens = ref([])
const editingIngredient = ref(null)
const isNewIngredient = ref(false)
const ingredientForm = ref({ name: '', unit: '' })
const selectedAllergenIds = ref([])
const ingredientError = ref('')

async function loadAllergens() {
  const response = await axios.get('http://localhost:8080/api/allergens', { headers: authHeaders() })
  allergens.value = response.data
}

function startNewIngredient() {
  cancelEditIngredient()
  isNewIngredient.value = true
  ingredientForm.value = { name: '', unit: '' }
}

function startEditIngredient(ingredient) {
  isNewIngredient.value = false
  editingIngredient.value = ingredient.id
  ingredientForm.value = { name: ingredient.name, unit: ingredient.unit || '' }
  selectedAllergenIds.value = (ingredient.allergens || []).map(a => a.id)
}

function cancelEditIngredient() {
  editingIngredient.value = null
  isNewIngredient.value = false
  ingredientError.value = ''
}

async function saveNewIngredient() {
  ingredientError.value = ''
  try {
    await axios.post('http://localhost:8080/api/ingredients', ingredientForm.value, { headers: authHeaders() })
    isNewIngredient.value = false
    await loadIngredients()
  } catch (err) {
    ingredientError.value = errorText(err, 'Hiba történt a mentés közben.')
    console.error(err)
  }
}

async function saveIngredient(ingredientId) {
  ingredientError.value = ''
  try {
    await axios.put(`http://localhost:8080/api/ingredients/${ingredientId}`, ingredientForm.value, { headers: authHeaders() })
    await axios.put(
      `http://localhost:8080/api/ingredients/${ingredientId}/allergens`,
      selectedAllergenIds.value,
      { headers: authHeaders() }
    )
    editingIngredient.value = null
    await loadIngredients()
    await loadMenu()
  } catch (err) {
    ingredientError.value = errorText(err, 'Hiba történt a mentés közben.')
    console.error(err)
  }
}

async function deleteIngredient(ingredient) {
  if (!confirm(`Biztosan törlöd ezt az összetevőt: ${ingredient.name}?`)) return
  ingredientError.value = ''
  try {
    await axios.delete(`http://localhost:8080/api/ingredients/${ingredient.id}`, { headers: authHeaders() })
    await loadIngredients()
  } catch (err) {
    ingredientError.value = errorText(err, 'Hiba történt a törlés közben.')
    console.error(err)
  }
}

function allergenNames(ingredient) {
  if (!ingredient.allergens || ingredient.allergens.length === 0) return '—'
  return ingredient.allergens.map(a => a.name).join(', ')
}

// Rendelések
const orders = ref([])
const orderStatuses = ['PENDING', 'CONFIRMED', 'READY', 'COMPLETED', 'CANCELLED']
const orderError = ref('')

async function loadOrders() {
  const response = await axios.get('http://localhost:8080/api/orders/all', { headers: authHeaders() })
  orders.value = response.data
}

async function updateOrderStatus(order, newStatus, event) {
  orderError.value = ''
  const previous = order.status
  try {
    await axios.put(`http://localhost:8080/api/orders/${order.id}/status`, { status: newStatus }, { headers: authHeaders() })
    order.status = newStatus
  } catch (err) {
    event.target.value = previous
    orderError.value = errorText(err, 'Az állapot módosítása nem sikerült.')
    console.error(err)
  }
}

// Foglalások
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
  loadIngredients()
  loadAllergens()
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
      <li class="nav-item">
        <button class="nav-link" :class="{ active: activeTab === 'ingredients' }" @click="activeTab = 'ingredients'">Összetevők</button>
      </li>
    </ul>

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
        </div>

        <hr />
        <h6>Összetevők</h6>
        <div v-for="(row, index) in menuForm.menuItemIngredient" :key="index" class="row g-2 mb-2 align-items-center">
          <div class="col-md-5">
            <select v-model="row.ingredientId" class="form-select">
              <option :value="null" disabled>Válassz összetevőt</option>
              <option v-for="ing in ingredients" :key="ing.id" :value="ing.id">
                {{ ing.unit ? `${ing.name} (${ing.unit})` : ing.name }}
              </option>
            </select>
          </div>
          <div class="col-md-4">
            <input
              v-if="hasUnit(row.ingredientId)"
              v-model.number="row.quantity"
              type="number"
              step="0.01"
              class="form-control"
              placeholder="Mennyiség"
            />
          </div>
          <div class="col-md-3">
            <button class="btn btn-outline-danger btn-sm" @click="removeIngredientRow(index)">Eltávolítás</button>
          </div>
        </div>
        <button class="btn btn-outline-secondary btn-sm mb-3" @click="addIngredientRow">+ Összetevő hozzáadása</button>

        <div class="d-flex gap-2">
          <button class="btn btn-success btn-sm" @click="saveMenuItem">Mentés</button>
          <button class="btn btn-outline-secondary btn-sm" @click="cancelEdit">Mégse</button>
        </div>
      </div>

      <table class="table">
        <thead>
          <tr>
            <th>Név</th><th>Leírás</th><th>Ár</th><th>Elérhető</th><th>Összetevők</th><th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in menuItems" :key="item.id">
            <td>{{ item.name }}</td>
            <td>{{ item.description }}</td>
            <td>{{ item.price }} Ft</td>
            <td>{{ item.available ? 'Igen' : 'Nem' }}</td>
            <td>
              <span v-if="!item.menuItemIngredient || item.menuItemIngredient.length === 0" class="text-muted">—</span>
              <span v-else>
                {{ item.menuItemIngredient.map(mii =>
                mii.ingredient.unit
                ? `${mii.ingredient.name} (${mii.quantity} ${mii.ingredient.unit})`
                : mii.ingredient.name).join(', ') }}
              </span>
            </td>
            <td class="d-flex gap-2">
              <button class="btn btn-outline-primary btn-sm" @click="startEdit(item)">Szerkesztés</button>
              <button class="btn btn-outline-danger btn-sm" @click="deleteMenuItem(item.id)">Törlés</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="activeTab === 'ingredients'">
      <button class="btn btn-primary mb-3" @click="startNewIngredient">Új összetevő</button>
      <div v-if="ingredientError" class="alert alert-danger">{{ ingredientError }}</div>

      <div v-if="isNewIngredient" class="card mb-3 p-3">
        <div class="row g-2">
          <div class="col-md-5">
            <input v-model="ingredientForm.name" class="form-control" placeholder="Név" />
          </div>
          <div class="col-md-3">
            <input v-model="ingredientForm.unit" class="form-control" placeholder="Mértékegység (üres = nincs)" list="unit-options" />
          </div>
          <div class="col-md-4 d-flex gap-2">
            <button class="btn btn-success btn-sm" @click="saveNewIngredient">Mentés</button>
            <button class="btn btn-outline-secondary btn-sm" @click="cancelEditIngredient">Mégse</button>
          </div>
        </div>
        <div class="form-text mt-2">Az allergéneket a mentés után, a "Szerkesztés" gombbal tudod hozzárendelni.</div>
      </div>

      <datalist id="unit-options">
        <option value="g" />
        <option value="db" />
        <option value="ml" />
      </datalist>

      <table class="table">
        <thead>
          <tr>
            <th>Név</th><th>Mértékegység</th><th>Allergének</th><th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ing in ingredients" :key="ing.id">
            <template v-if="editingIngredient === ing.id">
              <td>
                <input v-model="ingredientForm.name" class="form-control form-control-sm" placeholder="Név" />
              </td>
              <td>
                <input v-model="ingredientForm.unit" class="form-control form-control-sm" placeholder="üres = nincs" list="unit-options" />
              </td>
              <td>
                <div class="d-flex flex-wrap gap-2">
                  <div v-for="allergen in allergens" :key="allergen.id" class="form-check">
                    <input
                      class="form-check-input"
                      type="checkbox"
                      :id="`allergen-${ing.id}-${allergen.id}`"
                      :value="allergen.id"
                      v-model="selectedAllergenIds"
                    />
                    <label class="form-check-label" :for="`allergen-${ing.id}-${allergen.id}`">
                      {{ allergen.name }}
                    </label>
                  </div>
                </div>
              </td>
              <td class="text-nowrap">
                <button class="btn btn-success btn-sm me-2" @click="saveIngredient(ing.id)">Mentés</button>
                <button class="btn btn-outline-secondary btn-sm" @click="cancelEditIngredient">Mégse</button>
              </td>
            </template>
            <template v-else>
              <td>{{ ing.name }}</td>
              <td>{{ ing.unit || '—' }}</td>
              <td>{{ allergenNames(ing) }}</td>
              <td class="text-nowrap">
                <button class="btn btn-outline-primary btn-sm me-2" @click="startEditIngredient(ing)">Szerkesztés</button>
                <button class="btn btn-outline-danger btn-sm" @click="deleteIngredient(ing)">Törlés</button>
              </td>
            </template>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="activeTab === 'orders'">
      <div v-if="orderError" class="alert alert-danger">{{ orderError }}</div>
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
              <select class="form-select form-select-sm" :value="order.status" @change="updateOrderStatus(order, $event.target.value, $event)">
                <option v-for="status in orderStatuses" :key="status" :value="status">{{ status }}</option>
              </select>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

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