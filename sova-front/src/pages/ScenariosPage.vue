<template>
  <div class="scenarios-page">
    <div class="scenarios-header">
      <h1>Выберите сценарий</h1>
      <p class="subtitle">Практикуйте английский в реальных ситуациях</p>
    </div>

    <div class="scenarios-grid">
      <div
        v-for="scenario in scenarios"
        :key="scenario.id"
        class="scenario-card"
        :class="{ 'scenario-card--locked': scenario.locked }"
        @click="!scenario.locked && $router.push(`/scenarios/${scenario.id}`)"
      >
        <span class="scenario-card__emoji">{{ scenario.emoji }}</span>
        <h3>{{ scenario.title }}</h3>
        <p>{{ scenario.description }}</p>
        <span v-if="scenario.locked" class="scenario-card__lock">🔒 Скоро</span>
        <span v-else class="scenario-card__badge">Начать →</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const scenarios = [
  {
    id: 'coffee',
    emoji: '☕',
    title: 'Заказ в кофейне',
    description: 'Закажите кофе, чай или какао и оплатите заказ.',
    locked: false
  },
  {
    id: 'restaurant',
    emoji: '🍽️',
    title: 'Ресторан',
    description: 'Забронируйте столик и сделайте заказ.',
    locked: false
  },
  {
    id: 'airport',
    emoji: '✈️',
    title: 'Аэропорт',
    description: 'Пройдите регистрацию и посадку на самолёт.',
    locked: false
  },
  {
    id: 'hotel',
    emoji: '🏨',
    title: 'Заселение в отель',
    description: 'Забронируйте и заселитесь в отель.',
    locked: false
  }/*, { id: 'shop', emoji: '🛍️', title: 'Магазин одежды', description: 'Подберите размер и купите одежду.', route: '/scenarios/shop', locked: true },
   { id: 'taxi', emoji: '🚕', title: 'Такси', description: 'Вызовите такси и доберитесь до места.', route: '/scenarios/taxi', locked: true }*/
]
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

.scenarios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 28px;
}

.scenario-card {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  cursor: pointer;
  transition: all 0.25s ease;
  text-align: center;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.scenario-card:hover:not(.scenario-card--locked) {
  border-color: #2398ab;
  transform: translateY(-6px);
  box-shadow: 0 20px 32px -12px rgba(35, 152, 171, 0.2);
}

.scenario-card--locked {
  opacity: 0.5;
  cursor: not-allowed;
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
}

.scenario-card__badge {
  display: inline-block;
  background: #e0f2f5;
  color: #2398ab;
  padding: 6px 20px;
  border-radius: 40px;
  font-size: 13px;
  font-weight: 600;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.scenario-card__lock {
  display: inline-block;
  background: #f1f5f9;
  color: #94a3b8;
  padding: 6px 20px;
  border-radius: 40px;
  font-size: 13px;
  font-weight: 600;
  font-family: 'TildaSans', 'Arial', sans-serif;
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