import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import { setOnSessionExpired } from './api/index.js'
import { restoreSession, setSessionExpiredHandler } from './stores/session'
import './style.css'

restoreSession()

const app = createApp(App)
app.use(router)

setOnSessionExpired(() => {
  if (router.currentRoute.value.meta.requiresAuth) {
    router.push('/')
  }
})

setSessionExpiredHandler(() => {
  if (router.currentRoute.value.meta.requiresAuth) {
    router.push('/')
  }
})

app.mount('#app')