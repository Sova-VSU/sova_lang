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
      @submit="handleAuth"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import AuthModal from './components/AuthModal.vue'

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

function handleAuth(payload) {
  console.log('Auth:', payload)
  closeModal()
}
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