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
  }
}
