import { reactive, readonly } from 'vue'

const state = reactive({
  toasts: [],
})

const remove = (id) => {
  const idx = state.toasts.findIndex((t) => t.id === id)
  if (idx !== -1) state.toasts.splice(idx, 1)
}

const push = (toast) => {
  const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`
  const duration = toast.duration ?? 3000
  state.toasts.push({
    id,
    type: toast.type ?? 'info',
    title: toast.title ?? '',
    message: toast.message ?? '',
    duration,
  })

  if (duration > 0) {
    window.setTimeout(() => remove(id), duration)
  }

  return id
}

export function useToast() {
  return {
    state: readonly(state),
    push,
    remove,
    success: (message, opts = {}) => push({ ...opts, type: 'success', message }),
    error: (message, opts = {}) => push({ ...opts, type: 'danger', message }),
    info: (message, opts = {}) => push({ ...opts, type: 'info', message }),
  }
}
