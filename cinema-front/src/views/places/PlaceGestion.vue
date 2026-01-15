<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const toast = useToast()

const API_SALLES = `${API_BASE_URL}/api/salles`
const API_PLACES = `${API_BASE_URL}/api/places`
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`
const API_TARIFS = `${API_BASE_URL}/api/tarifs`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`

const loading = ref(false)
const error = ref('')

const salles = ref([])
const typePlaces = ref([])
const tarifs = ref([])
const categories = ref([])

const expandedSalleIds = ref([])
const placesBySalleId = ref({})
const loadingPlacesBySalleId = ref({})
const savingPlaceIds = ref([])

const isExpanded = (salleId) => expandedSalleIds.value.includes(String(salleId))

const toggleExpand = async (salleId) => {
  const id = String(salleId)
  if (isExpanded(id)) {
    expandedSalleIds.value = expandedSalleIds.value.filter((x) => x !== id)
    return
  }
  expandedSalleIds.value = [...expandedSalleIds.value, id]
  if (!placesBySalleId.value[id]) {
    await loadPlacesForSalle(id)
  }
}

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, tpRes, tRes, cRes] = await Promise.all([
      fetch(API_SALLES),
      fetch(API_TYPE_PLACES),
      fetch(API_TARIFS),
      fetch(API_CATEGORIES),
    ])
    if (!sRes.ok) throw new Error(`Salles HTTP ${sRes.status}`)
    if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)
    if (!tRes.ok) throw new Error(`Tarifs HTTP ${tRes.status}`)
    if (!cRes.ok) throw new Error(`Catégories HTTP ${cRes.status}`)
    salles.value = await sRes.json()
    typePlaces.value = await tpRes.json()
    tarifs.value = await tRes.json()
    categories.value = await cRes.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const loadPlacesForSalle = async (salleId) => {
  const id = String(salleId)
  loadingPlacesBySalleId.value = { ...loadingPlacesBySalleId.value, [id]: true }
  try {
    const res = await fetch(`${API_PLACES}?salleId=${encodeURIComponent(id)}`)
    if (!res.ok) throw new Error(`Places HTTP ${res.status}`)
    const list = await res.json()
    placesBySalleId.value = { ...placesBySalleId.value, [id]: Array.isArray(list) ? list : [] }
  } catch (e) {
    toast.error(e?.message ?? 'Erreur lors du chargement des places')
  } finally {
    loadingPlacesBySalleId.value = { ...loadingPlacesBySalleId.value, [id]: false }
  }
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

const palette = ['btn-primary', 'btn-success', 'btn-warning', 'btn-info', 'btn-secondary', 'btn-dark']

const typePlaceColorById = computed(() => {
  const ids = (typePlaces.value ?? []).map((x) => String(x.id))
  ids.sort((a, b) => Number(a) - Number(b))
  const m = new Map()
  ids.forEach((id, idx) => m.set(id, palette[idx % palette.length]))
  return m
})

const getPlaceLabel = (p) => {
  const rangee = p?.rangee ?? ''
  const numero = p?.numero != null ? String(p.numero) : ''
  return `${rangee}${numero}`.trim() || `#${p?.id}`
}

const isSaving = (placeId) => savingPlaceIds.value.includes(String(placeId))

const getButtonClass = (p) => {
  const typeId = p?.typePlace?.id != null ? String(p.typePlace.id) : ''
  const base = typeId ? typePlaceColorById.value.get(typeId) : 'btn-outline-secondary'
  return base || 'btn-outline-secondary'
}

const computeTypeCounts = (places) => {
  const map = new Map()
  for (const p of places ?? []) {
    const typeId = p?.typePlace?.id != null ? String(p.typePlace.id) : ''
    const type = typeId ? typePlaceById.value.get(typeId) : null
    const label = type?.libelle ?? (typeId ? `Type ${typeId}` : 'Inconnu')
    const key = typeId || label
    if (!map.has(key)) map.set(key, { typePlaceId: typeId, libelle: label, count: 0 })
    map.get(key).count += 1
  }
  const arr = Array.from(map.values())
  arr.sort((a, b) => String(a.libelle).localeCompare(String(b.libelle)))
  return arr
}

const formatMoney = (n) => {
  const v = Number(n)
  if (!Number.isFinite(v)) return '-'
  return v.toFixed(2)
}

const nextTypePlaceId = (currentId) => {
  const ids = (typePlaces.value ?? []).map((x) => String(x.id))
  ids.sort((a, b) => Number(a) - Number(b))
  if (ids.length === 0) return ''
  const cur = currentId != null ? String(currentId) : ''
  const idx = ids.indexOf(cur)
  if (idx === -1) return ids[0]
  return ids[(idx + 1) % ids.length]
}

const updatePlaceType = async (salleId, place) => {
  const salleKey = String(salleId)
  const placeId = String(place.id)
  if (isSaving(placeId)) return

  const currentTypeId = place?.typePlace?.id != null ? String(place.typePlace.id) : ''
  const newTypeId = nextTypePlaceId(currentTypeId)
  if (!newTypeId) {
    toast.error('Aucun type de place disponible')
    return
  }

  const newType = typePlaceById.value.get(String(newTypeId))

  savingPlaceIds.value = [...savingPlaceIds.value, placeId]

  const prev = place.typePlace
  place.typePlace = { id: Number(newTypeId), libelle: newType?.libelle }

  try {
    const payload = {
      id: Number(place.id),
      salle: { id: Number(place?.salle?.id ?? salleId) },
      rangee: place.rangee,
      numero: Number(place.numero),
      typePlace: { id: Number(newTypeId) },
    }

    const res = await fetch(`${API_PLACES}/${place.id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Type de place mis à jour')

    const list = placesBySalleId.value[salleKey] ?? []
    placesBySalleId.value = { ...placesBySalleId.value, [salleKey]: [...list] }
  } catch (e) {
    place.typePlace = prev
    toast.error(e?.message ?? "Erreur lors de l'enregistrement")
  } finally {
    savingPlaceIds.value = savingPlaceIds.value.filter((x) => x !== placeId)
  }
}

onMounted(loadRefs)
</script>

<template>
  <div class="pagetitle">
    <h1>Places</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Gestion des places</h5>

            <div class="d-flex gap-2 mb-3">
              <button class="btn btn-outline-primary" type="button" :disabled="loading" @click="loadRefs">
                Rafraîchir
              </button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else>
              <div class="mb-3">
                <div class="fw-semibold mb-2">Légende types de place</div>
                <div class="d-flex flex-wrap gap-2">
                  <span
                    v-for="tp in typePlaces"
                    :key="tp.id"
                    class="badge"
                    :class="(typePlaceColorById.get(String(tp.id)) ?? 'bg-secondary').replace('btn-', 'bg-')"
                  >
                    {{ tp.libelle }}
                  </span>
                </div>
              </div>

              <div class="table-responsive">
                <table class="table table-striped align-middle">
                  <thead>
                    <tr>
                      <th style="width: 60px"></th>
                      <th>Nom</th>
                      <th>Capacité</th>
                      <th style="width: 160px">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="s in salles" :key="s.id">
                      <tr>
                        <td>
                          <button class="btn btn-sm btn-outline-secondary" type="button" @click="toggleExpand(s.id)">
                            {{ isExpanded(s.id) ? '-' : '+' }}
                          </button>
                        </td>
                        <td>{{ s.nom }}</td>
                        <td>{{ s.capacite }}</td>
                        <td>
                          <button
                            class="btn btn-sm btn-outline-primary"
                            type="button"
                            :disabled="loadingPlacesBySalleId[String(s.id)]"
                            @click="loadPlacesForSalle(s.id)"
                          >
                            Rafraîchir places
                          </button>
                        </td>
                      </tr>

                      <tr v-if="isExpanded(s.id)">
                        <td colspan="4">
                          <div v-if="loadingPlacesBySalleId[String(s.id)]" class="text-muted">Chargement des places...</div>

                          <div v-else class="d-flex gap-3 align-items-start">
                            <div class="flex-grow-1 d-flex flex-wrap gap-2">
                              <button
                                v-for="p in placesBySalleId[String(s.id)] ?? []"
                                :key="p.id"
                                type="button"
                                class="btn btn-sm"
                                :class="getButtonClass(p)"
                                :disabled="isSaving(p.id)"
                                @click="updatePlaceType(s.id, p)"
                                :title="typePlaceById.get(String(p.typePlace?.id))?.libelle ?? ''"
                              >
                                {{ getPlaceLabel(p) }}
                              </button>

                              <div v-if="(placesBySalleId[String(s.id)] ?? []).length === 0" class="text-muted">
                                Aucune place
                              </div>
                            </div>

                            <div class="place-config-panel ms-2">
                              <div class="fw-semibold mb-2">Configuration de place (détails)</div>
                              <div class="mb-2">
                                <div class="text-muted">Maximum (capacité salle)</div>
                                <div class="fw-semibold">{{ s.capacite }}</div>
                              </div>

                              <div class="mb-2">
                                <div class="text-muted">Détail par type</div>
                                <div
                                  v-for="row in computeTypeCounts(placesBySalleId[String(s.id)] ?? [])"
                                  :key="String(s.id) + '|' + row.typePlaceId + '|' + row.libelle"
                                  class="d-flex justify-content-between"
                                >
                                  <span>
                                    {{ row.libelle }}
                                    {{ row.count }}
                                    <span class="text-muted">tarif</span>
                                    {{ formatMoney(prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0) }}
                                  </span>
                                  <span class="fw-semibold">
                                    {{ formatMoney(row.count * (prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0)) }}
                                  </span>
                                </div>
                                <div
                                  v-if="computeTypeCounts(placesBySalleId[String(s.id)] ?? []).length === 0"
                                  class="text-muted"
                                >
                                  -
                                </div>
                              </div>

                              <div class="pt-2 border-top d-flex justify-content-between">
                                <span class="text-muted">Total valeur max</span>
                                <span class="fw-semibold">
                                  {{
                                    formatMoney(
                                      computeTypeCounts(placesBySalleId[String(s.id)] ?? []).reduce(
                                        (sum, r) =>
                                          sum +
                                          r.count * (prixAdulteByTypePlaceId.get(String(r.typePlaceId)) ?? 0),
                                        0,
                                      ),
                                    )
                                  }}
                                </span>
                              </div>
                            </div>
                          </div>

                          <div class="text-muted mt-2">
                            Clique sur une place pour changer son type (cycle).
                          </div>
                        </td>
                      </tr>
                    </template>

                    <tr v-if="salles.length === 0">
                      <td colspan="4" class="text-center text-muted">Aucune salle</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.place-config-panel {
  min-width: 240px;
  max-width: 320px;
  border-left: 1px solid #dee2e6;
  padding-left: 1rem;
}
</style>
