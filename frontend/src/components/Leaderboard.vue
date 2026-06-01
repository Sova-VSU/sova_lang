<template>
  <div class="leaderboard">
    <div class="leaderboard-header">
      <h2>🏆 Таблица лидеров</h2>
      <p class="leaderboard-subtitle">Топ пользователей по количеству пройденных сценариев</p>
    </div>

    <div v-if="loading" class="leaderboard-loading">
      <div class="spinner"></div>
      <span>Загрузка...</span>
    </div>

    <div v-else-if="error" class="leaderboard-error">
      {{ error }}
    </div>

    <div v-else class="leaderboard-table">
      <div class="leaderboard-row leaderboard-row--header">
        <div class="rank">#</div>
        <div class="name">Пользователь</div>
        <div class="completed">Пройдено</div>

      </div>

      <div
        v-for="entry in leaderboard"
        :key="entry.id"
        class="leaderboard-row"
        :class="{ 'leaderboard-row--current-user': entry.id === currentUserId }"
      >
        <div class="rank">
          <span v-if="entry.rank === 1" class="medal gold">🥇</span>
          <span v-else-if="entry.rank === 2" class="medal silver">🥈</span>
          <span v-else-if="entry.rank === 3" class="medal bronze">🥉</span>
          <span v-else class="rank-number">{{ entry.rank }}</span>
        </div>
        <div class="name">
          <span class="user-avatar">{{ entry.name?.charAt(0) || '?' }}</span>
          <span class="user-name">{{ entry.name  }}</span>
          <span v-if="entry.id === currentUserId" class="current-badge">Вы</span>
        </div>
        <div class="completed">{{ entry.completedScenariosCount }}</div>
      </div>

      <div v-if="leaderboard.length === 0" class="leaderboard-empty">
        <span>📭 Пока нет участников</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { leaderboardAPI } from '../api'
import { useUserStore } from '../stores/userStore'

const userStore = useUserStore()
const leaderboard = ref([])
const loading = ref(true)
const error = ref(null)

const currentUserId = computed(() => userStore.state.user?.id)

async function fetchLeaderboard() {
  loading.value = true
  error.value = null
  try {
    const response = await leaderboardAPI.getLeaderboard()
    leaderboard.value = response.data
  } catch (err) {
    console.error('Failed to fetch leaderboard:', err)
    error.value = 'Не удалось загрузить таблицу лидеров'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLeaderboard()
})
</script>

<style scoped>
.leaderboard {
  background: white;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.leaderboard-header {
  text-align: center;
  margin-bottom: 24px;
}

.leaderboard-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.leaderboard-subtitle {
  font-size: 14px;
  color: #64748b;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.leaderboard-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px;
  color: #64748b;
}

.spinner {
  width: 24px;
  height: 24px;
  border: 3px solid #e2e8f0;
  border-top-color: #2398ab;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.leaderboard-error {
  text-align: center;
  padding: 48px;
  color: #ef4444;
}

.leaderboard-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.leaderboard-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: 0.2s;
}

.leaderboard-row--header {
  background: #e0f2f5;
  font-weight: 700;
  color: #1e293b;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.leaderboard-row--current-user {
  background: #e0f2f5;
  border: 1px solid #2398ab;
}

.rank {
  width: 60px;
  text-align: center;
  font-weight: 700;
}

.medal {
  font-size: 20px;
}

.rank-number {
  color: #64748b;
}

.name {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  background: #2398ab;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
}

.user-name {
  font-weight: 600;
  color: #1e293b;
}

.current-badge {
  background: #2398ab;
  color: white;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}

.completed, .xp {
  width: 80px;
  text-align: center;
  font-weight: 600;
  color: #1e293b;
}

.leaderboard-empty {
  text-align: center;
  padding: 48px;
  color: #94a3b8;
}

@media (max-width: 768px) {
  .leaderboard {
    padding: 16px;
  }
  
  .leaderboard-row {
    padding: 10px 12px;
    gap: 8px;
  }
  
  .rank {
    width: 45px;
  }
  
  .completed, .xp {
    width: 60px;
    font-size: 13px;
  }
  
  .user-name {
    font-size: 14px;
  }
  
  .user-avatar {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
}
</style>