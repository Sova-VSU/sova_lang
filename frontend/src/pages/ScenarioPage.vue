<template>
  <div v-if="loading" class="loading">Загрузка сценария...</div>
  <ScenarioPlayer
    v-else-if="scenario"
    :scenario="scenario"
    @back="$router.push('/scenarios')"
  />
  <div v-else class="not-found">
    <h2>Сценарий не найден</h2>
    <router-link to="/scenarios">← К списку сценариев</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ScenarioPlayer from '../components/scenario/ScenarioPlayer.vue'
import { scenariosAPI } from '../api'
import { useUserStore } from '../stores/userStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const scenario = ref(null)
const loading = ref(true)

onMounted(async () => {
  loading.value = true
  try {
    const response = await scenariosAPI.getScenarioById(route.params.id)
    scenario.value = response.data
  } catch (error) {
    console.error('Failed to load scenario:', error)
    if (error.response?.status === 403) {
      alert('Этот сценарий требует подписки. Оформите подписку в профиле.')
      router.push('/scenarios')
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.loading, .not-found {
  text-align: center;
  padding: 80px 24px;
}

.not-found a {
  color: #2398ab;
  text-decoration: none;
}
</style>