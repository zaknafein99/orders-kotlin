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
        <div class="item-inventory" :class="getInventoryStatusClass(item)">
          <i :class="getInventoryIcon(item)"></i>
          <span>
            Disponible: {{ item.quantity || 0 }}
            <span v-if="item.inventoryAfterOrder !== undefined && item.inventoryAfterOrder !== item.quantity" class="inventory-projection">
              (Después de la orden: {{ item.inventoryAfterOrder }})
            </span>
          </span>
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

const getInventoryStatusClass = (item) => {
  if (item.quantity <= 0) {
    return 'out-of-stock'
  } else if (item.quantity <= 5) {
    return 'low-stock'
  } else {
    return 'in-stock'
  }
}

const getInventoryIcon = (item) => {
  if (item.quantity <= 0) {
    return 'fa-times-circle'
  } else if (item.quantity <= 5) {
    return 'fa-exclamation-circle'
  } else {
    return 'fa-check-circle'
  }
}
</script>

<style>
/* Styles moved to /src/assets/styles/components/ItemSelector.css */

/* Inventory status styles */
.item-inventory {
  display: flex;
  align-items: center;
  font-size: 0.8rem;
  margin-bottom: 0.5rem;
  gap: 0.25rem;
}

.in-stock {
  color: #16a34a; /* Green */
}

.low-stock {
  color: #ca8a04; /* Amber/Orange */
}

.out-of-stock {
  color: #dc2626; /* Red */
}

.inventory-projection {
  font-style: italic;
  margin-left: 0.25rem;
  font-size: 0.75rem;
  color: #6b7280; /* Gray */
}
</style>
