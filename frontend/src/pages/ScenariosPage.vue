<template>
  <div class="scenarios-page">
    <div class="scenarios-header">
      <h1>Выберите сценарий</h1>
      <p class="subtitle">Практикуйте английский в реальных ситуациях</p>
    </div>

    <div v-if="loading" class="loading">Загрузка сценариев...</div>

    <div v-else class="scenarios-grid">
      <div
        v-for="scenario in scenarios"
        :key="scenario.id"
        class="scenario-card"
        :class="{ 
          'scenario-card--completed': completedScenarios.includes(scenario.id),
          'scenario-card--premium': !scenario.free && !hasActiveSubscription && !completedScenarios.includes(scenario.id)
        }"
        @click="handleScenarioClick(scenario)"
      >
        <span class="scenario-card__emoji">{{ scenario.emoji }}</span>
        <h3>{{ scenario.name }}</h3>
        <p>{{ scenario.description }}</p>
        
        <button 
          v-if="scenario.free || hasActiveSubscription"
          class="scenario-card__btn scenario-card__btn--play"
        >
          Начать →
        </button>
        
        <button 
          v-else
          class="scenario-card__btn scenario-card__btn--premium"
          disabled
        >
          ⭐ Требуется подписка
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'
import { scenariosAPI } from '../api'

const router = useRouter()
const userStore = useUserStore()

const scenarios = ref([])
const loading = ref(true)
const completedScenarios = ref([])

const isAuthenticated = computed(() => userStore.state.isAuthenticated)
const subscription = computed(() => userStore.state.subscription)
const hasActiveSubscription = computed(() => subscription.value?.status === 'ACTIVE')

async function loadCompletedScenarios() {
  if (!isAuthenticated.value) return
  
  try {
    const progressPromises = scenarios.value.map(s => 
      scenariosAPI.getScenarioProgress(s.id).catch(() => null)
    )
    const progresses = await Promise.all(progressPromises)
    
    completedScenarios.value = progresses
      .filter(p => p && p.data && p.data.completed)
      .map(p => p.data.scenarioId)
  } catch (error) {
    console.error('Failed to load progress:', error)
  }
}

function handleScenarioClick(scenario) {
  if (!scenario.free && !hasActiveSubscription.value) return
  router.push(`/scenarios/${scenario.id}`)
}

onMounted(async () => {
  loading.value = true
  
  if (isAuthenticated.value) {
    await userStore.fetchCurrentSubscription()
    await userStore.fetchUserStats()
  }
  
  try {
    const response = await scenariosAPI.getScenarios(1, 100)
    scenarios.value = response.data.items
    await loadCompletedScenarios()
  } catch (error) {
    console.error('Failed to load scenarios:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.scenarios-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px;
  min-height: calc(100vh - 70px);
}

.scenarios-header {
  text-align: center;
  margin-bottom: 48px;
}

.scenarios-header h1 {
  font-size: 36px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.subtitle {
  font-size: 18px;
  color: #64748b;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.loading {
  text-align: center;
  padding: 60px;
  color: #64748b;
  font-size: 18px;
}

.scenarios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 28px;
}

.scenario-card {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  transition: all 0.25s ease;
  text-align: center;
  border: 2px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  display: flex;
  flex-direction: column;
  cursor: pointer;
}

.scenario-card--completed {
  border: 2px solid #22c55e;
  background: #f8fffa;
}

.scenario-card:has(.scenario-card__btn--play):hover {
  border-color: #2398ab;
  transform: translateY(-6px);
  box-shadow: 0 20px 32px -12px rgba(35, 152, 171, 0.2);
}

.scenario-card:has(.scenario-card__btn--premium) {
  cursor: default;
  opacity: 0.7;
}

.scenario-card__emoji {
  font-size: 56px;
  display: block;
  margin-bottom: 16px;
}

.scenario-card h3 {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 10px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.scenario-card p {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 20px;
  line-height: 1.4;
  font-family: 'TildaSans', 'Arial', sans-serif;
  flex-grow: 1;
}

.scenario-card__btn {
  display: inline-block;
  padding: 10px 24px;
  border-radius: 40px;
  font-size: 14px;
  font-weight: 600;
  transition: 0.2s;
  border: none;
  font-family: 'TildaSans', 'Arial', sans-serif;
  width: 100%;
}

.scenario-card__btn--play {
  background: #e0f2f5;
  color: #2398ab;
  cursor: pointer;
  border: none;
}

.scenario-card:has(.scenario-card__btn--play):hover .scenario-card__btn--play {
  background: #c8e8ec;
  color: #1a7a8a;
}
.scenario-card__btn--premium {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .scenarios-page {
    padding: 32px 16px;
  }
  
  .scenarios-header h1 {
    font-size: 28px;
  }
  
  .subtitle {
    font-size: 16px;
  }
  
  .scenario-card {
    padding: 24px 20px;
  }
  
  .scenario-card h3 {
    font-size: 20px;
  }
}
</style>