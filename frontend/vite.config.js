import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置：
// 1. dev 模式下把 /api 代理到后端 8080，前端代码里就不需要写完整域名
// 2. 生产环境由 nginx 做同样的反向代理（见 nginx.conf）
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
