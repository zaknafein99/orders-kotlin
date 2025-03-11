import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import axios from 'axios'

const app = createApp(App)

app.use(router)

// Set base URL for API requests
// This means all axios requests will be prefixed with '/api'
// The vite proxy will then rewrite these requests by removing the '/api' prefix
// before forwarding them to the backend at http://localhost:8080
axios.defaults.baseURL = '/api'

// Enable credentials for cross-origin requests
axios.defaults.withCredentials = true

// Configure axios
axios.defaults.headers.common['User-Agent'] = 'insomnia/8.4.5'
axios.defaults.headers.post['Content-Type'] = 'application/json'

// Add request interceptor for debugging
axios.interceptors.request.use(config => {
  console.log(`Making request: ${config.method?.toUpperCase()} ${config.url}`)
  return config
}, error => {
  console.error('Request error:', error)
  return Promise.reject(error)
})

// Add response interceptor for debugging
axios.interceptors.response.use(response => {
  console.log(`Response received: ${response.status} ${response.config.url}`)
  return response
}, error => {
  // Handle specific error codes for authentication
  if (error.response && (error.response.status === 401 || error.response.status === 403)) {
    console.error('Authentication error:', error.response.status, error.config?.url)
    // Clear token from localStorage
    localStorage.removeItem('token')
  }
  console.error(`Request failed: ${error.message}`, error.config?.url)
  return Promise.reject(error)
})

app.mount('#app')
