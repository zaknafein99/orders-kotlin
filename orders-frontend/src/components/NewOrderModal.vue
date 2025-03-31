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
      
      <form @submit.prevent="handleSubmit" class="modal-form">
        <div class="form-group">
          <label for="customer">Cliente</label>
          <div class="customer-search">
            <input 
              type="tel" 
              id="customer" 
              v-model="searchPhone" 
              placeholder="Buscar por teléfono"
              @input="searchCustomer"
            >
            <div v-if="searchPhone && !selectedCustomer" class="customer-actions">
              <button 
                type="button" 
                class="new-customer-btn"
                @click="showNewCustomerModal = true"
              >
                <i class="fas fa-user-plus"></i>
                Nuevo Cliente
              </button>
            </div>
            <div v-if="selectedCustomer" class="selected-customer">
              <span>{{ selectedCustomer.name }}</span>
              <button 
                type="button" 
                class="clear-customer-btn"
                @click="clearCustomer"
              >
                <i class="fas fa-times"></i>
              </button>
            </div>
          </div>
        </div>
      </form>
    </div>
    
    <NewCustomerModal 
      :is-open="showNewCustomerModal"
      @close="showNewCustomerModal = false"
      @customer-created="handleCustomerCreated"
    />
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
import NewCustomerModal from './NewCustomerModal.vue'
import { useCustomerStore } from '../stores/customer'
import { useRouter } from 'vue-router'
import { createOrderObject, validateOrder } from '../utils/orderUtils'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  customer: {
    type: Object,
    required: false,
    default: null
  }
})

// Emits
const emit = defineEmits(['close', 'order-created', 'customer-selected'])

// Store
const customerStore = useCustomerStore()

// Customer search state
const searchPhone = ref('')
const selectedCustomer = ref(null)
const showNewCustomerModal = ref(false)

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
  console.log('Fetching trucks...')
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
        console.log('Default truck selected:', selectedTruck.value)
      } else {
        trucksError.value = 'No hay camiones disponibles'
        console.warn('No trucks available')
      }
    } else {
      trucksError.value = 'Formato de datos de camiones inválido'
      console.error('Invalid truck data format:', trucks)
    }
  } catch (error) {
    console.error('Error fetching trucks:', error)
    trucksError.value = `Error al cargar camiones: ${error.message || 'Error desconocido'}`
  } finally {
    isLoadingTrucks.value = false
  }
}

const fetchItems = async () => {
  console.log('Fetching items...')
  isLoadingItems.value = true
  itemsError.value = ''
  
  try {
    const data = await OrderService.getItems(currentPage.value, pageSize.value)
    console.log('Items response:', data)
    
    if (data && data.content) {
      availableItems.value = data.content
      totalPages.value = data.totalPages || 1
      totalItems.value = data.totalElements || data.content.length
      console.log(`Loaded ${availableItems.value.length} items, total pages: ${totalPages.value}`)
    } else if (Array.isArray(data)) {
      availableItems.value = data
      totalPages.value = Math.ceil(data.length / pageSize.value)
      totalItems.value = data.length
      console.log(`Loaded ${availableItems.value.length} items from array`)
    } else {
      itemsError.value = 'Formato de datos de items inválido'
      console.error('Invalid item data format:', data)
    }
    
    // Initialize quantities for new items
    availableItems.value.forEach(item => {
      if (itemQuantities[item.id] === undefined) {
        itemQuantities[item.id] = 0
      }
    })
  } catch (error) {
    console.error('Error fetching items:', error)
    itemsError.value = error.message || 'Error al cargar items'
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

const submitOrder = async () => {
  if (!props.customer) {
    console.error('No customer selected for order')
    return
  }

  if (!selectedTruck.value) {
    console.error('No truck selected for order')
    return
  }

  if (currentOrder.items.length === 0) {
    console.error('No items in order')
    return
  }

  try {
    isSubmittingOrder.value = true
    orderError.value = ''
    orderSuccess.value = ''

    console.log('Creating order with data:', {
      customer: props.customer,
      items: currentOrder.items,
      truck: selectedTruck.value,
      date: orderDate.value
    })

    const orderData = createOrderObject(
      props.customer,
      currentOrder.items,
      selectedTruck.value,
      orderDate.value
    )

    console.log('Order data created:', orderData)

    // Validate order before submitting
    const validation = validateOrder(orderData)
    if (!validation.isValid) {
      console.error('Order validation failed:', validation.errors)
      orderError.value = validation.errors.join(', ')
      return
    }

    const response = await OrderService.createOrder(orderData)
    console.log('Order created successfully:', response)

    orderSuccess.value = 'Orden creada exitosamente'
    emit('order-created', response)
    closeModal()
  } catch (error) {
    console.error('Error creating order:', error)
    orderError.value = error.response?.data?.message || 'Error al crear la orden'
  } finally {
    isSubmittingOrder.value = false
  }
}

const handleCustomerCreated = (customer) => {
  selectedCustomer.value = customer
  emit('customer-selected', customer)
}

const clearCustomer = () => {
  selectedCustomer.value = null
}

const router = useRouter()

const searchCustomer = async () => {
  if (!searchPhone.value) {
    selectedCustomer.value = null
    return
  }

  try {
    const customer = await customerStore.searchCustomer(searchPhone.value)
    if (customer) {
      selectedCustomer.value = customer
      emit('customer-selected', customer)
    } else {
      selectedCustomer.value = null
      // Don't emit customer-selected event when no customer is found
      // This will show the "Nuevo Cliente" button
    }
  } catch (error) {
    // Handle authentication errors
    if (error.response?.status === 403 || error.response?.status === 401) {
      // Clear any existing token
      localStorage.removeItem('token')
      // Redirect to login
      router.push('/login')
      return
    }
    
    // Only log other errors
    if (error.response?.status !== 404) {
      console.error('Error searching customer:', error)
    }
    selectedCustomer.value = null
    // Don't emit customer-selected event on error
  }
}

// Watch for modal visibility changes
watch(() => props.show, (newVal) => {
  if (newVal) {
    console.log('Modal opened, loading data...')
    // Reset state
    currentOrder.items = []
    orderError.value = ''
    orderSuccess.value = ''
    currentPage.value = 0
    
    // Fetch items and trucks
    Promise.all([
      fetchItems(),
      fetchTrucks()
    ]).catch(error => {
      console.error('Error loading modal data:', error)
    })
  }
})

// Add immediate option to watch to ensure it runs on component mount if modal is already open
watch(() => props.show, (newVal) => {
  if (newVal) {
    console.log('Modal is open, loading data...')
    Promise.all([
      fetchItems(),
      fetchTrucks()
    ]).catch(error => {
      console.error('Error loading modal data:', error)
    })
  }
}, { immediate: true })
</script>

<style>
/* Styles moved to /src/assets/styles/components/NewOrderModal.css */

.customer-search {
  position: relative;
}

.customer-actions {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
}

.new-customer-btn {
  background-color: var(--secondary-color);
  color: var(--primary-color);
  border: 1px solid var(--primary-color);
  padding: 0.5rem 1rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.new-customer-btn:hover {
  background-color: var(--primary-color);
  color: var(--secondary-color);
  transform: translateY(-1px);
}

.selected-customer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #f3f4f6;
  padding: 0.5rem;
  border-radius: 4px;
  margin-top: 0.5rem;
}

.selected-customer span {
  font-weight: 500;
  color: var(--dark-color);
}

.clear-customer-btn {
  background: none;
  border: none;
  color: #6b7280;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.clear-customer-btn:hover {
  background-color: #e5e7eb;
  color: var(--primary-color);
}
</style>
