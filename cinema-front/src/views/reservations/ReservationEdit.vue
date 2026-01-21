<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const props = defineProps(['id'])
const router = useRouter()
const route = useRoute()
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
const originalPlaceIds = ref([])
const selectedCategorieByPlaceId = ref({})

const mode = ref('multiple')

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

const occupiedPlaces = computed(() => (places.value ?? []).filter((p) => Boolean(p?.occupee) && !selectedPlaceIds.value.includes(p.id)))

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

const loadReservation = async () => {
  const reservationId = props.id || route.params.id
  if (!reservationId) return

  try {
    const res = await fetch(`${API_RESERVATIONS}/${reservationId}`)
    if (!res.ok) throw new Error(`Réservation HTTP ${res.status}`)
    const r = await res.json()

    form.value.clientId = String(r.client?.id || '')
    form.value.seanceId = String(r.seance?.id || '')
    
    // Group items by category and type to fill categorieRows
    const items = r.items || []
    const groups = {}
    items.forEach(it => {
      const p = it.place
      const typeId = String(p?.typePlaceId || '')
      const catId = String(it.categorieClient?.id || '')
      const key = `${typeId}|${catId}`
      if (!groups[key]) {
        groups[key] = {
          typePlaceId: typeId,
          categorieClientId: catId,
          quantite: 0,
          placeIds: []
        }
      }
      groups[key].quantite++
      groups[key].placeIds.push(p.id)
    })

    const rows = Object.values(groups).map(g => ({
      id: `existing-${g.typePlaceId}-${g.categorieClientId}`,
      typePlaceId: g.typePlaceId,
      categorieClientId: g.categorieClientId,
      quantite: g.quantite
    }))

    categorieRows.value = rows
    const ids = items.map(it => it.place?.id).filter(Boolean)
    selectedPlaceIds.value = [...ids]
    originalPlaceIds.value = [...ids]
    
    const mapping = {}
    items.forEach(it => {
      if (it.place?.id) {
        mapping[String(it.place.id)] = String(it.categorieClient?.id || '')
      }
    })
    selectedCategorieByPlaceId.value = mapping

  } catch (e) {
    toast.error(e?.message ?? 'Erreur lors du chargement de la réservation')
  }
}

const loadPlacesForSeance = async () => {
  places.value = []
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

const isSelected = (placeId) => {
  if (!placeId) return false
  return selectedPlaceIds.value.some((id) => String(id) === String(placeId))
}

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
    .filter((p) => p && (!p.occupee || selectedPlaceIds.value.includes(p.id)))
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

const isOriginal = (placeId) => {
  if (!placeId) return false
  return originalPlaceIds.value.some((id) => String(id) === String(placeId))
}

const togglePlace = (p) => {
  const id = p.id
  if (isSelected(id)) {
    // Annuler la place réservée : On désélectionne et on diminue la quantité
    const catId = String(selectedCategorieByPlaceId.value[String(id)] || '')
    const typeId = String(p.typePlaceId || '')

    // Désélectionner
    selectedPlaceIds.value = selectedPlaceIds.value.filter((x) => String(x) !== String(id))
    const m = { ...(selectedCategorieByPlaceId.value ?? {}) }
    delete m[String(id)]
    selectedCategorieByPlaceId.value = m

    // Diminuer la quantité dans categorieRows pour que le total nbPlaceRequired diminue aussi
    // On cherche d'abord une ligne qui correspond EXACTEMENT à la catégorie et au type
    let rowIdx = categorieRows.value.findIndex(r => 
      String(r.categorieClientId) === catId && String(r.typePlaceId) === typeId
    )
    
    // Si pas trouvé (cas rare où la catégorie aurait changé), on cherche juste par type
    if (rowIdx === -1) {
      rowIdx = categorieRows.value.findIndex(r => String(r.typePlaceId) === typeId)
    }

    if (rowIdx !== -1) {
      if (categorieRows.value[rowIdx].quantite > 1) {
        categorieRows.value[rowIdx].quantite--
      } else {
        // Si c'est le dernier de cette catégorie/type
        // On ne supprime la ligne que s'il y en a d'autres, sinon on met à 0
        if (categorieRows.value.length > 1) {
          categorieRows.value.splice(rowIdx, 1)
        } else {
          categorieRows.value[rowIdx].quantite = 0
        }
      }
    }
  } else {
    // Sélectionner une nouvelle place
    if (p.occupee && !selectedPlaceIds.value.includes(p.id)) return
    
    // Pour ajouter, il faut savoir quelle catégorie utiliser
    // On prend la première ligne de categorieRows par défaut ou on demande
    if (categorieRows.value.length > 0) {
      const firstRow = categorieRows.value[0]
      if (firstRow.categorieClientId && firstRow.typePlaceId) {
        // On cherche une ligne qui correspond au type de la place
        let matchingRow = categorieRows.value.find(r => String(r.typePlaceId) === String(p.typePlaceId))

        // Si on est au max OU si aucune ligne ne correspond à ce type de place, on ajuste la quantité
        if (!canSelectMorePlaces.value || !matchingRow) {
          if (matchingRow) {
            matchingRow.quantite++
          } else {
            // Sinon on crée une nouvelle ligne pour ce type
            matchingRow = {
              id: `new-${Date.now()}`,
              categorieClientId: firstRow.categorieClientId, // On reprend la catégorie de la 1ère ligne
              typePlaceId: String(p.typePlaceId),
              quantite: 1
            }
            categorieRows.value.push(matchingRow)
          }
        }

        selectedPlaceIds.value = [...selectedPlaceIds.value, id]
        // On associe la place à la catégorie de la ligne (qui existe maintenant forcément)
        selectedCategorieByPlaceId.value[String(id)] = String(matchingRow.categorieClientId)
      } else {
        toast.error("Veuillez d'abord configurer une catégorie dans le tableau.")
      }
    } else {
      toast.error("Veuillez d'abord ajouter une ligne de catégorie.")
    }
  }
}

const getPlaceLabelClass = (p) => {
  if (p?.occupee && !selectedPlaceIds.value.includes(p?.id)) return 'text-white'
  if (isSelected(p?.id)) return 'text-white'

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

    const reservationId = props.id || route.params.id
    const res = await fetch(`${API_RESERVATIONS}/${reservationId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Réservation modifiée')
    await router.push('/reservations')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadRefs()
  await loadReservation()
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
            <h5 class="card-title">Modification de réservation #{{ props.id || route.params.id }}</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-4" @submit.prevent="submit">
              <!-- Deux colonnes principales -->
              <div class="col-12 col-lg-6">
                <!-- Client et Séance (Lecture seule idéalement pour modification) -->
                <div class="row g-3 mb-4">
                  <div class="col-12 col-md-6">
                    <label class="form-label">Client</label>
                    <select v-model="form.clientId" class="form-select" disabled>
                      <option value="" disabled>Sélectionner...</option>
                      <option v-for="c in clients" :key="c.id" :value="String(c.id)">
                        {{ c.nom }} {{ c.prenom }} ({{ c.id }})
                      </option>
                    </select>
                  </div>

                  <div class="col-12 col-md-6">
                    <label class="form-label">Séance</label>
                    <select v-model="form.seanceId" class="form-select" disabled>
                      <option value="" disabled>Sélectionner...</option>
                      <option v-for="s in seances" :key="s.id" :value="String(s.id)">
                        {{ s.id }} - {{ s.dateHeure }} ({{ getFilmTitre(s.film?.id) }}, {{ getSalleNom(s.salle?.id) }})
                      </option>
                    </select>
                  </div>
                </div>

                <!-- Catégories -->
                <div class="row g-3 mb-4">
                  <div class="col-12">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                      <label class="form-label mb-0">Catégories et Quantités</label>
                      <button class="btn btn-sm btn-outline-primary" type="button" @click="addCategorieRow">
                        <i class="bi bi-plus-lg"></i> Ajouter
                      </button>
                    </div>
                    
                    <div v-for="(row, idx) in categorieRows" :key="row.id" class="row g-2 mb-2 align-items-end">
                      <div class="col-12 col-md-4">
                        <label v-if="idx === 0" class="form-label small text-muted">Catégorie</label>
                        <select v-model="row.categorieClientId" class="form-select" required>
                          <option value="" disabled>Catégorie...</option>
                          <option v-for="cat in categories" :key="cat.id" :value="String(cat.id)">
                            {{ cat.libelle }}
                          </option>
                        </select>
                      </div>
                      <div class="col-12 col-md-4">
                        <label v-if="idx === 0" class="form-label small text-muted">Type place</label>
                        <select v-model="row.typePlaceId" class="form-select" required>
                          <option value="" disabled>Type...</option>
                          <option v-for="tp in typePlaces" :key="tp.id" :value="String(tp.id)">
                            {{ tp.libelle }}
                          </option>
                        </select>
                      </div>
                      <div class="col-12 col-md-3">
                        <label v-if="idx === 0" class="form-label small text-muted">Qté</label>
                        <input v-model.number="row.quantite" class="form-select" min="0" required type="number" />
                      </div>
                      <div class="col-12 col-md-1">
                        <button
                          class="btn btn-outline-danger w-100"
                          type="button"
                          @click="removeCategorieRow(row.id)"
                          :disabled="categorieRows.length <= 1"
                        >
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Colonne droite : Sélection des places -->
              <div class="col-12 col-lg-6">
                <div class="d-flex justify-content-between align-items-center mb-3">
                  <h6 class="mb-0">Plan de la salle</h6>
                  <div class="d-flex gap-2">
                    <button class="btn btn-sm btn-outline-secondary" type="button" @click="autoSelectPlaces">
                      <i class="bi bi-magic me-1"></i> Auto
                    </button>
                    <button class="btn btn-sm btn-outline-danger" type="button" @click="clearSelection">
                      <i class="bi bi-x-lg me-1"></i> Vider
                    </button>
                  </div>
                </div>

                <!-- Grille des places -->
                <div class="places-container p-3 bg-light rounded border">
                  <div class="screen-indicator mb-4">ÉCRAN</div>
                  <div class="places-grid-wrapper">
                    <div class="places-grid">
                      <div v-if="!places.length" class="text-center py-5 text-muted">
                        Sélectionnez une séance pour voir les places
                      </div>
                      <div v-else class="grid-content">
                        <button
                          v-for="p in places"
                          :key="p.id"
                          class="place-btn"
                          :class="{
                            'occupied': p.occupee && !isSelected(p.id) && !isOriginal(p.id),
                            'selected': isSelected(p.id),
                            'original': isOriginal(p.id) && !isSelected(p.id)
                          }"
                          type="button"
                          :disabled="p.occupee && !isSelected(p.id) && !isOriginal(p.id)"
                          @click="togglePlace(p)"
                        >
                          <span :class="getPlaceLabelClass(p)">{{ p.rangee }}{{ p.numero }}</span>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Légende -->
                <div class="mt-3 small d-flex flex-wrap gap-3 justify-content-center">
                  <div class="d-flex align-items-center gap-1">
                    <div class="place-btn place-btn-sm selected"></div>
                    <span>Sélectionné</span>
                  </div>
                  <div class="d-flex align-items-center gap-1">
                    <div class="place-btn place-btn-sm original"></div>
                    <span>Ancienne place</span>
                  </div>
                  <div class="d-flex align-items-center gap-1">
                    <div class="place-btn place-btn-sm occupied"></div>
                    <span>Occupé</span>
                  </div>
                  <div class="d-flex align-items-center gap-1">
                    <div class="place-btn place-btn-sm border-primary"></div>
                    <span>Standard</span>
                  </div>
                  <div class="d-flex align-items-center gap-1">
                    <div class="place-btn place-btn-sm border-warning"></div>
                    <span>VIP</span>
                  </div>
                  <div class="d-flex align-items-center gap-1">
                    <div class="place-btn place-btn-sm border-info"></div>
                    <span>PMR</span>
                  </div>
                </div>
              </div>

              <hr class="my-4" />

              <!-- Récapitulatifs Bas -->
              <div class="col-12 col-lg-6">
                <div class="card h-100 shadow-sm border-0 bg-light">
                  <div class="card-body p-3">
                    <h6 class="card-title mb-3 d-flex align-items-center">
                      <i class="bi bi-check-circle-fill text-success me-2"></i>
                      Détail par type réservé
                    </h6>
                    <div v-if="!selectedByTypeAndCategorie.length" class="text-muted small italic">
                      Aucune place sélectionnée
                    </div>
                    <div v-else class="table-responsive">
                      <table class="table table-sm table-borderless mb-0 small">
                        <thead>
                          <tr>
                            <th>Type / Catégorie</th>
                            <th class="text-center">Qté</th>
                            <th class="text-end">Total</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="r in selectedByTypeAndCategorie" :key="`${r.typePlaceId}|${r.categorieClientId}`">
                            <td>{{ r.typeLibelle }} / {{ r.categorieLibelle }}</td>
                            <td class="text-center">{{ r.count }}</td>
                            <td class="text-end">
                              {{ formatMoney(r.count * (prixByTypePlaceAndCategorieId.get(`${r.typePlaceId}|${r.categorieClientId}`) ?? 0)) }} Ar
                            </td>
                          </tr>
                        </tbody>
                        <tfoot>
                          <tr class="border-top fw-bold">
                            <td>TOTAL</td>
                            <td class="text-center">{{ nbPlaceRequired }}</td>
                            <td class="text-end text-primary">{{ formatMoney(totalReservedMontant) }} Ar</td>
                          </tr>
                        </tfoot>
                      </table>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12 col-lg-6">
                <div class="card h-100 shadow-sm border-0 bg-light opacity-75">
                  <div class="card-body p-3">
                    <h6 class="card-title mb-3 d-flex align-items-center">
                      <i class="bi bi-exclamation-triangle-fill text-warning me-2"></i>
                      Détail par type non disponible
                    </h6>
                    <div v-if="!occupiedByType.length" class="text-muted small italic">
                      Toutes les places sont disponibles
                    </div>
                    <div v-else class="table-responsive">
                      <table class="table table-sm table-borderless mb-0 small">
                        <thead>
                          <tr>
                            <th>Type de place</th>
                            <th class="text-center">Qté</th>
                            <th class="text-end">Val. Est.</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr v-for="r in occupiedByType" :key="r.typePlaceId">
                            <td>{{ r.libelle }}</td>
                            <td class="text-center">{{ r.count }}</td>
                            <td class="text-end">
                              {{ formatMoney(r.count * (prixAdulteByTypePlaceId.get(String(r.typePlaceId)) ?? 0)) }} Ar
                            </td>
                          </tr>
                        </tbody>
                        <tfoot>
                          <tr class="border-top fw-bold">
                            <td>TOTAL OCCUPÉ</td>
                            <td class="text-center">{{ occupiedPlaces.length }}</td>
                            <td class="text-end">{{ formatMoney(totalOccupiedMontant) }} Ar</td>
                          </tr>
                        </tfoot>
                      </table>
                    </div>
                  </div>
                </div>
              </div>

              <div class="col-12 d-flex justify-content-between mt-4">
                <button class="btn btn-secondary" type="button" @click="router.push('/reservations')">
                  Annuler
                </button>
                <div class="d-flex gap-2 align-items-center">
                  <span v-if="selectedPlaceIds.length !== nbPlaceRequired" class="text-danger small me-3">
                    Il manque {{ nbPlaceRequired - selectedPlaceIds.length }} place(s) à sélectionner
                  </span>
                  <button
                    class="btn btn-primary px-5"
                    type="submit"
                    :disabled="loading || selectedPlaceIds.length !== nbPlaceRequired"
                  >
                    <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                    Enregistrer les modifications
                  </button>
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
.places-container {
  min-height: 450px;
}

.screen-indicator {
  width: 80%;
  margin: 0 auto;
  height: 10px;
  background: #cbd5e0;
  border-radius: 0 0 50% 50%;
  text-align: center;
  font-size: 10px;
  color: #718096;
  line-height: 25px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.places-grid-wrapper {
  overflow: auto;
  max-height: 350px;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  background: white;
  padding: 15px;
}

.grid-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 8px;
  justify-items: center;
}

.place-btn {
  width: 38px;
  height: 38px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: bold;
  border: 2px solid #dee2e6;
  background: white;
  border-radius: 6px;
  transition: all 0.2s;
}

.place-btn:hover:not(:disabled) {
  transform: scale(1.1);
  border-color: #6c757d;
}

.place-btn.selected {
  background-color: #198754 !important;
  border-color: #198754 !important;
  color: white !important;
}

.place-btn.original {
  background-color: #e8f5e9 !important;
  color: #198754 !important;
  border: 2px dashed #198754 !important;
}

.place-btn.occupied {
  background-color: #dc3545 !important;
  border-color: #dc3545 !important;
  color: white !important;
  cursor: not-allowed;
}

.place-btn-sm {
  width: 18px;
  height: 18px;
  border-width: 1px;
}

.border-primary { border-color: #0d6efd !important; }
.border-warning { border-color: #ffc107 !important; }
.border-info { border-color: #0dcaf0 !important; }

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: #012970;
}

.form-label {
  font-weight: 500;
  color: #444;
}

.italic { font-style: italic; }
</style>
