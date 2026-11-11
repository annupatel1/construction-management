import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: { port: 5173 },
  build: {
    // Split stable vendor code so browsers can cache it across app releases.
    // Vite 8 / Rolldown requires the function form of manualChunks.
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('react-router') || id.includes('@remix-run/router')) return 'router'
          if (id.includes('react-dom') || id.includes('/react/') || id.includes('scheduler')) return 'react'
          if (id.includes('lucide-react')) return 'icons'
          if (id.includes('axios')) return 'http'
          return 'vendor'
        },
      },
    },
  },
})
