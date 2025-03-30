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
            :is-loading-trucks="isLoadingTrucks"
            :trucks-error="trucksError"
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
import TruckService from '../services/TruckService'
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
const selectedTruck = ref(null) // Will be set after fetching trucks
const availableTrucks = ref([])
const isLoadingTrucks = ref(false)
const trucksError = ref('')
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

const fetchTrucks = async () => {
  isLoadingTrucks.value = true
  trucksError.value = ''
  
  try {
    const trucks = await TruckService.getAllTrucks()
    console.log('Trucks fetched:', trucks)
    
    if (Array.isArray(trucks)) {
      availableTrucks.value = trucks
      
      // Set default selected truck if available
      if (trucks.length > 0) {
        selectedTruck.value = trucks[0]
      }
    } else {
      trucksError.value = 'Invalid truck data format'
    }
  } catch (error) {
    console.error('Error fetching trucks:', error)
    trucksError.value = `Error fetching trucks: ${error.message || 'Unknown error'}`
  } finally {
    isLoadingTrucks.value = false
  }
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
  
  // Validate truck selection
  if (!selectedTruck.value) {
    orderError.value = 'Please select a truck for this order'
    isSubmittingOrder.value = false
    return
  }
  
  try {
    // Make sure to include the selected truck in the order data
    const orderWithTruck = {
      ...orderData,
      truck: selectedTruck.value
    }
    
    console.log('Submitting order with truck:', orderWithTruck)
    
    const createdOrder = await OrderService.createOrder(orderWithTruck)
    
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
    // Reset and fetch items and trucks when modal opens
    currentPage.value = 0
    fetchItems()
    fetchTrucks()
  }
})
</script>

<style>
/* Styles moved to /src/assets/styles/components/NewOrderModal.css */
</style>
