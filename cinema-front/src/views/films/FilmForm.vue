<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const API_FILMS = `${API_BASE_URL}/api/films`

const loading = ref(false)
const error = ref('')

const filmId = route.params?.id

const form = ref({
  titre: '',
  description: '',
  dureeMinutes: '',
  dateSortie: '',
  ageMin: '',
  langueOriginale: '',
})

const loadIfEdit = async () => {
  if (!filmId) return
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${API_FILMS}/${filmId}`)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const f = await res.json()
    form.value.titre = f?.titre ?? ''
    form.value.description = f?.description ?? ''
    form.value.dureeMinutes = f?.dureeMinutes != null ? String(f.dureeMinutes) : ''
    form.value.dateSortie = f?.dateSortie ?? ''
    form.value.ageMin = f?.ageMin != null ? String(f.ageMin) : ''
    form.value.langueOriginale = f?.langueOriginale ?? ''
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
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
      titre: form.value.titre,
      description: form.value.description || null,
      dureeMinutes: Number(form.value.dureeMinutes),
      dateSortie: form.value.dateSortie || null,
      ageMin: form.value.ageMin ? Number(form.value.ageMin) : null,
      langueOriginale: form.value.langueOriginale || null,
    }

    const url = filmId ? `${API_FILMS}/${filmId}` : API_FILMS
    const method = filmId ? 'PUT' : 'POST'

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success(filmId ? 'Film mis à jour' : 'Film créé')
    await router.push('/films')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadIfEdit)
</script>

<template>
  <div class="pagetitle">
    <h1>Films</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ filmId ? 'Modifier film' : 'Nouveau film' }}</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">Titre</label>
                <input v-model="form.titre" class="form-control" type="text" required />
              </div>

              <div class="col-12">
                <label class="form-label">Description</label>
                <textarea v-model="form.description" class="form-control" rows="3"></textarea>
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Durée (minutes)</label>
                <input v-model="form.dureeMinutes" class="form-control" type="number" min="1" required />
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Date sortie</label>
                <input v-model="form.dateSortie" class="form-control" type="date" />
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Âge minimum</label>
                <input v-model="form.ageMin" class="form-control" type="number" min="0" />
              </div>

              <div class="col-12">
                <label class="form-label">Langue originale</label>
                <input v-model="form.langueOriginale" class="form-control" type="text" />
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/films">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
