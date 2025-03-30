<template>
  <div class="order-tables">
    <div class="tables-header">
      <h2><i class="fas fa-clipboard-list"></i> Panel de Pedidos</h2>
      <button @click="fetchOrders" class="refresh-btn">
        <i class="fas fa-sync-alt refresh-icon"></i> Actualizar
      </button>
    </div>

    <div v-if="isLoading" class="loading">
      <i class="fas fa-spinner fa-spin fa-2x"></i>
      <p>Cargando pedidos...</p>
    </div>

    <div v-else>
      <div class="table-section">
        <h3 class="bg-primary-600"><i class="fas fa-clock text-secondary-500"></i> Pedidos Pendientes</h3>
        <div v-if="pendingOrders.length === 0" class="empty-state">
          <i class="fas fa-inbox"></i>
          <p>{{ translations.noOrders }}</p>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th class="date-column">{{ translations.createdAt }}</th>
              <th>{{ translations.customer }}</th>
              <th>{{ translations.itemCount }}</th>
              <th>{{ translations.total }}</th>
              <th>{{ translations.truck }}</th>
              <th>{{ translations.actions }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in pendingOrders" :key="order.id">
              <td class="date-cell">{{ formatDate(order.createdAt) }}</td>
              <td>
                <div class="customer-info">
                  <div class="customer-name">{{ order.customerName }}</div>
                  <div class="customer-phone">{{ order.customerPhone }}</div>
                </div>
              </td>
              <td>{{ order.items.length }}</td>
              <td>${{ order.total.toFixed(2) }}</td>
              <td>
                <div class="truck-selection-container">
                  <select 
                    v-model="orderTrucks[order.id]" 
                    class="truck-select"
                    @change="onTruckSelected(order.id, orderTrucks[order.id])"
                  >
                    <option value="">{{ translations.selectTruck }}</option>
                    <option v-for="truck in availableTrucks" :key="truck.id" :value="String(truck.id)">{{ truck.name }}</option>
                  </select>
                  <div v-if="truckUpdateLoading[order.id]" class="loading-indicator">{{ translations.updating }}...</div>
                  <div v-if="orderTrucks[order.id] && !truckUpdateLoading[order.id]" class="selected-truck-indicator">
                    {{ getSelectedTruckName(orderTrucks[order.id]) }}
                  </div>
                </div>
              </td>
              <td>
                <button 
                  @click="markAsDelivered(order.id)" 
                  class="action-btn deliver-btn"
                  :disabled="!order.truck && !orderTrucks[order.id]"
                >
                  <i class="fas fa-truck"></i> {{ translations.markDelivered }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="table-section">
        <h3 class="bg-primary-600"><i class="fas fa-truck text-secondary-500"></i> Pedidos Entregados</h3>
        <div v-if="deliveredOrders.length === 0" class="empty-state">
          <i class="fas fa-inbox"></i>
          <p>{{ translations.noOrders }}</p>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th class="date-column">{{ translations.createdAt }}</th>
              <th>{{ translations.customer }}</th>
              <th>{{ translations.itemCount }}</th>
              <th>{{ translations.total }}</th>
              <th>{{ translations.truck }}</th>
              <th class="date-column">{{ translations.deliveredAt }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in deliveredOrders" :key="order.id">
              <td class="date-cell">{{ formatDate(order.createdAt) }}</td>
              <td>
                <div class="customer-info">
                  <div class="customer-name">{{ order.customerName }}</div>
                  <div class="customer-phone">{{ order.customerPhone }}</div>
                </div>
              </td>
              <td>{{ order.items.length }}</td>
              <td>${{ order.total.toFixed(2) }}</td>
              <td>
                <div class="truck-selection-container">
                  <select 
                    v-model="orderTrucks[order.id]" 
                    class="truck-select"
                    @change="onTruckSelected(order.id, orderTrucks[order.id])"
                  >
                    <option value="">{{ translations.selectTruck }}</option>
                    <option v-for="truck in availableTrucks" :key="truck.id" :value="String(truck.id)">{{ truck.name }}</option>
                  </select>
                  <div v-if="truckUpdateLoading[order.id]" class="loading-indicator">{{ translations.updating }}...</div>
                  <div v-if="orderTrucks[order.id] && !truckUpdateLoading[order.id]" class="selected-truck-indicator">
                    {{ getSelectedTruckName(orderTrucks[order.id]) }}
                  </div>
                </div>
              </td>
              <td>{{ formatDate(order.deliveredAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive, watch } from 'vue'
import { eventBus } from '../utils/eventBus'
import OrderService from '../services/OrderService'
import TruckService from '../services/TruckService'
import { translations } from '../utils/translations'

// Track truck assignments for orders that don't have one yet
const orderTrucks = reactive({})

// Track loading state for truck updates
const truckUpdateLoading = reactive({})

// Available trucks for selection
const availableTrucks = ref([])
const trucksLoading = ref(false)
const trucksError = ref('')

const pendingOrders = ref([])
const deliveredOrders = ref([])
const isLoading = ref(false)

const fetchTrucks = async () => {
  trucksLoading.value = true
  trucksError.value = ''
  try {
    const trucks = await TruckService.getAllTrucks()
    availableTrucks.value = trucks
    console.log('Trucks fetched successfully:', trucks)
  } catch (error) {
    console.error('Error fetching trucks:', error)
    trucksError.value = 'Failed to load trucks'
  } finally {
    trucksLoading.value = false
  }
}

const checkToken = () => {
  const token = localStorage.getItem('token')
  console.log('Current token in localStorage:', token ? `${token.substring(0, 15)}...` : 'No token')
  return token
}

const fetchOrders = async () => {
  isLoading.value = true
  console.log('Starting to fetch orders...')
  
  // Check if we have a token
  const token = checkToken()
  if (!token) {
    console.error('No token found in localStorage, cannot fetch orders')
    return
  }
  try {
    // Fetch trucks first if not already loaded
    if (availableTrucks.value.length === 0) {
      console.log('No trucks loaded, fetching trucks first...')
      await fetchTrucks()
    }
    
    // Fetch pending orders using OrderService
    console.log('Fetching pending orders...')
    const pendingData = await OrderService.getPendingOrders()
    console.log('Pending orders response:', pendingData, 'Type:', typeof pendingData, 'Is Array:', Array.isArray(pendingData), 'Length:', pendingData ? pendingData.length : 0)
    
    if (!pendingData || !Array.isArray(pendingData) || pendingData.length === 0) {
      console.warn('No pending orders data received or data is not an array')
      pendingOrders.value = []
    } else {
      console.log('Mapping pending orders data...')
      console.log('First order sample:', pendingData[0])
      console.log('First order customer:', pendingData[0].customer)
      
      pendingOrders.value = pendingData.map(order => {
        console.log('Processing order:', order.id, 'Customer:', order.customer)
        return {
          id: order.id,
          customerName: order.customer.name,
          customerPhone: order.customer.phoneNumber,
          createdAt: new Date(order.date),
          items: order.items,
          total: order.totalPrice,
          truck: order.truck ? {
            id: order.truck.id,
            name: order.truck.name
          } : null
        }
      })
      
      // Sort pending orders by date, newest first
      pendingOrders.value.sort((a, b) => b.createdAt - a.createdAt)
    }
    
    // Fetch delivered orders using OrderService
    console.log('Fetching delivered orders...')
    const deliveredData = await OrderService.getDeliveredOrders()
    console.log('Delivered orders response:', deliveredData, 'Type:', typeof deliveredData, 'Is Array:', Array.isArray(deliveredData), 'Length:', deliveredData ? deliveredData.length : 0)
    
    if (!deliveredData || !Array.isArray(deliveredData) || deliveredData.length === 0) {
      console.warn('No delivered orders data received or data is not an array')
      deliveredOrders.value = []
    } else {
      console.log('Mapping delivered orders data...')
      console.log('First delivered order sample:', deliveredData[0])
      
      // Create a set of pending order IDs for quick lookup
      const pendingOrderIds = new Set(pendingOrders.value.map(order => order.id))
      console.log('Pending order IDs:', Array.from(pendingOrderIds))
      
      // Filter out any delivered orders that are also in pending orders
      const filteredDeliveredData = deliveredData.filter(order => {
        const isDuplicate = pendingOrderIds.has(order.id)
        if (isDuplicate) {
          console.log(`Order ID ${order.id} appears in both pending and delivered lists, excluding from delivered list`)
        }
        return !isDuplicate
      })
      
      console.log(`Filtered out ${deliveredData.length - filteredDeliveredData.length} duplicate orders from delivered list`)
      
      deliveredOrders.value = filteredDeliveredData.map(order => ({
        id: order.id,
        customerName: order.customer.name,
        customerPhone: order.customer.phoneNumber,
        createdAt: new Date(order.date),
        items: order.items,
        deliveredAt: new Date(order.date), // Using same date since backend doesn't have delivery date
        total: order.totalPrice,
        truck: order.truck ? {
          id: order.truck.id,
          name: order.truck.name
        } : null
      }))
      
      // Sort delivered orders by date, newest first
      deliveredOrders.value.sort((a, b) => b.createdAt - a.createdAt)
    }
    
    // Initialize the orderTrucks object with current truck IDs
    const allOrders = [...pendingOrders.value, ...deliveredOrders.value]
    allOrders.forEach(order => {
      if (order.truck) {
        // Make sure to convert to string for v-model compatibility
        orderTrucks[order.id] = String(order.truck.id)
      } else {
        orderTrucks[order.id] = ''
      }
    })
    
    console.log('Initialized orderTrucks:', orderTrucks)
    
    console.log('Orders refreshed successfully')
  } catch (error) {
    console.error('Error fetching orders:', error)
    if (error.response) {
      console.error('Error response data:', error.response.data)
      console.error('Error response status:', error.response.status)
      console.error('Error response headers:', error.response.headers)
    } else if (error.request) {
      console.error('Error request:', error.request)
    } else {
      console.error('Error message:', error.message)
    }
    console.error('Error config:', error.config)
  } finally {
    isLoading.value = false
    console.log('Fetch orders completed, loading state set to false')
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const day = d.getDate().toString().padStart(2, '0')
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  const year = d.getFullYear()
  return `${day}/${month}/${year}`
}

const getSelectedTruckName = (truckId) => {
  if (!truckId) return ''
  const truck = availableTrucks.value.find(t => String(t.id) === String(truckId))
  return truck ? truck.name : ''
}

const onTruckSelected = async (orderId, truckId) => {
  if (!truckId) return
  
  try {
    console.log('Truck selected for order:', orderId, 'truck:', truckId)
    // Store the previous truck ID in case we need to revert
    const previousTruckId = orderTrucks[orderId]
    
    // Set loading state
    truckUpdateLoading[orderId] = true
    
    // Call the API to update the truck
    await OrderService.assignTruckToOrder(orderId, truckId)
    
    // Update the UI to show the selected truck
    console.log('Truck updated successfully in UI')
    
    // Refresh orders to ensure UI is up to date
    await fetchOrders()
  } catch (error) {
    console.error('Error handling truck selection:', error)
    // Reset to previous value on error
    const order = [...pendingOrders.value, ...deliveredOrders.value].find(o => o.id === orderId)
    if (order && order.truck) {
      orderTrucks[orderId] = String(order.truck.id)
    } else {
      orderTrucks[orderId] = ''
    }
    alert(translations.errorUpdatingTruck)
  } finally {
    truckUpdateLoading[orderId] = false
  }
}

const assignTruckToOrder = async (orderId, truckId) => {
  try {
    console.log('Assigning truck to order:', orderId, 'truck:', truckId)
    
    // Call the API to assign the truck
    const response = await TruckService.updateOrderTruck(orderId, truckId)
    console.log('API response:', response)
    
    // Update the local state in both pending and delivered orders
    const selectedTruck = availableTrucks.value.find(truck => truck.id === parseInt(truckId))
    if (!selectedTruck) {
      console.error('Selected truck not found in available trucks')
      return
    }
    
    // Ensure orderTrucks is updated with the new value
    orderTrucks[orderId] = String(truckId)
    
    // Check if the order is in pending orders
    const pendingIndex = pendingOrders.value.findIndex(order => order.id === orderId)
    if (pendingIndex !== -1) {
      pendingOrders.value[pendingIndex].truck = {
        id: selectedTruck.id,
        name: selectedTruck.name
      }
    }
    
    // Check if the order is in delivered orders
    const deliveredIndex = deliveredOrders.value.findIndex(order => order.id === orderId)
    if (deliveredIndex !== -1) {
      deliveredOrders.value[deliveredIndex].truck = {
        id: selectedTruck.id,
        name: selectedTruck.name
      }
    }
    
    console.log('Truck assigned successfully')
  } catch (error) {
    console.error('Error assigning truck to order:', error)
    if (error.response) {
      console.error('Error response data:', error.response.data)
      console.error('Error response status:', error.response.status)
      console.error('Error response headers:', error.response.headers)
    } else if (error.request) {
      console.error('Error request:', error.request)
    } else {
      console.error('Error message:', error.message)
    }
    console.error('Error config:', error.config)
    throw error // Rethrow to handle in the calling function
  }
}

const markAsDelivered = async (orderId) => {
  try {
    console.log('Marking order as delivered:', orderId)
    
    // If the order doesn't have a truck assigned yet, assign it first
    const orderIndex = pendingOrders.value.findIndex(order => order.id === orderId)
    if (orderIndex !== -1) {
      const order = pendingOrders.value[orderIndex]
      
      // Check if we need to assign a truck first
      if (orderTrucks[orderId] && (!order.truck || order.truck.id !== parseInt(orderTrucks[orderId]))) {
        console.log('Assigning truck before marking as delivered')
        try {
          await assignTruckToOrder(orderId, orderTrucks[orderId])
        } catch (error) {
          console.error('Failed to assign truck before delivery:', error)
          return // Don't proceed with delivery if truck assignment fails
        }
      }
    }
    
    // Use OrderService to mark order as delivered
    await OrderService.markOrderAsDelivered(orderId)
    
    // After successful API call, update the local state
    if (orderIndex !== -1) {
      const order = {...pendingOrders.value[orderIndex]}
      order.deliveredAt = new Date()
      
      // Check if this order already exists in the delivered orders list
      const existingDeliveredIndex = deliveredOrders.value.findIndex(o => o.id === order.id)
      if (existingDeliveredIndex !== -1) {
        console.log(`Order ID ${order.id} already exists in delivered orders, updating instead of adding`)
        deliveredOrders.value[existingDeliveredIndex] = order
      } else {
        deliveredOrders.value.unshift(order)
      }
      
      // Remove from pending orders
      pendingOrders.value.splice(orderIndex, 1)
      
      // Make sure the truck selection is preserved in the delivered order
      if (orderTrucks[orderId]) {
        const truckId = parseInt(orderTrucks[orderId])
        const selectedTruck = availableTrucks.value.find(truck => truck.id === truckId)
        if (selectedTruck) {
          // Find the order in the delivered list (it might be at index 0 or at existingDeliveredIndex)
          const deliveredOrderIndex = existingDeliveredIndex !== -1 ? existingDeliveredIndex : 0
          deliveredOrders.value[deliveredOrderIndex].truck = {
            id: selectedTruck.id,
            name: selectedTruck.name
          }
        }
      }
    }
  } catch (error) {
    console.error('Error marking order as delivered:', error)
    if (error.response) {
      console.error('Error response data:', error.response.data)
      console.error('Error response status:', error.response.status)
      console.error('Error response headers:', error.response.headers)
    } else if (error.request) {
      console.error('Error request:', error.request)
    } else {
      console.error('Error message:', error.message)
    }
    console.error('Error config:', error.config)
  }
}

onMounted(() => {
  fetchOrders()
  
  // Set up event listeners
  eventBus.on('login-success', (token) => {
    console.log('Login success event received in OrderTables')
    fetchOrders()
  })
  
  eventBus.on('order-submitted', () => {
    console.log('Order submitted event received in OrderTables')
    fetchOrders()
  })
})

// Clean up event listeners
onUnmounted(() => {
  eventBus.off('login-success')
  eventBus.off('order-submitted')
})
</script>

<style>
/* Styles moved to /src/assets/styles/components/OrderTables.css */
</style>
