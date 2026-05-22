<template>
  <header class="header">
    <div class="header__inner">
      <router-link to="/" class="logo">ScenarioEnglish</router-link>

      <nav class="header__nav">
        <router-link to="/" class="nav-link">Главная</router-link>
        <router-link to="/scenarios" class="nav-link">Сценарии</router-link>
      </nav>

      <div class="header__actions">
        <template v-if="!isAuthenticated">
          <button class="btn btn--outline" @click="$emit('open-login')">Вход</button>
          <button class="btn btn--primary" @click="$emit('open-register')">Регистрация</button>
        </template>
        <template v-else>
          <div class="user-info">
            <router-link to="/profile" class="nav-link">
              <span class="user-name">{{ userName }}</span>
            </router-link>
            <button class="btn btn--outline" @click="handleLogout">Выйти</button>
          </div>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const isAuthenticated = computed(() => userStore.state.isAuthenticated)
const userName = computed(() => userStore.state.user?.name || '')

defineEmits(['open-login', 'open-register'])

function handleLogout() {
  userStore.logout()
  router.push('/')
}
</script>

<style scoped>
.header {
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header__inner {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary-color);
  text-decoration: none;
  letter-spacing: -0.3px;
}

.header__nav {
  display: flex;
  gap: 28px;
}

.nav-link {
  color: #4b5563;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  transition: color 0.2s;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--primary-color);
}

.header__actions {
  display: flex;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link:hover .user-name,
.user-name:hover {
  color: #2398ab;
}
</style>