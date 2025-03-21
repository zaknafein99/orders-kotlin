import axios from 'axios'

// Create an Axios instance with custom config
const api = axios.create({
  baseURL: 'http://localhost:8080', // Backend API URL
  headers: {
    'Content-Type': 'application/json'
  }
})

// Add a request interceptor to include auth token
api.interceptors.request.use(
  (config) => {
    console.log('API Request:', config.method.toUpperCase(), config.url)
    // Don't add token for auth endpoints
    if (!config.url.startsWith('/auth')) {
      const token = localStorage.getItem('token')
      if (token) {
        console.log('Adding Authorization header with token')
        config.headers.Authorization = `Bearer ${token}`
      } else {
        console.warn('No token found in localStorage')
      }
    } else {
      console.log('Auth endpoint, not adding token')
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Add a response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized access
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
