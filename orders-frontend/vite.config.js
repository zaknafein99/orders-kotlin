import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/workspaces/orders-kotlin/orders-frontend/src'
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/api/, ''),
        configure: (proxy, options) => {
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('Proxying request:', req.method, req.url);
            // Log authorization header (partially)
            if (req.headers.authorization) {
              console.log('Auth header:', req.headers.authorization.substring(0, 20) + '...');
            }
          });
          proxy.on('proxyRes', (proxyRes, req, res) => {
            console.log('Received response:', proxyRes.statusCode, req.url);
            // Log more details for truck endpoint
            if (req.url.includes('/truck')) {
              console.log('Truck endpoint response status:', proxyRes.statusCode);
            }
          });
          proxy.on('error', (err, req, res) => {
            console.error('Proxy error:', err);
          });
        }
      }
    }
  }
})
