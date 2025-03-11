<template>
  <div class="order-tables">
    <div class="tables-header">
      <h2>Orders Dashboard</h2>
      <button @click="fetchOrders" class="refresh-btn">
        <span class="refresh-icon">↻</span> Refresh
      </button>
    </div>

    <div v-if="isLoading" class="loading">
      <div class="spinner"></div>
      <p>Loading orders...</p>
    </div>

    <div v-else>
      <div class="table-section">
        <h3>Pending Orders</h3>
        <div v-if="pendingOrders.length === 0" class="empty-state">
          <p>No pending orders</p>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Date</th>
              <th>Items</th>
              <th>Total</th>
              <th>Actions</th>
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
                <button @click="markAsDelivered(order.id)" class="action-btn deliver-btn">
                  Mark Delivered
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="table-section">
        <h3>Delivered Orders</h3>
        <div v-if="deliveredOrders.length === 0" class="empty-state">
          <p>No delivered orders</p>
        </div>
        <table v-else>
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Customer</th>
              <th>Date</th>
              <th>Items</th>
              <th>Total</th>
              <th>Delivered At</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in deliveredOrders" :key="order.id">
              <td>#{{ order.id }}</td>
              <td>{{ order.customerName }}</td>
              <td>{{ formatDate(order.createdAt) }}</td>
              <td>{{ order.items.length }}</td>
              <td>${{ order.total.toFixed(2) }}</td>
              <td>{{ formatDate(order.deliveredAt) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { eventBus } from '../utils/eventBus'
import axios from 'axios'

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
    // Check if we have a token in localStorage
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('No authentication token found - please log in')
      return
    }

    console.log('Fetching orders with token:', token.substring(0, 15) + '...')

    // Fetch pending orders
    const pendingResponse = await axios.get('/orders/pending', {
      headers: {
        'Authorization': `Bearer ${token}` 
      }
    })
    
    console.log('Pending orders response:', pendingResponse.data)
    
    pendingOrders.value = pendingResponse.data.map(order => ({
      id: order.id,
      customerName: order.customer.name,
      createdAt: new Date(order.date),
      items: order.items,
      total: order.totalPrice
    }))
    
    // Fetch delivered orders
    const deliveredResponse = await axios.get('/orders/delivered', {
      headers: {
        'Authorization': `Bearer ${token}` 
      }
    })
    
    console.log('Delivered orders response:', deliveredResponse.data)
    
    deliveredOrders.value = deliveredResponse.data.map(order => ({
      id: order.id,
      customerName: order.customer.name,
      createdAt: new Date(order.date),
      items: order.items,
      deliveredAt: new Date(order.date), // Using same date since backend doesn't have delivery date
      total: order.totalPrice
    }))
    
    console.log('Orders refreshed successfully')
  } catch (error) {
    console.error('Error fetching orders:', error)
    if (error.response) {
      if (error.response.status === 401 || error.response.status === 403) {
        console.error('Authentication failed. Please log in again.')
        // Clear token and force re-login
        localStorage.removeItem('token')
      }
    }
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

const markAsDelivered = async (orderId) => {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('No authentication token found')
      return
    }
    
    await axios.post(`/orders/${orderId}/deliver`, {}, {
      headers: {
        'Authorization': `Bearer ${token}` 
      }
    })
    
    // After successful API call, update the local state
    const orderIndex = pendingOrders.value.findIndex(order => order.id === orderId)
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
</style>
