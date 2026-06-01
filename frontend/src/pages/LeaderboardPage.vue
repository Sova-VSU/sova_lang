<template>
  <div class="leaderboard-page">
    <Leaderboard />
  </div>
</template>

<script setup>
import Leaderboard from '../components/LeaderBoard.vue'
</script>

<style scoped>
.leaderboard-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 48px 24px;
  min-height: calc(100vh - 70px);
}
</style>