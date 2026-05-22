<template>
  <Teleport to="body">
    <div class="modal-overlay" @click.self="emit('close')">
      <div class="modal">
        <button class="modal__close" type="button" @click="emit('close')">
          ×
        </button>

        <h2 class="modal__title">{{ title }}</h2>

        <form class="modal__form" @submit.prevent="submitForm">
          <div v-if="mode === 'register'" class="form-group">
            <label for="name">Имя</label>
            <input
              id="name"
              v-model="form.name"
              type="text"
              placeholder="Введите имя"
            />
          </div>

          <div class="form-group">
            <label for="email">Email</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              placeholder="example@mail.com"
              autocomplete="email"
            />
          </div>

          <div class="form-group">
            <label for="password">Пароль</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              placeholder="Введите пароль"
              :autocomplete="mode === 'login' ? 'current-password' : 'new-password'"
            />
          </div>

          <div v-if="mode === 'register'" class="form-group">
            <label for="confirmPassword">Подтвердите пароль</label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              type="password"
              placeholder="Повторите пароль"
              autocomplete="new-password"
            />
          </div>

          <p v-if="errorMessage" class="modal__error">
            {{ errorMessage }}
          </p>
          <p v-if="storeLoading" class="modal__loading">
            Загрузка...
          </p>

          <button class="btn btn--primary btn--full" type="submit" :disabled="storeLoading">
            {{ submitText }}
          </button>
        </form>

        <button class="modal__switch" type="button" @click="emit('switch-mode')">
          {{ switchText }}
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, reactive, ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useUserStore } from '../stores/userStore'

const props = defineProps({
  mode: {
    type: String,
    default: 'login'
  }
})

const emit = defineEmits(['close', 'switch-mode', 'success'])

const userStore = useUserStore()
const storeState = computed(() => userStore.state)

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const errorMessage = ref('')
const storeLoading = computed(() => storeState.value.loading)

const title = computed(() =>
  props.mode === 'login' ? 'Вход в аккаунт' : 'Регистрация'
)

const submitText = computed(() =>
  props.mode === 'login' ? 'Войти' : 'Зарегистрироваться'
)

const switchText = computed(() =>
  props.mode === 'login'
    ? 'Нет аккаунта? Зарегистрироваться'
    : 'Уже есть аккаунт? Войти'
)

function resetForm() {
  form.name = ''
  form.email = ''
  form.password = ''
  form.confirmPassword = ''
  errorMessage.value = ''
}

watch(() => props.mode, resetForm)

async function submitForm() {
  errorMessage.value = ''

  if (props.mode === 'register' && !form.name.trim()) {
    errorMessage.value = 'Введите имя'
    return
  }

  if (!form.email.trim() || !form.password.trim()) {
    errorMessage.value = 'Заполните email и пароль'
    return
  }

  if (props.mode === 'register' && form.password !== form.confirmPassword) {
    errorMessage.value = 'Пароли не совпадают'
    return
  }

  let success = false
  if (props.mode === 'login') {
    success = await userStore.login(form.email, form.password)
  } else {
    success = await userStore.register(form.name, form.email, form.password)
  }

  if (success) {
    resetForm()
    emit('success')
    emit('close')
  } else {
    errorMessage.value = storeState.value.error || 'Произошла ошибка'
  }
}

function handleEsc(event) {
  if (event.key === 'Escape') {
    emit('close')
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleEsc)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleEsc)
})
</script>

<style scoped>
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

.modal {
  width: 100%;
  max-width: 420px;
  background: white;
  border-radius: 20px;
  padding: 28px;
  position: relative;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.2);
}

.modal__close {
  position: absolute;
  top: 12px;
  right: 14px;
  border: none;
  background: transparent;
  font-size: 28px;
  cursor: pointer;
  color: #64748b;
}

.modal__title {
  margin-bottom: 20px;
  color: #0f172a;
}

.modal__form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  color: #334155;
  font-weight: 600;
}

.form-group input {
  width: 100%;
  border: 1px solid #cbd5e1;
  border-radius: 12px;
  padding: 12px 14px;
  outline: none;
  transition: 0.2s ease;
}

.form-group input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.modal__error {
  color: #dc2626;
  font-size: 14px;
}

.modal__loading {
  color: #2563eb;
  font-size: 14px;
  text-align: center;
}

.btn {
  border: none;
  border-radius: 12px;
  padding: 12px 16px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s ease;
}

.btn--primary {
  background: #2563eb;
  color: white;
}

.btn--primary:hover:not(:disabled) {
  background: #1d4ed8;
}

.btn--primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn--full {
  width: 100%;
}

.modal__switch {
  margin-top: 14px;
  width: 100%;
  border: none;
  background: transparent;
  color: #2563eb;
  font-weight: 600;
  cursor: pointer;
}
</style>