import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// Em desenvolvimento, /api é encaminhado ao back-end Spring Boot (mesma origem => cookie de sessão simples).
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: env.BACKEND_URL || 'http://localhost:8080',
          changeOrigin: true,
          // o back-end vê a chamada como same-origin (evita rejeição de CORS por porta diferente)
          configure: (proxy) => proxy.on('proxyReq', (req) => req.removeHeader('origin')),
        },
      },
    },
  }
})
