<template>
  <Transition name="feedback-anim">
    <div v-if="show" class="feedback" :class="'feedback--' + type">
      <p class="feedback__text">{{ text }}</p>
      <p v-if="tip" class="feedback__tip">💡 {{ tip }}</p>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  show: Boolean,
  type: { type: String, default: 'success' },
  text: String,
  tip: String
})
</script>

<style scoped>
.feedback {
  border-radius: 14px;
  padding: 16px 20px;
  margin-bottom: 20px;
  animation: shakeIn 0.3s ease;
}

.feedback--success {
  background: #f0fdf4;
  border: 2px solid #86efac;
}

.feedback--warning {
  background: #fffbeb;
  border: 2px solid #fcd34d;
}

.feedback--error {
  background: #fef2f2;
  border: 2px solid #fca5a5;
}

.feedback__text {
  font-weight: 600;
  margin-bottom: 6px;
  color: #1e293b;
}

.feedback__tip {
  color: #64748b;
  font-size: 14px;
}

.feedback-anim-enter-active {
  transition: all 0.3s ease;
}

.feedback-anim-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

@keyframes shakeIn {
  0% { transform: translateX(-4px); }
  25% { transform: translateX(4px); }
  50% { transform: translateX(-2px); }
  75% { transform: translateX(2px); }
  100% { transform: translateX(0); }
}
</style>