import { describe, it, expect, beforeEach, vi } from 'vitest'
import { userState, clearSession, restoreSession, expireSession, setSessionExpiredHandler } from '../stores/session'

describe('session', () => {
  beforeEach(() => {
    localStorage.clear()
    clearSession()
  })

  it('clearSession очищает localStorage и сбрасывает состояние', () => {
    localStorage.setItem('accessToken', 'token123')
    localStorage.setItem('refreshToken', 'refresh123')
    userState.user = { id: '1', name: 'Ivan' }
    userState.isAuthenticated = true

    clearSession()

    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(localStorage.getItem('refreshToken')).toBeNull()
    expect(userState.user).toBeNull()
    expect(userState.isAuthenticated).toBe(false)
    expect(userState.subscription).toBeNull()
    expect(userState.stats).toBeNull()
  })

  it('restoreSession устанавливает isAuthenticated=true если есть токен', () => {
    localStorage.setItem('accessToken', 'some-token')
    restoreSession()
    expect(userState.isAuthenticated).toBe(true)
  })

  it('restoreSession устанавливает isAuthenticated=false без токена', () => {
    restoreSession()
    expect(userState.isAuthenticated).toBe(false)
  })

  it('expireSession вызывает clearSession и обработчик', () => {
    localStorage.setItem('accessToken', 'token')
    userState.isAuthenticated = true
    const handler = vi.fn()
    setSessionExpiredHandler(handler)

    expireSession()

    expect(userState.isAuthenticated).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
    expect(handler).toHaveBeenCalledOnce()
  })

  it('expireSession не падает если обработчик не задан', () => {
    setSessionExpiredHandler(null)
    expect(() => expireSession()).not.toThrow()
  })
})
