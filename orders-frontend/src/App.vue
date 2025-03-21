<script setup>
import { onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import AuthService from './services/AuthService'

onMounted(() => {
  // Initialize authentication state when the app starts
  AuthService.initAuth()
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
    <header class="bg-white shadow-md sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <!-- Logo and Navigation -->
          <div class="flex">
            <div class="flex-shrink-0 flex items-center">
              <router-link to="/" class="flex items-center">
                <i class="fas fa-boxes text-primary-600 text-2xl mr-2"></i>
                <span class="text-xl font-semibold text-slate-800">{{ $t('appTitle') }}</span>
              </router-link>
            </div>
            
            <!-- Desktop Navigation -->
            <nav class="hidden md:ml-8 md:flex md:space-x-6">
              <router-link 
                to="/dashboard" 
                class="inline-flex items-center px-1 pt-1 text-sm font-medium text-slate-700 hover:text-primary-600 border-b-2 border-transparent hover:border-primary-500 transition-colors"
              >
                <i class="fas fa-chart-line mr-1"></i>
                {{ $t('dashboard') }}
              </router-link>
              
              <router-link 
                to="/customers" 
                class="inline-flex items-center px-1 pt-1 text-sm font-medium text-slate-700 hover:text-primary-600 border-b-2 border-transparent hover:border-primary-500 transition-colors"
              >
                <i class="fas fa-users mr-1"></i>
                {{ $t('customers') }}
              </router-link>
              
              <router-link 
                to="/orders" 
                class="inline-flex items-center px-1 pt-1 text-sm font-medium text-slate-700 hover:text-primary-600 border-b-2 border-transparent hover:border-primary-500 transition-colors"
              >
                <i class="fas fa-clipboard-list mr-1"></i>
                {{ $t('orders') }}
              </router-link>
            </nav>
          </div>
          
          <!-- Right side menu (profile, etc) -->
          <div class="flex items-center space-x-4">
            <button class="inline-flex items-center px-4 py-1.5 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500">
              <i class="fas fa-plus mr-1"></i>
              {{ $t('createOrder') }}
            </button>
            
            <!-- Profile dropdown -->
            <div class="ml-3 relative">
              <div>
                <button type="button" class="flex text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500">
                  <span class="sr-only">{{ $t('profile') }}</span>
                  <div class="h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center text-white">
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
  @apply border-primary-500 text-primary-600;
}

.router-link-exact-active {
  @apply border-primary-500 text-primary-600 font-medium;
}

.card {
  @apply bg-white rounded-lg shadow-md p-6 mb-6;
}

.form-group {
  @apply mb-4;
}

.form-label {
  @apply block text-sm font-medium text-slate-700 mb-1;
}

.form-input {
  @apply mt-1 block w-full rounded-md border-slate-300 shadow-sm focus:border-primary-500 focus:ring-primary-500 sm:text-sm;
}

.form-error {
  @apply mt-2 text-sm text-danger-600;
}

.btn {
  @apply inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2;
}

.btn-primary {
  @apply text-white bg-primary-600 hover:bg-primary-700 focus:ring-primary-500;
}

.btn-secondary {
  @apply text-white bg-slate-600 hover:bg-slate-700 focus:ring-slate-500;
}

.btn-danger {
  @apply text-white bg-danger-600 hover:bg-danger-700 focus:ring-danger-500;
}

.btn-success {
  @apply text-white bg-success-600 hover:bg-success-700 focus:ring-success-500;
}

.btn-outline {
  @apply text-primary-600 bg-white border-primary-300 hover:bg-primary-50 focus:ring-primary-500;
}

.btn-sm {
  @apply px-3 py-1.5 text-xs;
}

.btn-lg {
  @apply px-6 py-3 text-base;
}

.alert {
  @apply p-4 mb-4 rounded-md border-l-4;
}

.alert-success {
  @apply bg-success-50 border-success-500 text-success-700;
}

.alert-danger {
  @apply bg-danger-50 border-danger-500 text-danger-700;
}

.alert-info {
  @apply bg-primary-50 border-primary-500 text-primary-700;
}

.alert-warning {
  @apply bg-warning-50 border-warning-500 text-warning-700;
}
</style>
