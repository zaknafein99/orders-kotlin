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
      
      <button @click="login" :disabled="isLoading" class="btn-primary btn-full" style="background-color: #e62222; color: white;">
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
            style="background-color: #e62222; color: white;"
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
      
      // The proxy is configured to rewrite /api to the root path
      // We should NOT include /api twice in the URL
      const response = await axios.get(
        `/customer/by-phone/${phoneNumber.value}?size=5&page=0`
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
  padding: 1rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.auth-section,
.search-section {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.card-header {
  margin-bottom: 0.5rem;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.card-title i {
  color: #e62222;
  font-size: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: #4b5563;
}

.input-with-icon {
  position: relative;
  width: 100%;
}

.input-with-icon i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 0.875rem;
}

.input-with-icon input {
  width: 100%;
  padding: 0.625rem 0.75rem 0.625rem 2.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 0.875rem;
  line-height: 1.25rem;
  color: #1f2937;
  background-color: #fff;
  transition: all 0.2s;
}

.input-with-icon input:focus {
  outline: none;
  border-color: #e62222;
  box-shadow: 0 0 0 3px rgba(230, 34, 34, 0.1);
}

.input-with-icon input::placeholder {
  color: #9ca3af;
}

.search-button-container {
  margin-top: 0.75rem;
}

.search-btn {
  width: 100%;
  padding: 0.625rem;
  background-color: #e62222;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 500;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-btn:hover:not(:disabled) {
  background-color: #d41d1d;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.search-btn:disabled {
  background-color: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
}

.search-btn i {
  font-size: 0.875rem;
}

.alert {
  padding: 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.alert-danger {
  background-color: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.alert i {
  font-size: 1rem;
}

.customer-details {
  margin-top: 1rem;
  padding: 1.25rem;
  background-color: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  border-left: 4px solid #e62222;
}

.customer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.customer-header h3 {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.customer-badge {
  padding: 0.25rem 0.5rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.badge-active {
  background-color: #dcfce7;
  color: #166534;
}

.badge-inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.customer-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.info-item i {
  color: #64748b;
  font-size: 0.875rem;
  margin-top: 0.125rem;
}

.info-item div {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.info-label {
  font-size: 0.75rem;
  color: #64748b;
}

.info-value {
  font-size: 0.875rem;
  color: #1e293b;
}

.new-order-btn {
  margin-top: 1.25rem;
  width: 100%;
  padding: 0.75rem;
  background-color: #e62222;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 500;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.new-order-btn:hover {
  background-color: #d41d1d;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1.5rem;
  color: #64748b;
}

.spinner {
  width: 1.5rem;
  height: 1.5rem;
  border: 2px solid #e2e8f0;
  border-top-color: #e62222;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
