<template>
  <div class="order-summary-component">
    <h3>Current Order</h3>
    
    <!-- Empty order state -->
    <div v-if="items.length === 0" class="empty-order">
      <p>Your order is empty</p>
      <p class="instruction">Add items from the list on the left</p>
    </div>
    
    <!-- Order items -->
    <div v-else class="order-content">
      <!-- Truck selection -->
      <div class="truck-selection">
        <label for="truck-select">Select Truck:</label>
        <select id="truck-select" v-model="selectedTruck" @change="updateTruck">
          <option v-for="truck in trucks" :key="truck.id" :value="truck">
            {{ truck.name }}
          </option>
        </select>
      </div>
      
      <!-- Order date -->
      <div class="order-date">
        <label for="order-date">Order Date:</label>
        <input 
          type="date" 
          id="order-date" 
          v-model="orderDate" 
          :min="todayFormatted" 
          @change="updateDate"
        />
      </div>
      
      <!-- Order items -->
      <div class="order-items">
        <div v-for="(item, index) in items" :key="index" class="order-item">
          <div class="order-item-details">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-quantity">x{{ item.quantity }}</span>
            <span class="item-price">{{ formatPrice(Number(item.price) * item.quantity) }}</span>
          </div>
          <button @click="removeItem(index)" class="remove-btn">Remove</button>
        </div>
      </div>
      
      <!-- Order summary -->
      <div class="order-total-section">
        <div class="order-total">
          <strong>Total:</strong> {{ formatPrice(calculateOrderTotal(items)) }}
        </div>
        <button 
          @click="submitOrder" 
          :disabled="isSubmitting || items.length === 0 || !selectedTruck" 
          class="submit-btn"
        >
          {{ isSubmitting ? 'Submitting...' : 'Submit Order' }}
        </button>
        <div v-if="error" class="order-error">
          {{ error }}
        </div>
        <div v-if="successMessage" class="order-success">
          {{ successMessage }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, defineProps, defineEmits } from 'vue'
import { calculateOrderTotal, formatPrice, createOrderObject, validateOrder } from '../utils/orderUtils'

const props = defineProps({
  items: {
    type: Array,
    required: true
  },
  customer: {
    type: Object,
    required: true
  },
  trucks: {
    type: Array,
    default: () => [
      { id: 1, name: 'truck1' },
      { id: 2, name: 'truck2' }
    ]
  },
  isSubmitting: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  },
  successMessage: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['remove-item', 'submit', 'update-truck', 'update-date'])

// State
const selectedTruck = ref(props.trucks[0])
const orderDate = ref(new Date().toISOString().split('T')[0])

// Computed properties
const todayFormatted = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// Methods

const removeItem = (index) => {
  emit('remove-item', index)
}

const submitOrder = () => {
  if (props.items.length === 0) return
  
  const orderData = createOrderObject(
    props.customer,
    props.items,
    selectedTruck.value,
    orderDate.value
  )
  
  // Validate order before submitting
  const validation = validateOrder(orderData)
  if (!validation.isValid) {
    console.error('Order validation failed:', validation.errors)
    return
  }
  
  emit('submit', orderData)
}

const updateTruck = () => {
  emit('update-truck', selectedTruck.value)
}

const updateDate = () => {
  emit('update-date', orderDate.value)
}
</script>

<style scoped>
.order-summary-component {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.empty-order {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #666;
  background-color: #f5f5f5;
  border-radius: 8px;
  flex-grow: 1;
}

.instruction {
  font-size: 14px;
  margin-top: 10px;
  color: #999;
}

.order-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.truck-selection, .order-date {
  margin-bottom: 15px;
}

.truck-selection label, .order-date label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

select, input[type="date"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.order-items {
  margin-top: 20px;
  margin-bottom: 20px;
  max-height: 200px;
  overflow-y: auto;
  flex-grow: 1;
}

.order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.order-item-details {
  display: flex;
  gap: 10px;
  align-items: center;
}

.item-name {
  font-weight: bold;
}

.item-quantity {
  background-color: #e3f2fd;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.item-price {
  color: #2e7d32;
  font-weight: bold;
}

.remove-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.order-total-section {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.order-total {
  font-size: 18px;
  margin-bottom: 15px;
}

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
</style>
