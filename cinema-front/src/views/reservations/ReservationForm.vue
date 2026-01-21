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
const selectedCategorieByPlaceId = ref({})

const mode = ref('single')

const form = ref({
  clientId: '',
  seanceId: '',
  categorieClientId: '',
  typePlaceId: '',
  quantite: 1,
})

const categorieRows = ref([])
const newRowCounter = ref(0)

const createNewCategorieRow = () => {
  newRowCounter.value += 1
  return {
    id: `new-${newRowCounter.value}`,
    categorieClientId: '',
    typePlaceId: '',
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
  const map = selectedCategorieByPlaceId.value ?? {}
  const canUseMapping =
    selectedPlaceIds.value.length > 0 &&
    selectedPlaceIds.value.every((pid) => map[String(pid)] != null && String(map[String(pid)]) !== '')

  if (canUseMapping) {
    return selectedPlaceIds.value
      .map((pid) => ({
        placeId: Number(pid),
        categorieClientId: Number(map[String(pid)]),
      }))
      .filter((x) => x.placeId && x.categorieClientId)
  }

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
  selectedCategorieByPlaceId.value = {}
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

const autoSelectPlaces = () => {
  const requests = []
  if (mode.value === 'single') {
    const catId = String(form.value.categorieClientId ?? '')
    const qty = Number(form.value.quantite ?? 0)
    const typeId = String(form.value.typePlaceId ?? '')
    if (!catId) {
      toast.error('Catégorie client obligatoire')
      return
    }
    if (!typeId) {
      toast.error('Type de place obligatoire')
      return
    }
    if (!qty || qty <= 0) {
      toast.error('Quantité invalide')
      return
    }
    requests.push({ categorieClientId: catId, quantite: qty, typePlaceId: typeId })
  } else {
    for (const r of categorieRows.value ?? []) {
      const catId = String(r?.categorieClientId ?? '')
      const qty = Number(r?.quantite ?? 0)
      const typeId = String(r?.typePlaceId ?? '')
      if (!catId) {
        toast.error('Catégorie client obligatoire')
        return
      }
      if (!typeId) {
        toast.error('Type de place obligatoire')
        return
      }
      if (!qty || qty <= 0) {
        toast.error('Quantité invalide')
        return
      }
      requests.push({ categorieClientId: catId, quantite: qty, typePlaceId: typeId })
    }
  }

  const sortedCandidates = (places.value ?? [])
    .filter((p) => p && !p.occupee)
    .slice()
    .sort((a, b) => {
      const ra = String(a?.rangee ?? '')
      const rb = String(b?.rangee ?? '')
      const r = ra.localeCompare(rb)
      if (r !== 0) return r
      const na = Number(a?.numero ?? 0)
      const nb = Number(b?.numero ?? 0)
      return na - nb
    })

  const used = new Set()
  const picked = []
  const mapping = {}

  for (const req of requests) {
    const need = Number(req.quantite)
    const typeId = String(req.typePlaceId)
    const matches = sortedCandidates.filter((p) => String(p?.typePlaceId ?? '') === typeId && !used.has(String(p.id)))
    if (matches.length < need) {
      toast.error(`Places disponibles insuffisantes pour le type demandé (${matches.length}/${need})`)
      return
    }
    for (let i = 0; i < need; i++) {
      const p = matches[i]
      used.add(String(p.id))
      picked.push(p.id)
      mapping[String(p.id)] = String(req.categorieClientId)
    }
  }

  selectedPlaceIds.value = picked
  selectedCategorieByPlaceId.value = mapping
}

const clearSelection = () => {
  selectedPlaceIds.value = []
  selectedCategorieByPlaceId.value = {}
}

const togglePlace = (p) => {
  if (p.occupee) return
  const id = p.id
  if (isSelected(id)) {
    selectedPlaceIds.value = selectedPlaceIds.value.filter((x) => x !== id)
    const m = { ...(selectedCategorieByPlaceId.value ?? {}) }
    delete m[String(id)]
    selectedCategorieByPlaceId.value = m
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
              <div class="col-12 col-lg-6">
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
                        <div class="col-12 col-md-4">
                          <label class="form-label">Catégorie client</label>
                          <select v-model="form.categorieClientId" class="form-select" required>
                            <option value="" disabled>Sélectionner...</option>
                            <option v-for="cat in categories" :key="cat.id" :value="String(cat.id)">
                              {{ cat.libelle }} ({{ cat.id }})
                            </option>
                          </select>
                        </div>
                        <div class="col-12 col-md-4">
                          <label class="form-label">Type de place</label>
                          <select v-model="form.typePlaceId" class="form-select" required>
                            <option value="" disabled>Sélectionner...</option>
                            <option v-for="tp in typePlaces" :key="tp.id" :value="String(tp.id)">
                              {{ tp.libelle }}
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
                              <th style="width: 45%">Catégorie</th>
                              <th style="width: 35%">Type de place</th>
                              <th style="width: 10%">Quantité</th>
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
                                <select v-model="r.typePlaceId" class="form-select" required>
                                  <option value="" disabled>Sélectionner...</option>
                                  <option v-for="tp in typePlaces" :key="tp.id" :value="String(tp.id)">
                                    {{ tp.libelle }}
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
              </div>

              <!-- Colonne droite : Places -->
              <div class="col-12 col-lg-6">
                <div class="card shadow-none border">
                  <div class="card-body places-card-body">
                    <h6 class="card-title">Sélection des places</h6>

                    <div v-if="!form.seanceId" class="alert alert-warning">
                      <i class="bi bi-info-circle"></i> Veuillez sélectionner une séance pour voir les places disponibles.
                    </div>

                    <div v-if="!form.seanceId" class="places-grid mb-3"></div>

                    <div v-else>
                      <div class="d-flex gap-2 mb-2">
                        <button class="btn btn-sm btn-outline-primary" type="button" :disabled="!places.length" @click="autoSelectPlaces">
                          Sélection automatique
                        </button>
                        <button class="btn btn-sm btn-outline-secondary" type="button" :disabled="selectedPlaceIds.length === 0" @click="clearSelection">
                          Effacer
                        </button>
                      </div>

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
                        
                        <div class="row">
                          <div class="col-md-6 legend">
                            <div class="d-flex align-items-center gap-2 mb-1">
                              <span class="legend-color" style="background-color: #198754; width: 16px; height: 16px;"></span>
                              <small>Sélectionnée</small>
                            </div>
                            <div class="d-flex align-items-center gap-2 mb-1">
                              <span class="legend-color" style="background-color: #dc3545; width: 16px; height: 16px;"></span>
                              <small>Occupée</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                              <span class="legend-color" style="background-color: #6c757d; border: 1px solid #6c757d; width: 16px; height: 16px;"></span>
                              <small>Disponible</small>
                            </div>
                          </div>

                          <div class="col-md-6 legend border-start">
                            <div class="d-flex align-items-center gap-2 mb-1">
                              <span class="legend-color" style="background-color: var(--bs-primary); width: 16px; height: 16px;"></span>
                              <small>STANDARD</small>
                            </div>
                            <div class="d-flex align-items-center gap-2 mb-1">
                              <span class="legend-color" style="background-color: var(--bs-warning); width: 16px; height: 16px;"></span>
                              <small>VIP</small>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                              <span class="legend-color" style="background-color: var(--bs-info); width: 16px; height: 16px;"></span>
                              <small>PMR</small>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Récapitulatifs en bas -->
              <div class="col-12 col-lg-6">
                <div class="card shadow-none border h-100">
                  <div class="card-body">
                    <h6 class="card-title fs-6">Détail par type réservé</h6>

                    <div v-if="selectedPlaceIds.length === 0" class="text-muted small">Aucune place sélectionnée</div>

                    <div v-else>
                      <div class="table-responsive">
                        <table class="table table-sm table-borderless mb-0">
                          <tbody>
                            <tr
                              v-for="row in selectedByTypeAndCategorie"
                              :key="String(row.typePlaceId) + '|' + String(row.categorieClientId)"
                            >
                              <td>{{ row.count }}x {{ row.typeLibelle }} ({{ row.categorieLibelle }})</td>
                              <td class="text-end">
                                {{ formatMoney(prixByTypePlaceAndCategorieId.get(`${String(row.typePlaceId)}|${String(row.categorieClientId)}`) ?? 0) }} Ar
                              </td>
                              <td class="text-end fw-bold">
                                {{
                                  formatMoney(
                                    row.count *
                                      (prixByTypePlaceAndCategorieId.get(`${String(row.typePlaceId)}|${String(row.categorieClientId)}`) ?? 0),
                                  )
                                }} Ar
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>

                      <div class="pt-2 mt-2 border-top d-flex justify-content-between align-items-center">
                        <span class="text-muted small uppercase">TOTAL RÉSERVÉ</span>
                        <span class="fw-bold text-primary fs-5">{{ formatMoney(totalReservedMontant) }} Ar</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12 col-lg-6">
                <div class="card shadow-none border h-100">
                  <div class="card-body">
                    <h6 class="card-title fs-6">Détail par type non disponible</h6>

                    <div v-if="occupiedPlaces.length === 0" class="text-muted small">Aucune place non disponible</div>

                    <div v-else>
                      <div class="table-responsive">
                        <table class="table table-sm table-borderless mb-0">
                          <tbody>
                            <tr
                              v-for="row in occupiedByType"
                              :key="'occ|' + row.typePlaceId + '|' + row.libelle"
                            >
                              <td>{{ row.count }}x {{ row.libelle }}</td>
                              <td class="text-end">
                                {{ formatMoney(prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0) }} Ar
                              </td>
                              <td class="text-end fw-bold">
                                {{ formatMoney(row.count * (prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0)) }} Ar
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>

                      <div class="pt-2 mt-2 border-top d-flex justify-content-between align-items-center">
                        <span class="text-muted small uppercase">TOTAL OCCUPÉ</span>
                        <span class="fw-bold text-danger fs-5">{{ formatMoney(totalOccupiedMontant) }} Ar</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Boutons d'action en bas de page -->
              <div class="col-12 mt-4">
                <div class="card shadow-none border-0 bg-light">
                  <div class="card-body d-flex justify-content-end gap-3 py-3">
                    <RouterLink class="btn btn-secondary px-4" to="/reservations">
                      <i class="bi bi-x-circle me-1"></i> Annuler
                    </RouterLink>
                    <button class="btn btn-primary px-5" type="submit" :disabled="loading">
                      <i v-if="loading" class="spinner-border spinner-border-sm me-1"></i>
                      <i v-else class="bi bi-check-circle me-1"></i>
                      Confirmer la réservation
                    </button>
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
  height: 350px;
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 0.375rem;
  padding: 1rem;
  background-color: #f8f9fa;
}

.place-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  margin: 1px;
  padding: 0;
}

.legend-color {
  border-radius: 2px;
  display: inline-block;
  flex-shrink: 0;
}

.places-info {
  padding-top: 1rem;
  margin-top: 0.5rem;
}

.legend {
  font-size: 0.8rem;
  color: #6c757d;
}

.uppercase {
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

@media (max-width: 992px) {
  .places-grid {
    height: 300px;
  }
}
</style>