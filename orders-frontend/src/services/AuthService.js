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
        
        // Store token in localStorage
        localStorage.setItem('token', accessToken)
        
        // Emit login success event
        eventBus.emit('login-success', accessToken)
        
        return accessToken
      })
  },

  /**
   * Logout the current user
   */
  logout() {
    localStorage.removeItem('token')
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
    return localStorage.getItem('token')
  },

  /**
   * Initialize authentication state
   * Checks if there's a token in localStorage and emits the appropriate event
   */
  initAuth() {
    const token = this.getToken()
    if (token) {
      console.log('Found existing token, emitting login-success event')
      eventBus.emit('login-success', token)
      return true
    }
    return false
  }
}
