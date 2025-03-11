import axios from 'axios'
import { eventBus } from '../utils/eventBus'

export default {
  /**
   * Login with email and password
   * @param {String} email User email
   * @param {String} password User password
   * @returns {Promise} Promise with login result
   */
  login(email, password) {
    return axios.post('/auth', { email, password })
      .then(response => {
        const { accessToken } = response.data
        
        console.log(`Received token from server: ${accessToken.substring(0, 15)}...`)
        
        // Store token in localStorage exactly as received
        localStorage.setItem('token', accessToken)
        
        // Optionally store credentials for token refresh (encrypted in a real app)
        localStorage.setItem('userEmail', email)
        localStorage.setItem('userPassword', password)
        
        // Set the token in axios default headers - use exact token without modification
        const authHeader = `Bearer ${accessToken}`
        axios.defaults.headers.common['Authorization'] = authHeader
        console.log(`Set Authorization header: ${authHeader.substring(0, 20)}...`)
        
        // Emit login success event
        eventBus.emit('login-success', accessToken)
        
        return accessToken
      })
  },

  /**
   * Logout the current user
   */
  logout() {
    // Remove token and credentials from localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('userEmail')
    localStorage.removeItem('userPassword')
    
    // Clear authorization header
    delete axios.defaults.headers.common['Authorization']
    
    // Emit logout event
    eventBus.emit('logout')
  },

  /**
   * Check if user is authenticated
   * @returns {Boolean} True if authenticated
   */
  isAuthenticated() {
    return !!localStorage.getItem('token')
  },

  /**
   * Get the current authentication token
   * @returns {String|null} The token or null if not authenticated
   */
  getToken() {
    const token = localStorage.getItem('token')
    if (token) {
      console.log(`Retrieved token from storage: ${token.substring(0, 15)}...`)
    }
    return token
  },

  /**
   * Initialize authentication state
   * Checks if there's a token in localStorage and emits the appropriate event
   */
  initAuth() {
    const token = this.getToken()
    if (token) {
      console.log('Found existing token, emitting login-success event')
      
      // Set the token in axios default headers
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      
      // Verify token validity by making a test request
      this.verifyToken(token)
        .then(isValid => {
          if (isValid) {
            eventBus.emit('login-success', token)
          } else {
            console.log('Token is invalid, removing')
            this.logout()
          }
        })
        .catch(() => {
          console.log('Token verification failed, removing')
          this.logout()
        })
      
      return true
    }
    return false
  },
  
  /**
   * Verify if the token is still valid
   * @param {String} token The token to verify
   * @returns {Promise<boolean>} Promise that resolves to true if token is valid
   */
  verifyToken(token) {
    // Make a simple request to check if the token is still valid
    return axios.get('/customer/list?size=1', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    .then(() => true)
    .catch(error => {
      console.error('Token verification failed:', error.response ? error.response.status : error)
      return false
    })
  },
  
  /**
   * Refresh the authentication token
   * @returns {Promise<string|null>} Promise that resolves to the new token or null if refresh failed
   */
  refreshToken() {
    console.log('Attempting to refresh token')
    
    // Get the current user credentials from localStorage if available
    const userEmail = localStorage.getItem('userEmail')
    const userPassword = localStorage.getItem('userPassword')
    
    if (userEmail && userPassword) {
      console.log(`Found stored credentials for ${userEmail}, attempting to refresh token`)
      
      // Re-authenticate with stored credentials
      return this.login(userEmail, userPassword)
        .then(newToken => {
          console.log('Token refreshed successfully')
          return newToken
        })
        .catch(error => {
          console.error('Failed to refresh token:', error)
          return null
        })
    } else {
      console.error('No stored credentials found, cannot refresh token')
      return Promise.resolve(null)
    }
  }
}
