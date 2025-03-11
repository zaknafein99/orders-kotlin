<template>
  <div v-if="show" class="modal">
    <div class="modal-content">
      <span class="close" @click="closeModal">&times;</span>
      <h2>{{ translations.newOrder }} - {{ customer?.name }}</h2>
      
      <!-- Items loading state -->
      <div v-if="isLoadingItems" class="modal-loading">
        <div class="spinner"></div>
        <p>Cargando items disponibles...</p>
      </div>
      
      <!-- Items error state -->
      <div v-if="itemsError" class="modal-error">
        <p>{{ itemsError }}</p>
        <button @click="fetchItems" class="retry-btn">Reintentar</button>
      </div>
      
      <!-- Modal layout -->
      <div class="order-modal-layout">
        <!-- Left side: Available items list -->
        <div class="available-items-section">
          <h3>Items Disponibles</h3>
          <ItemSelector 
            :items="availableItems"
            :is-loading="isLoadingItems"
            :error="itemsError"
            :current-page="currentPage"
            :total-pages="totalPages"
            @add-item="addToOrder"
            @change-page="changePage"
            @retry="fetchItems"
          />
        </div>
        
        <!-- Right side: Current order using OrderSummary component -->
        <div class="current-order-section">
          <OrderSummary 
            :items="currentOrder.items"
            :customer="customer"
            :trucks="availableTrucks"
            :is-submitting="isSubmittingOrder"
            :error="orderError"
            :success-message="orderSuccess"
            @remove-item="removeFromOrder"
            @submit="submitOrder"
            @update-truck="updateTruck"
            @update-date="updateDate"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { eventBus } from '../utils/eventBus'
import ItemSelector from './ItemSelector.vue'
import OrderSummary from './OrderSummary.vue'
import OrderService from '../services/OrderService'
import { translations } from '../utils/translations'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  customer: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['close', 'order-created'])

// Items state
const availableItems = ref([])
const isLoadingItems = ref(false)
const itemsError = ref('')
const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(1)
const totalItems = ref(0)

// Order state
const currentOrder = reactive({
  items: []
})
const selectedTruck = ref({ id: 1, name: 'truck1' }) // Default truck
const availableTrucks = ref([
  { id: 1, name: 'truck1' },
  { id: 2, name: 'truck2' }
])
const orderDate = ref(new Date().toISOString().split('T')[0])
const isSubmittingOrder = ref(false)
const orderError = ref('')
const orderSuccess = ref('')

// Item quantities for UI
const itemQuantities = reactive({})

// Computed properties
const todayFormatted = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// Methods
const closeModal = () => {
  // Reset state
  currentOrder.items = []
  
  // Clear item quantities
  Object.keys(itemQuantities).forEach(key => {
    itemQuantities[key] = 0
  })
  
  orderError.value = ''
  orderSuccess.value = ''
  currentPage.value = 0
  
  // Emit close event
  emit('close')
}

const fetchItems = async () => {
  isLoadingItems.value = true
  itemsError.value = ''
  
  try {
    const data = await OrderService.getItems(currentPage.value, pageSize.value)
    console.log('Items response:', data)
    
    if (data && data.content) {
      availableItems.value = data.content
      totalPages.value = data.totalPages || 1
      totalItems.value = data.totalElements || data.content.length
    } else if (Array.isArray(data)) {
      availableItems.value = data
      totalPages.value = Math.ceil(data.length / pageSize.value)
      totalItems.value = data.length
    } else {
      itemsError.value = 'Invalid item data format'
    }
    
    // Initialize quantities for new items
    availableItems.value.forEach(item => {
      if (itemQuantities[item.id] === undefined) {
        itemQuantities[item.id] = 0
      }
    })
  } catch (error) {
    console.error('Error fetching items:', error)
    itemsError.value = error.message || 'Failed to load items'
  } finally {
    isLoadingItems.value = false
  }
}

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < totalPages.value) {
    currentPage.value = newPage
    fetchItems()
  }
}

const addToOrder = (item) => {
  // Check if item already exists in order
  const existingItemIndex = currentOrder.items.findIndex(i => i.id === item.id)
  
  if (existingItemIndex >= 0) {
    // Update existing item
    currentOrder.items[existingItemIndex].quantity += item.quantity
  } else {
    // Add new item
    currentOrder.items.push(item)
  }
}

const removeFromOrder = (index) => {
  currentOrder.items.splice(index, 1)
}

// Additional methods for OrderSummary component
const updateTruck = (truck) => {
  selectedTruck.value = truck
}

const updateDate = (date) => {
  orderDate.value = date
}

const submitOrder = async (orderData) => {
  isSubmittingOrder.value = true
  orderError.value = ''
  orderSuccess.value = ''
  
  try {
    console.log('Submitting order:', orderData)
    
    const createdOrder = await OrderService.createOrder(orderData)
    
    console.log('Order created:', createdOrder)
    orderSuccess.value = 'Order created successfully!'
    
    // Reset order after successful submission
    currentOrder.items = []
    
    // Notify parent component
    emit('order-created', createdOrder)
    
    // Notify other components via eventBus to refresh order tables
    eventBus.emit('order-submitted', createdOrder)
    
    // Close modal after a delay
    setTimeout(() => {
      closeModal()
    }, 2000)
    
  } catch (error) {
    console.error('Error creating order:', error)
    orderError.value = error.message || 'Failed to create order'
  } finally {
    isSubmittingOrder.value = false
  }
}

// Watch for modal visibility changes
watch(() => props.show, (newVal) => {
  if (newVal) {
    // Reset and fetch items when modal opens
    currentPage.value = 0
    fetchItems()
  }
})
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 1200px;
  max-height: 90vh;
  overflow-y: auto;
  padding: 20px;
  position: relative;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.close {
  position: absolute;
  top: 10px;
  right: 15px;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.close:hover {
  color: #000;
}

.modal-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
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

.modal-error {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
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

.order-modal-layout {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.available-items-section {
  flex: 1;
  border-right: 1px solid #eee;
  padding-right: 20px;
}

.current-order-section {
  flex: 1;
  padding-left: 20px;
}

/* Simplified CSS since most styling is now in child components */

.submit-btn {
  background-color: #4caf50;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
  font-size: 16px;
}

.submit-btn:disabled {
  background-color: #a5d6a7;
  cursor: not-allowed;
}

.order-error {
  margin-top: 15px;
  color: #d32f2f;
  background-color: #ffebee;
  padding: 10px;
  border-radius: 4px;
}

.order-success {
  margin-top: 15px;
  color: #2e7d32;
  background-color: #e8f5e9;
  padding: 10px;
  border-radius: 4px;
}

@media (max-width: 768px) {
  .order-modal-layout {
    flex-direction: column;
  }
  
  .available-items-section {
    border-right: none;
    border-bottom: 1px solid #eee;
    padding-right: 0;
    padding-bottom: 20px;
  }
  
  .current-order-section {
    padding-left: 0;
    padding-top: 20px;
  }
}
</style>
