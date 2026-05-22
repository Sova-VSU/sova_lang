<template>
  <div class="app">
    <AppHeader
      @open-login="openModal('login')"
      @open-register="openModal('register')"
    />

    <main class="main-content">
      <router-view />
    </main>

    <AppFooter />

    <AuthModal
      v-if="isModalOpen"
      :mode="authMode"
      @close="closeModal"
      @switch-mode="switchMode"
      @success="onAuthSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import AuthModal from './components/AuthModal.vue'
import { useUserStore, restoreSession } from './stores/userStore'

const userStore = useUserStore()

const isModalOpen = ref(false)
const authMode = ref('login')

function openModal(mode) {
  authMode.value = mode
  isModalOpen.value = true
}

function closeModal() {
  isModalOpen.value = false
}

function switchMode() {
  authMode.value = authMode.value === 'login' ? 'register' : 'login'
}

function onAuthSuccess() {
  // Можно добавить уведомление или редирект
  console.log('Auth successful')
}

onMounted(async () => {
  restoreSession()
  if (userStore.state.isAuthenticated) {
    await userStore.fetchCurrentUser()
  }
})
</script>

<style scoped>
.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}
</style>