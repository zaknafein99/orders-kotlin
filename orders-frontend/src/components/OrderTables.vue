<template>
  <div class="order-tables">
    <div class="tables-header">
      <h2>Panel de Pedidos</h2>
      <button @click="fetchOrders" class="refresh-btn">
        <span class="refresh-icon">↻</span> Actualizar
      </button>
    </div>

    <div v-if="isLoading" class="loading">
      <div class="spinner"></div>
      <p>Cargando pedidos...</p>
    </div>

    <div v-else>
      <div class="table-section">
        <h3>{{ translations.pendingOrders }}</h3>
        <div v-if="pendingOrders.length === 0" class="empty-state">
          <p>{{ translations.noOrders }}</p>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th>{{ translations.orderID }}</th>
              <th>{{ translations.customer }}</th>
              <th>{{ translations.createdAt }}</th>
              <th>{{ translations.itemCount }}</th>
              <th>{{ translations.total }}</th>
              <th>{{ translations.truck }}</th>
              <th>{{ translations.actions }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in pendingOrders" :key="order.id">
              <td>#{{ order.id }}</td>
              <td>{{ order.customerName }}</td>
              <td>{{ formatDate(order.createdAt) }}</td>
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
                  {{ translations.markDelivered }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="table-section">
        <h3>{{ translations.deliveredOrders }}</h3>
        <div v-if="deliveredOrders.length === 0" class="empty-state">
          <p>{{ translations.noOrders }}</p>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th>{{ translations.orderID }}</th>
              <th>{{ translations.customer }}</th>
              <th>{{ translations.createdAt }}</th>
              <th>{{ translations.itemCount }}</th>
              <th>{{ translations.total }}</th>
              <th>{{ translations.truck }}</th>
              <th>{{ translations.deliveredAt }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in deliveredOrders" :key="order.id">
              <td>#{{ order.id }}</td>
              <td>{{ order.customerName }}</td>
              <td>{{ formatDate(order.createdAt) }}</td>
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

const pendingOrders = ref([
  {
    id: 1001,
    customerName: 'John Doe',
    createdAt: new Date(2025, 2, 10),
    items: [
      { name: 'Pizza', price: 12.99 },
      { name: 'Soda', price: 2.49 }
    ],
    total: 15.48
  },
  {
    id: 1002,
    customerName: 'Jane Smith',
    createdAt: new Date(2025, 2, 11),
    items: [
      { name: 'Burger', price: 8.99 },
      { name: 'Fries', price: 3.99 },
      { name: 'Soda', price: 2.49 }
    ],
    total: 15.47
  }
])

const deliveredOrders = ref([
  {
    id: 1000,
    customerName: 'Alice Johnson',
    createdAt: new Date(2025, 2, 9),
    deliveredAt: new Date(2025, 2, 9),
    items: [
      { name: 'Salad', price: 6.99 },
      { name: 'Pasta', price: 10.99 }
    ],
    total: 17.98
  }
])

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

const fetchOrders = async () => {
  isLoading.value = true
  try {
    // Fetch trucks first if not already loaded
    if (availableTrucks.value.length === 0) {
      await fetchTrucks()
    }
    
    // Fetch pending orders using OrderService
    const pendingData = await OrderService.getPendingOrders()
    console.log('Pending orders response:', pendingData)
    
    pendingOrders.value = pendingData.map(order => ({
      id: order.id,
      customerName: order.customer.name,
      createdAt: new Date(order.date),
      items: order.items,
      total: order.totalPrice,
      truck: order.truck ? {
        id: order.truck.id,
        name: order.truck.name
      } : null
    }))
    
    // Fetch delivered orders using OrderService
    const deliveredData = await OrderService.getDeliveredOrders()
    console.log('Delivered orders response:', deliveredData)
    
    deliveredOrders.value = deliveredData.map(order => ({
      id: order.id,
      customerName: order.customer.name,
      createdAt: new Date(order.date),
      items: order.items,
      deliveredAt: new Date(order.date), // Using same date since backend doesn't have delivery date
      total: order.totalPrice,
      truck: order.truck ? {
        id: order.truck.id,
        name: order.truck.name
      } : null
    }))
    
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
  } finally {
    isLoading.value = false
  }
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
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
      deliveredOrders.value.unshift(order)
      pendingOrders.value.splice(orderIndex, 1)
      
      // Make sure the truck selection is preserved in the delivered order
      if (orderTrucks[orderId]) {
        const truckId = parseInt(orderTrucks[orderId])
        const selectedTruck = availableTrucks.value.find(truck => truck.id === truckId)
        if (selectedTruck) {
          deliveredOrders.value[0].truck = {
            id: selectedTruck.id,
            name: selectedTruck.name
          }
        }
      }
    }
  } catch (error) {
    console.error('Error marking order as delivered:', error)
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

<style scoped>
.order-tables {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.tables-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: #f8f9fa;
  color: #333;
  border: 1px solid #ddd;
}

.refresh-icon {
  font-size: 1.2rem;
}

.table-section {
  margin-bottom: 2rem;
}

.table-section h3 {
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #f5f5f5;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 1rem;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  background-color: #f8f9fa;
  font-weight: bold;
}

.action-btn {
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
}

.deliver-btn {
  background-color: #17a2b8;
}

.empty-state {
  padding: 2rem;
  text-align: center;
  background-color: #f8f9fa;
  border-radius: 6px;
  color: #6c757d;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #42b883;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.truck-select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 100%;
  min-width: 150px;
  font-size: 0.9rem;
  background-color: #fff;
  color: #333;
  appearance: auto;
}

.truck-select option {
  background-color: #fff;
  color: #333;
  padding: 8px;
}

.truck-select:focus {
  border-color: #42b883;
  outline: none;
  box-shadow: 0 0 0 2px rgba(66, 184, 131, 0.2);
}

.deliver-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.loading-indicator {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
  font-style: italic;
  display: block;
}

.truck-selection-container {
  display: flex;
  flex-direction: column;
}

.selected-truck-indicator {
  font-size: 13px;
  color: #42b883;
  font-weight: bold;
  margin-top: 4px;
}
</style>
