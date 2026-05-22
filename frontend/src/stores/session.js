import { reactive } from 'vue'

export const userState = reactive({
  user: null,
  stats: null,
  subscription: null,
  isAuthenticated: false,
  loading: false,
  error: null
})

let onSessionExpired = null

export function setSessionExpiredHandler(handler) {
  onSessionExpired = handler
}

export function restoreSession() {
  userState.isAuthenticated = !!localStorage.getItem('accessToken')
}

export function clearSession() {
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  userState.user = null
  userState.stats = null
  userState.subscription = null
  userState.isAuthenticated = false
  userState.error = null
}

export function expireSession() {
  clearSession()
  onSessionExpired?.()
}
