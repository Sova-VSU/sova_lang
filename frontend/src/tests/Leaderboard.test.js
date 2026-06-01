import { mount, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'

vi.mock('../stores/userStore', () => ({
  useUserStore: () => ({ state: { user: { id: 'user-1' } } })
}))

vi.mock('../api', () => ({
  leaderboardAPI: {
    getLeaderboard: vi.fn()
  }
}))

import Leaderboard from '../components/Leaderboard.vue'
import { leaderboardAPI } from '../api'

const mockEntries = [
  { id: 'user-1', name: 'Alice', completedScenariosCount: 5, totalXp: 500, rank: 1 },
  { id: 'user-2', name: 'Bob',   completedScenariosCount: 3, totalXp: 300, rank: 2 },
  { id: 'user-3', name: 'Carol', completedScenariosCount: 1, totalXp: 100, rank: 3 },
]

describe('Leaderboard', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('показывает спиннер во время загрузки', () => {
    leaderboardAPI.getLeaderboard.mockReturnValue(new Promise(() => {}))
    const wrapper = mount(Leaderboard)
    expect(wrapper.find('.leaderboard-loading').exists()).toBe(true)
  })

  it('рендерит строки лидерборда после загрузки', async () => {
    leaderboardAPI.getLeaderboard.mockResolvedValue({ data: mockEntries })
    const wrapper = mount(Leaderboard)
    await flushPromises()
    const rows = wrapper.findAll('.leaderboard-row:not(.leaderboard-row--header)')
    expect(rows).toHaveLength(3)
  })

  it('первые три места показывают медали', async () => {
    leaderboardAPI.getLeaderboard.mockResolvedValue({ data: mockEntries })
    const wrapper = mount(Leaderboard)
    await flushPromises()
    expect(wrapper.find('.medal.gold').exists()).toBe(true)
    expect(wrapper.find('.medal.silver').exists()).toBe(true)
    expect(wrapper.find('.medal.bronze').exists()).toBe(true)
  })

  it('четвёртое место показывает номер, а не медаль', async () => {
    const fourEntries = [
      ...mockEntries,
      { id: 'user-4', name: 'Dave', completedScenariosCount: 0, totalXp: 0, rank: 4 }
    ]
    leaderboardAPI.getLeaderboard.mockResolvedValue({ data: fourEntries })
    const wrapper = mount(Leaderboard)
    await flushPromises()
    expect(wrapper.find('.rank-number').exists()).toBe(true)
    expect(wrapper.find('.rank-number').text()).toBe('4')
  })

  it('текущий пользователь выделяется классом --current-user', async () => {
    leaderboardAPI.getLeaderboard.mockResolvedValue({ data: mockEntries })
    const wrapper = mount(Leaderboard)
    await flushPromises()
    expect(wrapper.find('.leaderboard-row--current-user').exists()).toBe(true)
    expect(wrapper.find('.current-badge').text()).toBe('Вы')
  })

  it('показывает сообщение об ошибке при сбое запроса', async () => {
    leaderboardAPI.getLeaderboard.mockRejectedValue(new Error('Network error'))
    const wrapper = mount(Leaderboard)
    await flushPromises()
    expect(wrapper.find('.leaderboard-error').exists()).toBe(true)
    expect(wrapper.find('.leaderboard-error').text()).toContain('Не удалось загрузить')
  })

  it('показывает сообщение когда список пуст', async () => {
    leaderboardAPI.getLeaderboard.mockResolvedValue({ data: [] })
    const wrapper = mount(Leaderboard)
    await flushPromises()
    expect(wrapper.find('.leaderboard-empty').exists()).toBe(true)
  })

  it('отображает количество пройденных сценариев', async () => {
    leaderboardAPI.getLeaderboard.mockResolvedValue({ data: mockEntries })
    const wrapper = mount(Leaderboard)
    await flushPromises()
    const completed = wrapper.findAll('.completed').slice(1) // пропускаем заголовок
    expect(completed[0].text()).toBe('5')
    expect(completed[1].text()).toBe('3')
  })
})
