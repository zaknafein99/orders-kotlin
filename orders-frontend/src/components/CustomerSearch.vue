<template>
  <div class="customer-search">
    <div v-if="!isAuthenticated" class="auth-section">
      <h3>Login to continue</h3>
      <input
        v-model="authData.email"
        type="email"
        placeholder="Email"
      />
      <input
        v-model="authData.password"
        type="password"
        placeholder="Password"
      />
      <button @click="login" :disabled="isLoading">{{ isLoading ? 'Logging in...' : 'Login' }}</button>
      <p v-if="authError" class="error">{{ authError }}</p>
    </div>

    <div v-if="isAuthenticated" class="search-section">
      <h3>Search Customer</h3>
      <div class="search-input-group">
        <input
          v-model="phoneNumber"
          type="text"
          placeholder="Search by phone number"
        />
        <button @click="searchCustomer" :disabled="isSearching || !phoneNumber || phoneNumber.length < 3">Search</button>
      </div>
      <div v-if="isSearching" class="loading-indicator">Searching...</div>
      <div v-if="searchError" class="error">{{ searchError }}</div>

      <div v-if="customer" class="customer-details">
        <h3>{{ customer.name }}</h3>
        <p><strong>Phone:</strong> {{ customer.phoneNumber }}</p>
        <p><strong>Address:</strong> {{ customer.address }}</p>
        <p><strong>Type:</strong> {{ customer.type === 'C' ? 'Customer' : 'Vendor' }}</p>
        <p><strong>Status:</strong> {{ customer.state === 'A' ? 'Active' : 'Inactive' }}</p>
        <button @click="showNewOrderModal = true" class="new-order-btn">New Order</button>
      </div>
    </div>

    <!-- New Order Modal -->
    <div v-if="showNewOrderModal" class="modal">
      <div class="modal-content">
        <span class="close" @click="showNewOrderModal = false">&times;</span>
        <h2>Create New Order</h2>
        <div class="items-list">
          <div v-for="(item, index) in availableItems" :key="index" class="item">
            <div class="item-details">
              <span>{{ item.name }}</span>
              <span>${{ item.price.toFixed(2) }}</span>
            </div>
            <div class="item-actions">
              <button @click="addToOrder(item)">Add to Order</button>
            </div>
          </div>
        </div>
        
        <div v-if="currentOrder.items.length > 0" class="current-order">
          <h3>Current Order</h3>
          <div v-for="(item, index) in currentOrder.items" :key="index" class="order-item">
            <span>{{ item.name }}</span>
            <span>${{ item.price.toFixed(2) }}</span>
            <button @click="removeFromOrder(index)" class="remove-btn">Remove</button>
          </div>
          <div class="order-total">
            <strong>Total: ${{ calculateTotal().toFixed(2) }}</strong>
          </div>
          <button @click="submitOrder" class="submit-btn">Submit Order</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import axios from 'axios'
import { eventBus } from '../utils/eventBus'

// Authentication
const isAuthenticated = ref(false)
const authData = reactive({
  email: 'admin@gmail.com',
  password: 'password'
})
const authError = ref('')
const isLoading = ref(false)
const isSearching = ref(false)
const token = ref('')

// Customer search
const phoneNumber = ref('')
const customer = ref(null)
const searchError = ref('')

// Order management
const showNewOrderModal = ref(false)
const availableItems = ref([
  { id: 1, name: 'Pizza', price: 12.99 },
  { id: 2, name: 'Burger', price: 8.99 },
  { id: 3, name: 'Salad', price: 6.99 },
  { id: 4, name: 'Pasta', price: 10.99 },
  { id: 5, name: 'Soda', price: 2.49 },
  { id: 6, name: 'Fries', price: 3.99 },
])
const currentOrder = reactive({
  items: []
})

// Login function
const login = async () => {
  isLoading.value = true
  authError.value = ''
  
  try {
    console.log('Attempting login with:', authData)
    const response = await axios.post('/auth', {
      email: authData.email,
      password: authData.password
    })
    
    console.log('Login response:', response.data)
    
    // The response contains accessToken and refreshToken
    if (response.data && response.data.accessToken) {
      token.value = response.data.accessToken
      isAuthenticated.value = true
      
      // Store token in localStorage
      localStorage.setItem('token', token.value)
      
      console.log('Authentication successful, token stored in localStorage')
      
      // Notify other components that login is successful
      eventBus.emit('login-success', token.value)
      
      // Refresh customer search if needed
      if (phoneNumber.value.length > 0) {
        await searchCustomer()
      }
    } else {
      console.error('Unexpected authentication response format:', response.data)
      authError.value = 'Login failed: Unexpected server response'
    }
  } catch (error) {
    console.error('Authentication error:', error)
    
    // Provide more specific error messages based on the response
    if (error.response) {
      if (error.response.status === 401) {
        authError.value = 'Invalid username or password.'
      } else if (error.response.status === 403) {
        authError.value = 'You are not authorized to access this system.'
      } else {
        authError.value = `Login failed: ${error.response.data?.message || error.message}`
      }
    } else {
      authError.value = 'Login failed. Please check your network connection.'
    }
  } finally {
    isLoading.value = false
  }
}

// No longer using debounced search - now using a button click

// Search customer by phone number
const searchCustomer = async () => {
  // Don't proceed if phone number is empty or too short
  if (!phoneNumber.value || phoneNumber.value.length < 3) {
    searchError.value = 'Please enter at least 3 digits of the phone number';
    return
  }
  
  // Clear any previous search errors
  searchError.value = ''
  // Clear previous customer data
  customer.value = null
  
  isSearching.value = true
  searchError.value = ''
  try {
      // Use the token from localStorage (which is set during login) instead of the ref value
      const storedToken = localStorage.getItem('token')
      
      // Check if we're authenticated
      if (!storedToken) {
        searchError.value = 'Please log in first'
        return
      }
      
      console.log('Searching for customer with phone:', phoneNumber.value)
      console.log('Using token from localStorage:', storedToken.substring(0, 15) + '...')
      
      // Notice: axios.defaults.baseURL is already set to '/api' in main.js
      // So this will be sent as /api/customer/by-phone/... and vite proxy will rewrite to /customer/by-phone/...
      const response = await axios.get(
        `/customer/by-phone/${phoneNumber.value}?size=5&page=0`,
        {
          headers: {
            'Authorization': `Bearer ${storedToken}`
          }
        }
      )
      
      console.log('Customer search response:', response.data)
      
      // Check if we received a direct customer object (has id property)
      if (response.data && response.data.id) {
        customer.value = response.data
        console.log('Customer found:', customer.value)
      }
      // Check if we received a paginated response with content array
      else if (response.data && response.data.content && response.data.content.length > 0) {
        customer.value = response.data.content[0]
        console.log('Customer found from paginated response:', customer.value)
      } else {
        customer.value = null
        searchError.value = 'No customer found with that phone number'
      }
    } catch (error) {
      console.error('Error fetching customer:', error)
      customer.value = null
      if (error.response) {
        console.error('Response status:', error.response.status)
        console.error('Response data:', error.response.data)
        
        if (error.response.status === 401 || error.response.status === 403) {
          searchError.value = 'Authentication failed. Please log in again.'
          isAuthenticated.value = false
          token.value = ''
          localStorage.removeItem('token')
        } else {
          searchError.value = `Error searching for customer: ${error.response.status}`
        }
      } else {
        searchError.value = 'Error searching for customer: Network error'
      }
      customer.value = null
    } finally {
      isSearching.value = false
    }
  }


// Order functions
const addToOrder = (item) => {
  currentOrder.items.push({...item})
}

const removeFromOrder = (index) => {
  currentOrder.items.splice(index, 1)
}

const calculateTotal = () => {
  return currentOrder.items.reduce((total, item) => total + item.price, 0)
}

const submitOrder = async () => {
  if (!customer.value || currentOrder.items.length === 0) return
  
  try {
    // Get token from localStorage
    const storedToken = localStorage.getItem('token')
    if (!storedToken) {
      alert('Please log in to submit orders')
      return
    }
    
    // Prepare order data
    const orderData = {
      customerId: customer.value.id,
      items: currentOrder.items.map(item => ({
        itemId: item.id,
        quantity: 1, // Default quantity
        price: item.price
      }))
    }
    
    console.log('Submitting order:', orderData)
    
    // Make API call
    const response = await axios.post('/orders', orderData, {
      headers: {
        'Authorization': `Bearer ${storedToken}`
      }
    })
    
    console.log('Order submission response:', response.data)
    
    // Handle success
    alert(`Order for ${customer.value.name} submitted successfully!`)
    currentOrder.items = []
    showNewOrderModal.value = false
    
    // Refresh orders list through event bus
    eventBus.emit('order-submitted')
  } catch (error) {
    console.error('Error submitting order:', error)
    alert(`Failed to submit order: ${error.response?.data?.message || error.message}`)
  }
}
</script>

<style scoped>
.customer-search {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.auth-section,
.search-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.customer-details {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #42b883;
}

.new-order-btn {
  margin-top: 1rem;
  width: 100%;
}

.error {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.loading-indicator {
  color: #666;
  font-size: 0.9em;
  margin-bottom: 0.5rem;
  font-style: italic;
}

.search-input-group {
  display: flex;
  gap: 0.5rem;
}

.search-input-group input {
  flex-grow: 1;
}

.search-input-group button {
  white-space: nowrap;
  background-color: #42b883;
  border: none;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.search-input-group button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

/* Modal styles */
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
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
}

.close {
  position: absolute;
  top: 1rem;
  right: 1.5rem;
  font-size: 1.5rem;
  cursor: pointer;
}

.items-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  margin: 1.5rem 0;
}

.item {
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.item-details {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.current-order {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
  border-bottom: 1px solid #f5f5f5;
}

.remove-btn {
  background-color: #dc3545;
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
}

.order-total {
  margin: 1rem 0;
  text-align: right;
  font-size: 1.1rem;
}

.submit-btn {
  width: 100%;
  padding: 0.75rem;
  background-color: #28a745;
}
</style>
