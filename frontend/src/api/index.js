import axios from 'axios'
import { expireSession } from '../stores/session'

// В dev Vite проксирует запросы на api-gateway (см. vite.config.js).
// Для production: VITE_API_URL=http://localhost:8080
const API_BASE_URL = import.meta.env.VITE_API_URL || ''

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

let sessionExpiredHandler = null

export function setOnSessionExpired(handler) {
  sessionExpiredHandler = handler
}

function handleSessionExpired() {
  expireSession()
  sessionExpiredHandler?.()
}

// Интерсептор для добавления токена авторизации
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Интерсептор для обработки ошибок и обновления токена
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const isRefreshRequest = originalRequest?.url?.includes('/auth/refresh')

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry &&
      !isRefreshRequest
    ) {
      originalRequest._retry = true
      const refreshToken = localStorage.getItem('refreshToken')
      const requestUrl = originalRequest.url || ''
      if (!refreshToken) {
        handleSessionExpired()
        return Promise.reject(error)
      }

      try {
        const response = await api.post('/auth/refresh', { refreshToken })
        const { accessToken, refreshToken: newRefreshToken } = response.data
        localStorage.setItem('accessToken', accessToken)
        localStorage.setItem('refreshToken', newRefreshToken)
        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return api(originalRequest)
      } catch {
        handleSessionExpired()
        return Promise.reject(error)
      }
    }

    return Promise.reject(error)
  }
)

// Auth API
export const authAPI = {
  login: (email, password) => api.post('/auth/login', { email, password }),
  register: (name, email, password) => api.post('/auth/register', { name, email, password }),
  refresh: (refreshToken) => api.post('/auth/refresh', { refreshToken })
}

// Users API
export const usersAPI = {
  getCurrentUser: () => api.get('/users/me'),
  updateName: (name) => api.patch('/users/me/name', { name }),
  updatePassword: (currentPassword, newPassword) =>
    api.patch('/users/me/password', { currentPassword, newPassword }),
  getUserStats: () => api.get('/users/me/stats'),
  deleteUser: () => api.delete('/users/me')
}

// Scenarios API
export const scenariosAPI = {
  getScenarios: (page = 1, pageSize = 20, free = null) => {
    const params = { page, pageSize }
    if (free !== null) params.free = free
    return api.get('/scenarios', { params })
  },
  getScenarioById: (id) => api.get(`/scenarios/${id}`),
  getScenarioProgress: (scenarioId) => api.get(`/scenarios/${scenarioId}/progress`)
}

// Subscriptions API
export const subscriptionsAPI = {
  createSubscription: (durationInDays) => api.post('/subscriptions', { durationInDays }),
  getCurrentSubscription: () => api.get('/subscriptions/current'),
  cancelSubscription: () => api.delete('/subscriptions/current')
}

export default api
