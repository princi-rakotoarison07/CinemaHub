<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_RESERVATIONS = `${API_BASE_URL}/api/reservations`
const API_CLIENTS = `${API_BASE_URL}/api/clients`
const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`
const API_FILMS = `${API_BASE_URL}/api/films`
const API_SALLES = `${API_BASE_URL}/api/salles`

const loading = ref(false)
const error = ref('')

const clients = ref([])
const seances = ref([])
const categories = ref([])
const films = ref([])
const salles = ref([])
const places = ref([])
const selectedPlaceIds = ref([])

const form = ref({
  clientId: '',
  seanceId: '',
  categorieClientId: '',
})

const selectedSeance = computed(() =>
  seances.value.find((s) => String(s.id) === String(form.value.seanceId)),
)

const getFilmTitre = (filmId) => {
  const f = films.value.find((x) => String(x.id) === String(filmId))
  return f?.titre ?? `Film ${filmId}`
}

const getSalleNom = (salleId) => {
  const s = salles.value.find((x) => String(x.id) === String(salleId))
  return s?.nom ?? `Salle ${salleId}`
}

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [cRes, sRes, catRes, fRes, saRes] = await Promise.all([
      fetch(API_CLIENTS),
      fetch(API_SEANCES),
      fetch(API_CATEGORIES),
      fetch(API_FILMS),
      fetch(API_SALLES),
    ])
    if (!cRes.ok) throw new Error(`Clients HTTP ${cRes.status}`)
    if (!sRes.ok) throw new Error(`Séances HTTP ${sRes.status}`)
    if (!catRes.ok) throw new Error(`Catégories HTTP ${catRes.status}`)
    if (!fRes.ok) throw new Error(`Films HTTP ${fRes.status}`)
    if (!saRes.ok) throw new Error(`Salles HTTP ${saRes.status}`)

    clients.value = await cRes.json()
    seances.value = await sRes.json()
    categories.value = await catRes.json()
    films.value = await fRes.json()
    salles.value = await saRes.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const loadPlacesForSeance = async () => {
  places.value = []
  selectedPlaceIds.value = []
  const seanceId = selectedSeance.value?.id
  if (!seanceId) return

  try {
    const res = await fetch(`${API_SEANCES}/${seanceId}/places`)
    if (!res.ok) throw new Error(`Places HTTP ${res.status}`)
    places.value = await res.json()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur lors du chargement des places')
  }
}

watch(
  () => form.value.seanceId,
  async () => {
    await loadPlacesForSeance()
  },
)

const isSelected = (placeId) => selectedPlaceIds.value.includes(placeId)

const togglePlace = (p) => {
  if (p.occupee) return
  const id = p.id
  if (isSelected(id)) {
    selectedPlaceIds.value = selectedPlaceIds.value.filter((x) => x !== id)
  } else {
    selectedPlaceIds.value = [...selectedPlaceIds.value, id]
  }
}

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    if (!form.value.categorieClientId) {
      throw new Error('Catégorie client obligatoire')
    }
    if (selectedPlaceIds.value.length === 0) {
      throw new Error('Sélectionner au moins une place')
    }

    const payload = {
      clientId: Number(form.value.clientId),
      seanceId: Number(form.value.seanceId),
      items: selectedPlaceIds.value.map((placeId) => ({
        placeId: Number(placeId),
        categorieClientId: Number(form.value.categorieClientId),
      })),
    }

    const res = await fetch(API_RESERVATIONS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Réservation créée')
    await router.push('/reservations')
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
    <h1>Réservations</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Nouvelle réservation</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">Client</label>
                <select v-model="form.clientId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="c in clients" :key="c.id" :value="String(c.id)">
                    {{ c.nom }} {{ c.prenom }} ({{ c.id }})
                  </option>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label">Séance</label>
                <select v-model="form.seanceId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="s in seances" :key="s.id" :value="String(s.id)">
                    {{ s.id }} - {{ s.dateHeure }} ({{ getFilmTitre(s.film?.id) }}, {{ getSalleNom(s.salle?.id) }})
                  </option>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label">Catégorie client</label>
                <select v-model="form.categorieClientId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="cat in categories" :key="cat.id" :value="String(cat.id)">
                    {{ cat.libelle }} ({{ cat.id }})
                  </option>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label">Places</label>
                <div v-if="!form.seanceId" class="text-muted">Sélectionne une séance pour voir les places.</div>
                <div v-else class="d-flex flex-wrap gap-2">
                  <button
                    v-for="p in places"
                    :key="p.id"
                    type="button"
                    class="btn btn-sm"
                    :class="[
                      p.occupee
                        ? 'btn-danger'
                        : isSelected(p.id)
                          ? 'btn-success'
                          : 'btn-outline-secondary',
                    ]"
                    :disabled="p.occupee"
                    @click="togglePlace(p)"
                  >
                    {{ p.label }}
                  </button>
                </div>
                <div class="text-muted mt-2">
                  Sélectionnées: {{ selectedPlaceIds.length }}
                </div>
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">
                  Enregistrer
                </button>
                <RouterLink class="btn btn-secondary" to="/reservations">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
