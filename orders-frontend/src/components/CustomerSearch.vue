<template>
  <div class="customer-search card">
    <div v-if="!isAuthenticated" class="auth-section">
      <div class="card-header">
        <h3 class="card-title"><i class="fas fa-lock"></i> {{ translations.loginToContinue }}</h3>
      </div>
      
      <div class="form-group">
        <label for="email">{{ translations.email }}</label>
        <div class="input-with-icon">
          <i class="fas fa-envelope"></i>
          <input
            id="email"
            v-model="authData.email"
            type="email"
            :placeholder="translations.email"
          />
        </div>
      </div>
      
      <div class="form-group">
        <label for="password">{{ translations.password }}</label>
        <div class="input-with-icon">
          <i class="fas fa-key"></i>
          <input
            id="password"
            v-model="authData.password"
            type="password"
            :placeholder="translations.password"
          />
        </div>
      </div>
      
      <button @click="login" :disabled="isLoading" class="btn-primary btn-full">
        <i v-if="isLoading" class="fas fa-spinner fa-spin"></i>
        <i v-else class="fas fa-sign-in-alt"></i>
        {{ isLoading ? translations.loggingIn : translations.login }}
      </button>
      
      <div v-if="authError" class="alert alert-danger">
        <i class="fas fa-exclamation-circle"></i> {{ authError }}
      </div>
    </div>

    <div v-if="isAuthenticated" class="search-section">
      <div class="card-header">
        <h3 class="card-title"><i class="fas fa-search"></i> {{ translations.searchCustomer }}</h3>
      </div>
      
      <div class="form-group">
        <label for="phoneNumber">{{ translations.phoneNumber }}</label>
        <div class="input-with-icon">
          <i class="fas fa-phone"></i>
          <input
            id="phoneNumber"
            v-model="phoneNumber"
            type="text"
            :placeholder="translations.searchByPhoneNumber"
          />
        </div>
        <div class="search-button-container">
          <button 
            @click="searchCustomer" 
            :disabled="isSearching || !phoneNumber || phoneNumber.length < 3"
            class="btn-primary search-btn"
          >
            <i v-if="isSearching" class="fas fa-spinner fa-spin"></i>
            <i v-else class="fas fa-search"></i>
            {{ translations.search }}
          </button>
        </div>
      </div>
      
      <div v-if="isSearching" class="loading-state">
        <div class="spinner"></div>
        <p>{{ translations.searching }}</p>
      </div>
      
      <div v-if="searchError" class="alert alert-danger">
        <i class="fas fa-exclamation-circle"></i> {{ searchError }}
      </div>

      <div v-if="customer" class="customer-details card">
        <div class="customer-header">
          <h3><i class="fas fa-user"></i> {{ customer.name }}</h3>
          <span class="customer-badge" :class="customer.state === 'A' ? 'badge-active' : 'badge-inactive'">
            {{ customer.state === 'A' ? 'Activo' : 'Inactivo' }}
          </span>
        </div>
        
        <div class="customer-info">
          <div class="info-item">
            <i class="fas fa-phone"></i>
            <div>
              <span class="info-label">{{ translations.phoneNumber }}</span>
              <span class="info-value">{{ customer.phoneNumber }}</span>
            </div>
          </div>
          
          <div class="info-item">
            <i class="fas fa-map-marker-alt"></i>
            <div>
              <span class="info-label">{{ translations.address }}</span>
              <span class="info-value">{{ customer.address }}</span>
            </div>
          </div>
          
          <div class="info-item">
            <i class="fas fa-tag"></i>
            <div>
              <span class="info-label">Tipo</span>
              <span class="info-value">{{ customer.type === 'C' ? 'Cliente' : 'Proveedor' }}</span>
            </div>
          </div>
        </div>
        
        <button @click="openNewOrderModal" class="btn-secondary btn-full new-order-btn">
          <i class="fas fa-plus-circle"></i> {{ translations.newOrder }}
        </button>
      </div>
    </div>

    <!-- Using the new modular NewOrderModal component -->
    <NewOrderModal 
      :show="showNewOrderModal" 
      :customer="customer" 
      @close="closeNewOrderModal" 
      @order-created="handleOrderCreated" 
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'
import { eventBus } from '../utils/eventBus'
import NewOrderModal from './NewOrderModal.vue'
import AuthService from '../services/AuthService'
import { translations } from '../utils/translations'

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

// Function to open the order modal
const openNewOrderModal = () => {
  showNewOrderModal.value = true
}

// Function to close the order modal
const closeNewOrderModal = () => {
  showNewOrderModal.value = false
}

// Handle order created event from the modal
const handleOrderCreated = (order) => {
  console.log('Order created:', order)
  // Notify other components that an order was created
  eventBus.emit('order-created', order)
  // Also emit order-submitted event to refresh order tables
  eventBus.emit('order-submitted', order)
}

// Login function
const login = async () => {
  isLoading.value = true
  authError.value = ''
  
  try {
    console.log('Attempting login with:', authData)
    
    // Use AuthService for login
    const accessToken = await AuthService.login(authData.email, authData.password)
    
    token.value = accessToken
    isAuthenticated.value = true
    
    console.log('Authentication successful')
    
    // Refresh customer search if needed
    if (phoneNumber.value.length > 0) {
      await searchCustomer()
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
        authError.value = `${translations.loginFailed}: ${error.response.data?.message || error.message}`
      }
    } else {
      authError.value = `${translations.loginFailed}. ${translations.networkError}`
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
      console.log('Searching for customer with phone:', phoneNumber.value)
      
      // Make sure we're using the correct API endpoint with the /api prefix
      const response = await axios.get(
        `/api/customer/by-phone/${phoneNumber.value}?size=5&page=0`
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
          searchError.value = translations.authError
          isAuthenticated.value = false
          token.value = ''
          localStorage.removeItem('token')
        } else {
          searchError.value = `${translations.errorSearchingCustomer}: ${error.response.status}`
        }
      } else {
        searchError.value = `${translations.errorSearchingCustomer}: ${translations.networkError}`
      }
      customer.value = null
    } finally {
      isSearching.value = false
    }
  }

// Check for existing token on component mount
onMounted(() => {
  if (AuthService.isAuthenticated()) {
    token.value = AuthService.getToken()
    isAuthenticated.value = true
    console.log('User is already authenticated')
  }
})
</script>

<style scoped>
.customer-search {
  height: 100%;
  max-width: 100%;
  overflow-x: hidden;
}

.auth-section,
.search-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.btn-full {
  width: 100%;
}

.input-with-icon {
  position: relative;
}

.input-with-icon i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--gray-color);
}

.input-with-icon input {
  padding-left: 40px;
}

.customer-details {
  margin-top: 1.5rem;
  padding: 1rem;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid var(--primary-color);
  max-width: 100%;
  overflow-x: hidden;
  word-wrap: break-word;
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

.search-button-container {
  margin-top: 1rem;
  display: flex;
  justify-content: center;
  width: 100%;
}

.search-btn {
  white-space: nowrap;
  background-color: var(--primary-color);
  border: none;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  cursor: pointer;
  min-width: 80%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-weight: bold;
  font-size: 1rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
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
  position: relative;
  transition: all 0.2s ease;
}

.item:hover {
  border-color: #42b883;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.item-name {
  font-weight: bold;
  color: #333;
}

.item-price {
  color: #42b883;
  font-weight: bold;
}

.item-description {
  margin: 0.5rem 0;
  font-size: 0.85rem;
  color: #666;
  flex-grow: 1;
}

.modal-loading,
.modal-error,
.no-items {
  padding: 2rem;
  text-align: center;
  color: #666;
}

.modal-error {
  color: #dc3545;
}

.retry-btn {
  margin-top: 1rem;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.retry-btn:hover {
  background-color: #e9ecef;
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
