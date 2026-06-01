<template>
  <div class="task task--fill-blank">
    <h3 class="task__title"> {{ config.title || 'Заполните пропуск:' }}</h3>

    <div class="fill-blank__sentence">
      <span v-for="(part, i) in sentenceParts" :key="i">
        <span v-if="part.type === 'text'">{{ part.value }}</span>
        <span
          v-else
          class="fill-blank__slot"
          :class="{ 'fill-blank__slot--filled': selectedAnswer }"
        >
          {{ selectedAnswer || '___' }}
        </span>
      </span>
    </div>

    <div class="fill-blank__options">
      <button
        v-for="opt in config.options"
        :key="opt"
        class="option-btn"
        :class="{ 'option-btn--selected': selectedAnswer === opt }"
        @click="selectedAnswer = opt"
      >
        {{ opt }}
      </button>
    </div>

    <button
      class="task__check-btn"
      :disabled="!selectedAnswer"
      @click="check"
    >
      ✓ Проверить
    </button>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  config: { type: Object, required: true }
})

const emit = defineEmits(['complete'])

const selectedAnswer = ref(null)
const sentenceParts = computed(() => {
  const parts = props.config.sentence.split('___')
  const result = []
  parts.forEach((p, i) => {
    result.push({ type: 'text', value: p })
    if (i < parts.length - 1) {
      result.push({ type: 'blank' })
    }
  })
  return result
})

function check() {
  const correct = Array.isArray(props.config.correctAnswer)
    ? props.config.correctAnswer.includes(selectedAnswer.value)
    : selectedAnswer.value === props.config.correctAnswer

  emit('complete', {
    correct,
    userAnswer: selectedAnswer.value,
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

.fill-blank__sentence {
  background: #fefce8;
  border-radius: 14px;
  padding: 20px 24px;
  font-size: 18px;
  text-align: center;
  margin-bottom: 20px;
  color: #1e293b;
}

.fill-blank__slot {
  display: inline-block;
  min-width: 80px;
  padding: 4px 12px;
  border-bottom: 2px dashed #94a3b8;
  font-weight: 700;
  color: #64748b;
  margin: 0 4px;
}

.fill-blank__slot--filled {
  border-bottom-color: #22c55e;
  color: #22c55e;
}

.fill-blank__options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.option-btn {
  background: #eff6ff;
  border: 2px solid #bfdbfe;
  color: #1e40af;
  border-radius: 10px;
  padding: 10px 20px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.15s;
}

.option-btn:hover {
  background: #dbeafe;
}

.option-btn--selected {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
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