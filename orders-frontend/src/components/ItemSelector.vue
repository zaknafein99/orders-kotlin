<template>
  <div class="item-selector">
    <!-- Pagination controls -->
    <div class="pagination-controls">
      <button 
        @click="$emit('change-page', currentPage - 1)" 
        :disabled="currentPage === 0 || isLoading"
        class="pagination-btn"
      >
        &laquo; Anterior
      </button>
      <span class="page-info">Página {{ currentPage + 1 }} de {{ totalPages }}</span>
      <button 
        @click="$emit('change-page', currentPage + 1)" 
        :disabled="currentPage >= totalPages - 1 || isLoading"
        class="pagination-btn"
      >
        Siguiente &raquo;
      </button>
    </div>
    
    <!-- Items loading state -->
    <div v-if="isLoading" class="items-loading">
      <div class="spinner"></div>
      <p>Cargando items...</p>
    </div>
    
    <!-- Items error state -->
    <div v-if="error" class="items-error">
      <p>{{ error }}</p>
      <button @click="$emit('retry')" class="retry-btn">Reintentar</button>
    </div>
    
    <!-- Items list -->
    <div v-if="!isLoading && !error && items.length > 0" class="items-list">
      <div v-for="item in items" :key="item.id" class="item">
        <div class="item-details">
          <span class="item-name">{{ item.name }}</span>
          <span class="item-price">{{ formatPrice(item.price) }}</span>
        </div>
        <div class="item-description" v-if="item.description">
          {{ item.description }}
        </div>
        <div class="item-category" v-if="item.category">
          Categoría: {{ item.category }}
        </div>
        <div class="item-actions">
          <div class="quantity-control">
            <button @click="decrementQuantity(item)" class="quantity-btn" :disabled="getItemQuantity(item.id) <= 0">
              -
            </button>
            <span class="quantity">{{ getItemQuantity(item.id) }}</span>
            <button @click="incrementQuantity(item)" class="quantity-btn">
              +
            </button>
          </div>
          <button 
            @click="addToOrder(item)" 
            :disabled="getItemQuantity(item.id) <= 0" 
            class="add-btn"
          >
            Agregar al Pedido
          </button>
        </div>
      </div>
    </div>
    
    <!-- No items state -->
    <div v-if="!isLoading && !error && items.length === 0" class="no-items">
      <p>No hay items disponibles</p>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, reactive } from 'vue'
import { formatPrice } from '../utils/orderUtils'
import { translations } from '../utils/translations'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  },
  currentPage: {
    type: Number,
    default: 0
  },
  totalPages: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['add-item', 'change-page', 'retry'])

// Item quantities for UI
const itemQuantities = reactive({})

// Methods
const getItemQuantity = (itemId) => {
  return itemQuantities[itemId] || 0
}

const incrementQuantity = (item) => {
  if (!itemQuantities[item.id]) {
    itemQuantities[item.id] = 0
  }
  itemQuantities[item.id]++
}

const decrementQuantity = (item) => {
  if (itemQuantities[item.id] > 0) {
    itemQuantities[item.id]--
  }
}

const addToOrder = (item) => {
  const quantity = itemQuantities[item.id]
  if (quantity <= 0) return
  
  // Emit event to parent component
  emit('add-item', {
    id: item.id,
    name: item.name,
    description: item.description,
    price: item.price,
    quantity: quantity,
    category: item.category
  })
  
  // Reset quantity
  itemQuantities[item.id] = 0
}
</script>

<style scoped>
.item-selector {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.pagination-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.pagination-btn {
  background-color: #f0f0f0;
  border: 1px solid #ddd;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.items-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.items-error {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 10px;
}

.retry-btn {
  background-color: #d32f2f;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 10px;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
  max-height: 400px;
  overflow-y: auto;
}

.item {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 12px;
  background-color: #fafafa;
}

.item-details {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.item-name {
  font-weight: bold;
}

.item-price {
  color: #2e7d32;
  font-weight: bold;
}

.item-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.item-category {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.item-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 1px solid #ddd;
  background-color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.quantity {
  font-weight: bold;
  min-width: 20px;
  text-align: center;
}

.add-btn {
  background-color: #2196f3;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:disabled {
  background-color: #bbdefb;
  cursor: not-allowed;
}

.no-items {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 8px;
}
</style>
