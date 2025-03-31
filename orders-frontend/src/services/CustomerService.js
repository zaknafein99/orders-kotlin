import axios from 'axios'

// Create axios instance with default config
const api = axios.create({
  baseURL: '' // Remove baseURL to use relative paths
})

// Add request interceptor to add auth token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
    console.log('Request URL:', config.url)
    console.log('Request headers:', config.headers)
    console.log('Request method:', config.method)
  } else {
    console.warn('No token found in localStorage')
  }
  return config
})

// Add response interceptor for debugging
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API Error:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      data: error.response?.data,
      headers: error.response?.headers,
      config: {
        url: error.config?.url,
        method: error.config?.method,
        headers: error.config?.headers
      }
    })
    return Promise.reject(error)
  }
)

class CustomerService {
  async getAllCustomers() {
    const response = await api.get('/customer')
    return response.data
  }

  async getCustomerById(id) {
    const response = await api.get(`/customer/${id}`)
    return response.data
  }

  async createCustomer(customerData) {
    // Transform the data to match the backend's expected format
    const transformedData = {
      name: customerData.name,
      address: customerData.address,
      phoneNumber: customerData.phone,
      type: customerData.type || 'E', // Default to 'E' if not provided
      state: customerData.state || 'A' // Default to 'A' if not provided
    }
    
    const response = await api.post('/customer', transformedData)
    return response.data
  }

  async updateCustomer(id, customerData) {
    const response = await api.put(`/customer/${id}`, customerData)
    return response.data
  }

  async deleteCustomer(id) {
    await api.delete(`/customer/${id}`)
  }

  async searchCustomer(phone) {
    try {
      console.log('Searching for customer with phone:', phone)
      const response = await api.get(`/customer/by-phone/${phone}`, {
        params: { 
          size: 5,
          page: 0
        }
      })
      console.log('Search response:', response.data)
      // Return the first customer if any are found
      return response.data?.content?.[0] || null
    } catch (error) {
      if (error.response?.status === 404) {
        return null
      }
      throw error
    }
  }
}

export default new CustomerService() 