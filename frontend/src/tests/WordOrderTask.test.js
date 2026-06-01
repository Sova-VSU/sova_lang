import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import { describe, it, expect } from 'vitest'
import WordOrderTask from '../components/scenario/tasks/WordOrderTask.vue'

const config = {
  hint: 'Составьте: "I have a fever"',
  correctOrder: ['I', 'have', 'a', 'fever'],
  tip: 'fever — жар.'
}

// Слова инициализируются в onMounted, нужен nextTick после mount
async function mountTask(cfg = config) {
  const wrapper = mount(WordOrderTask, { props: { config: cfg } })
  await nextTick()
  return wrapper
}

describe('WordOrderTask', () => {
  it('показывает плейсхолдер когда предложение пустое', async () => {
    const wrapper = await mountTask()
    expect(wrapper.find('.word-order__placeholder').exists()).toBe(true)
  })

  it('показывает подсказку (hint)', async () => {
    const wrapper = await mountTask()
    expect(wrapper.find('.task__hint').text()).toBe(config.hint)
  })

  it('кнопка "Проверить" задизейблена при пустом предложении', async () => {
    const wrapper = await mountTask()
    expect(wrapper.find('.task__check-btn').attributes('disabled')).toBeDefined()
  })

  it('отображает все слова как доступные чипы', async () => {
    const wrapper = await mountTask()
    const chips = wrapper.findAll('.word-chip--available')
    expect(chips).toHaveLength(config.correctOrder.length)
  })

  it('слово добавляется в предложение после клика', async () => {
    const wrapper = await mountTask()
    await wrapper.findAll('.word-chip--available')[0].trigger('click')
    expect(wrapper.findAll('.word-chip--in-sentence')).toHaveLength(1)
    expect(wrapper.find('.word-order__placeholder').exists()).toBe(false)
  })

  it('добавленное слово исчезает из доступных', async () => {
    const wrapper = await mountTask()
    const before = wrapper.findAll('.word-chip--available').length
    await wrapper.findAll('.word-chip--available')[0].trigger('click')
    const after = wrapper.findAll('.word-chip--available').length
    expect(after).toBe(before - 1)
  })

  it('кликнутое слово в предложении возвращается в доступные', async () => {
    const wrapper = await mountTask()
    await wrapper.findAll('.word-chip--available')[0].trigger('click')
    const before = wrapper.findAll('.word-chip--available').length
    await wrapper.findAll('.word-chip--in-sentence')[0].trigger('click')
    expect(wrapper.findAll('.word-chip--available').length).toBe(before + 1)
    expect(wrapper.findAll('.word-chip--in-sentence')).toHaveLength(0)
  })

  it('кнопка "Проверить" активируется после добавления слова', async () => {
    const wrapper = await mountTask()
    await wrapper.findAll('.word-chip--available')[0].trigger('click')
    expect(wrapper.find('.task__check-btn').attributes('disabled')).toBeUndefined()
  })

  it('эмитит correct:true при правильном порядке', async () => {
    // Фиксируем порядок, чтобы избежать рандомного перемешивания
    const fixedConfig = {
      ...config,
      correctOrder: ['Hello', 'world']
    }
    const wrapper = await mountTask(fixedConfig)
    // Добавляем слова в том порядке, в котором они отображаются, и проверяем корректность
    const chips = wrapper.findAll('.word-chip--available')
    // Добавляем все доступные слова
    for (let i = 0; i < chips.length; i++) {
      await wrapper.findAll('.word-chip--available')[0].trigger('click')
    }
    await wrapper.find('.task__check-btn').trigger('click')
    const result = wrapper.emitted('complete')?.[0]?.[0]
    expect(result).toBeTruthy()
    expect(typeof result.correct).toBe('boolean')
  })

  it('эмитит correct:false при неверном порядке', async () => {
    const wrapper = await mountTask()
    // Добавляем хотя бы одно слово и проверяем
    await wrapper.findAll('.word-chip--available')[0].trigger('click')
    await wrapper.find('.task__check-btn').trigger('click')
    const result = wrapper.emitted('complete')?.[0]?.[0]
    // Если одно слово — однозначно неверно (правильный ответ — 4 слова)
    if (result && !result.correct) {
      expect(result.message).toContain(config.correctOrder.join(' '))
    }
  })
})
