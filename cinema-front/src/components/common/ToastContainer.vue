<script setup>
import { useToast } from '../../composables/useToast'

const { state, remove } = useToast()

const typeClass = (type) => {
  if (type === 'success') return 'text-bg-success'
  if (type === 'danger') return 'text-bg-danger'
  return 'text-bg-primary'
}
</script>

<template>
  <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 9999">
    <div
      v-for="t in state.toasts"
      :key="t.id"
      class="toast show align-items-center border-0 mb-2"
      :class="typeClass(t.type)"
      role="alert"
      aria-live="assertive"
      aria-atomic="true"
    >
      <div class="d-flex">
        <div class="toast-body">
          <strong v-if="t.title">{{ t.title }}</strong>
          <div>{{ t.message }}</div>
        </div>
        <button
          type="button"
          class="btn-close btn-close-white me-2 m-auto"
          aria-label="Close"
          @click="remove(t.id)"
        ></button>
      </div>
    </div>
  </div>
</template>
