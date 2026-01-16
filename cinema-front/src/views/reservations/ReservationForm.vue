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
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`
const API_TARIFS = `${API_BASE_URL}/api/tarifs`
const API_CONFIGURATION_TARIFS = `${API_BASE_URL}/api/configuration-tarifs`

const loading = ref(false)
const error = ref('')

const clients = ref([])
const seances = ref([])
const categories = ref([])
const films = ref([])
const salles = ref([])
const typePlaces = ref([])
const tarifs = ref([])
const configurationTarifs = ref([])
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

const typePlaceById = computed(() => {
  const m = new Map()
  for (const tp of typePlaces.value ?? []) {
    if (tp?.id != null) m.set(String(tp.id), tp)
  }
  return m
})

const categorieById = computed(() => {
  const m = new Map()
  for (const c of categories.value ?? []) {
    if (c?.id != null) m.set(String(c.id), c)
  }
  return m
})

const adulteCategorieId = computed(() => {
  for (const c of categories.value ?? []) {
    if (String(c?.libelle ?? '').toUpperCase() === 'ADULTE') return String(c.id)
  }
  return ''
})

const prixAdulteByTypePlaceId = computed(() => {
  const map = new Map()
  const adulteId = adulteCategorieId.value
  for (const t of tarifs.value ?? []) {
    if (t?.actif === false) continue
    const typeId = t?.typePlace?.id != null ? String(t.typePlace.id) : ''
    const catId = t?.categorieClient?.id != null ? String(t.categorieClient.id) : ''
    if (!typeId || !catId) continue
    if (adulteId && catId !== adulteId) continue
    if (t?.prix == null) continue
    map.set(typeId, Number(t.prix))
  }
  return map
})

const configurationTarifByTarif2Id = computed(() => {
  const map = new Map()
  for (const cfg of configurationTarifs.value ?? []) {
    if (cfg?.actif === false) continue
    const tarif2Id = cfg?.tarif2?.id != null ? String(cfg.tarif2.id) : ''
    if (!tarif2Id) continue
    map.set(tarif2Id, cfg)
  }
  return map
})

const tarifById = computed(() => {
  const map = new Map()
  for (const t of tarifs.value ?? []) {
    if (t?.id != null) map.set(String(t.id), t)
  }
  return map
})

const prixByTypePlaceAndCategorieId = computed(() => {
  const map = new Map()
  for (const t of tarifs.value ?? []) {
    if (t?.actif === false) continue
    const typeId = t?.typePlace?.id != null ? String(t.typePlace.id) : ''
    const catId = t?.categorieClient?.id != null ? String(t.categorieClient.id) : ''
    if (!typeId || !catId) continue

    const tarifId = t?.id != null ? String(t.id) : ''
    const basePrice = t?.prix != null ? Number(t.prix) : 0
    if (!tarifId) {
      map.set(`${typeId}|${catId}`, basePrice)
      continue
    }

    const cfg = configurationTarifByTarif2Id.value.get(tarifId)
    if (!cfg) {
      map.set(`${typeId}|${catId}`, basePrice)
      continue
    }

    const tarif1Id = cfg?.tarif1?.id != null ? String(cfg.tarif1.id) : ''
    const pct = cfg?.pourcentage != null ? Number(cfg.pourcentage) : 0
    const tarif1 = tarif1Id ? tarifById.value.get(tarif1Id) : null
    const tarif1Price = tarif1?.prix != null ? Number(tarif1.prix) : NaN
    if (!Number.isFinite(tarif1Price) || !Number.isFinite(pct)) {
      map.set(`${typeId}|${catId}`, basePrice)
      continue
    }

    map.set(`${typeId}|${catId}`, (tarif1Price * pct) / 100)
  }
  return map
})

const placeTypeIdByPlaceId = computed(() => {
  const m = new Map()
  for (const p of places.value ?? []) {
    if (p?.id == null) continue
    const typeId = p?.typePlaceId != null ? String(p.typePlaceId) : ''
    if (!typeId) continue
    m.set(String(p.id), typeId)
  }
  return m
})

const selectedPlaces = computed(() => {
  const ids = new Set((selectedPlaceIds.value ?? []).map((x) => String(x)))
  return (places.value ?? []).filter((p) => ids.has(String(p?.id)))
})

const occupiedPlaces = computed(() => (places.value ?? []).filter((p) => Boolean(p?.occupee)))

const groupPlacesByType = (list) => {
  const map = new Map()
  for (const p of list ?? []) {
    const typeId = p?.typePlaceId != null ? String(p.typePlaceId) : ''
    const tp = typeId ? typePlaceById.value.get(typeId) : null
    const libelle = tp?.libelle ?? (typeId ? `Type ${typeId}` : 'Inconnu')
    const key = typeId || libelle
    if (!map.has(key)) map.set(key, { typePlaceId: typeId, libelle, count: 0 })
    map.get(key).count += 1
  }
  const arr = Array.from(map.values())
  arr.sort((a, b) => String(a.libelle).localeCompare(String(b.libelle)))
  return arr
}

const selectedItems = computed(() => buildItemsFromSelection())

const selectedByTypeAndCategorie = computed(() => {
  const map = new Map()

  for (const it of selectedItems.value ?? []) {
    const placeId = it?.placeId != null ? String(it.placeId) : ''
    const catId = it?.categorieClientId != null ? String(it.categorieClientId) : ''
    if (!placeId || !catId) continue

    const typeId = placeTypeIdByPlaceId.value.get(placeId) ?? ''
    const tp = typeId ? typePlaceById.value.get(typeId) : null
    const typeLibelle = tp?.libelle ?? (typeId ? `Type ${typeId}` : 'Inconnu')

    const cat = categorieById.value.get(catId)
    const catLibelle = cat?.libelle ?? (catId ? `Cat ${catId}` : 'Inconnu')

    const key = `${typeId}|${catId}`
    if (!map.has(key)) {
      map.set(key, {
        typePlaceId: typeId,
        typeLibelle,
        categorieClientId: catId,
        categorieLibelle: catLibelle,
        count: 0,
      })
    }
    map.get(key).count += 1
  }

  const arr = Array.from(map.values())
  arr.sort((a, b) => {
    const t = String(a.typeLibelle).localeCompare(String(b.typeLibelle))
    if (t !== 0) return t
    return String(a.categorieLibelle).localeCompare(String(b.categorieLibelle))
  })
  return arr
})

const selectedByType = computed(() => groupPlacesByType(selectedPlaces.value))

const occupiedByType = computed(() => groupPlacesByType(occupiedPlaces.value))

const totalReservedMontant = computed(() =>
  selectedByTypeAndCategorie.value.reduce((sum, r) => {
    const prix = prixByTypePlaceAndCategorieId.value.get(`${String(r.typePlaceId)}|${String(r.categorieClientId)}`) ?? 0
    return sum + r.count * prix
  }, 0),
)

const totalOccupiedMontant = computed(() =>
  occupiedByType.value.reduce(
    (sum, r) => sum + r.count * (prixAdulteByTypePlaceId.value.get(String(r.typePlaceId)) ?? 0),
    0,
  ),
)

const formatMoney = (n) => {
  const v = Number(n)
  if (!Number.isFinite(v)) return '-'
  return v.toFixed(2)
}

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [cRes, sRes, catRes, fRes, saRes, tpRes, tRes, cfgRes] = await Promise.all([
      fetch(API_CLIENTS),
      fetch(API_SEANCES),
      fetch(API_CATEGORIES),
      fetch(API_FILMS),
      fetch(API_SALLES),
      fetch(API_TYPE_PLACES),
      fetch(API_TARIFS),
      fetch(API_CONFIGURATION_TARIFS),
    ])
    if (!cRes.ok) throw new Error(`Clients HTTP ${cRes.status}`)
    if (!sRes.ok) throw new Error(`Séances HTTP ${sRes.status}`)
    if (!catRes.ok) throw new Error(`Catégories HTTP ${catRes.status}`)
    if (!fRes.ok) throw new Error(`Films HTTP ${fRes.status}`)
    if (!saRes.ok) throw new Error(`Salles HTTP ${saRes.status}`)
    if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)
    if (!tRes.ok) throw new Error(`Tarifs HTTP ${tRes.status}`)
    if (!cfgRes.ok) throw new Error(`Configuration tarifs HTTP ${cfgRes.status}`)

    clients.value = await cRes.json()
    seances.value = await sRes.json()
    categories.value = await catRes.json()
    films.value = await fRes.json()
    salles.value = await saRes.json()
    typePlaces.value = await tpRes.json()
    tarifs.value = await tRes.json()
    configurationTarifs.value = await cfgRes.json()
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

const getPlaceLabelClass = (p) => {
  if (p?.occupee || isSelected(p?.id)) return 'text-white'

  const typeId = p?.typePlaceId != null ? String(p.typePlaceId) : ''
  const libelle = typeId ? typePlaceById.value.get(typeId)?.libelle : ''
  const upper = String(libelle ?? '').toUpperCase()

  if (upper === 'STANDARD') return 'text-primary'
  if (upper === 'VIP') return 'text-warning'
  if (upper === 'PMR') return 'text-info'
  return 'text-dark'
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
                            <span :class="getPlaceLabelClass(p)">{{ p.label }}</span>
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

                        <div class="legend mt-2 pt-2 border-top">
                          <div class="d-flex align-items-center gap-2 mb-1">
                            <span class="legend-color" style="background-color: var(--bs-primary); width: 20px; height: 20px;"></span>
                            <small>STANDARD</small>
                          </div>
                          <div class="d-flex align-items-center gap-2 mb-1">
                            <span class="legend-color" style="background-color: var(--bs-warning); width: 20px; height: 20px;"></span>
                            <small>VIP</small>
                          </div>
                          <div class="d-flex align-items-center gap-2">
                            <span class="legend-color" style="background-color: var(--bs-info); width: 20px; height: 20px;"></span>
                            <small>PMR</small>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12">
                <div class="mt-4 pt-3 border-top">
                  <div class="fw-semibold mb-2">Détail par type réservé</div>

                  <div v-if="selectedPlaceIds.length === 0" class="text-muted">Aucune place sélectionnée</div>

                  <div v-else>
                    <div
                      v-for="row in selectedByTypeAndCategorie"
                      :key="String(row.typePlaceId) + '|' + String(row.categorieClientId)"
                      class="d-flex justify-content-between"
                    >
                      <span>
                        {{ row.count }} {{ row.typeLibelle }} {{ row.categorieLibelle }} tarif
                        {{ formatMoney(prixByTypePlaceAndCategorieId.get(`${String(row.typePlaceId)}|${String(row.categorieClientId)}`) ?? 0) }}
                        total
                        {{
                          formatMoney(
                            row.count *
                              (prixByTypePlaceAndCategorieId.get(`${String(row.typePlaceId)}|${String(row.categorieClientId)}`) ?? 0),
                          )
                        }}
                      </span>
                    </div>

                    <div class="pt-2 mt-2 border-top d-flex justify-content-between">
                      <span class="text-muted">TOTAL :</span>
                      <span class="fw-semibold">{{ formatMoney(totalReservedMontant) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12">
                <div class="mt-4 pt-3 border-top">
                  <div class="fw-semibold mb-2">Détail par type non disponible</div>

                  <div v-if="occupiedPlaces.length === 0" class="text-muted">Aucune place non disponible</div>

                  <div v-else>
                    <div
                      v-for="row in occupiedByType"
                      :key="'occ|' + row.typePlaceId + '|' + row.libelle"
                      class="d-flex justify-content-between"
                    >
                      <span>
                        {{ row.count }} {{ row.libelle }} tarif {{ formatMoney(prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0) }} total {{ formatMoney(row.count * (prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0)) }}
                      </span>
                    </div>

                    <div class="pt-2 mt-2 border-top d-flex justify-content-between">
                      <span class="text-muted">TOTAL :</span>
                      <span class="fw-semibold">{{ formatMoney(totalOccupiedMontant) }}</span>
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