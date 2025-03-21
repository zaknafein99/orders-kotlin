import { eventBus } from '../utils/eventBus'
import AuthService from './AuthService'
import { calculateOrderTotal } from '../utils/orderUtils'
import api from './api'

/**
 * Service for handling order-related API calls and operations
 */
export default {
  /**
   * Fetch all pending orders
   * @returns {Promise} Promise with pending orders data
   */
  getPendingOrders() {
    return api.get('/orders')
    .then(response => {
      console.log('Orders API response:', response)
      console.log('Orders response data:', response.data)
      if (response.data && response.data.content) {
        console.log('Orders content found, returning content array')
        return response.data.content
      } else {
        console.log('No content field found in response, returning response data or empty array')
        return Array.isArray(response.data) ? response.data : []
      }
    })
    .catch(error => {
      console.error('Error fetching orders:', error)
      if (error.response) {
        console.error('Error response data:', error.response.data)
        console.error('Error response status:', error.response.status)
      }
      this.handleAuthError(error)
      throw error
    })
  },

  /**
   * Fetch all delivered orders
   * @returns {Promise} Promise with delivered orders data
   */
  getDeliveredOrders() {
    return api.get('/orders/delivered')
    .then(response => {
      console.log('Delivered orders API response:', response)
      console.log('Delivered orders response data:', response.data)
      if (response.data && response.data.content) {
        console.log('Delivered orders content found, returning content array')
        return response.data.content
      } else {
        console.log('No content field found in delivered response, returning response data or empty array')
        return Array.isArray(response.data) ? response.data : []
      }
    })
    .catch(error => {
      console.error('Error fetching delivered orders:', error)
      if (error.response) {
        console.error('Error response data:', error.response.data)
        console.error('Error response status:', error.response.status)
      }
      this.handleAuthError(error)
      throw error
    })
  },

  /**
   * Fetch available items for ordering
   * @param {Number} page Page number (0-based)
   * @param {Number} size Items per page
   * @returns {Promise} Promise with items data
   */
  getItems(page = 0, size = 10) {
    const token = localStorage.getItem('token')
    if (!token) {
      eventBus.emit('auth-error')
      return Promise.reject(new Error('Authentication required'))
    }

    return api.get(`/item/list?page=${page}&size=${size}&order=id,asc`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(response => response.data)
    .catch(error => {
      this.handleAuthError(error)
      throw error
    })
  },

  /**
   * Create a new order
   * @param {Object} orderData Order data object
   * @returns {Promise} Promise with created order data
   */
  createOrder(orderData) {
    // Refresh the token to ensure it's valid
    const token = AuthService.getToken()
    if (!token) {
      console.error('No authentication token found')
      eventBus.emit('auth-error')
      return Promise.reject(new Error('Authentication required'))
    }

    // Log the order data being sent to the API
    console.log('Sending order data to API:', JSON.stringify(orderData, null, 2))
    console.log('Using token:', token.substring(0, 15) + '...')
    
    // Make sure axios has the token in its default headers
    // Use the exact token without any modifications
    const authHeader = `Bearer ${token}`
    axios.defaults.headers.common['Authorization'] = authHeader
    console.log(`Authorization header set: ${authHeader.substring(0, 20)}...`)

    // Create a complete version of the order data that matches exactly what the backend expects
    const apiOrderData = {
      id: null,
      customer: {
        id: orderData.customer.id,
        name: orderData.customer.name,
        address: orderData.customer.address || '',
        phoneNumber: orderData.customer.phoneNumber || '',
        type: orderData.customer.type || 'E',
        state: orderData.customer.state || 'A'
      },
      items: orderData.items.map(item => ({
        id: item.id,
        name: item.name,
        description: item.description || '',
        price: Number(item.price),
        quantity: item.quantity,
        category: item.category || 'Uncategorized'
      })),
      truck: orderData.truck ? {
        id: orderData.truck.id,
        name: orderData.truck.name
      } : null,
      
      // Ensure truck is properly set
      truckId: orderData.truck?.id,
      date: orderData.date,
      totalPrice: typeof orderData.totalPrice === 'number' ? orderData.totalPrice : calculateOrderTotal(orderData.items),
      status: "PENDING"
    }

    console.log('Simplified order data for API:', JSON.stringify(apiOrderData, null, 2))

    // Make sure the data is properly formatted as a JSON string
    return axios.post('/orders', apiOrderData, {
      headers: {
        'Authorization': authHeader,
        'Content-Type': 'application/json'
      },
      // Add timeout and additional debugging
      timeout: 10000,
      validateStatus: (status) => {
        console.log(`Response status: ${status}`)
        return status >= 200 && status < 300
      }
    })
    .then(response => {
      const createdOrder = response.data
      console.log('Order created successfully:', createdOrder)
      // Refresh order tables
      this.refreshOrders(createdOrder)
      return createdOrder
    })
    .catch(error => {
      console.error('Order creation error details:', error.response ? error.response.data : error)
      console.error('Full error object:', error)
      if (error.request) {
        console.error('Request data sent:', error.config.data)
      }
      
      // Special handling for order creation errors
      if (error.response && error.response.status === 403) {
        console.error('Authorization error during order creation. Will attempt to refresh token.')
        
        // Try to refresh the token instead of logging out immediately
        return AuthService.refreshToken()
          .then(newToken => {
            if (newToken) {
              console.log('Token refreshed successfully, retrying order creation')
              return this.createOrder(orderData)
            } else {
              console.error('Token refresh failed, emitting auth-error')
              eventBus.emit('auth-error')
              throw error
            }
          })
          .catch(() => {
            console.error('Token refresh failed with error, emitting auth-error')
            eventBus.emit('auth-error')
            throw error
          })
      } else {
        this.handleAuthError(error)
        throw error
      }
    })
  },

  /**
   * Update an order's status
   * @param {Number} orderId Order ID
   * @param {String} status New status
   * @returns {Promise} Promise with updated order data
   */
  updateOrderStatus(orderId, status) {
    const token = AuthService.getToken()
    if (!token) {
      eventBus.emit('auth-error')
      return Promise.reject(new Error('Authentication required'))
    }

    return axios.put(`/orders/${orderId}/status`, { status }, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    .then(response => {
      const updatedOrder = response.data
      // Refresh order tables
      this.refreshOrders(updatedOrder)
      return updatedOrder
    })
    .catch(error => {
      this.handleAuthError(error)
      throw error
    })
  },

  /**
   * Mark an order as delivered
   * @param {Number} orderId Order ID
   * @returns {Promise} Promise with updated order data
   */
  /**
   * Assign a truck to an order
   * @param {Number} orderId Order ID
   * @param {Number} truckId Truck ID
   * @returns {Promise} Promise with updated order data
   */
  assignTruckToOrder(orderId, truckId) {
    console.log(`Assigning truck ${truckId} to order ${orderId}`)
    
    // Create a payload with the truck ID
    const payload = { 
      truckId: parseInt(truckId)
    }
    
    console.log('Updating truck with payload:', payload)
    
    // Use a POST request to update the order with the new truck
    return api.post(`/orders/${orderId}/truck`, payload)
      .then(response => {
        console.log('Truck updated successfully:', response.data)
        const updatedOrder = response.data
        // Refresh order tables
        this.refreshOrders(updatedOrder)
        return updatedOrder
      })
      .catch(error => {
        console.error('Error updating truck:', error.response ? error.response.data : error.message)
        this.handleAuthError(error)
        throw error
      })
  },

  /**
   * Mark an order as delivered
   * @param {Number} orderId Order ID
   * @param {Number} truckId Optional truck ID if not already assigned
   * @returns {Promise} Promise with updated order data
   */
  markOrderAsDelivered(orderId, truckId = null) {
    console.log(`Marking order ${orderId} as delivered${truckId ? ` with truck ${truckId}` : ''}`)
    
    // The backend endpoint doesn't accept a payload, it just needs the orderId in the URL
    // If we need to assign a truck, we should do that first in a separate call
    
    return api.post(`/orders/${orderId}/deliver`)
      .then(response => {
        console.log('Order marked as delivered successfully:', response.data)
        const updatedOrder = response.data
        // Refresh order tables
        this.refreshOrders(updatedOrder)
        return updatedOrder
      })
      .catch(error => {
        console.error('Error marking order as delivered:', error.response ? error.response.data : error.message)
        this.handleAuthError(error)
        throw error
      })
  },

  /**
   * Handle API errors including authentication errors
   * @param {Error} error Error object
   */
  handleAuthError(error) {
    if (error.response) {
      const status = error.response.status
      console.error(`API Error: ${status} - ${error.response.statusText}`)
      
      // Log detailed error information if available
      if (error.response.data) {
        console.error('Error details:', error.response.data)
        
        // Log specific error message if available
        if (error.response.data.message) {
          console.error('Error message:', error.response.data.message)
        }
      }
      
      if (status === 401 || status === 403) {
        console.error('Authentication error detected')
        
        // Try to refresh the token first instead of immediately logging out
        AuthService.refreshToken()
          .then(newToken => {
            if (newToken) {
              console.log('Token refreshed successfully')
              // No need to emit auth-error, we've recovered
            } else {
              console.error('Token refresh failed, logging out')
              AuthService.logout()
              eventBus.emit('auth-error')
            }
          })
          .catch(() => {
            console.error('Token refresh failed with error, logging out')
            AuthService.logout()
            eventBus.emit('auth-error')
          })
      } else if (status === 400) {
        // Bad request - log more details
        console.error('Bad request error. Request data:', error.config ? error.config.data : 'No data available')
        
        // Parse the request data to see what might be missing
        try {
          const requestData = JSON.parse(error.config.data)
          console.error('Parsed request data:', requestData)
          
          // Check for missing required fields
          const requiredFields = ['customer', 'items', 'date', 'status']
          const missingFields = requiredFields.filter(field => !requestData[field])
          
          if (missingFields.length > 0) {
            console.error('Missing required fields:', missingFields.join(', '))
          }
        } catch (e) {
          console.error('Error parsing request data:', e)
        }
        
        eventBus.emit('api-error', { message: 'Invalid order data format', details: error.response.data })
      }
    } else if (error.request) {
      // The request was made but no response was received
      console.error('No response received from server', error.request)
      eventBus.emit('api-error', { message: 'No response from server' })
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('Error setting up request:', error.message)
      eventBus.emit('api-error', { message: 'Error setting up request' })
    }
  },
  
  /**
   * Trigger a refresh of the orders tables
   * @param {Object} order Optional order object that was just created or updated
   */
  refreshOrders(order = null) {
    console.log('Refreshing orders tables')
    eventBus.emit('order-submitted', order)
  }
}
