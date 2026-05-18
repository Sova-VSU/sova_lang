<template>
  <div class="barista-block">
    <div class="barista-avatar">{{ avatar }}</div>
    <div class="barista-bubble">
      <p class="barista-text">"{{ text }}"</p>
      <button class="speak-btn" @click="speak" :disabled="isSpeaking">
        🔊 Воспроизвести
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  text: String,
  avatar: { type: String, default: '🧑‍🍳' }
})

const isSpeaking = ref(false)

function speak() {
  if (!window.speechSynthesis) return
  window.speechSynthesis.cancel()

  const utterance = new SpeechSynthesisUtterance(props.text)
  utterance.lang = 'en-US'
  utterance.rate = 0.9

  isSpeaking.value = true
  utterance.onend = () => (isSpeaking.value = false)
  utterance.onerror = () => (isSpeaking.value = false)

  window.speechSynthesis.speak(utterance)
}
</script>

<style scoped>
.barista-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.barista-avatar {
  font-size: 40px;
}

.barista-bubble {
  background: var(--scenario-npc-bg, #fef9c3);
  border-radius: 16px;
  padding: 20px 24px;
  text-align: center;
  max-width: 500px;
}

.barista-text {
  font-size: 17px;
  color: #1e293b;
  font-style: italic;
  margin-bottom: 12px;
}

.speak-btn {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  padding: 8px 16px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.2s;
}

.speak-btn:hover:not(:disabled) {
  border-color: #3b82f6;
  color: #3b82f6;
}

.speak-btn:disabled {
  opacity: 0.5;
}
</style>