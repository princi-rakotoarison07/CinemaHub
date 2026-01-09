<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/tests`

const route = useRoute()
const router = useRouter()

const toast = useToast()

const id = computed(() => route.params.id)
const isEdit = computed(() => !!id.value)

const form = ref({
  ip: '',
  port: '',
})

const loading = ref(false)
const error = ref('')

const loadOne = async () => {
  if (!isEdit.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${API_BASE}/${id.value}`)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    if (!data) throw new Error('Introuvable')
    form.value.ip = data.ip ?? ''
    form.value.port = data.port ?? ''
  } catch (e) {
    error.value = e?.message ?? 'Erreur'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    const payload = {
      ip: form.value.ip,
      port: Number(form.value.port),
    }

    const url = isEdit.value ? `${API_BASE}/${id.value}` : API_BASE
    const method = isEdit.value ? 'PUT' : 'POST'

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success(isEdit.value ? 'Modifié avec succès' : 'Créé avec succès')

    await router.push('/tests')
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors de l\'enregistrement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadOne)
</script>

<template>
  <div class="pagetitle">
    <h1>Test</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#">Home</a></li>
        <li class="breadcrumb-item"><RouterLink to="/tests">Test</RouterLink></li>
        <li class="breadcrumb-item active">{{ isEdit ? 'Modifier' : 'Nouveau' }}</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ isEdit ? 'Modifier un test' : 'Créer un test' }}</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">IP</label>
                <input v-model="form.ip" class="form-control" type="text" required />
              </div>

              <div class="col-12">
                <label class="form-label">Port</label>
                <input v-model="form.port" class="form-control" type="number" required />
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">
                  Enregistrer
                </button>
                <RouterLink class="btn btn-secondary" to="/tests">Annuler</RouterLink>
              </div>
            </form>

            <div v-if="loading" class="text-muted mt-3">Chargement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
