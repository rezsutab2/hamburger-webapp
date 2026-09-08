import { reactive } from 'vue'

export const cart = reactive({
  items: [] // { menuItemId, name, price, quantity }
})

export function addToCart(menuItem) {
  const existing = cart.items.find(i => i.menuItemId === menuItem.id)
  if (existing) {
    existing.quantity++
  } else {
    cart.items.push({
      menuItemId: menuItem.id,
      name: menuItem.name,
      price: menuItem.price,
      quantity: 1
    })
  }
}

export function removeFromCart(menuItemId) {
  const index = cart.items.findIndex(i => i.menuItemId === menuItemId)
  if (index !== -1) {
    cart.items.splice(index, 1)
  }
}

export function clearCart() {
  cart.items = []
}

export function cartTotal() {
  return cart.items.reduce((sum, i) => sum + i.price * i.quantity, 0)
}