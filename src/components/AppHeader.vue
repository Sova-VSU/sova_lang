<template>
  <header class="header">
    <div class="header__inner">
      <router-link to="/" class="logo">ScenarioEnglish</router-link>

      <nav class="header__nav">
        <router-link to="/" class="nav-link">Главная</router-link>
        <router-link to="/scenarios" class="nav-link">Сценарии</router-link>
      </nav>

      <div class="header__actions">
        <button class="btn btn--outline" @click="$emit('open-login')">Вход</button>
        <button class="btn btn--primary" @click="$emit('open-register')">Регистрация</button>
      </div>
    </div>
  </header>
</template>

<script setup>
defineEmits(['open-login', 'open-register'])
</script>

<style scoped>
.header {
  background: #0f172a;
  color: white;
  padding: 0 24px;
}

.header__inner {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.logo {
  font-size: 22px;
  font-weight: 800;
  color: white;
  text-decoration: none;
}

.header__nav {
  display: flex;
  gap: 20px;
}

.nav-link {
  color: #cbd5e1;
  text-decoration: none;
  font-weight: 500;
  transition: 0.2s;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: white;
}

.header__actions {
  display: flex;
  gap: 10px;
}

.btn {
  border: none;
  border-radius: 10px;
  padding: 8px 16px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.btn--outline {
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.btn--outline:hover {
  background: rgba(255, 255, 255, 0.08);
}

.btn--primary {
  background: #3b82f6;
  color: white;
}

.btn--primary:hover {
  background: #2563eb;
}
</style>