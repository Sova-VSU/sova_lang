import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import MultipleChoiceTask from '../components/scenario/tasks/MultipleChoiceTask.vue'

const config = {
  question: 'Как сказать «у меня температура»?',
  options: ['I have temperature.', 'I have a fever.', 'I am hot.', 'Temperature me.'],
  correctIndex: 1,
  tip: 'fever — жар, температура.'
}

describe('MultipleChoiceTask', () => {
  it('отображает вопрос', () => {
    const wrapper = mount(MultipleChoiceTask, { props: { config } })
    expect(wrapper.find('.task__question').text()).toBe(config.question)
  })

  it('отображает все варианты с буквами A, B, C, D', () => {
    const wrapper = mount(MultipleChoiceTask, { props: { config } })
    const btns = wrapper.findAll('.option-btn')
    expect(btns).toHaveLength(4)
    expect(btns[0].find('.option-btn__letter').text()).toBe('A.')
    expect(btns[1].find('.option-btn__letter').text()).toBe('B.')
  })

  it('кнопка "Проверить" задизейблена без выбора', () => {
    const wrapper = mount(MultipleChoiceTask, { props: { config } })
    expect(wrapper.find('.task__check-btn').attributes('disabled')).toBeDefined()
  })

  it('кнопка "Проверить" активируется после выбора', async () => {
    const wrapper = mount(MultipleChoiceTask, { props: { config } })
    await wrapper.findAll('.option-btn')[0].trigger('click')
    expect(wrapper.find('.task__check-btn').attributes('disabled')).toBeUndefined()
  })

  it('эмитит correct:true для правильного индекса', async () => {
    const wrapper = mount(MultipleChoiceTask, { props: { config } })
    await wrapper.findAll('.option-btn')[1].trigger('click') // correctIndex = 1
    await wrapper.find('.task__check-btn').trigger('click')
    const result = wrapper.emitted('complete')[0][0]
    expect(result.correct).toBe(true)
    expect(result.userAnswer).toBe('I have a fever.')
  })

  it('эмитит correct:false с правильным ответом в сообщении', async () => {
    const wrapper = mount(MultipleChoiceTask, { props: { config } })
    await wrapper.findAll('.option-btn')[0].trigger('click') // неверный
    await wrapper.find('.task__check-btn').trigger('click')
    const result = wrapper.emitted('complete')[0][0]
    expect(result.correct).toBe(false)
    expect(result.message).toContain('I have a fever.')
  })

  it('применяет класс --selected к выбранному варианту', async () => {
    const wrapper = mount(MultipleChoiceTask, { props: { config } })
    const btn = wrapper.findAll('.option-btn')[2]
    await btn.trigger('click')
    expect(btn.classes()).toContain('option-btn--selected')
  })

  it('работает без поля question', () => {
    const configNoQ = { ...config, question: undefined }
    const wrapper = mount(MultipleChoiceTask, { props: { config: configNoQ } })
    expect(wrapper.find('.task__question').exists()).toBe(false)
  })
})
