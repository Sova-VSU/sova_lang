<template>
  <div class="choices">
    <h3 class="choices__label">{{ label }}</h3>
    <div class="choices__grid">
      <button
        v-for="option in options"
        :key="option.id"
        class="choice-card"
        :class="{ 'choice-card--selected': selected === option.id }"
        @click="$emit('select', option.id)"
      >
        <span class="choice-card__emoji">{{ option.emoji }}</span>
        <span class="choice-card__label">{{ option.label }}</span>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  options: Array,
  selected: String
})

defineEmits(['select'])
</script>

<style scoped>
.choices {
  margin-bottom: 24px;
}

.choices__label {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #334155;
  margin-bottom: 12px;
}

.choices__grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.choice-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px 24px;
  cursor: pointer;
  transition: 0.2s;
  min-width: 100px;
}

.choice-card:hover {
  border-color: #f59e0b;
}

.choice-card--selected {
  border-color: #f59e0b;
  background: #fffbeb;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.15);
}

.choice-card__emoji {
  font-size: 32px;
}

.choice-card__label {
  font-weight: 600;
  color: #1e293b;
  font-size: 14px;
}
</style>