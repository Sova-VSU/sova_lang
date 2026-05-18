<template>
  <Transition name="fade">
    <div v-if="show" class="results-overlay" @click.self="$emit('close')">
      <div class="results-card">
        <div class="results-icon">🎉</div>
        <h2>Сценарий завершён!</h2>
        <p class="results-subtitle">Отличная работа!</p>

        <!-- Блок с итоговым сообщением -->
        <div class="results-summary">
          <span class="results-summary__icon">🏆</span>
          <span class="results-summary__text">
            {{ completionMessage || 'Вы успешно прошли этот сценарий!' }}
          </span>
        </div>

        <div class="results-stats">
          <div class="stat">
            <span class="stat__icon">📊</span>
            <span class="stat__number">{{ stepsCount }}</span>
            <span class="stat__label">Шагов пройдено</span>
          </div>

          <div class="stat stat--green">
            <span class="stat__icon">✅</span>
            <span class="stat__number">{{ correctCount }}</span>
            <span class="stat__label">Верных ответов</span>
          </div>

          <div class="stat stat--red">
            <span class="stat__icon">❌</span>
            <span class="stat__number">{{ errorCount }}</span>
            <span class="stat__label">Ошибок</span>
          </div>
        </div>

        <div class="results-actions">
          <button class="btn-primary" @click="$emit('restart')">
            🔄 Пройти ещё раз
          </button>
          <router-link to="/scenarios" class="btn-secondary">
            К сценариям
          </router-link>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  show: Boolean,
  history: { type: Array, default: () => [] },
  completionMessage: { type: String, default: '' } // <-- Новое поле
})

defineEmits(['close', 'restart'])

const stepsCount = computed(() => props.history.length)

const correctCount = computed(() =>
  props.history.filter(h => h.status === 'polite' || h.status === 'correct').length
)

const errorCount = computed(() =>
  props.history.filter(h => h.status === 'error' || h.status === 'rude').length
)
</script>

<style scoped>
.results-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.65);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.results-card {
  background: white;
  border-radius: 24px;
  padding: 40px 36px;
  max-width: 480px;
  width: 100%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.results-icon {
  font-size: 64px;
  margin-bottom: 8px;
  animation: bounce 1s ease;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

.results-card h2 {
  font-size: 28px;
  margin-bottom: 8px;
  color: #0f172a;
}

.results-subtitle {
  color: #64748b;
  margin-bottom: 28px;
  font-size: 15px;
}

/* Стили для итогового сообщения */
.results-summary {
  background: linear-gradient(135deg, #f0fdf4, #dcfce7);
  border: 1px solid #86efac;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 16px;
  text-align: left;
}

.results-summary__icon {
  font-size: 32px;
}

.results-summary__text {
  font-size: 16px;
  font-weight: 700;
  color: #166534;
  line-height: 1.4;
}

.results-stats {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 28px;
  flex-wrap: wrap;
}

.stat {
  flex: 1;
  min-width: 110px;
  background: #f8fafc;
  border-radius: 16px;
  padding: 18px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat__icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.stat__number {
  font-size: 32px;
  font-weight: 800;
  color: #2563eb;
  line-height: 1;
}

.stat--green { background: #f0fdf4; }
.stat--green .stat__number { color: #22c55e; }

.stat--red { background: #fef2f2; }
.stat--red .stat__number { color: #ef4444; }

.stat__label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  font-weight: 600;
}

.results-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.btn-primary {
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
  transition: 0.2s;
}

.btn-primary:hover {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-secondary {
  background: #f1f5f9;
  color: #475569;
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.fade-enter-active { transition: opacity 0.3s; }
.fade-enter-from { opacity: 0; }
</style>