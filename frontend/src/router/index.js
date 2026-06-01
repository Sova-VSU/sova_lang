
import { createRouter, createWebHistory } from 'vue-router'
import { restoreSession } from '../stores/userStore'

import HomePage from '../pages/HomePage.vue'
import ScenariosPage from '../pages/ScenariosPage.vue'
import ScenarioPage from '../pages/ScenarioPage.vue'
import ProfilePage from '../pages/ProfilePage.vue'
import LeaderboardPage from '../pages/LeaderboardPage.vue'
const routes = [
  { path: '/', name: 'Home', component: HomePage },
  { path: '/scenarios', name: 'Scenarios', component: ScenariosPage },
  { path: '/scenarios/:id', name: 'Scenario', component: ScenarioPage },
  { path: '/profile', name: 'Profile', component: ProfilePage, meta: { requiresAuth: true } },
  { path: '/leaderboard', name: 'Leaderboard', component: LeaderboardPage }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  restoreSession()

  if (to.meta.requiresAuth && !localStorage.getItem('accessToken')) {
    next('/')
  } else {
    next()
  }
})

export default router