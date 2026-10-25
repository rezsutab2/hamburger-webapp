<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'
import { addToCart, getQuantityInCart } from '@/store/cart'

const router = useRouter()
const menuItems = ref([])

function isLoggedIn() {
  return !!localStorage.getItem('token')
}

function handleAddToCart(item) {
  if (!isLoggedIn()) {
    router.push('/login')
    return
  }
  addToCart(item)
}

function ingredientsLabel(item) {
  if (!item.menuItemIngredient || item.menuItemIngredient.length === 0) {
    return 'Nincs megadott összetevő.'
  }
  return item.menuItemIngredient
    .map(mii =>
      mii.ingredient.unit
        ? `${mii.ingredient.name} (${mii.quantity} ${mii.ingredient.unit})`
        : mii.ingredient.name
    )
    .join(', ')
}

function allergensLabel(item) {
  if (!item.menuItemIngredient || item.menuItemIngredient.length === 0) return ''
  const names = new Set()
  item.menuItemIngredient.forEach(mii => {
    (mii.ingredient.allergens || []).forEach(a => names.add(a.name))
  })
  return names.size > 0 ? Array.from(names).join(', ') : 'Nincs feltüntetett allergén.'
}

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
            <h5 class="card-title d-flex align-items-center gap-2">
              {{ item.name }}
              <span class="info-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 16 16" fill="currentColor">
                <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z"/>
                <path d="M8.93 6.588l-2.29.287-.082.38.45.083c.294.07.352.176.288.469l-.738 3.468c-.194.897.105 1.319.808 1.319.545 0 1.178-.252 1.465-.598l.088-.416c-.2.176-.492.246-.686.246-.275 0-.375-.193-.304-.533L8.93 6.588zM9 4.5a1 1 0 1 1-2 0 1 1 0 0 1 2 0z"/>
                </svg>
                <span class="tooltip-bubble">
                  <strong>Összetevők:</strong> {{ ingredientsLabel(item) }}<br />
                  <strong>Allergének:</strong> {{ allergensLabel(item) }}
                </span>
              </span>
            </h5>
            <p class="card-text">{{ item.description }}</p>
            <p class="card-text"><strong>{{ item.price }} Ft</strong></p>
            <button class="btn btn-primary position-relative" @click="handleAddToCart(item)">
              Kosárba
              <span v-if="isLoggedIn() && getQuantityInCart(item.id) > 0"
              class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
                {{ getQuantityInCart(item.id) }}
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.info-icon {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #e9ecef;
  color: #495057;
  cursor: help;
}

.info-icon svg {
  display: block;
}

.tooltip-bubble {
  visibility: hidden;
  opacity: 0;
  position: absolute;
  bottom: 125%;
  left: 50%;
  transform: translateX(-50%);
  background-color: #212529;
  color: #fff;
  text-align: left;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: normal;
  white-space: normal;
  width: 260px;
  z-index: 10;
  transition: opacity 0.15s ease-in-out;
}

.info-icon:hover .tooltip-bubble {
  visibility: visible;
  opacity: 1;
}
</style>