import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import FillBlankTask from '../components/scenario/tasks/FillBlankTask.vue'

const config = {
  sentence: 'I need something for a ___ .',
  options: ['headache', 'medicine', 'pharmacy'],
  correctAnswer: 'headache',
  tip: 'something for... — что-нибудь от...'
}

describe('FillBlankTask', () => {
  it('отображает все варианты ответа', () => {
    const wrapper = mount(FillBlankTask, { props: { config } })
    const buttons = wrapper.findAll('.option-btn')
    expect(buttons).toHaveLength(3)
    expect(buttons[0].text()).toBe('headache')
  })

  it('кнопка "Проверить" задизейблена пока нет выбора', () => {
    const wrapper = mount(FillBlankTask, { props: { config } })
    const checkBtn = wrapper.find('.task__check-btn')
    expect(checkBtn.attributes('disabled')).toBeDefined()
  })

  it('кнопка "Проверить" активируется после выбора', async () => {
    const wrapper = mount(FillBlankTask, { props: { config } })
    await wrapper.findAll('.option-btn')[0].trigger('click')
    expect(wrapper.find('.task__check-btn').attributes('disabled')).toBeUndefined()
  })

  it('эмитит correct:true при правильном ответе', async () => {
    const wrapper = mount(FillBlankTask, { props: { config } })
    await wrapper.findAll('.option-btn')[0].trigger('click') // headache — верный
    await wrapper.find('.task__check-btn').trigger('click')
    const emitted = wrapper.emitted('complete')
    expect(emitted).toBeTruthy()
    expect(emitted[0][0].correct).toBe(true)
    expect(emitted[0][0].userAnswer).toBe('headache')
  })

  it('эмитит correct:false при неправильном ответе', async () => {
    const wrapper = mount(FillBlankTask, { props: { config } })
    await wrapper.findAll('.option-btn')[1].trigger('click') // medicine — неверный
    await wrapper.find('.task__check-btn').trigger('click')
    const emitted = wrapper.emitted('complete')
    expect(emitted[0][0].correct).toBe(false)
    expect(emitted[0][0].message).toContain('headache')
  })

  it('поддерживает массив правильных ответов', async () => {
    const multiConfig = {
      ...config,
      sentence: 'I\'ll try the ___ , please.',
      options: ['espresso', 'latte', 'tea'],
      correctAnswer: ['espresso', 'latte']
    }
    const wrapper = mount(FillBlankTask, { props: { config: multiConfig } })
    await wrapper.findAll('.option-btn')[1].trigger('click') // latte — в массиве
    await wrapper.find('.task__check-btn').trigger('click')
    expect(wrapper.emitted('complete')[0][0].correct).toBe(true)
  })

  it('применяет класс --filled к слоту после выбора', async () => {
    const wrapper = mount(FillBlankTask, { props: { config } })
    expect(wrapper.find('.fill-blank__slot--filled').exists()).toBe(false)
    await wrapper.findAll('.option-btn')[0].trigger('click')
    expect(wrapper.find('.fill-blank__slot--filled').exists()).toBe(true)
  })

  it('применяет класс --selected к выбранной кнопке', async () => {
    const wrapper = mount(FillBlankTask, { props: { config } })
    const firstBtn = wrapper.findAll('.option-btn')[0]
    await firstBtn.trigger('click')
    expect(firstBtn.classes()).toContain('option-btn--selected')
  })
})
