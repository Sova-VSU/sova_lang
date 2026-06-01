<template>
  <div class="task task--matching">
    <h3 class="task__title"> {{ config.title || 'Соедините пары:' }}</h3>

    <div class="matching-container">
      <!-- Левая колонка -->
      <div class="matching-column">
        <div
          v-for="item in leftItems"
          :key="item.id"
          class="matching-item matching-item--left"
          :class="getItemClass(item, 'left')"
          @click="selectLeft(item)"
        >
          {{ item.text }}
        </div>
      </div>

      <!-- Правая колонка -->
      <div class="matching-column">
        <div
          v-for="item in rightItems"
          :key="item.id"
          class="matching-item matching-item--right"
          :class="getItemClass(item, 'right')"
          @click="selectRight(item)"
        >
          {{ item.text }}
        </div>
      </div>
    </div>

    <div class="matching-stats">
      Найдено пар: {{ matchedCount }}/{{ totalPairs }}
      <span class="matching-errors">Ошибок: {{ errorCount }}</span>
    </div>

    <button
      v-if="matchedCount === totalPairs"
      class="task__check-btn"
      @click="finish"
    >
      ✓ Завершить
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  config: { type: Object, required: true }
})

const emit = defineEmits(['complete'])

const leftItems = ref([])
const rightItems = ref([])

const selectedLeft = ref(null)
const selectedRight = ref(null)

const errorPair = ref(null)
const errorCount = ref(0)

const totalPairs = computed(() => props.config.pairs.length)
const matchedCount = computed(() =>
  leftItems.value.filter(i => i.matched).length
)

onMounted(() => {
  initGame()
})

function initGame() {
  const pairs = props.config.pairs

  leftItems.value = pairs.map((p, index) => ({
    id: index,
    text: p.en,
    matched: false
  }))

  rightItems.value = pairs
    .map((p, index) => ({
      id: index,
      text: p.ru,
      matched: false
    }))
    .sort(() => Math.random() - 0.5)
}

function selectLeft(item) {
  if (item.matched || errorPair.value) return
  selectedLeft.value = item
  tryMatch()
}

function selectRight(item) {
  if (item.matched || errorPair.value) return
  selectedRight.value = item
  tryMatch()
}

function tryMatch() {
  if (!selectedLeft.value || !selectedRight.value) return

  if (selectedLeft.value.id === selectedRight.value.id) {
    selectedLeft.value.matched = true
    selectedRight.value.matched = true
    resetSelection()
  } else {
    errorPair.value = {
      left: selectedLeft.value.id,
      right: selectedRight.value.id
    }

    errorCount.value++

    setTimeout(() => {
      errorPair.value = null
      resetSelection()
    }, 600)
  }
}

function resetSelection() {
  selectedLeft.value = null
  selectedRight.value = null
}

function getItemClass(item, side) {
  return {
    'matching-item--selected':
      (side === 'left' && selectedLeft.value?.id === item.id) ||
      (side === 'right' && selectedRight.value?.id === item.id),

    'matching-item--matched': item.matched,

    'matching-item--error':
      errorPair.value &&
      (
        (side === 'left' && errorPair.value.left === item.id) ||
        (side === 'right' && errorPair.value.right === item.id)
      )
  }
}

function finish() {
  emit('complete', {
    correct: true,
    userAnswer: `${matchedCount.value} pairs`,
    type: 'polite',
    message: `✅ Все ${totalPairs.value} пар соединены!`,
    tip: props.config.tip || ''
  })
}
</script>

<style scoped>
.task__title {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  margin-bottom: 16px;
  color: #334155;
}

.matching-container {
  display: flex;
  gap: 20px;
  justify-content: space-between;
}

.matching-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.matching-item {
  padding: 12px;
  border-radius: 10px;
  border: 2px solid #e2e8f0;
  cursor: pointer;
  font-weight: 600;
  text-align: center;
  transition: 0.25s;
}

.matching-item--left {
  background: #eff6ff;
  color: #1e40af;
}

.matching-item--right {
  background: #fef3c7;
  color: #92400e;
}

.matching-item--selected {
  border-color: #3b82f6;
  transform: scale(1.05);
}

.matching-item--matched {
  background: #d1fae5 !important;
  border-color: #22c55e !important;
  color: #166534 !important;
  cursor: default;
}

.matching-item--error {
  background: #fee2e2 !important;
  border-color: #ef4444 !important;
  color: #991b1b !important;
  animation: shake 0.3s ease;
}

.matching-stats {
  margin-top: 16px;
  font-weight: 600;
  color: #475569;
  text-align: center;
}

.matching-errors {
  margin-left: 20px;
  color: #ef4444;
}

.task__check-btn {
  margin-top: 16px;
  background: #22c55e;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 28px;
  font-weight: 700;
  cursor: pointer;
  width: 100%;
}

@keyframes shake {
  0% { transform: translateX(-3px); }
  25% { transform: translateX(3px); }
  50% { transform: translateX(-2px); }
  75% { transform: translateX(2px); }
  100% { transform: translateX(0); }
}
</style>
