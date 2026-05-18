<template>
  <section class="home">
    <div class="hero">
      <h1>Учите английский через реальные ситуации</h1>
      <p>Проходите интерактивные сценарии: заказ в кафе, покупки, путешествия и многое другое.</p>
      <router-link to="/scenarios" class="hero-btn">Начать обучение →</router-link>
    </div>
  </section>
</template>

<style scoped>
.home {
  padding: 48px 24px;
}

.hero {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: white;
  border-radius: 24px;
  padding: 64px 48px;
  text-align: center;
}

.hero h1 {
  font-size: 42px;
  margin-bottom: 16px;
}

.hero p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 32px;
}

.hero-btn {
  display: inline-block;
  background: white;
  color: #2563eb;
  padding: 14px 32px;
  border-radius: 14px;
  font-weight: 700;
  text-decoration: none;
  transition: 0.2s;
}

.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
</style>