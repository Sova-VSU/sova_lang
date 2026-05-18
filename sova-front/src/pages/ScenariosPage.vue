<template>
  <section class="scenarios-page">
    <h1>Выберите сценарий</h1>
    <p class="subtitle">Практикуйте английский в реальных ситуациях</p>

    <div class="scenarios-grid">
      <router-link
        v-for="scenario in scenarios"
        :key="scenario.id"
        :to="`/scenarios/${scenario.id}`"
        class="scenario-card"
        :class="{ 'scenario-card--locked': scenario.locked }"
      >
        <span class="scenario-card__emoji">{{ scenario.emoji }}</span>
        <h3>{{ scenario.title }}</h3>
        <p>{{ scenario.description }}</p>
        <span v-if="scenario.locked" class="scenario-card__lock">🔒 Скоро</span>
      </router-link>
    </div>
  </section>
</template>

<script setup>
const scenarios = [
  {
    id: 'coffee',
    emoji: '☕',
    title: 'Заказ в кофейне',
    description: 'Закажите кофе, чай или какао и оплатите заказ.',
    route: '/scenarios/coffee',
    locked: false
  },
  {
    id: 'restaurant',
    emoji: '🍽️',
    title: 'Ресторан',
    description: 'Забронируйте столик и сделайте заказ.',
    route: '/scenarios/restaurant',
    locked: false
  },
  {
    id: 'airport',
    emoji: '✈️',
    title: 'Аэропорт',
    description: 'Пройдите регистрацию и посадку на самолёт.',
    route: '/scenarios/airport',
    locked: false
  },
  {
    id: 'hotel',
    emoji: '🏨',
    title: 'Заселение в отель',
    description: 'Забронируйте и заселитесь в отель.',
    route: '/scenarios/hotel',
    locked: false
  },
  {
    id: 'shop',
    emoji: '🛍️',
    title: 'Магазин одежды',
    description: 'Подберите размер и купите одежду.',
    route: '/scenarios/shop',
    locked: true
  },
  {
    id: 'taxi',
    emoji: '🚕',
    title: 'Такси',
    description: 'Вызовите такси и доберитесь до места.',
    route: '/scenarios/taxi',
    locked: true
  }
]
</script>

<style scoped>
.scenarios-page {
  padding: 48px 24px;
  text-align: center;
}

.scenarios-page h1 {
  font-size: 36px;
  color: #0f172a;
  margin-bottom: 8px;
}

.subtitle {
  color: #64748b;
  font-size: 18px;
  margin-bottom: 40px;
}

.scenarios-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.scenario-card {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 20px;
  padding: 32px 24px;
  text-decoration: none;
  color: #0f172a;
  transition: 0.25s;
  position: relative;
  text-align: center;
}

.scenario-card:hover:not(.scenario-card--locked) {
  border-color: #3b82f6;
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(59, 130, 246, 0.12);
}

.scenario-card--locked {
  opacity: 0.5;
  pointer-events: none;
}

.scenario-card__emoji {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.scenario-card h3 {
  margin-bottom: 8px;
  font-size: 20px;
}

.scenario-card p {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 16px;
}

.scenario-card__badge {
  display: inline-block;
  background: #eff6ff;
  color: #2563eb;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.scenario-card__lock {
  display: inline-block;
  background: #f1f5f9;
  color: #94a3b8;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}
</style>