<template>
  <footer class="footer">
    <div class="container footer__inner">
      <p>© {{ year }} App. Все права защищены.</p>
      <p>Sova2.0</p>
      
    </div>
  </footer>
</template>

<script setup>
const year = new Date().getFullYear()
</script>

<style scoped>
.footer {
  background: #111827;
  color: #cbd5e1;
  padding: 20px 0;
  margin-top: auto;
}

.footer__inner {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
</style>