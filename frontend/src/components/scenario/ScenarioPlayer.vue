<template>
  <section
    class="scenario-page"
    :style="themeStyles"
  >
    <!-- фон -->
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

    <div class="corner-actions">
      <button class="corner-btn corner-btn--left" @click="$emit('back')">
        ← Назад
      </button>
      <button class="corner-btn corner-btn--right" @click="soundEnabled = !soundEnabled">
        {{ soundEnabled ? '🔊 Звук' : '🔇 Выкл' }}
      </button>
    </div>

    <div class="scenario-card-wrapper">
      <div class="scenario-card">
        <!-- Заголовок шага -->
        <div class="scenario-card__header">
          <span class="scenario-card__emoji">{{ currentStep.emoji || scenario.emoji }}</span>
          <h2>{{ currentStep.title || scenario.title }}</h2>
        </div>

        <!--  NPC -->
        <div v-if="currentStep.npc" class="npc-block">
          <div class="npc-avatar">{{ currentStep.npc.avatar || '🧑‍🍳' }}</div>
          <div class="npc-bubble" :style="{ background: theme.npcBubbleColor || '#fef9c3' }">
            <p class="npc-text">"{{ currentStep.npc.text }}"</p>
            <button class="speak-btn" @click="speak" :disabled="isSpeaking">
              🔊 Воспроизвести
            </button>
          </div>
        </div>

        <!-- Выбор -->
        <ChoiceCards
          v-if="currentStep.choices"
          :label="currentStep.choices.label"
          :options="currentStep.choices.options"
          :selected="selectedChoice"
          @select="handleChoice"
        />

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

        <!-- Кнопки действий -->
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
            Попробовать снова
          </button>

          <button
            v-if="canGoNext && currentStep.isFinal"
            class="btn-finish"
            @click="handleComplete"
          >
            Завершить сценарий
          </button>
        </div>
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
import ChoiceCards from './ChoiceCards.vue'
import FeedbackBlock from './FeedbackBlock.vue'
import ScenarioResults from './ScenarioResults.vue'
import { getTaskComponent } from './tasks/index.js'
import { useUserStore } from '../../stores/userStore' 


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
const isSpeaking = ref(false)

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
function speak() {
  if (!window.speechSynthesis || !soundEnabled.value) return
  window.speechSynthesis.cancel()

  const utterance = new SpeechSynthesisUtterance(currentStep.value.npc.text)
  utterance.lang = 'en-US'
  utterance.rate = 0.9

  isSpeaking.value = true
  utterance.onend = () => (isSpeaking.value = false)
  utterance.onerror = () => (isSpeaking.value = false)

  window.speechSynthesis.speak(utterance)
}

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
const userStore = useUserStore()
async function handleComplete() {
    await userStore.completeScenario(props.scenario.id)
    showResults.value = true
  }

</script>

<style scoped>
.scenario-page {
  min-height: 100vh;
  width: 100%;
  background: var(--scenario-bg);
  transition: background 0.5s ease;
  position: relative;
}

/* Декоративные эмодзи */
.scenario-decorations {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 0;
}

.decoration {
  position: absolute;
  font-size: 80px;
  opacity: 0.17;
  animation: float 8s ease-in-out infinite;
}

.decoration:nth-child(1) { top: 10%; left: 5%; }
.decoration:nth-child(2) { top: 60%; right: 8%; font-size: 100px; }
.decoration:nth-child(3) { bottom: 15%; left: 10%; font-size: 90px; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.corner-actions {
  position: fixed;
  top: 80px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
  z-index: 100;
  pointer-events: none;
}

.corner-btn {
  pointer-events: auto;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0, 0, 0, 0.1);
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  cursor: pointer;
  border-radius: 40px;
  padding: 8px 20px;
  transition: all 0.2s;
  font-family: 'TildaSans', 'Arial', sans-serif;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.corner-btn--left {
  margin-left: 0;
}

.corner-btn--right {
  margin-right: 0;
}

.corner-btn:hover {
  background: white;
  border-color: var(--scenario-accent, #2398ab);
  color: var(--scenario-accent, #2398ab);
  transform: translateY(-1px);
}

.scenario-card-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 40px 24px;
  position: relative;
  z-index: 1;
}

.scenario-card {
  max-width: 800px;
  width: 100%;
  background: var(--scenario-card-bg);
  border-radius: 32px;
  padding: 36px 36px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  border-top: 4px solid var(--scenario-accent);
}

.scenario-card__header {
  text-align: center;
  margin-bottom: 28px;
}

.scenario-card__emoji {
  font-size: 56px;
  display: block;
  margin-bottom: 8px;
}

.scenario-card__header h2 {
  font-size: 28px;
  font-weight: 800;
  color: var(--scenario-accent);
  margin: 0;
}

/* Блок NPC */
.npc-block {
  display: flex;
  gap: 16px;
  margin-bottom: 28px;
  align-items: flex-start;
}

.npc-avatar {
  font-size: 56px;
  flex-shrink: 0;
}

.npc-bubble {
  flex: 1;
  background: var(--scenario-npc-bg, #fef9c3);
  border-radius: 20px;
  padding: 20px 24px;
}

.npc-text {
  font-size: 17px;
  color: #1e293b;
  font-style: italic;
  margin-bottom: 12px;
  line-height: 1.4;
}

.speak-btn {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 40px;
  padding: 6px 16px;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
}

.speak-btn:hover:not(:disabled) {
  border-color: var(--scenario-accent, #2398ab);
  color: var(--scenario-accent, #2398ab);
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
  margin-top: 28px;
}

.btn-next {
  background: var(--scenario-primary-btn);
  color: white;
  border: none;
  border-radius: 40px;
  padding: 12px 32px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  transition: 0.2s;
}

.btn-next:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.btn-retry {
  background: #f59e0b;
  color: white;
  border: none;
  border-radius: 40px;
  padding: 12px 32px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
}

.btn-retry:hover {
  background: #d97706;
  transform: translateY(-2px);
}

.btn-finish {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
  color: white;
  border: none;
  border-radius: 40px;
  padding: 12px 32px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.2s;
}

.btn-finish:hover {
  transform: translateY(-2px);
  opacity: 0.9;
}

@media (max-width: 768px) {
  .corner-actions {
    top: 70px;
    padding: 0 12px;
  }
  
  .corner-btn {
    padding: 6px 14px;
    font-size: 12px;
  }
  
  .scenario-card-wrapper {
    padding: 30px 16px 40px;
  }
  
  .scenario-card {
    padding: 24px 20px;
  }
  
  .npc-block {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .npc-avatar {
    font-size: 48px;
  }
  
  .npc-bubble {
    width: 100%;
  }
  
  .scenario-card__header h2 {
    font-size: 24px;
  }
}
</style>