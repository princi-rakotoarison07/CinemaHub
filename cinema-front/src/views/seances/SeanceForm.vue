<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_FILMS = `${API_BASE_URL}/api/films`
const API_SALLES = `${API_BASE_URL}/api/salles`

const loading = ref(false)
const error = ref('')

const films = ref([])
const salles = ref([])

const form = ref({
  filmId: '',
  salleId: '',
  dateHeureLocal: '',
  langue: '',
  version: '',
})

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [fRes, sRes] = await Promise.all([fetch(API_FILMS), fetch(API_SALLES)])
    if (!fRes.ok) throw new Error(`Films HTTP ${fRes.status}`)
    if (!sRes.ok) throw new Error(`Salles HTTP ${sRes.status}`)
    films.value = await fRes.json()
    salles.value = await sRes.json()
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
    if (!form.value.filmId) throw new Error('Film obligatoire')
    if (!form.value.salleId) throw new Error('Salle obligatoire')
    if (!form.value.dateHeureLocal) throw new Error('Date/heure obligatoire')

    const iso = new Date(form.value.dateHeureLocal).toISOString()

    const payload = {
      film: { id: Number(form.value.filmId) },
      salle: { id: Number(form.value.salleId) },
      dateHeure: iso,
      langue: form.value.langue,
      version: form.value.version,
    }

    const res = await fetch(API_SEANCES, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Séance créée')
    await router.push('/seances')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadRefs)
</script>

<template>
  <div class="pagetitle">
    <h1>Séances</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Nouvelle séance</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12 col-md-6">
                <label class="form-label">Film</label>
                <select v-model="form.filmId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="f in films" :key="f.id" :value="String(f.id)">
                    {{ f.titre }} ({{ f.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Salle</label>
                <select v-model="form.salleId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="s in salles" :key="s.id" :value="String(s.id)">
                    {{ s.nom }} ({{ s.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Date/Heure</label>
                <input v-model="form.dateHeureLocal" class="form-control" type="datetime-local" required />
              </div>

              <div class="col-12 col-md-3">
                <label class="form-label">Langue</label>
                <input v-model="form.langue" class="form-control" type="text" />
              </div>

              <div class="col-12 col-md-3">
                <label class="form-label">Version</label>
                <input v-model="form.version" class="form-control" type="text" placeholder="VF / VOST" />
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/seances">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
