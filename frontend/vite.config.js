import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  
  server: {
    port: 3000,
    allowedHosts: ['d9877e2f2bd4.ngrok-free.app'],
  }
})
