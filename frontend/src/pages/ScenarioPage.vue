<template>
  <ScenarioPlayer
    v-if="scenario"
    :scenario="scenario"
    @back="$router.push('/scenarios')"
  />
  <div v-else class="not-found">
    <h2>Сценарий не найден</h2>
    <router-link to="/scenarios">← К списку сценариев</router-link>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import ScenarioPlayer from '../components/scenario/ScenarioPlayer.vue'
import coffeeScenario from '../data/scenarios/coffee.json'
import hotelScenario from '../data/scenarios/hotel.json'
import airportScenario from '../data/scenarios/airport.json'
import restaurantScenario from '../data/scenarios/restaurant.json'

const scenarios = {
  coffee: coffeeScenario,
  hotel: hotelScenario,
  airport: airportScenario,
  restaurant: restaurantScenario
}

const route = useRoute()
const scenario = computed(() => scenarios[route.params.id] || null)
</script>

<style scoped>
.not-found {
  text-align: center;
  padding: 80px 24px;
}
</style>