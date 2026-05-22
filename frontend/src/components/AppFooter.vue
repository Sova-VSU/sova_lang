<template>
  <footer class="footer">
    <div class="footer__inner">
      <p class="footer__copyright">© {{ year }} Scenario English. Все права защищены.</p>
      <div class="footer__links">
        <span>Sova2.0</span>
      </div>
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.footer {
  background: #1e293b;
  color: #94a3b8;
  padding: 28px 32px;
  margin-top: auto;
}

.footer__inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.footer__copyright {
  font-size: 14px;
}

.footer__links {
  font-size: 14px;
  color: var(--primary-light);
}
</style>