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
                <select 
                  v-if="!order.truck" 
                  v-model="orderTrucks[order.id]" 
                  class="truck-select"
                  @change="onTruckSelected(order.id, orderTrucks[order.id])"
                >
                  <option value="">{{ translations.selectTruck }}</option>
                  <option v-for="truck in availableTrucks" :key="truck.id" :value="truck.id">{{ truck.name }}</option>
                </select>
                <span v-else>{{ order.truck.name }}</span>
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
              <td>{{ order.truck ? order.truck.name : translations.noTruck }}</td>
              <td>{{ formatDate(order.deliveredAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { eventBus } from '../utils/eventBus'
import OrderService from '../services/OrderService'
import { translations } from '../utils/translations'

// Track truck assignments for orders that don't have one yet
const orderTrucks = reactive({})

// Available trucks for selection
const availableTrucks = ref([
  { id: 1, name: 'Camión 1' },
  { id: 2, name: 'Camión 2' },
  { id: 3, name: 'Camión 3' }
])

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

const fetchOrders = async () => {
  isLoading.value = true
  try {
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

const onTruckSelected = async (orderId, truckId) => {
  if (!truckId) return
  
  try {
    console.log('Truck selected for order:', orderId, 'truck:', truckId)
    await assignTruckToOrder(orderId, truckId)
  } catch (error) {
    console.error('Error handling truck selection:', error)
  }
}

const assignTruckToOrder = async (orderId, truckId) => {
  try {
    console.log('Assigning truck to order:', orderId, 'truck:', truckId)
    
    // Call the API to assign the truck
    await OrderService.assignTruckToOrder(orderId, truckId)
    
    // Update the local state
    const orderIndex = pendingOrders.value.findIndex(order => order.id === orderId)
    if (orderIndex !== -1) {
      const selectedTruck = availableTrucks.value.find(truck => truck.id === truckId)
      pendingOrders.value[orderIndex].truck = selectedTruck
    }
  } catch (error) {
    console.error('Error assigning truck to order:', error)
  }
}

const markAsDelivered = async (orderId) => {
  try {
    console.log('Marking order as delivered:', orderId)
    
    // If the order doesn't have a truck assigned yet, assign it first
    const orderIndex = pendingOrders.value.findIndex(order => order.id === orderId)
    if (orderIndex !== -1) {
      const order = pendingOrders.value[orderIndex]
      if (!order.truck && orderTrucks[orderId]) {
        const truckId = orderTrucks[orderId]
        // First assign the truck to the order
        await assignTruckToOrder(orderId, truckId)
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
</style>
