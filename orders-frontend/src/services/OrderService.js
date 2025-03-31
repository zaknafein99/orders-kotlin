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
    console.log('Fetching pending orders from API')
    const token = localStorage.getItem('token')
    console.log('Using token for orders request:', token ? `${token.substring(0, 15)}...` : 'No token')
    
    // Log the exact request we're making to compare with the curl command
    console.log('Making request to: /orders/pending with Authorization: Bearer ' + (token ? token.substring(0, 15) + '...' : 'No token'))
    
    return api.get('/orders/pending', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    .then(response => {
      console.log('Orders API response status:', response.status)
      console.log('Orders response data type:', typeof response.data)
      console.log('Orders response full data:', JSON.stringify(response.data, null, 2))
      
      let ordersData = [];
      
      if (response.data && response.data.content) {
        console.log('Orders content found, returning content array with length:', response.data.content.length)
        ordersData = response.data.content;
      } else if (Array.isArray(response.data)) {
        console.log('Response data is an array, returning directly with length:', response.data.length)
        ordersData = response.data;
      } else {
        console.log('Response data is not in expected format, attempting to parse')
        // Try to handle different response formats
        if (response.data && typeof response.data === 'object') {
          // If it's an object but not with a content property, it might be a single order or have a different structure
          const possibleArrays = Object.values(response.data).filter(val => Array.isArray(val))
          if (possibleArrays.length > 0) {
            console.log('Found array in response object, using first array with length:', possibleArrays[0].length)
            ordersData = possibleArrays[0];
          }
          
          // If it looks like a single order, wrap it in an array
          else if (response.data.id && response.data.customer) {
            console.log('Response appears to be a single order, wrapping in array')
            ordersData = [response.data];
          }
        }
      }
      
      // Filter out any orders that have status 'DELIVERED'
      const pendingOrders = ordersData.filter(order => {
        const isDelivered = order.status === 'DELIVERED';
        if (isDelivered) {
          console.log(`Order ID ${order.id} has status DELIVERED, filtering out from pending orders`);
        }
        return !isDelivered;
      });
      
      console.log(`Filtered out ${ordersData.length - pendingOrders.length} delivered orders from pending list`);
      return pendingOrders;
    })
    .catch(error => {
      console.error('Error fetching orders:', error)
      if (error.response) {
        console.error('Error response data:', error.response.data)
        console.error('Error response status:', error.response.status)
        console.error('Error response headers:', error.response.headers)
      } else if (error.request) {
        console.error('No response received:', error.request)
      } else {
        console.error('Error message:', error.message)
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
    console.log('Fetching delivered orders from API')
    const token = localStorage.getItem('token')
    console.log('Using token for delivered orders request:', token ? `${token.substring(0, 15)}...` : 'No token')
    
    // Use a query parameter to specifically request delivered orders
    return api.get('/orders?status=DELIVERED', {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    .then(response => {
      console.log('Delivered orders API response status:', response.status)
      console.log('Delivered orders response data type:', typeof response.data)
      console.log('Delivered orders response full data:', JSON.stringify(response.data, null, 2))
      
      if (response.data && response.data.content) {
        console.log('Delivered orders content found, returning content array with length:', response.data.content.length)
        return response.data.content
      } else if (Array.isArray(response.data)) {
        console.log('Delivered orders response data is an array, returning directly with length:', response.data.length)
        return response.data
      } else {
        console.log('Delivered orders response data is not in expected format, attempting to parse')
        // Try to handle different response formats
        if (response.data && typeof response.data === 'object') {
          // If it's an object but not with a content property, it might be a single order or have a different structure
          const possibleArrays = Object.values(response.data).filter(val => Array.isArray(val))
          if (possibleArrays.length > 0) {
            console.log('Found array in delivered orders response object, using first array with length:', possibleArrays[0].length)
            return possibleArrays[0]
          }
          
          // If it looks like a single order, wrap it in an array
          if (response.data.id && response.data.customer) {
            console.log('Delivered orders response appears to be a single order, wrapping in array')
            return [response.data]
          }
        }
        
        console.warn('Could not find delivered orders data in response, returning empty array')
        return []
      }
    })
    .catch(error => {
      console.error('Error fetching delivered orders:', error)
      if (error.response) {
        console.error('Error response data:', error.response.data)
        console.error('Error response status:', error.response.status)
        console.error('Error response headers:', error.response.headers)
      } else if (error.request) {
        console.error('No response received for delivered orders:', error.request)
      } else {
        console.error('Error message for delivered orders:', error.message)
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

    return api.get(`/item?page=${page}&size=${size}&order=id,asc`, {
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
    
    // Use the exact token without any modifications
    const authHeader = `Bearer ${token}`
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
    console.log('=== ORDER CREATION REQUEST ===');
    console.log('URL: /orders');
    console.log('Method: POST');
    console.log('Headers:', {
      'Authorization': authHeader.substring(0, 20) + '...',
      'Content-Type': 'application/json'
    });
    console.log('Request Body:', JSON.stringify(apiOrderData, null, 2));
    
    return api.post('/orders', apiOrderData, {
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
      console.log('=== ORDER CREATION RESPONSE ===');
      console.log('Status:', response.status);
      console.log('Headers:', response.headers);
      console.log('Data:', JSON.stringify(response.data, null, 2));
      
      const createdOrder = response.data
      console.log('Order created successfully with ID:', createdOrder.id);
      
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
    
    // Get the token for authorization
    const token = localStorage.getItem('token')
    if (!token) {
      console.error('No authentication token found')
      eventBus.emit('auth-error')
      return Promise.reject(new Error('Authentication required'))
    }
    
    console.log('Using token for deliver request:', token ? `${token.substring(0, 15)}...` : 'No token')
    
    // The backend endpoint doesn't accept a payload, it just needs the orderId in the URL
    // If we need to assign a truck, we should do that first in a separate call
    
    return api.post(`/orders/${orderId}/deliver`, {}, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    })
    .then(response => {
      console.log('Order marked as delivered successfully:', response.data)
      const updatedOrder = response.data
      // Refresh order tables with a slightly longer delay for delivery
      setTimeout(() => {
        console.log('Refreshing orders after delivery')
        this.refreshOrders(updatedOrder)
      }, 800) // Slightly longer delay for delivery operations
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
    console.log('Refreshing orders tables with new order ID:', order?.id)
    // Force a small delay to ensure the backend has processed the order
    setTimeout(() => {
      console.log('Emitting order-submitted event to refresh tables')
      eventBus.emit('order-submitted', order)
    }, 500) // 500ms delay to ensure backend processing is complete
  },
  
  /**
   * Handle authentication errors
   * @param {Error} error The error object from the API call
   */
  handleAuthError(error) {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      console.warn('Authentication error detected, attempting to handle')
      
      // Get the current token
      const token = localStorage.getItem('token')
      if (!token) {
        console.error('No token found in localStorage')
        eventBus.emit('auth-error')
        return
      }
      
      console.log('Current token:', token.substring(0, 15) + '...')
      console.log('Attempting to refresh authentication before logging out')
      
      // Try to refresh the token if the backend supports it
      // For now, we'll just clear the token and emit the auth error
      // This could be enhanced to use a refresh token if available
      localStorage.removeItem('token')
      eventBus.emit('auth-error')
    }
  }
}
