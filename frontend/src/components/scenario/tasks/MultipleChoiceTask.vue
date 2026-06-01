<template>
  <div class="task task--multiple-choice">
    <h3 class="task__title"> {{ config.title || 'Выберите правильную реплику:' }}</h3>

    <p v-if="config.question" class="task__question">{{ config.question }}</p>

    <div class="mc__options">
      <button
        v-for="(opt, idx) in config.options"
        :key="idx"
        class="option-btn"
        :class="{ 'option-btn--selected': selectedIndex === idx }"
        @click="selectedIndex = idx"
      >
        <span class="option-btn__letter">{{ String.fromCharCode(65 + idx) }}.</span>
        <span>{{ opt }}</span>
      </button>
    </div>

    <button
      class="task__check-btn"
      :disabled="selectedIndex === null"
      @click="check"
    >
      ✓ Проверить
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  config: { type: Object, required: true }
})

const emit = defineEmits(['complete'])

const selectedIndex = ref(null)

function check() {
  const correct = selectedIndex.value === props.config.correctIndex
  const userAnswer = props.config.options[selectedIndex.value]

  emit('complete', {
    correct,
    userAnswer,
    type: correct ? 'polite' : 'error',
    message: correct
      ? '✅ Правильно!'
      : '❌ Неправильно. Попробуйте ещё раз!',
    tip: correct ? props.config.tip : ''
  })
}
</script>

<style scoped>
.task__title {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #334155;
  margin-bottom: 16px;
}

.task__question {
  font-size: 16px;
  margin-bottom: 16px;
  color: #1e293b;
  text-align: center;
}

.mc__options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #eff6ff;
  border: 2px solid #bfdbfe;
  color: #1e40af;
  border-radius: 12px;
  padding: 14px 20px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.15s;
  text-align: left;
}

.option-btn:hover {
  background: #dbeafe;
}

.option-btn--selected {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.option-btn__letter {
  font-weight: 800;
  opacity: 0.8;
}

.task__check-btn {
  margin-top: 20px;
  background: #22c55e;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 28px;
  font-weight: 700;
  cursor: pointer;
  width: 100%;
  font-size: 15px;
  transition: 0.2s;
}

.task__check-btn:hover:not(:disabled) {
  background: #16a34a;
}

.task__check-btn:disabled {
  opacity: 0.4;
  cursor: default;
}
</style>