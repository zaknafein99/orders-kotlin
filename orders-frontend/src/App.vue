<script setup>
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import AuthService from './services/AuthService'

// Log that App.vue is being rendered
console.log('App.vue is being rendered')

onMounted(() => {
  // Initialize authentication state when the app starts
  console.log('App.vue onMounted hook triggered')
  try {
    AuthService.initAuth()
    console.log('AuthService.initAuth() completed successfully')
  } catch (error) {
    console.error('Error in AuthService.initAuth():', error)
  }
})

const route = useRoute()

// Determine if we should show the layout (header/footer) based on the route
const showLayout = computed(() => {
  return route.name !== 'login'
})
</script>

<template>
  
  
  <!-- Login page doesn't use the layout -->
  <router-view v-if="!showLayout" />
  
  <!-- Main layout for authenticated pages -->
  <div v-else class="min-h-screen flex flex-col bg-slate-50">
    <!-- Header -->
    <header class="bg-primary-600 shadow-md sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <!-- Logo and Navigation -->
          <div class="flex">
            <div class="flex-shrink-0 flex items-center">
              <router-link to="/" class="flex items-center">
                <img src="./assets/acodike-logo.svg" alt="Acodike Supergas" class="h-10 w-auto mr-2" />
                <span class="text-xl font-semibold text-white">Sistema de Pedidos</span>
              </router-link>
            </div>
            
            <!-- Desktop Navigation -->
            <nav class="hidden md:ml-8 md:flex md:space-x-6">
              <router-link 
                to="/dashboard" 
                class="inline-flex items-center px-1 pt-1 text-sm font-medium text-white hover:text-secondary-400 border-b-2 border-transparent hover:border-secondary-500 transition-colors"
              >
                <i class="fas fa-chart-line mr-1"></i>
                {{ $t('dashboard') }}
              </router-link>
              
              <router-link 
                to="/customers" 
                class="inline-flex items-center px-1 pt-1 text-sm font-medium text-white hover:text-secondary-400 border-b-2 border-transparent hover:border-secondary-500 transition-colors"
              >
                <i class="fas fa-users mr-1"></i>
                {{ $t('customers') }}
              </router-link>
              
              <router-link 
                to="/orders" 
                class="inline-flex items-center px-1 pt-1 text-sm font-medium text-white hover:text-secondary-400 border-b-2 border-transparent hover:border-secondary-500 transition-colors"
              >
                <i class="fas fa-clipboard-list mr-1"></i>
                {{ $t('orders') }}
              </router-link>
            </nav>
          </div>
          
          <!-- Right side menu (profile, etc) -->
          <div class="flex items-center space-x-4">
            <button class="inline-flex items-center px-4 py-1.5 border border-transparent rounded-md shadow-sm text-sm font-medium text-primary-700 bg-secondary-500 hover:bg-secondary-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-secondary-400">
              <i class="fas fa-plus mr-1"></i>
              {{ $t('createOrder') }}
            </button>
            
            <!-- Profile dropdown -->
            <div class="ml-3 relative">
              <div>
                <button type="button" class="flex text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500">
                  <span class="sr-only">{{ $t('profile') }}</span>
                  <div class="h-8 w-8 rounded-full bg-secondary-500 flex items-center justify-center text-primary-700">
                    <i class="fas fa-user"></i>
                  </div>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
    
    <!-- Main content -->
    <main class="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <router-view />
    </main>
    
    <!-- Footer -->
    <footer class="bg-white border-t border-slate-200">
      <div class="max-w-7xl mx-auto py-4 px-4 sm:px-6 lg:px-8">
        <p class="text-center text-sm text-slate-500">
          &copy; {{ new Date().getFullYear() }} {{ $t('ordersManagementSystem') }} - {{ $t('allRightsReserved') }}
        </p>
      </div>
    </footer>
  </div>
</template>

<style>
/* All styles are now managed through Tailwind CSS */
.router-link-active {
  border-color: #e62222;
  color: #e62222;
}

.router-link-exact-active {
  border-color: #e62222;
  color: #e62222;
  font-weight: 500;
}

.card {
  background-color: white;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 500;
  color: #334155;
  margin-bottom: 0.25rem;
}

.form-input {
  margin-top: 0.25rem;
  display: block;
  width: 100%;
  border-radius: 0.375rem;
  border-color: #cbd5e1;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.form-input:focus {
  border-color: #e62222;
  box-shadow: 0 0 0 2px rgba(230, 34, 34, 0.2);
}

.form-error {
  margin-top: 0.5rem;
  font-size: 0.875rem;
  color: #dc2626;
}

.btn {
  display: inline-flex;
  align-items: center;
  padding: 0.5rem 1rem;
  border: 1px solid transparent;
  border-radius: 0.375rem;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  font-size: 0.875rem;
  font-weight: 500;
}

.btn:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.5), 0 0 0 4px rgba(230, 34, 34, 0.5);
}

.btn-primary {
  color: white;
  background-color: #e62222;
}

.btn-primary:hover {
  background-color: #c91c1c;
}

.btn-primary:focus {
  box-shadow: 0 0 0 2px rgba(230, 34, 34, 0.5);
}

.btn-secondary {
  color: white;
  background-color: #475569;
}

.btn-secondary:hover {
  background-color: #334155;
}

.btn-secondary:focus {
  box-shadow: 0 0 0 2px rgba(71, 85, 105, 0.5);
}

.btn-danger {
  color: white;
  background-color: #dc2626;
}

.btn-danger:hover {
  background-color: #b91c1c;
}

.btn-danger:focus {
  box-shadow: 0 0 0 2px rgba(220, 38, 38, 0.5);
}

.btn-success {
  color: white;
  background-color: #16a34a;
}

.btn-success:hover {
  background-color: #15803d;
}

.btn-success:focus {
  box-shadow: 0 0 0 2px rgba(22, 163, 74, 0.5);
}

.btn-outline {
  color: #e62222;
  background-color: white;
  border-color: #f87171;
}

.btn-outline:hover {
  background-color: #fee2e2;
}

.btn-outline:focus {
  box-shadow: 0 0 0 2px rgba(230, 34, 34, 0.5);
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
}

.btn-lg {
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
}

.alert {
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 0.375rem;
  border-left-width: 4px;
}

.alert-success {
  background-color: #dcfce7;
  border-color: #16a34a;
  color: #166534;
}

.alert-danger {
  background-color: #fee2e2;
  border-color: #dc2626;
  color: #991b1b;
}

.alert-info {
  background-color: #fee2e2;
  border-color: #e62222;
  color: #b91c1c;
}

.alert-warning {
  background-color: #fef9c3;
  border-color: #ca8a04;
  color: #854d0e;
}
</style>
