<template>
  <div class="dashboard p-6">
    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="card bg-white p-4 rounded-lg shadow">
        <h3 class="text-lg font-semibold text-slate-700 mb-2">Today's Sales</h3>
        <div class="flex justify-between items-center">
          <div>
            <p class="text-2xl font-bold text-primary-600">${{ formatNumber(statistics.today?.totalSales || 0) }}</p>
            <p class="text-sm text-slate-500">{{ statistics.today?.orderCount || 0 }} orders</p>
          </div>
          <i class="fas fa-chart-line text-2xl text-primary-500"></i>
        </div>
      </div>

      <div class="card bg-white p-4 rounded-lg shadow">
        <h3 class="text-lg font-semibold text-slate-700 mb-2">Monthly Sales</h3>
        <div class="flex justify-between items-center">
          <div>
            <p class="text-2xl font-bold text-primary-600">${{ formatNumber(statistics.month?.totalSales || 0) }}</p>
            <p class="text-sm text-slate-500">{{ statistics.month?.orderCount || 0 }} orders</p>
          </div>
          <i class="fas fa-calendar-alt text-2xl text-primary-500"></i>
        </div>
      </div>

      <div class="card bg-white p-4 rounded-lg shadow">
        <h3 class="text-lg font-semibold text-slate-700 mb-2">Pending Orders</h3>
        <div class="flex justify-between items-center">
          <div>
            <p class="text-2xl font-bold text-warning-600">{{ statistics.pendingOrders || 0 }}</p>
            <p class="text-sm text-slate-500">awaiting delivery</p>
          </div>
          <i class="fas fa-clock text-2xl text-warning-500"></i>
        </div>
      </div>

      <div class="card bg-white p-4 rounded-lg shadow">
        <h3 class="text-lg font-semibold text-slate-700 mb-2">Average Order Value</h3>
        <div class="flex justify-between items-center">
          <div>
            <p class="text-2xl font-bold text-success-600">${{ formatNumber(statistics.averageOrderValue || 0) }}</p>
            <p class="text-sm text-slate-500">per order</p>
          </div>
          <i class="fas fa-dollar-sign text-2xl text-success-500"></i>
        </div>
      </div>
    </div>

    <!-- Truck Sales Reports -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
      <!-- Daily Sales -->
      <div class="card bg-white p-4 rounded-lg shadow">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold text-slate-700">Daily Truck Sales</h3>
          <input 
            type="date" 
            v-model="selectedDate" 
            class="form-input"
            @change="fetchDailySales"
          >
        </div>
        <div class="overflow-x-auto">
          <table class="table">
            <thead>
              <tr>
                <th>Truck</th>
                <th>Orders</th>
                <th>Total Sales</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(sales, truckId) in dailySales.truckSales" :key="truckId">
                <td>Truck {{ truckId }}</td>
                <td>{{ sales.orderCount }}</td>
                <td>${{ formatNumber(sales.totalSales) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Weekly Sales -->
      <div class="card bg-white p-4 rounded-lg shadow">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-semibold text-slate-700">Weekly Sales</h3>
          <input 
            type="date" 
            v-model="selectedWeekStart" 
            class="form-input"
            @change="fetchWeeklySales"
          >
        </div>
        <div class="overflow-x-auto">
          <table class="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Orders</th>
                <th>Total Sales</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="day in weeklySales.dailySales" :key="day.date">
                <td>{{ formatDate(day.date) }}</td>
                <td>{{ day.orderCount }}</td>
                <td>${{ formatNumber(day.totalSales) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Sales Chart -->
    <div class="card bg-white p-4 rounded-lg shadow mb-6">
      <h3 class="text-lg font-semibold text-slate-700 mb-4">Sales Chart</h3>
      <div style="height: 300px; position: relative;">
        <canvas id="salesChart"></canvas>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  TimeScale,
  LineController,
  BarController
} from 'chart.js'
import 'chartjs-adapter-date-fns'
import { format } from 'date-fns'
import { enUS } from 'date-fns/locale'

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  LineController,
  BarController,
  Title,
  Tooltip,
  Legend,
  TimeScale
)

const statistics = ref({})
const dailySales = ref({ truckSales: {} })
const weeklySales = ref({ dailySales: [] })
const selectedDate = ref(new Date().toISOString().split('T')[0])
const selectedWeekStart = ref(new Date().toISOString().split('T')[0])
const salesChart = ref(null)

const fetchStatistics = async () => {
  try {
    console.log('Fetching dashboard statistics...')
    const response = await axios.get('/dashboard/statistics')
    console.log('Statistics data:', response.data)
    statistics.value = response.data || {}

    // Check if the month data exists and contains daily sales
    if (statistics.value.month?.dailySales && Array.isArray(statistics.value.month.dailySales)) {
      console.log('Daily sales data:', statistics.value.month.dailySales)
      
      if (statistics.value.month.dailySales.length > 0) {
        updateSalesChart(statistics.value.month.dailySales)
      } else {
        console.warn('Daily sales array is empty')
        // Create chart with empty data
        updateSalesChart([])
      }
    } else {
      console.warn('No daily sales data found in API response:', statistics.value)
      // Create chart with empty data
      updateSalesChart([])
    }
  } catch (error) {
    console.error('Error fetching statistics:', error)
    if (error.response) {
      console.error('Error response:', error.response.data)
      console.error('Status code:', error.response.status)
    }
    
    // Set default values for statistics
    statistics.value = {
      today: { totalSales: 0, orderCount: 0 },
      month: { totalSales: 0, orderCount: 0, dailySales: [] },
      pendingOrders: 0,
      totalTrucks: 0,
      averageOrderValue: 0
    }
    
    // Create chart with empty data
    updateSalesChart([])
  }
}

const fetchDailySales = async () => {
  try {
    const response = await axios.get(`/dashboard/truck-sales/daily?date=${selectedDate.value}`)
    dailySales.value = response.data
  } catch (error) {
    console.error('Error fetching daily sales:', error)
  }
}

const fetchWeeklySales = async () => {
  try {
    const response = await axios.get(`/dashboard/truck-sales/weekly?startDate=${selectedWeekStart.value}`)
    weeklySales.value = response.data
  } catch (error) {
    console.error('Error fetching weekly sales:', error)
  }
}

const updateSalesChart = (data) => {
  console.log('Updating sales chart with data:', data)
  const ctx = document.getElementById('salesChart')
  if (!ctx) {
    console.error('Sales chart canvas element not found')
    return
  }

  // Destroy existing chart if it exists
  if (salesChart.value) {
    salesChart.value.destroy()
  }

  try {
    // Handle empty data case
    if (!data || data.length === 0) {
      console.log('No data available for chart, displaying empty chart')
      
      // Create empty chart with "No data available" message
      salesChart.value = new ChartJS(ctx, {
        type: 'bar',
        data: {
          datasets: [{
            label: 'Sales',
            data: [],
            backgroundColor: 'rgba(76, 175, 80, 0.6)',
            borderColor: '#4CAF50',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              enabled: false
            }
          },
          scales: {
            x: {
              type: 'time',
              time: {
                unit: 'day'
              },
              title: {
                display: true,
                text: 'Date'
              }
            },
            y: {
              type: 'linear',
              beginAtZero: true,
              title: {
                display: true,
                text: 'Sales ($)'
              },
              ticks: {
                callback: function(value) {
                  return '$' + value.toFixed(2)
                }
              }
            }
          }
        },
        plugins: [{
          id: 'noDataMessage',
          afterDraw: (chart) => {
            if (chart.data.datasets[0].data.length === 0) {
              // No data available
              const ctx = chart.ctx;
              const width = chart.width;
              const height = chart.height;
              
              chart.clear();
              
              ctx.save();
              ctx.textAlign = 'center';
              ctx.textBaseline = 'middle';
              ctx.font = '16px sans-serif';
              ctx.fillStyle = '#666';
              ctx.fillText('No sales data available for this period', width / 2, height / 2);
              ctx.restore();
            }
          }
        }]
      })
      return;
    }
    
    // Parse dates and prepare data for display
    const chartData = data.map(item => ({
      x: new Date(item.date),
      y: item.totalSales
    }))
    
    console.log('Chart data:', chartData)

    salesChart.value = new ChartJS(ctx, {
      type: 'bar',
      data: {
        datasets: [{
          label: 'Sales',
          data: chartData,
          backgroundColor: 'rgba(76, 175, 80, 0.6)',
          borderColor: '#4CAF50',
          borderWidth: 1
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            mode: 'index',
            intersect: false,
            callbacks: {
              title: function(tooltipItems) {
                return format(new Date(tooltipItems[0].parsed.x), 'MMM dd, yyyy');
              },
              label: function(context) {
                return `$${context.parsed.y.toFixed(2)}`;
              }
            }
          }
        },
        scales: {
          x: {
            type: 'time',
            time: {
              unit: 'day',
              displayFormats: {
                day: 'MMM dd'
              }
            },
            title: {
              display: true,
              text: 'Date'
            }
          },
          y: {
            type: 'linear',
            beginAtZero: true,
            title: {
              display: true,
              text: 'Sales ($)'
            },
            ticks: {
              callback: function(value) {
                return '$' + value.toFixed(2)
              }
            }
          }
        }
      }
    })
    
    console.log('Chart created successfully')
  } catch (error) {
    console.error('Error creating chart:', error)
  }
}

const formatNumber = (number) => {
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(number)
}

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString()
}

onMounted(() => {
  fetchStatistics()
  fetchDailySales()
  fetchWeeklySales()
})
</script>

<style scoped>
.dashboard {
  min-height: calc(100vh - 4rem);
}

.card {
  transition: all 0.3s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
</style>
