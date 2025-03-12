<script setup>
import { onMounted } from 'vue'
import AuthService from './services/AuthService'
import { translations } from './utils/translations'

onMounted(() => {
  // Initialize authentication state when the app starts
  AuthService.initAuth()
})
</script>

<template>
  <div class="app">
    <header class="app-header">
      <div class="container header-container">
        <h1><i class="fas fa-boxes"></i> {{ translations.appTitle }}</h1>
      </div>
    </header>
    <main class="container">
      <router-view />
    </main>
    <footer class="app-footer">
      <div class="container">
        <p>&copy; {{ new Date().getFullYear() }} Orders Management System</p>
      </div>
    </footer>
  </div>
</template>

<style>
:root {
  --primary-color: #3498db;
  --primary-dark: #2980b9;
  --secondary-color: #2ecc71;
  --secondary-dark: #27ae60;
  --accent-color: #f39c12;
  --danger-color: #e74c3c;
  --dark-color: #2c3e50;
  --light-color: #f5f7fa;
  --gray-color: #95a5a6;
  --text-color: #333;
  --border-radius: 6px;
  --box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s ease;
  --table-font-size: 0.72rem;
  --table-header-font-size: 0.62rem;
  --page-bg-color: #f0f4f8;
  --tables-bg-color: #ffffff;
  --search-bg-color: rgba(52, 152, 219, 0.05);
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Inter', 'Segoe UI', 'Roboto', sans-serif;
  background-color: var(--page-bg-color);
  color: var(--text-color);
  line-height: 1.6;
  padding: 0;
  margin: 0;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  max-width: 1780px;
  margin: 0 auto;
  background-color: var(--page-bg-color);
}

.container {
  width: 100%;
  max-width: 1780px;
  margin: 0 auto;
  padding: 0 0.4rem;
}

.app-header {
  background-color: var(--dark-color);
  color: white;
  padding: 0.75rem 0;
  box-shadow: var(--box-shadow);
  position: sticky;
  top: 0;
  z-index: 100;
  border-radius: 0 0 var(--border-radius) var(--border-radius);
  margin: 0 0.2rem 1rem 0.2rem;
}

.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-header h1 {
  font-size: 1.3rem;
  background-color: rgba(255, 255, 255, 0.15);
  padding: 0.4rem 1rem;
  border-radius: 25px;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

main {
  flex: 1;
  padding: 0.5rem 0;
}

.app-footer {
  background-color: var(--dark-color);
  color: white;
  padding: 1rem 0;
  margin: 1rem 0.2rem 0.2rem 0.2rem;
  text-align: center;
  font-size: 0.9rem;
  border-radius: var(--border-radius) var(--border-radius) 0 0;
}

button {
  background-color: var(--primary-color);
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-weight: 600;
  transition: var(--transition);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  font-size: 0.95rem;
}

button:hover {
  background-color: var(--primary-dark);
  transform: translateY(-1px);
  box-shadow: var(--box-shadow);
}

button:active {
  transform: translateY(0);
}

button:disabled {
  background-color: var(--gray-color);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

button.btn-secondary {
  background-color: var(--secondary-color);
}

button.btn-secondary:hover {
  background-color: var(--secondary-dark);
}

button.btn-accent {
  background-color: var(--accent-color);
}

button.btn-danger {
  background-color: var(--danger-color);
}

button.btn-outline {
  background-color: transparent;
  border: 1px solid var(--primary-color);
  color: var(--primary-color);
}

button.btn-outline:hover {
  background-color: var(--primary-color);
  color: white;
}

button.btn-sm {
  padding: 0.3rem 0.8rem;
  font-size: 0.85rem;
}

input, select, textarea {
  padding: 0.7rem 1rem;
  border: 1px solid #ddd;
  border-radius: var(--border-radius);
  width: 100%;
  margin-bottom: 1rem;
  font-size: 0.95rem;
  transition: var(--transition);
}

input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--dark-color);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-error {
  color: var(--danger-color);
  font-size: 0.85rem;
  margin-top: 0.25rem;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 2rem;
  background-color: white;
  box-shadow: var(--box-shadow);
  border-radius: var(--border-radius);
  overflow: hidden;
}

th, td {
  padding: 0.6rem 0.75rem;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 0.95rem;
}

th {
  background-color: var(--dark-color);
  color: white;
  font-weight: 600;
  text-transform: uppercase;
  font-size: 0.85rem;
  letter-spacing: 0.5px;
}

tr:last-child td {
  border-bottom: none;
}

tr:hover {
  background-color: rgba(245, 247, 250, 0.7);
}

/* Card styles */
.card {
  background-color: white;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  padding: 1.5rem;
  margin-bottom: 1.5rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--dark-color);
  margin: 0;
}

/* Alert styles */
.alert {
  padding: 1rem;
  border-radius: var(--border-radius);
  margin-bottom: 1.5rem;
  border-left: 4px solid transparent;
}

.alert-success {
  background-color: rgba(46, 204, 113, 0.1);
  border-left-color: var(--secondary-color);
  color: var(--secondary-dark);
}

.alert-danger {
  background-color: rgba(231, 76, 60, 0.1);
  border-left-color: var(--danger-color);
  color: var(--danger-color);
}

.alert-info {
  background-color: rgba(52, 152, 219, 0.1);
  border-left-color: var(--primary-color);
  color: var(--primary-dark);
}

/* Responsive utilities */
@media (max-width: 768px) {
  .container {
    padding: 0 0.75rem;
  }
  
  main {
    padding: 1.5rem 0;
  }
  
  th, td {
    padding: 0.75rem;
  }
  
  .card {
    padding: 1.25rem;
  }
}
</style>
