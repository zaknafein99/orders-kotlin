<template>
  <div class="home">
    <div class="layout">
      <CustomerSearch />
      <OrderTables />
    </div>
  </div>
</template>

<script setup>
import CustomerSearch from '../components/CustomerSearch.vue'
import OrderTables from '../components/OrderTables.vue'
</script>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 2rem;
  padding: 2rem;
}
</style>
