import axios from 'axios'
import AuthService from './AuthService'

export default {
  /**
   * Get all trucks from the API
   * @returns {Promise<Array>} Promise that resolves to an array of trucks
   */
  getAllTrucks() {
    console.log('Fetching all trucks')
    
    // Get token using AuthService
    const token = AuthService.getToken()
    
    // Check if we're authenticated
    if (!token) {
      console.error('Not authenticated, cannot fetch trucks')
      return Promise.reject(new Error('Authentication required'))
    }
    
    // Ensure token is set in axios default headers
    const authHeader = `Bearer ${token}`
    axios.defaults.headers.common['Authorization'] = authHeader
    
    return axios.get('/truck')
      .then(response => {
        console.log('Trucks fetched successfully:', response.data)
        return response.data
      })
      .catch(error => {
        console.error('Error fetching trucks:', error.response ? error.response.data : error.message)
        throw error
      })
  },
  
  /**
   * Update the truck of an order
   * @param {Number} orderId - The ID of the order to update
   * @param {Number} truckId - The ID of the truck to assign
   * @returns {Promise} Promise that resolves when the truck is updated
   */
  updateOrderTruck(orderId, truckId) {
    console.log(`Updating truck for order ${orderId} to truck ${truckId}`)
    
    // Get token using AuthService
    const token = AuthService.getToken()
    
    // Check if we're authenticated
    if (!token) {
      console.error('Not authenticated, cannot update truck')
      return Promise.reject(new Error('Authentication required'))
    }
    
    // Import OrderService dynamically to avoid circular dependency
    const OrderService = require('./OrderService').default
    
    // Use OrderService to update the truck
    return OrderService.assignTruckToOrder(orderId, truckId)
      .then(response => {
        console.log('Truck updated successfully:', response)
        return response
      })
      .catch(error => {
        console.error('Error updating truck:', error.response ? error.response.data : error.message)
        throw error
      })
  }
}
