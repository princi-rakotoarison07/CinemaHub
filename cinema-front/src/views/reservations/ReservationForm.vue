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

const mode = ref('single')

const form = ref({
  clientId: '',
  seanceId: '',
  categorieClientId: '',
  quantite: 1,
})

const categorieRows = ref([])
const newRowCounter = ref(0)

const createNewCategorieRow = () => {
  newRowCounter.value += 1
  return {
    id: `new-${newRowCounter.value}`,
    categorieClientId: '',
    quantite: 1,
  }
}

const addCategorieRow = () => {
  categorieRows.value = [...categorieRows.value, createNewCategorieRow()]
}

const removeCategorieRow = (rowId) => {
  categorieRows.value = categorieRows.value.filter((r) => String(r.id) !== String(rowId))
}

const nbPlaceRequired = computed(() => {
  if (mode.value === 'single') return Number(form.value.quantite ?? 0)
  return (categorieRows.value ?? []).reduce((sum, r) => sum + Number(r?.quantite ?? 0), 0)
})

const canSelectMorePlaces = computed(() => selectedPlaceIds.value.length < nbPlaceRequired.value)

const categorieCountById = computed(() => {
  if (mode.value === 'single') {
    const m = new Map()
    const id = String(form.value.categorieClientId ?? '')
    const q = Number(form.value.quantite ?? 0)
    if (id && q > 0) m.set(id, q)
    return m
  }
  const m = new Map()
  for (const r of categorieRows.value ?? []) {
    const id = String(r?.categorieClientId ?? '')
    if (!id) continue
    const q = Number(r?.quantite ?? 0)
    m.set(id, (m.get(id) ?? 0) + q)
  }
  return m
})

const buildItemsFromSelection = () => {
  const placeIds = selectedPlaceIds.value.map((x) => Number(x))
  const cats = Array.from(categorieCountById.value.entries())
    .map(([categorieClientId, quantite]) => ({ categorieClientId: Number(categorieClientId), quantite: Number(quantite) }))
    .filter((x) => x.categorieClientId && x.quantite > 0)
    .sort((a, b) => a.categorieClientId - b.categorieClientId)

  const items = []
  let idx = 0
  for (const c of cats) {
    for (let i = 0; i < c.quantite; i++) {
      const placeId = placeIds[idx]
      if (!placeId) break
      items.push({ placeId, categorieClientId: c.categorieClientId })
      idx += 1
    }
  }
  return items
}

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
    if (!canSelectMorePlaces.value) return
    selectedPlaceIds.value = [...selectedPlaceIds.value, id]
  }
}

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    if (mode.value === 'single') {
      if (!form.value.categorieClientId) throw new Error('Catégorie client obligatoire')
      if (Number(form.value.quantite) <= 0) throw new Error('Quantité invalide')
    } else {
      if (!categorieRows.value.length) throw new Error('Ajouter au moins une catégorie client')
      for (const r of categorieRows.value) {
        if (!r.categorieClientId) throw new Error('Catégorie client obligatoire')
        if (Number(r.quantite) <= 0) throw new Error('Quantité invalide')
      }
    }

    if (nbPlaceRequired.value <= 0) throw new Error('Nombre de places invalide')
    if (selectedPlaceIds.value.length !== nbPlaceRequired.value) {
      throw new Error(`Sélectionner exactement ${nbPlaceRequired.value} place(s)`) 
    }

    const payload = {
      clientId: Number(form.value.clientId),
      seanceId: Number(form.value.seanceId),
      items: buildItemsFromSelection(),
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
onMounted(() => {
  if (categorieRows.value.length === 0) addCategorieRow()
})
</script>

<template>
  <div class="pagetitle">
    <h1>Réservations</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Nouvelle réservation</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-4" @submit.prevent="submit">
              <!-- Deux colonnes principales -->
              <div class="col-12 col-lg-8">
                <!-- Mode d'insertion -->
                <div class="row g-3 mb-4">
                  <div class="col-12">
                    <label class="form-label">Mode d'insertion</label>
                    <div class="d-flex gap-3">
                      <div class="form-check">
                        <input
                          id="mode-single"
                          v-model="mode"
                          class="form-check-input"
                          type="radio"
                          value="single"
                        />
                        <label class="form-check-label" for="mode-single">Simple</label>
                      </div>
                      <div class="form-check">
                        <input
                          id="mode-multiple"
                          v-model="mode"
                          class="form-check-input"
                          type="radio"
                          value="multiple"
                        />
                        <label class="form-check-label" for="mode-multiple">Multiple (par catégories)</label>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Client et Séance -->
                <div class="row g-3 mb-4">
                  <div class="col-12 col-md-6">
                    <label class="form-label">Client</label>
                    <select v-model="form.clientId" class="form-select" required>
                      <option value="" disabled>Sélectionner...</option>
                      <option v-for="c in clients" :key="c.id" :value="String(c.id)">
                        {{ c.nom }} {{ c.prenom }} ({{ c.id }})
                      </option>
                    </select>
                  </div>

                  <div class="col-12 col-md-6">
                    <label class="form-label">Séance</label>
                    <select v-model="form.seanceId" class="form-select" required>
                      <option value="" disabled>Sélectionner...</option>
                      <option v-for="s in seances" :key="s.id" :value="String(s.id)">
                        {{ s.id }} - {{ s.dateHeure }} ({{ getFilmTitre(s.film?.id) }}, {{ getSalleNom(s.salle?.id) }})
                      </option>
                    </select>
                  </div>
                </div>

                <!-- Catégories selon le mode -->
                <div class="row g-3 mb-4">
                  <div class="col-12">
                    <div v-if="mode === 'single'">
                      <div class="row g-3">
                        <div class="col-12 col-md-8">
                          <label class="form-label">Catégorie client</label>
                          <select v-model="form.categorieClientId" class="form-select" required>
                            <option value="" disabled>Sélectionner...</option>
                            <option v-for="cat in categories" :key="cat.id" :value="String(cat.id)">
                              {{ cat.libelle }} ({{ cat.id }})
                            </option>
                          </select>
                        </div>
                        <div class="col-12 col-md-4">
                          <label class="form-label">Quantité</label>
                          <input v-model.number="form.quantite" class="form-control" type="number" min="1" step="1" required />
                        </div>
                      </div>
                    </div>

                    <div v-else>
                      <label class="form-label">Catégories client (multiple)</label>
                      <div class="table-responsive mb-3">
                        <table class="table table-sm align-middle">
                          <thead>
                            <tr>
                              <th style="width: 60%">Catégorie</th>
                              <th style="width: 30%">Quantité</th>
                              <th style="width: 10%"></th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="r in categorieRows" :key="r.id">
                              <td>
                                <select v-model="r.categorieClientId" class="form-select" required>
                                  <option value="" disabled>Sélectionner...</option>
                                  <option v-for="cat in categories" :key="cat.id" :value="String(cat.id)">
                                    {{ cat.libelle }} ({{ cat.id }})
                                  </option>
                                </select>
                              </td>
                              <td>
                                <input v-model.number="r.quantite" class="form-control" type="number" min="1" step="1" required />
                              </td>
                              <td class="text-end">
                                <button
                                  class="btn btn-sm btn-outline-danger"
                                  type="button"
                                  :disabled="categorieRows.length <= 1"
                                  @click="removeCategorieRow(r.id)"
                                >
                                  Supprimer
                                </button>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                      <button class="btn btn-outline-primary mb-3" type="button" @click="addCategorieRow">Ajouter catégorie</button>
                    </div>

                    <div class="mt-3 pt-2 border-top">
                      <strong>Nombre de places à sélectionner : {{ nbPlaceRequired }}</strong>
                    </div>
                  </div>
                </div>

                <!-- Boutons d'action -->
                <div class="row g-3">
                  <div class="col-12 d-flex gap-2">
                    <button class="btn btn-primary" type="submit" :disabled="loading">
                      Enregistrer
                    </button>
                    <RouterLink class="btn btn-secondary" to="/reservations">Annuler</RouterLink>
                  </div>
                </div>

                <div v-if="loading" class="text-muted mt-3">Chargement...</div>
              </div>

              <!-- Colonne droite : Places -->
              <div class="col-12 col-lg-4">
                <div class="card">
                  <div class="card-body places-card-body">
                    <h6 class="card-title">Sélection des places</h6>
                    
                    <div v-if="!form.seanceId" class="alert alert-warning">
                      <i class="bi bi-info-circle"></i> Veuillez sélectionner une séance pour voir les places disponibles.
                    </div>

                    <div v-if="!form.seanceId" class="places-grid mb-3"></div>
                    
                    <div v-else>
                      <div class="places-grid mb-3">
                        <div class="d-flex flex-wrap gap-1">
                          <button
                            v-for="p in places"
                            :key="p.id"
                            type="button"
                            class="btn btn-sm place-btn"
                            :class="[
                              p.occupee
                                ? 'btn-danger'
                                : isSelected(p.id)
                                  ? 'btn-success'
                                  : 'btn-outline-secondary',
                            ]"
                            :disabled="p.occupee || (!isSelected(p.id) && !canSelectMorePlaces)"
                            @click="togglePlace(p)"
                            :title="p.occupee ? 'Place occupée' : 'Cliquer pour sélectionner'"
                          >
                            {{ p.label }}
                          </button>
                        </div>
                      </div>
                      
                      <div class="places-info">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                          <span class="text-muted">Sélectionnées :</span>
                          <span class="badge bg-primary fs-6">
                            {{ selectedPlaceIds.length }} / {{ nbPlaceRequired }}
                          </span>
                        </div>
                        
                        <div class="legend">
                          <div class="d-flex align-items-center gap-2 mb-1">
                            <span class="legend-color" style="background-color: #198754; width: 20px; height: 20px;"></span>
                            <small>Sélectionnée</small>
                          </div>
                          <div class="d-flex align-items-center gap-2 mb-1">
                            <span class="legend-color" style="background-color: #dc3545; width: 20px; height: 20px;"></span>
                            <small>Occupée</small>
                          </div>
                          <div class="d-flex align-items-center gap-2">
                            <span class="legend-color" style="background-color: #6c757d; border: 1px solid #6c757d; width: 20px; height: 20px;"></span>
                            <small>Disponible</small>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.places-grid {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 0.375rem;
  padding: 1rem;
  background-color: #f8f9fa;
}

.places-card-body {
  min-height: 560px;
}

.place-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  margin: 2px;
}

.legend-color {
  border-radius: 3px;
  display: inline-block;
}

.places-info {
  padding: 1rem;
  border-top: 1px solid #dee2e6;
  margin-top: 1rem;
}

.legend {
  font-size: 0.85rem;
  color: #6c757d;
}

@media (max-width: 992px) {
  .places-grid {
    max-height: 300px;
  }

  .places-card-body {
    min-height: 480px;
  }
}
</style>