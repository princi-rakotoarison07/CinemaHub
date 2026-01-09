<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/salles`

const route = useRoute()
const router = useRouter()

const toast = useToast()

const id = computed(() => route.params.id)
const isEdit = computed(() => !!id.value)

const form = ref({
  nom: '',
  capacite: '',
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
    form.value.nom = data.nom ?? ''
    form.value.capacite = data.capacite ?? ''
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
      nom: form.value.nom,
      capacite: Number(form.value.capacite),
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

    await router.push('/salles')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadOne)
</script>

<template>
  <div class="pagetitle">
    <h1>Salles</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#">Home</a></li>
        <li class="breadcrumb-item"><RouterLink to="/salles">Salles</RouterLink></li>
        <li class="breadcrumb-item active">{{ isEdit ? 'Modifier' : 'Nouveau' }}</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ isEdit ? 'Modifier une salle' : 'Créer une salle' }}</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">Nom</label>
                <input v-model="form.nom" class="form-control" type="text" required />
              </div>

              <div class="col-12">
                <label class="form-label">Capacité</label>
                <input v-model="form.capacite" class="form-control" type="number" min="1" required />
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">
                  Enregistrer
                </button>
                <RouterLink class="btn btn-secondary" to="/salles">Annuler</RouterLink>
              </div>
            </form>

            <div v-if="loading" class="text-muted mt-3">Chargement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
