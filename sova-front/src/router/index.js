import { createRouter, createWebHistory } from 'vue-router'

import HomePage from '../pages/HomePage.vue'
import ScenariosPage from '../pages/ScenariosPage.vue'
import ScenarioPage from '../pages/ScenarioPage.vue'

const routes = [
  { path: '/', name: 'Home', component: HomePage },
  { path: '/scenarios', name: 'Scenarios', component: ScenariosPage },
  { path: '/scenarios/:id', name: 'Scenario', component: ScenarioPage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router