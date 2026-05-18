<template>
  <div class="task task--phrase-builder">
    <h3 class="task__title">{{ config.title || 'Составьте фразу:' }}</h3>

    <div class="phrase-builder__sentence">
      <TransitionGroup name="word-anim">
        <button
          v-for="(word, index) in sentence"
          :key="word + '-' + index"
          class="word-chip word-chip--in-sentence"
          @click="removeWord(index)"
        >
          {{ word }}
        </button>
      </TransitionGroup>
      <span v-if="sentence.length === 0" class="phrase-builder__placeholder">
        Нажмите на слова ниже...
      </span>
    </div>

    <h4 class="task__subtitle">Доступные слова:</h4>
    <div class="phrase-builder__words">
      <button
        v-for="(word, index) in availableFiltered"
        :key="word + '-avail-' + index"
        class="word-chip word-chip--available"
        @click="addWord(word, index)"
      >
        {{ word }}
      </button>
    </div>

    <button
      class="task__check-btn"
      :disabled="sentence.length === 0"
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

const sentence = ref([])
const removedIndices = ref(new Set())

const availableFiltered = computed(() =>
  props.config.availableWords.filter((_, i) => !removedIndices.value.has(i))
)

function addWord(word, indexInFiltered) {
  const originalIndex = getOriginalIndex(indexInFiltered)
  sentence.value.push(word)
  removedIndices.value.add(originalIndex)
}

function removeWord(index) {
  const word = sentence.value[index]
  sentence.value.splice(index, 1)

  for (let i = 0; i < props.config.availableWords.length; i++) {
    if (props.config.availableWords[i] === word && removedIndices.value.has(i)) {
      removedIndices.value.delete(i)
      break
    }
  }
}

function getOriginalIndex(filteredIndex) {
  let count = 0
  for (let i = 0; i < props.config.availableWords.length; i++) {
    if (!removedIndices.value.has(i)) {
      if (count === filteredIndex) return i
      count++
    }
  }
  return -1
}

function check() {
  const userAnswer = sentence.value.join(' ')
  const isPolite = sentence.value.some(w =>
    (props.config.politeWords || []).includes(w)
  )

  // Проверка соответствия выбранному варианту (если задано)
  let isCorrectChoice = true
  if (props.config.requiredWord) {
    isCorrectChoice = sentence.value
      .map(w => w.toLowerCase())
      .includes(props.config.requiredWord.toLowerCase())
  }

  if (!isCorrectChoice) {
    emit('complete', {
      correct: false,
      userAnswer,
      type: 'mismatch',
      message: '❌ Ваша фраза не соответствует выбранному варианту!',
      tip: 'Картинка и текст должны совпадать.'
    })
    return
  }

  emit('complete', {
    correct: true,
    userAnswer,
    type: isPolite ? 'polite' : 'rude',
    message: isPolite
      ? (props.config.politeMessage || '✅ Отлично! Вежливая форма.')
      : (props.config.rudeMessage || '⚠️ Понятно, но попробуйте вежливее.'),
    tip: isPolite ? props.config.politeTip : props.config.rudeTip
  })
}
</script>

<style scoped>
.task {
  margin-top: 16px;
}

.task__title {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #334155;
  margin-bottom: 12px;
}

.task__subtitle {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin: 16px 0 10px;
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

.phrase-builder__sentence {
  min-height: 56px;
  background: #fefce8;
  border: 2px dashed #e2e8f0;
  border-radius: 14px;
  padding: 12px 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.phrase-builder__placeholder {
  color: #94a3b8;
  font-size: 14px;
}

.phrase-builder__words {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.word-chip {
  border-radius: 10px;
  padding: 8px 14px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: 0.15s;
}

.word-chip--in-sentence {
  background: white;
  border: 2px solid #cbd5e1;
  color: #1e293b;
}

.word-chip--in-sentence:hover {
  border-color: #ef4444;
  color: #ef4444;
}

.word-chip--available {
  background: #eff6ff;
  border: 2px solid #bfdbfe;
  color: #1e40af;
}

.word-chip--available:hover {
  background: #dbeafe;
}

.word-anim-enter-active,
.word-anim-leave-active {
  transition: all 0.2s ease;
}

.word-anim-enter-from,
.word-anim-leave-to {
  opacity: 0;
  transform: scale(0.8);
}
</style>