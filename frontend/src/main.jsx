import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import '@fontsource-variable/manrope'
import '@fontsource-variable/jetbrains-mono'
import './styles/index.css'
import App from './App.jsx'
import { AuthProvider } from './auth.jsx'
import { ToastProvider } from './components/Toast.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  </StrictMode>,
)
