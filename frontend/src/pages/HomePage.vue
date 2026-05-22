<template>
  <div class="home">
    <!-- Верхний блок с голубым фоном -->
    <div class="hero">
      <div class="hero__illustration">
        <span class="hero-emoji" style="animation-delay: 0s">☕</span>
        <span class="hero-emoji" style="animation-delay: 0.5s">✈️</span>
        <span class="hero-emoji" style="animation-delay: 1s">🏨</span>
        <span class="hero-emoji" style="animation-delay: 0.3s">🍽️</span>
        <span class="hero-emoji" style="animation-delay: 0.8s">🛍️</span>
      </div>
      <div class="hero__content">
        <h1>Scenario English</h1>
        <p class="hero__description">
           — это веб-приложение, которое помогает изучать английский язык через реальные жизненные ситуации. Здесь вы сможете сделать заказ в кофейне, заселиться в отель и решить другие задачи на английском языке.
        </p>
      </div>
    </div>

    <!-- Блок "Как работает Scenario English" на белом фоне -->
    <div class="how-it-works">
      <div class="container">
        <div class="section-header">
          <h2>Преимущества Scenario English</h2>
          <p class="section-subtitle">
            Scenario English предлагает уникальный подход к изучению языка, сочетая теорию и практику, что улучшает запоминание и ускоряет прогресс.
          </p>
        </div>

        <div class="features-list">
          <div class="feature-item">
            <span class="feature-check">✓</span>
            <div class="feature-text">
              <h3>Практический  подход</h3>
              <p>Изучайте английский в контексте реальных ситуаций, что помогает лучше запоминать слова и  и использовать английский в повседневной жизни.</p>
            </div>
          </div>

          <div class="feature-item">
            <span class="feature-check">✓</span>
            <div class="feature-text">
              <h3>Разнообразие сценариев</h3>
              <p>Приложение предлагает широкий выбор сценариев, охватывающих различные аспекты жизни, от покупок до путешествий.</p>
            </div>
          </div>


          <div class="feature-item">
            <span class="feature-check">✓</span>
            <div class="feature-text">
              <h3>Интерактивность</h3>
              <p>Приложение предлагает интерактивные задания, которые делают процесс изучения более увлекательным и эффективным.</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Блок "Начните обучение" на голубом фоне -->
    <div class="start-section">
      <div class="start-card">
        <h2  h2>Начните изучать английский язык с помощью Scenario English</h2>
        <router-link to="/scenarios" class="start-btn">
          Начать обучение
        </router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  min-height: calc(100vh - 70px);
  display: flex;
  flex-direction: column;
}

/* Верхний блок с голубым фоном */
.hero {
  background: #2398ab;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  position: relative;
  min-height: 400px;
}

.hero__illustration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.hero-emoji {
  position: absolute;
  font-size: 64px;
  opacity: 0.2;
  animation: float 6s ease-in-out infinite;
}

.hero-emoji:nth-child(1) { top: 15%; left: 10%; }
.hero-emoji:nth-child(2) { top: 65%; right: 12%; }
.hero-emoji:nth-child(3) { bottom: 20%; left: 15%; }
.hero-emoji:nth-child(4) { top: 30%; right: 20%; }
.hero-emoji:nth-child(5) { bottom: 40%; right: 25%; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.hero__content {
  max-width: 700px;
  text-align: center;
  margin: 0 auto;
  position: relative;
  z-index: 2;
}

.hero h1 {
  font-size: 48px;
  font-weight: 700;
  color: white;
  margin-bottom: 24px;
  line-height: 1.2;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.hero__description {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.95);
  line-height: 1.5;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

/* Блок "Как работает Scenario English" на белом фоне */
.how-it-works {
  background: white;
  padding: 64px 24px;
}

.container {
  max-width: 900px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-header h2 {
  font-size: 30px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 20px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.section-subtitle {
  font-size: 18px;
  color: #64748b;
  max-width: 700px;
  margin: 0 auto;
  line-height: 1.5;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.features-list {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.feature-item {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.feature-check {
  font-size: 24px;
  color: #2398ab;
  font-weight: 700;
  flex-shrink: 0;
  background: #e0f2f5;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.feature-text {
  flex: 1;
}

.feature-text h3 {
  font-size: 22px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 8px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.feature-text p {
  font-size: 16px;
  color: #64748b;
  line-height: 1.5;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

/* Блок "Начните обучение" на голубом фоне */
.start-section {
  background: #2398ab;
  padding: 60px 24px 80px;
  display: flex;
  justify-content: center;
}

.start-card {
  max-width: 700px;
  text-align: center;
  background: transparent;
  padding: 48px 40px;
}

.start-card h2 {
  font-size: 36px;
  font-weight: 600;
  color: white;
  margin-bottom: 30px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.start-card p {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  margin-bottom: 32px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.start-btn {
  display: inline-block;
  background: white;
  color: #2398ab;
  text-decoration: none;
  padding: 14px 36px;
  border-radius: 40px;
  font-weight: 600;
  font-size: 16px;
  transition: all 0.3s ease;
  font-family: 'TildaSans', 'Arial', sans-serif;
  border: none;
  cursor: pointer;
}

.start-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

@media (max-width: 768px) {
  .hero h1 {
    font-size: 32px;
  }
  
  .hero__description {
    font-size: 16px;
  }
  
  .hero-emoji {
    font-size: 40px;
    opacity: 0.15;
  }
  
  .section-header h2 {
    font-size: 28px;
  }
  
  .section-subtitle {
    font-size: 16px;
  }
  
  .feature-item {
    gap: 14px;
  }
  
  .feature-check {
    width: 34px;
    height: 34px;
    font-size: 20px;
  }
  
  .feature-text h3 {
    font-size: 20px;
  }
  
  .feature-text p {
    font-size: 14px;
  }
  
  .start-card {
    padding: 32px 24px;
  }
  
  .start-card h2 {
    font-size: 26px;
  }
  
  .start-btn {
    padding: 12px 28px;
    font-size: 15px;
  }
}
</style>