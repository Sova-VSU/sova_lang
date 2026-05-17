<template>
  <section
    class="scenario-page"
    :style="themeStyles"
  >
    <!-- Декорации на фоне -->
    <div v-if="theme.decorations && theme.decorations.length" class="scenario-decorations">
      <span
        v-for="(deco, i) in theme.decorations"
        :key="i"
        class="decoration"
        :style="{ animationDelay: i * 0.5 + 's' }"
      >
        {{ deco }}
      </span>
    </div>

    <!-- Верхняя панель -->
    <div class="scenario-topbar">
      <button class="back-btn" @click="$emit('back')">← Назад</button>
      <span class="step-counter">{{ currentStep.title || scenario.title }}</span>
      <button class="sound-btn" @click="soundEnabled = !soundEnabled">
        {{ soundEnabled ? '🔊 Звук' : '🔇 Выкл' }}
      </button>
    </div>

    <div class="scenario-card">
      <!-- Заголовок шага -->
      <div class="scenario-card__header">
        <span class="scenario-card__emoji">{{ currentStep.emoji || scenario.emoji }}</span>
        <h2>{{ scenario.title }}</h2>
      </div>

      <!-- Реплика NPC -->
      <BaristaMessage
        v-if="currentStep.npc"
        :text="currentStep.npc.text"
        :avatar="currentStep.npc.avatar"
      />

      <!-- Выбор -->
      <ChoiceCards
        v-if="currentStep.choices"
        :label="currentStep.choices.label"
        :options="currentStep.choices.options"
        :selected="selectedChoice"
        @select="handleChoice"
      />

      <!-- Предупреждение -->
      <div v-if="needChoiceWarning" class="choice-warning">
        ⚠️ Сначала выберите вариант выше
      </div>

      <!-- Задание -->
      <component
        v-if="showTask"
        :is="taskComponent"
        :key="taskKey"
        :config="resolvedTaskConfig"
        @complete="handleTaskComplete"
      />

      <!-- Фидбэк -->
      <FeedbackBlock
        :show="feedbackVisible"
        :type="feedbackData.type"
        :text="feedbackData.text"
        :tip="feedbackData.tip"
      />

      <!-- Кнопки -->
      <div class="scenario-card__actions">
        <button
          v-if="canGoNext && !currentStep.isFinal"
          class="btn-next"
          @click="goToNextStep"
        >
          Далее →
        </button>

        <button
          v-if="feedbackVisible && !lastResultCorrect"
          class="btn-retry"
          @click="retryTask"
        >
          🔄 Попробовать снова
        </button>

        <button
          v-if="canGoNext && currentStep.isFinal"
          class="btn-finish"
          @click="showResults = true"
        >
          🎉 Завершить сценарий
        </button>
      </div>
    </div>

    <ScenarioResults
      :show="showResults"
      :history="history"
      :completion-message="scenario.completionMessage"
      @close="showResults = false"
      @restart="restartScenario"
    />
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import BaristaMessage from './BaristaMessage.vue'
import ChoiceCards from './ChoiceCards.vue'
import FeedbackBlock from './FeedbackBlock.vue'
import ScenarioResults from './ScenarioResults.vue'
import { getTaskComponent } from './tasks/index.js'

const props = defineProps({
  scenario: { type: Object, required: true }
})

defineEmits(['back'])

// ===== ТЕМА =====
const theme = computed(() => props.scenario.theme || {
  background: '#f4f7fb',
  cardBackground: '#fffdf7',
  accentColor: '#3b82f6',
  primaryButton: '#2563eb',
  npcBubbleColor: '#fef9c3',
  decorations: []
})

const themeStyles = computed(() => ({
  '--scenario-bg': theme.value.background,
  '--scenario-card-bg': theme.value.cardBackground,
  '--scenario-accent': theme.value.accentColor,
  '--scenario-primary-btn': theme.value.primaryButton,
  '--scenario-npc-bg': theme.value.npcBubbleColor
}))

// ===== Состояние =====
const currentStepId = ref(props.scenario.startStepId)
const selectedChoice = ref(null)
const soundEnabled = ref(true)
const feedbackVisible = ref(false)
const feedbackData = ref({ type: 'success', text: '', tip: '' })
const lastResultCorrect = ref(false)
const showResults = ref(false)
const taskKey = ref(0)
const history = ref([])
const taskCompleted = ref(false)
const needChoiceWarning = ref(false)

// ===== Computed =====
const currentStep = computed(() => props.scenario.steps[currentStepId.value])

const currentTaskIndex = ref(0)

const currentTaskRaw = computed(() => {
  if (!currentStep.value.tasks) return null
  return currentStep.value.tasks[currentTaskIndex.value] || null
})

function resolveTaskConfig(task, choice) {
  if (!task) return null
  if (task.configByChoice && choice != null) {
    return task.configByChoice[choice] ?? task.config
  }
  const cfg = task.config
  if (typeof cfg === 'function') {
    return cfg(choice)
  }
  return cfg
}

const resolvedTaskConfig = computed(() => {
  return resolveTaskConfig(currentTaskRaw.value, selectedChoice.value)
})

const taskComponent = computed(() => {
  if (!currentTaskRaw.value) return null
  return getTaskComponent(currentTaskRaw.value.type)
})

const hasChoices = computed(() => !!currentStep.value.choices)
const hasTasks = computed(() => !!currentStep.value.tasks?.length)
const requiresChoice = computed(() => !!currentStep.value.requireChoice)

const showTask = computed(() => {
  if (!currentTaskRaw.value || !taskComponent.value) return false
  if (taskCompleted.value) return false
  if (requiresChoice.value && !selectedChoice.value) return false
  return true
})

const canGoNext = computed(() => {
  if (hasChoices.value && requiresChoice.value && !selectedChoice.value) return false

  if (hasTasks.value) {
    const isLastTask = currentTaskIndex.value >= currentStep.value.tasks.length - 1
    if (!isLastTask) return false
    if (!taskCompleted.value || !lastResultCorrect.value) return false
  }

  if (!hasTasks.value && hasChoices.value && !selectedChoice.value) return false

  return true
})

// ===== Watchers =====
watch(currentStepId, () => {
  resetStepState()
})

// ===== Методы =====
function handleChoice(choiceId) {
  selectedChoice.value = choiceId
  needChoiceWarning.value = false

  if (hasTasks.value && !taskCompleted.value) {
    taskKey.value++
    feedbackVisible.value = false
  }
}

function handleTaskComplete(result) {
  if (requiresChoice.value && !selectedChoice.value) {
    needChoiceWarning.value = true
    return
  }

  feedbackData.value = {
    type: result.correct ? 'success' : 'error',
    text: result.message,
    tip: result.tip || ''
  }
  feedbackVisible.value = true
  lastResultCorrect.value = result.correct
  taskCompleted.value = result.correct

  const choiceLabel = selectedChoice.value
    ? currentStep.value.choices?.options.find(o => o.id === selectedChoice.value)?.label || selectedChoice.value
    : ''

  history.value.push({
    step: currentStep.value.title || currentStepId.value,
    answer: choiceLabel ? `${choiceLabel}: ${result.userAnswer}` : result.userAnswer,
    status: result.correct
      ? (result.type === 'rude' ? 'rude' : 'polite')
      : 'error'
  })

  if (soundEnabled.value && window.speechSynthesis) {
    const msg = new SpeechSynthesisUtterance(
      result.correct ? 'Great job!' : 'Try again.'
    )
    msg.lang = 'en-US'
    msg.rate = 0.9
    window.speechSynthesis.speak(msg)
  }
}

function retryTask() {
  feedbackVisible.value = false
  lastResultCorrect.value = false
  taskCompleted.value = false
  taskKey.value++
}

function goToNextStep() {
  if (
    currentStep.value.tasks &&
    currentTaskIndex.value < currentStep.value.tasks.length - 1
  ) {
    currentTaskIndex.value++
    feedbackVisible.value = false
    lastResultCorrect.value = false
    taskCompleted.value = false
    taskKey.value++
    return
  }

  const step = currentStep.value
  let nextId = step.next
  if (step.nextByChoice && selectedChoice.value != null) {
    nextId = step.nextByChoice[selectedChoice.value] ?? step.next
  } else if (typeof step.next === 'function') {
    nextId = step.next(selectedChoice.value, history.value)
  }

  if (nextId && props.scenario.steps[nextId]) {
    currentStepId.value = nextId
  } else {
    showResults.value = true
  }
}

function resetStepState() {
  selectedChoice.value = null
  feedbackVisible.value = false
  lastResultCorrect.value = false
  feedbackData.value = { type: 'success', text: '', tip: '' }
  currentTaskIndex.value = 0
  taskKey.value++
  taskCompleted.value = false
  needChoiceWarning.value = false
}

function restartScenario() {
  currentStepId.value = props.scenario.startStepId
  history.value = []
  showResults.value = false
  resetStepState()
}
</script>

<style scoped>
.scenario-page {
  padding: 16px 24px 48px;
  max-width: 800px;
  margin: 0 auto;
  position: relative;
  min-height: 100vh;
  background: var(--scenario-bg);
  transition: background 0.5s ease;
}

/* Декоративные эмодзи */
.scenario-decorations {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.decoration {
  position: absolute;
  font-size: 80px;
  opacity: 0.08;
  animation: float 8s ease-in-out infinite;
}

.decoration:nth-child(1) {
  top: 10%;
  left: 5%;
}

.decoration:nth-child(2) {
  top: 60%;
  right: 8%;
  font-size: 100px;
}

.decoration:nth-child(3) {
  bottom: 15%;
  left: 10%;
  font-size: 90px;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.scenario-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  position: relative;
  z-index: 1;
}

.back-btn,
.sound-btn {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
  border-radius: 10px;
  padding: 6px 14px;
}

.sound-btn:hover,
.back-btn:hover {
  background: white;
}

.step-counter {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  padding: 6px 16px;
  border-radius: 10px;
}

.scenario-card {
  background: var(--scenario-card-bg);
  border-radius: 24px;
  padding: 36px 32px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
  border-top: 4px solid var(--scenario-accent);
}

.scenario-card__header {
  text-align: center;
  margin-bottom: 24px;
}

.scenario-card__emoji {
  font-size: 48px;
  display: block;
  margin-bottom: 8px;
}

.scenario-card__header h2 {
  font-size: 28px;
  font-weight: 800;
  color: var(--scenario-accent);
  letter-spacing: 1px;
}

.choice-warning {
  background: #fef3c7;
  border: 2px solid #fbbf24;
  border-radius: 12px;
  padding: 12px 16px;
  margin: 16px 0;
  text-align: center;
  color: #92400e;
  font-weight: 600;
}

.scenario-card__actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 24px;
}

.btn-next {
  background: var(--scenario-primary-btn);
  color: white;
  border: none;
  border-radius: 14px;
  padding: 14px 32px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-next:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.btn-retry {
  background: #f59e0b;
  color: white;
  border: none;
  border-radius: 14px;
  padding: 14px 32px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
}

.btn-retry:hover {
  background: #d97706;
}

.btn-finish {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: white;
  border: none;
  border-radius: 14px;
  padding: 14px 32px;
  font-weight: 700;
  cursor: pointer;
}
</style>