import { authAPI, usersAPI, subscriptionsAPI } from '../api'
import { userState, restoreSession, clearSession } from './session'

export { userState, restoreSession, clearSession } from './session'

function formatApiError(error, fallback) {
  if (error.response?.data?.message) {
    return error.response.data.message
  }
  if (error.code === 'ERR_NETWORK' || !error.response) {
    return 'Сервер недоступен. Запустите MongoDB, core-service (8081), subscription-service (8082) и api-gateway (8080).'
  }
  return fallback
}

export const useUserStore = () => {
  const login = async (email, password) => {
    userState.loading = true
    userState.error = null
    try {
      const response = await authAPI.login(email, password)
      const { tokens, user } = response.data
      localStorage.setItem('accessToken', tokens.accessToken)
      localStorage.setItem('refreshToken', tokens.refreshToken)
      userState.user = user
      userState.isAuthenticated = true
      await fetchCurrentSubscription()
      await fetchUserStats()
      return true
    } catch (error) {
      userState.error = formatApiError(error, 'Ошибка входа')
      return false
    } finally {
      userState.loading = false
    }
  }

  const register = async (name, email, password) => {
    userState.loading = true
    userState.error = null
    try {
      const response = await authAPI.register(name, email, password)
      const { tokens, user } = response.data
      localStorage.setItem('accessToken', tokens.accessToken)
      localStorage.setItem('refreshToken', tokens.refreshToken)
      userState.user = user
      userState.isAuthenticated = true
      return true
    } catch (error) {
      userState.error = formatApiError(error, 'Ошибка регистрации')
      return false
    } finally {
      userState.loading = false
    }
  }

  const logout = () => {
    clearSession()
  }

  const fetchCurrentUser = async () => {
    restoreSession()
    if (!userState.isAuthenticated) return false

    try {
      const response = await usersAPI.getCurrentUser()
      userState.user = response.data
      userState.isAuthenticated = true
      return true
    } catch (error) {
      if (error.response?.status === 401) {
        // Сессия сбрасывается интерсептором api; здесь только сообщение
        userState.error = error.response?.data?.message || 'Сессия истекла'
        if (!localStorage.getItem('accessToken')) {
          userState.isAuthenticated = false
        }
      } else {
        userState.error = formatApiError(error, 'Не удалось загрузить профиль')
      }
      return false
    }
  }

  const fetchUserStats = async () => {
    if (!userState.isAuthenticated) return
    try {
      const response = await usersAPI.getUserStats()
      userState.stats = response.data
    } catch (error) {
      // Не сбрасываем сессию — профиль должен оставаться доступным
      console.error('Failed to fetch stats:', error)
    }
  }

  const fetchCurrentSubscription = async () => {
    if (!userState.isAuthenticated) return
    try {
      const response = await subscriptionsAPI.getCurrentSubscription()
      userState.subscription = response.data
    } catch (error) {
      if (error.response?.status !== 404) {
        console.error('Failed to fetch subscription:', error)
      }
      userState.subscription = null
    }
  }

  const updateUserName = async (name) => {
    try {
      const response = await usersAPI.updateName(name)
      userState.user = response.data
      return true
    } catch (error) {
      userState.error = error.response?.data?.message || 'Ошибка обновления имени'
      return false
    }
  }

  const updateUserPassword = async (currentPassword, newPassword) => {
    try {
      await usersAPI.updatePassword(currentPassword, newPassword)
      return true
    } catch (error) {
      userState.error = error.response?.data?.message || 'Ошибка обновления пароля'
      return false
    }
  }

  const createSubscription = async (durationInDays) => {
    try {
      const response = await subscriptionsAPI.createSubscription(durationInDays)
      userState.subscription = response.data
      return true
    } catch (error) {
      userState.error = error.response?.data?.message || 'Ошибка оформления подписки'
      return false
    }
  }

  const cancelSubscription = async () => {
    try {
      const response = await subscriptionsAPI.cancelSubscription()
      userState.subscription = response.data
      return true
    } catch (error) {
      userState.error = error.response?.data?.message || 'Ошибка отмены подписки'
      return false
    }
  }

  const deleteAccount = async () => {
    try {
      await usersAPI.deleteUser()
      clearSession()
      return true
    } catch (error) {
      userState.error = error.response?.data?.message || 'Ошибка удаления аккаунта'
      return false
    }
  }

  return {
    state: userState,
    login,
    register,
    logout,
    fetchCurrentUser,
    fetchUserStats,
    fetchCurrentSubscription,
    updateUserName,
    updateUserPassword,
    createSubscription,
    cancelSubscription,
    deleteAccount
  }
}
