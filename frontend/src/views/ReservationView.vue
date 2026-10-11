<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const date = ref('')
const startTime = ref('')
const guestCount = ref(2)

const tables = ref([])
const selectedTableId = ref(null)
const isLoadingTables = ref(false)
const isSubmitting = ref(false)
const isCancelling = ref(false)
const isLoadingReservation = ref(true)
const errorMessage = ref('')
const successMessage = ref('')

const existingReservation = ref(null)

function authHeaders() {
  const token = localStorage.getItem('token')
  return { Authorization: `Bearer ${token}` }
}

async function loadExistingReservation() {
  isLoadingReservation.value = true
  try {
    const response = await axios.get('http://localhost:8080/api/reservations', {
      headers: authHeaders()
    })
    existingReservation.value = response.data.find(r => r.status !== 'CANCELLED') || null
  } catch (err) {
    errorMessage.value = 'Hiba történt a foglalások betöltése közben.'
    console.error(err)
  } finally {
    isLoadingReservation.value = false
  }
}

onMounted(loadExistingReservation)

async function fetchAvailability() {
  if (!date.value || !startTime.value) return

  isLoadingTables.value = true
  errorMessage.value = ''
  selectedTableId.value = null
  tables.value = []

  try {
    const response = await axios.get('http://localhost:8080/api/tables/availability', {
      params: { date: date.value, startTime: startTime.value },
      headers: authHeaders()
    })
    tables.value = response.data
  } catch (err) {
    errorMessage.value = 'Hiba történt az asztalok betöltése közben.'
    console.error(err)
  } finally {
    isLoadingTables.value = false
  }
}

function selectTable(table) {
  if (!table.available) return
  selectedTableId.value = table.id
}

async function submitReservation() {
  if (!date.value || !startTime.value) {
    errorMessage.value = 'Add meg a dátumot és az időpontot.'
    return
  }
  if (!selectedTableId.value) {
    errorMessage.value = 'Válassz egy szabad asztalt.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''

  const payload = {
    table: { id: selectedTableId.value },
    date: date.value,
    startTime: startTime.value,
    guestCount: guestCount.value
  }

  try {
    const response = await axios.post('http://localhost:8080/api/reservations', payload, {
      headers: authHeaders()
    })
    existingReservation.value = response.data
    successMessage.value = 'Sikeres foglalás!'
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Hiba történt a foglalás során.'
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
}

async function cancelExisting() {
  if (!existingReservation.value) return
  isCancelling.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    await axios.delete(`http://localhost:8080/api/reservations/${existingReservation.value.id}`, {
      headers: authHeaders()
    })
    existingReservation.value = null
    date.value = ''
    startTime.value = ''
    tables.value = []
  } catch (err) {
    errorMessage.value = 'Hiba történt a lemondás közben.'
    console.error(err)
  } finally {
    isCancelling.value = false
  }
}
</script>

<template>
  <div class="container mt-4">
    <h2 class="mb-4">Asztalfoglalás</h2>

    <div v-if="isLoadingReservation" class="text-muted">Betöltés...</div>

    <template v-else-if="existingReservation">
      <div v-if="successMessage" class="alert alert-success">{{ successMessage }}</div>
      <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <div class="alert alert-info d-flex justify-content-between align-items-center">
        <div>
          Foglalásod: <strong>{{ existingReservation.date }} {{ existingReservation.startTime?.slice(0, 5) }}</strong>,
          Asztal {{ existingReservation.table.tableNumber }}, {{ existingReservation.guestCount }} fő.
        </div>
        <button class="btn btn-outline-danger btn-sm" :disabled="isCancelling" @click="cancelExisting">
          Lemondás
        </button>
      </div>
    </template>

    <template v-else>
      <div v-if="errorMessage" class="alert alert-danger">{{ errorMessage }}</div>

      <div class="row g-3 mb-4">
        <div class="col-md-4">
          <label class="form-label">Dátum</label>
          <input v-model="date" type="date" class="form-control" @change="fetchAvailability" />
        </div>
        <div class="col-md-4">
          <label class="form-label">Időpont</label>
          <input v-model="startTime" type="time" class="form-control" @change="fetchAvailability" />
        </div>
        <div class="col-md-4">
          <label class="form-label">Létszám</label>
          <input v-model.number="guestCount" type="number" min="1" class="form-control" />
        </div>
      </div>

      <div v-if="isLoadingTables" class="text-muted">Asztalok betöltése...</div>

      <div v-else-if="tables.length > 0" class="row row-cols-2 row-cols-md-4 g-3 mb-4">
        <div v-for="table in tables" :key="table.id" class="col">
          <div
            class="table-tile"
            :class="{
              available: table.available,
              occupied: !table.available,
              selected: selectedTableId === table.id
            }"
            @click="selectTable(table)"
          >
            <div class="fw-bold">Asztal {{ table.tableNumber }}</div>
            <div class="small">{{ table.capacity }} fő</div>
            <div class="small">{{ table.available ? 'Szabad' : 'Foglalt' }}</div>
          </div>
        </div>
      </div>

      <div v-else-if="date && startTime" class="text-muted mb-4">
        Nincs megjeleníthető asztal.
      </div>

      <button
        class="btn btn-primary"
        :disabled="isSubmitting || !selectedTableId"
        @click="submitReservation"
      >
        Foglalás leadása
      </button>
    </template>
  </div>
</template>

<style scoped>
.table-tile {
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 1rem;
  text-align: center;
  cursor: pointer;
  user-select: none;
}
.table-tile.available {
  background-color: #d4edda;
}
.table-tile.occupied {
  background-color: #f8d7da;
  cursor: not-allowed;
  opacity: 0.7;
}
.table-tile.selected {
  border-color: #0d6efd;
}
</style>