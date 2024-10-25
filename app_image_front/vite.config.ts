import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '^/images': {
        target: 'http://localhost:8181' // Spring boot backend address (Application address when the project is run from root)
      }
    }
  },
  build: {
    outDir: 'target/dist',
    assetsDir: 'static'
  }
})
