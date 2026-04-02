import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
  server: {
    port: 5173,
    host: true,
    proxy: {
      '/auth':    { target: 'http://localhost:8189', changeOrigin: true },
      '/users':   { target: 'http://localhost:8189', changeOrigin: true },
      '/tasks':   { target: 'http://localhost:8189', changeOrigin: true },
      '/reports': { target: 'http://localhost:8189', changeOrigin: true },
      '/ws':      { target: 'ws://localhost:8189',   changeOrigin: true, ws: true },
    },
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
})
