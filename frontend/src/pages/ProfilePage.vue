<template>
  <div class="profile-page">
    <div class="profile-container">
      <div class="profile-header">

        <h1>Профиль пользователя</h1>
        <p class="profile-email">{{ user?.email || 'user@example.com' }}</p>
      </div>

      <div v-if="loading" class="loading-state">
        Загрузка...
      </div>

      <template v-else>
        <div v-if="user" class="profile-content">
          <!-- Личная информация -->
          <div class="profile-section">
            <h2>Личная информация</h2>
            <div class="info-row">
              <span class="label">Имя:</span>
              <span class="value">{{ user?.name || 'Не указано' }}</span>
              <button class="edit-btn" @click="showEditNameModal = true">
                 Редактировать
              </button>
            </div>
            <div class="info-row">
              <span class="label">Email:</span>
              <span class="value">{{ user?.email }}</span>
            </div>
          </div>

          <!-- Статистика -->
          <div class="profile-section">
            <h2>Статистика</h2>
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-value">{{ stats?.totalXp || 0 }}</div>
                <div class="stat-label">Всего XP</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ stats?.level || 1 }}</div>
                <div class="stat-label">Уровень</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ stats?.streak || 0 }}</div>
                <div class="stat-label">Дней подряд</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ stats?.completedScenariosCount || 0 }}</div>
                <div class="stat-label">Пройдено сценариев</div>
              </div>
            </div>
          </div>

          <!-- Подписка -->
          <div class="profile-section">
            <h2>Подписка</h2>
            <div v-if="subscription" class="subscription-info">
              <div class="subscription-status" :class="subscriptionStatusClass">
                Статус: {{ subscriptionStatusText }}
              </div>
              <div class="subscription-dates">
                <div>Активирована: {{ formatDate(subscription.startedAt) }}</div>
                <div>Действует до: {{ formatDate(subscription.endsAt) }}</div>
              </div>
              <button v-if="subscription.status === 'ACTIVE'" 
                      class="btn-cancel" 
                      @click="cancelSubscription">
                Отменить подписку
              </button>
            </div>
            <div v-else class="subscription-offer">
              <p>У вас нет активной подписки.</p>
              <p class="offer-text">
                Оформите подписку, чтобы получить доступ ко всем платным сценариям!
              </p>
              <button class="btn-subscribe" @click="showSubscribeModal = true">
                Оформить подписку
              </button>
            </div>
          </div>

          <!-- Безопасность -->
          <div class="profile-section">
            <h2>Безопасность</h2>
            <div class="security-buttons">
              <button class="btn-change-password" @click="showChangePasswordModal = true">
                Сменить пароль
              </button>
              <button class="btn-delete" @click="confirmDelete">
                Удалить аккаунт
              </button>
            </div>
          </div>

          <div class="profile-actions">
            <button class="btn-logout" @click="handleLogout">
              Выйти из аккаунта
            </button>
          </div>
        </div>
      </template>
    </div>

    <!-- Модальные окна (оставляем как есть) -->
    <div v-if="showEditNameModal" class="modal-overlay" @click.self="showEditNameModal = false">
      <div class="modal-small">
        <h3>Редактировать имя</h3>
        <input v-model="newName" type="text" placeholder="Новое имя" />
        <div class="modal-actions">
          <button @click="showEditNameModal = false">Отмена</button>
          <button class="btn-primary" @click="updateName">Сохранить</button>
        </div>
      </div>
    </div>

    <div v-if="showChangePasswordModal" class="modal-overlay" @click.self="showChangePasswordModal = false">
      <div class="modal">
        <h3>Смена пароля</h3>
        <div class="form-group">
          <label>Текущий пароль</label>
          <input v-model="passwordForm.current" type="password" />
        </div>
        <div class="form-group">
          <label>Новый пароль</label>
          <input v-model="passwordForm.new" type="password" />
        </div>
        <div class="form-group">
          <label>Подтвердите пароль</label>
          <input v-model="passwordForm.confirm" type="password" />
        </div>
        <p v-if="passwordError" class="error">{{ passwordError }}</p>
        <div class="modal-actions">
          <button @click="showChangePasswordModal = false">Отмена</button>
          <button class="btn-primary" @click="updatePassword">Сохранить</button>
        </div>
      </div>
    </div>

    <div v-if="showSubscribeModal" class="modal-overlay" @click.self="showSubscribeModal = false">
      <div class="modal">
        <h3>Оформить подписку</h3>
        <p style="color:#64748b; margin-bottom:20px;">Выберите срок подписки:</p>
        <div class="duration-options">
          <button
            v-for="option in durationOptions"
            :key="option.days"
            class="duration-btn"
            :class="{ selected: selectedDuration === option.days }"
            @click="selectedDuration = option.days"
          >
            <span class="duration-label">{{ option.label }}</span>
            <span class="duration-price">{{ option.price }}</span>
          </button>
        </div>
        <div class="modal-actions">
          <button @click="showSubscribeModal = false">Отмена</button>
          <button class="btn-primary" @click="handleSubscribe">Оформить</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, restoreSession } from '../stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const storeState = computed(() => userStore.state)

const loading = ref(false)
const showEditNameModal = ref(false)
const showChangePasswordModal = ref(false)
const showSubscribeModal = ref(false)
const selectedDuration = ref(30)
const durationOptions = [
  { days: 30,  label: '1 месяц',  price: '299 ₽' },
  { days: 90,  label: '3 месяца', price: '799 ₽' },
  { days: 365, label: '1 год',    price: '2490 ₽' },
]
const newName = ref('')
const passwordForm = ref({
  current: '',
  new: '',
  confirm: ''
})
const passwordError = ref('')

const user = computed(() => storeState.value.user)
const stats = computed(() => storeState.value.stats)
const subscription = computed(() => storeState.value.subscription)

const subscriptionStatusText = computed(() => {
  if (!subscription.value) return ''
  switch (subscription.value.status) {
    case 'ACTIVE': return 'Активна'
    case 'ENDED': return 'Завершена'
    case 'CANCELLED': return 'Отменена'
    default: return subscription.value.status
  }
})

const subscriptionStatusClass = computed(() => {
  if (!subscription.value) return ''
  switch (subscription.value.status) {
    case 'ACTIVE': return 'status-active'
    case 'ENDED': return 'status-ended'
    case 'CANCELLED': return 'status-cancelled'
    default: return ''
  }
})

function formatDate(dateString) {
  if (!dateString) return '—'
  const date = new Date(dateString)
  return date.toLocaleDateString('ru-RU')
}

async function updateName() {
  if (!newName.value.trim()) return
  const success = await userStore.updateUserName(newName.value)
  if (success) {
    showEditNameModal.value = false
    newName.value = ''
  }
}

async function updatePassword() {
  passwordError.value = ''
  if (passwordForm.value.new !== passwordForm.value.confirm) {
    passwordError.value = 'Пароли не совпадают'
    return
  }
  if (passwordForm.value.new.length < 8) {
    passwordError.value = 'Пароль должен быть не менее 8 символов'
    return
  }
  const success = await userStore.updateUserPassword(
    passwordForm.value.current,
    passwordForm.value.new
  )
  if (success) {
    showChangePasswordModal.value = false
    passwordForm.value = { current: '', new: '', confirm: '' }
  } else {
    passwordError.value = storeState.value.error
  }
}

function confirmDelete() {
  if (confirm('ВНИМАНИЕ! Это действие необратимо. Вы уверены?')) {
    deleteAccount()
  }
}

async function deleteAccount() {
  const success = await userStore.deleteAccount()
  if (success) {
    router.push('/')
  }
}

async function handleSubscribe() {
  await userStore.createSubscription(selectedDuration.value)
  showSubscribeModal.value = false
}

async function cancelSubscription() {
  if (confirm('Вы уверены, что хотите отменить подписку?')) {
    await userStore.cancelSubscription()
  }
}

function handleLogout() {
  userStore.logout()
  router.push('/')
}

onMounted(async () => {
  restoreSession()
  if (userStore.state.isAuthenticated) {
    await userStore.fetchCurrentUser()
    await userStore.fetchUserStats()
    await userStore.fetchCurrentSubscription()
  }
})
</script>

<style scoped>
.profile-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 48px 24px;
  min-height: calc(100vh - 70px);
  background: #f8fafc;
}

.profile-container {
  background: white;
  border-radius: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  overflow: hidden;
}

.profile-header {
  background: #2398ab;
  color: white;
  padding: 48px 32px;
  text-align: center;
}
/*
.profile-avatar {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.avatar-emoji {
  font-size: 48px;
}*/

.profile-header h1 {
  font-size: 28px;
  margin-bottom: 8px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.profile-email {
  opacity: 0.9;
  font-size: 14px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.loading-state {
  padding: 60px;
  text-align: center;
  color: #64748b;
}

.profile-content {
  padding: 32px;
}

.profile-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.profile-section h2 {
  font-size: 20px;
  color: #1e293b;
  margin-bottom: 20px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-row .label {
  width: 100px;
  font-weight: 600;
  color: #64748b;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.info-row .value {
  flex: 1;
  color: #1e293b;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.edit-btn {
  background: none;
  border: none;
  color: #2398ab;
  cursor: pointer;
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 8px;
  transition: 0.2s;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.edit-btn:hover {
  background: #e0f2f5;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  transition: 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: #2398ab;
  margin-bottom: 8px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.subscription-info {
  background: #f8fafc;
  border-radius: 16px;
  padding: 20px;
}

.subscription-status {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 12px;
}

.status-active {
  color: #2398ab;
}

.status-ended {
  color: #ef4444;
}

.status-cancelled {
  color: #f59e0b;
}

.subscription-dates {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 16px;
}

.subscription-offer {
  background: #e0f2f5;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
}

.offer-text {
  margin: 12px 0;
  color: #1a7a8a;
}

.security-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.btn-subscribe,
.btn-cancel,
.btn-change-password,
.btn-delete {
  padding: 10px 24px;
  border: none;
  border-radius: 40px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
  font-family: 'TildaSans', 'Arial', sans-serif;
  font-size: 14px;
}

.btn-subscribe {
  background: #2398ab;
  color: white;
}

.btn-subscribe:hover {
  background: #1a7a8a;
}

.btn-cancel {
  background: #ef4444;
  color: white;
}

.btn-cancel:hover {
  background: #dc2626;
}

.btn-change-password {
  background: #f1f5f9;
  color: #1e293b;
  border: 1px solid #e2e8f0;
}

.btn-change-password:hover {
  background: #e2e8f0;
}

.btn-delete {
  background: #fef2f2;
  color: #ef4444;
  border: 1px solid #fecaca;
}

.btn-delete:hover {
  background: #fee2e2;
}

.profile-actions {
  margin-top: 32px;
  text-align: center;
}

.btn-logout {
  background: white;
  color: #64748b;
  border: 1px solid #e2e8f0;
  border-radius: 40px;
  padding: 12px 32px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.btn-logout:hover {
  background: #f1f5f9;
  color: #1e293b;
}

/* Модальные окна */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  z-index: 1000;
}

.modal, .modal-small {
  background: white;
  border-radius: 20px;
  padding: 28px;
  max-width: 480px;
  width: 100%;
}

.modal-small {
  max-width: 400px;
}

.modal h3 {
  margin-bottom: 20px;
  color: #1e293b;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 600;
  color: #334155;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.form-group input {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 10px 14px;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}

.modal-actions button {
  padding: 8px 20px;
  border-radius: 40px;
  border: none;
  cursor: pointer;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.modal-actions .btn-primary {
  background: #2398ab;
  color: white;
}

.modal-actions .btn-primary:hover {
  background: #1a7a8a;
}

.error {
  color: #dc2626;
  font-size: 14px;
  margin-top: 8px;
}

.duration-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 8px;
}

.duration-btn {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border: 2px solid #e2e8f0;
  border-radius: 14px;
  background: white;
  cursor: pointer;
  transition: 0.2s;
  font-family: 'TildaSans', 'Arial', sans-serif;
}

.duration-btn:hover {
  border-color: #2398ab;
}

.duration-btn.selected {
  border-color: #2398ab;
  background: #e0f2f5;
}

.duration-label {
  font-weight: 600;
  color: #1e293b;
}

.duration-price {
  color: #2398ab;
  font-weight: 700;
}

@media (max-width: 768px) {
  .profile-page {
    padding: 24px 16px;
  }
  
  .profile-header {
    padding: 32px 20px;
  }
  
  .profile-header h1 {
    font-size: 24px;
  }
  
  .profile-content {
    padding: 20px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>