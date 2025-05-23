<template>
  <div class="dashboard p-6">
    <!-- Loading Indicator -->
    <div v-if="loading" class="fixed inset-0 bg-white bg-opacity-75 flex justify-center items-center z-50">
      <div class="bg-white p-4 rounded-lg shadow-lg flex items-center">
        <div class="spinner mr-3"></div>
        <p>{{ $t('loading') }}</p>
      </div>
    </div>

    <!-- Dashboard content - always rendered -->
    <div>
      <!-- Statistics Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <div class="card bg-white p-4 rounded-lg shadow">
          <h3 class="text-lg font-semibold text-slate-700 mb-2">{{ $t('todaySales') }}</h3>
          <div class="flex justify-between items-center">
            <div>
              <p class="text-2xl font-bold text-primary-600">${{ formatNumber(statistics.today?.totalSales || 0) }}</p>
              <p class="text-sm text-slate-500">{{ statistics.today?.orderCount || 0 }} {{ $t('orders') }}</p>
            </div>
            <i class="fas fa-chart-line text-2xl text-primary-500"></i>
          </div>
        </div>

        <div class="card bg-white p-4 rounded-lg shadow">
          <h3 class="text-lg font-semibold text-slate-700 mb-2">{{ $t('monthlySales') }}</h3>
          <div class="flex justify-between items-center">
            <div>
              <p class="text-2xl font-bold text-primary-600">${{ formatNumber(statistics.month?.totalSales || 0) }}</p>
              <p class="text-sm text-slate-500">{{ statistics.month?.orderCount || 0 }} {{ $t('orders') }}</p>
            </div>
            <i class="fas fa-calendar-alt text-2xl text-primary-500"></i>
          </div>
        </div>

        <div class="card bg-white p-4 rounded-lg shadow">
          <h3 class="text-lg font-semibold text-slate-700 mb-2">{{ $t('pendingOrders') }}</h3>
          <div class="flex justify-between items-center">
            <div>
              <p class="text-2xl font-bold text-warning-600">{{ statistics.pendingOrders || 0 }}</p>
              <p class="text-sm text-slate-500">{{ $t('awaitingDelivery') }}</p>
            </div>
            <i class="fas fa-clock text-2xl text-warning-500"></i>
          </div>
        </div>

        <div class="card bg-white p-4 rounded-lg shadow">
          <h3 class="text-lg font-semibold text-slate-700 mb-2">{{ $t('averageOrderValue') }}</h3>
          <div class="flex justify-between items-center">
            <div>
              <p class="text-2xl font-bold text-success-600">${{ formatNumber(statistics.averageOrderValue || 0) }}</p>
              <p class="text-sm text-slate-500">{{ $t('perOrder') }}</p>
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
            <h3 class="text-lg font-semibold text-slate-700">{{ $t('dailyTruckSales') }}</h3>
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
                  <th>{{ $t('truck') }}</th>
                  <th>{{ $t('orders') }}</th>
                  <th>{{ $t('totalSales') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(sales, truckId) in dailySales.truckSales" :key="truckId">
                  <td>{{ $t('truck') }} {{ truckId }}</td>
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
            <h3 class="text-lg font-semibold text-slate-700">{{ $t('weeklySales') }}</h3>
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
                  <th>{{ $t('date') }}</th>
                  <th>{{ $t('orders') }}</th>
                  <th>{{ $t('totalSales') }}</th>
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

      <!-- Sales Chart - ALWAYS rendered -->
      <div class="card bg-white p-4 rounded-lg shadow mb-6">
        <h3 class="text-lg font-semibold text-slate-700 mb-4">{{ $t('monthlySalesChart') }}</h3>
        <div id="chartContainer" style="height: 300px; position: relative;">
          <canvas ref="salesChartCanvas" id="salesChart"></canvas>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
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
import { formatCurrency, formatDate, formatNumber } from '../utils/formatters'
import { useI18n } from 'vue-i18n'

// Initialize i18n
const { t } = useI18n()

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

let salesChart = null;
let chartInitAttempts = 0;
const MAX_CHART_INIT_ATTEMPTS = 5;

// Date selections
const selectedDate = ref(format(new Date(), 'yyyy-MM-dd'))
const selectedWeekStart = ref(format(new Date(), 'yyyy-MM-dd'))

// Canvas reference
const salesChartCanvas = ref(null)

// Data
const statistics = ref({})
const dailySales = ref({ truckSales: {} })
const weeklySales = ref({ dailySales: [] })
const loading = ref(true)
const chartData = ref([]) // Store chart data

onMounted(async () => {
  try {
    // Wait for DOM to be ready
    await nextTick();
    // Start data fetching
    await fetchAllData();
  } catch (error) {
    console.error(t('errorFetchingDashboardData'), error);
  } finally {
    loading.value = false;
    // Try to initialize chart after loading is complete and DOM is updated
    nextTick(() => {
      if (chartData.value.length > 0) {
        initializeChart(chartData.value);
      } else {
        initializeChart([]);
      }
    });
  }
})

onBeforeUnmount(() => {
  // Clean up chart when component is destroyed
  if (salesChart) {
    salesChart.destroy()
    salesChart = null
  }
})

const fetchAllData = async () => {
  try {
    loading.value = true
    await Promise.all([
      fetchStatistics(),
      fetchDailySales(),
      fetchWeeklySales(),
      fetchMonthlySales()
    ])
  } catch (error) {
    console.error(t('errorFetchingDashboardData'), error)
  } finally {
    loading.value = false
  }
}

const fetchStatistics = async () => {
  try {
    const response = await axios.get('/dashboard/statistics')
    statistics.value = response.data
  } catch (error) {
    console.error(t('errorFetchingStatistics'), error)
  }
}

const fetchDailySales = async () => {
  try {
    const response = await axios.get(`/dashboard/truck-sales/daily?date=${selectedDate.value}`)
    dailySales.value = response.data
  } catch (error) {
    console.error(t('errorFetchingDailySales'), error)
  }
}

const fetchWeeklySales = async () => {
  try {
    const response = await axios.get(`/dashboard/truck-sales/weekly?startDate=${selectedWeekStart.value}`)
    weeklySales.value = response.data
  } catch (error) {
    console.error(t('errorFetchingWeeklySales'), error)
  }
}

const fetchMonthlySales = async () => {
  try {
    // Get first day of the current month for the API call
    const currentDate = new Date(selectedDate.value);
    const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
    const formattedDate = format(firstDayOfMonth, 'yyyy-MM-dd');
    
    console.log(`Fetching monthly sales for date: ${formattedDate}`);
    const response = await axios.get(`/dashboard/truck-sales/monthly?date=${formattedDate}`);
    console.log('Monthly data response:', response.data);
    
    let extractedData = [];
    
    // Check various possible data structures in the response
    if (response.data) {
      if (Array.isArray(response.data.dailySales)) {
        // Direct dailySales array
        extractedData = response.data.dailySales;
      } else if (response.data.month && Array.isArray(response.data.month.dailySales)) {
        // dailySales nested in month object
        extractedData = response.data.month.dailySales;
      } else if (Array.isArray(response.data)) {
        // Response itself is an array
        extractedData = response.data;
      } else if (typeof response.data === 'object') {
        // Extract data from any array property we can find
        for (const key in response.data) {
          if (Array.isArray(response.data[key])) {
            extractedData = response.data[key];
            break;
          }
        }
      }
    }
    
    console.log('Extracted chart data:', extractedData);
    // Store the data for later use
    chartData.value = extractedData;
    
    // Only update chart if loading is complete
    if (!loading.value) {
      initializeChart(extractedData);
    }
  } catch (error) {
    console.error(t('errorFetchingMonthlySales'), error);
    chartData.value = [];
    if (!loading.value) {
      initializeChart([]);
    }
  }
}

// Separate chart initialization function
const initializeChart = (data) => {
  // Schedule for next tick to ensure DOM is updated
  nextTick(() => {
    try {
      // Make sure the chart container exists
      const chartContainer = document.getElementById('chartContainer');
      if (!chartContainer) {
        throw new Error('Chart container not found');
      }
      
      // Ensure we have a canvas - create one if needed
      let canvas = getChartCanvas();
      if (!canvas) {
        console.log('Canvas not found, creating new canvas element');
        canvas = document.createElement('canvas');
        canvas.id = 'salesChart';
        chartContainer.innerHTML = '';
        chartContainer.appendChild(canvas);
        // Update the ref if possible
        salesChartCanvas.value = canvas;
      }
      
      updateSalesChart(data);
    } catch (error) {
      console.error('Failed to initialize chart:', error);
      // Retry initialization with increasing delay
      if (chartInitAttempts < MAX_CHART_INIT_ATTEMPTS) {
        chartInitAttempts++;
        const delay = chartInitAttempts * 200; // Increase delay with each attempt
        console.log(`Retrying chart initialization in ${delay}ms (attempt ${chartInitAttempts}/${MAX_CHART_INIT_ATTEMPTS})`);
        setTimeout(() => initializeChart(data), delay);
      } else {
        console.error(`Failed to initialize chart after ${MAX_CHART_INIT_ATTEMPTS} attempts`);
      }
    }
  });
};

const updateSalesChart = async (data) => {
  console.log('Chart data input:', data);
  
  // Wait for DOM update to ensure canvas is rendered
  await nextTick();
  
  // Get canvas with fallbacks
  const canvas = getChartCanvas();
  
  // Ensure canvas exists
  if (!canvas) {
    console.error('Chart canvas element not found, retrying in 100ms...');
    // Try again after a short delay
    setTimeout(() => updateSalesChart(data), 100);
    return;
  }
  
  // Safely destroy any existing chart
  if (salesChart) {
    try {
      salesChart.destroy();
    } catch (error) {
      console.warn('Error destroying existing chart:', error);
      // Continue anyway
    }
    salesChart = null;
  }
  
  // Create an empty chart if there's no data
  if (!data || data.length === 0) {
    try {
      const ctx = canvas.getContext('2d');
      if (!ctx) {
        console.error('Failed to get 2D context from canvas');
        return;
      }
      
      salesChart = new ChartJS(canvas, {
        type: 'bar',
        data: {
          datasets: [{
            label: t('monthlySales'),
            data: [],
            backgroundColor: 'rgba(75, 192, 192, 0.5)',
            borderColor: 'rgb(75, 192, 192)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            title: {
              display: true,
              text: t('monthlySalesChart'),
              font: {
                size: 16
              }
            },
            tooltip: {
              enabled: false
            },
            legend: {
              display: false
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
                text: t('date')
              }
            },
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: t('sales')
              }
            }
          }
        }
      });
      
      // Add a "No data" label
      ctx.save();
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.font = '16px sans-serif';
      ctx.fillStyle = '#666';
      ctx.fillText(t('noMonthlySalesData'), canvas.width / 2, canvas.height / 2);
      ctx.restore();
    } catch (error) {
      console.error('Error creating empty chart:', error);
    }
    
    return;
  }

  try {
    // Process the data to ensure dates are properly formatted
    const processedData = data.map(item => {
      // Handle different data formats
      let dateValue, salesValue;
      
      if (typeof item === 'object') {
        if (item.date) {
          // If date is a string, ensure it's properly formatted
          dateValue = typeof item.date === 'string' 
            ? (item.date.includes('T') ? new Date(item.date) : new Date(item.date + 'T00:00:00'))
            : new Date(item.date);
        } else if (item.key || item.day) {
          // Handle alternate date formats
          dateValue = new Date(item.key || item.day || selectedDate.value);
        } else {
          // Fallback to current date
          dateValue = new Date();
        }
        
        // Handle different sales property names
        salesValue = item.totalSales || item.sales || item.value || 0;
      } else {
        // Handle primitive format
        dateValue = new Date();
        salesValue = item || 0;
      }
      
      return {
        x: dateValue,
        y: salesValue
      };
    });
    
    const sortedData = [...processedData].sort((a, b) => a.x - b.x);
    
    const chartData = {
      datasets: [{
        label: t('monthlySales'),
        data: sortedData,
        backgroundColor: 'rgba(75, 192, 192, 0.5)',
        borderColor: 'rgb(75, 192, 192)',
        borderWidth: 1
      }]
    };

    salesChart = new ChartJS(canvas, {
      type: 'bar',
      data: chartData,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: t('monthlySalesChart'),
            font: {
              size: 16
            }
          },
          tooltip: {
            callbacks: {
              title: function(context) {
                return format(context[0].raw.x, 'PPP', { locale: enUS });
              },
              label: function(context) {
                return `${t('sales')}: $${formatNumber(context.raw.y)}`;
              }
            }
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
              text: t('date')
            }
          },
          y: {
            beginAtZero: true,
            title: {
              display: true,
              text: t('sales')
            }
          }
        }
      }
    });
  } catch (error) {
    console.error('Error creating chart:', error);
  }
}

// Add this function to safely get the canvas element
const getChartCanvas = () => {
  // Try the ref first
  if (salesChartCanvas.value) {
    return salesChartCanvas.value;
  }
  
  // Fallback to getElementById
  const canvasElement = document.getElementById('salesChart');
  if (canvasElement) {
    return canvasElement;
  }
  
  // Last resort - query selector
  return document.querySelector('canvas#salesChart');
};
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

.spinner {
  border: 3px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 3px solid #3498db;
  width: 24px;
  height: 24px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>

