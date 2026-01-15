<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const seanceId = computed(() => Number(route.params.id))

const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_SALLES = `${API_BASE_URL}/api/salles`
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`
const API_TARIFS = `${API_BASE_URL}/api/tarifs`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`

const loading = ref(false)
const saving = ref(false)
const error = ref('')

const places = ref([])
const typePlaces = ref([])
const tarifs = ref([])
const categories = ref([])
const salle = ref(null)

const savingPlaceIds = ref([])

const isSaving = (placeId) => savingPlaceIds.value.includes(String(placeId))

const typePlaceById = computed(() => {
  const m = new Map()
  for (const tp of typePlaces.value ?? []) {
    if (tp?.id != null) m.set(String(tp.id), tp)
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

const getButtonClass = (p) => {
  const typeId = p?.typePlaceId != null ? String(p.typePlaceId) : ''
  const base = typeId ? typePlaceColorById.value.get(typeId) : 'btn-outline-secondary'
  return base || 'btn-outline-secondary'
}

const computeTypeCounts = (list) => {
  const map = new Map()
  for (const p of list ?? []) {
    const typeId = p?.typePlaceId != null ? String(p.typePlaceId) : ''
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

const loadSalle = async (seance) => {
  const salleId = seance?.salle?.id != null ? Number(seance.salle.id) : null
  if (!salleId) {
    salle.value = null
    return
  }
  const res = await fetch(`${API_SALLES}/${salleId}`)
  if (!res.ok) throw new Error(`Salle HTTP ${res.status}`)
  salle.value = await res.json()
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, pRes, tpRes, tRes, cRes] = await Promise.all([
      fetch(`${API_SEANCES}/${seanceId.value}`),
      fetch(`${API_SEANCES}/${seanceId.value}/places`),
      fetch(API_TYPE_PLACES),
      fetch(API_TARIFS),
      fetch(API_CATEGORIES),
    ])

    if (!sRes.ok) throw new Error(`Séance HTTP ${sRes.status}`)
    if (!pRes.ok) throw new Error(`Places HTTP ${pRes.status}`)
    if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)
    if (!tRes.ok) throw new Error(`Tarifs HTTP ${tRes.status}`)
    if (!cRes.ok) throw new Error(`Catégories HTTP ${cRes.status}`)

    const seanceJson = await sRes.json()
    const pJson = await pRes.json()
    const tpJson = await tpRes.json()

    tarifs.value = await tRes.json()
    categories.value = await cRes.json()

    places.value = Array.isArray(pJson) ? pJson : []
    typePlaces.value = Array.isArray(tpJson) ? tpJson : []

    await loadSalle(seanceJson)
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const updatePlaceType = async (place) => {
  if (!place || place.occupee) return

  const placeId = String(place.id)
  if (isSaving(placeId)) return

  const currentTypeId = place?.typePlaceId != null ? String(place.typePlaceId) : ''
  const newTypeId = nextTypePlaceId(currentTypeId)
  if (!newTypeId) {
    toast.error('Aucun type de place disponible')
    return
  }

  savingPlaceIds.value = [...savingPlaceIds.value, placeId]
  const prevTypeId = place.typePlaceId
  place.typePlaceId = Number(newTypeId)

  try {
    const items = [{ placeId: Number(place.id), typePlaceId: Number(newTypeId) }]
    const res = await fetch(`${API_SEANCES}/${seanceId.value}/places/types`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(items),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    const updated = await res.json()
    places.value = Array.isArray(updated) ? updated : places.value
  } catch (e) {
    place.typePlaceId = prevTypeId
    toast.error(e?.message ?? "Erreur lors de l'enregistrement")
  } finally {
    savingPlaceIds.value = savingPlaceIds.value.filter((x) => x !== placeId)
  }
}

const back = async () => {
  await router.push('/seances')
}

onMounted(load)
</script>

<template>
  <div class="pagetitle">
    <h1>Configuration des places</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Séance #{{ seanceId }}</h5>

            <div class="d-flex gap-2 mb-3">
              <button class="btn btn-outline-primary" type="button" @click="load" :disabled="loading || saving">
                Rafraîchir
              </button>
              <button class="btn btn-secondary" type="button" @click="back" :disabled="loading || saving">
                Retour
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

              <div class="d-flex gap-3 align-items-start">
                <div class="flex-grow-1 d-flex flex-wrap gap-2">
                  <button
                    v-for="p in places"
                    :key="p.id"
                    type="button"
                    class="btn btn-sm"
                    :class="getButtonClass(p)"
                    :disabled="p.occupee || isSaving(p.id)"
                    @click="updatePlaceType(p)"
                    :title="typePlaceById.get(String(p.typePlaceId))?.libelle ?? ''"
                  >
                    {{ p.label }}
                  </button>

                  <div v-if="places.length === 0" class="text-muted">Aucun élément</div>
                </div>

                <div class="place-config-panel ms-2">
                  <div class="fw-semibold mb-2">Configuration de place (détails)</div>
                  <div class="mb-2">
                    <div class="text-muted">Maximum (capacité salle)</div>
                    <div class="fw-semibold">{{ salle?.capacite ?? places.length }}</div>
                  </div>

                  <div class="mb-2">
                    <div class="text-muted">Détail par type</div>
                    <div
                      v-for="row in computeTypeCounts(places)"
                      :key="row.typePlaceId + '|' + row.libelle"
                      class="d-flex justify-content-between"
                    >
                      <span>
                        {{ row.libelle }} {{ row.count }} <span class="text-muted">tarif</span>
                        {{ formatMoney(prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0) }}
                      </span>
                      <span class="fw-semibold">
                        {{ formatMoney(row.count * (prixAdulteByTypePlaceId.get(String(row.typePlaceId)) ?? 0)) }}
                      </span>
                    </div>
                    <div v-if="computeTypeCounts(places).length === 0" class="text-muted">-</div>
                  </div>

                  <div class="pt-2 border-top d-flex justify-content-between">
                    <span class="text-muted">Total valeur max</span>
                    <span class="fw-semibold">
                      {{
                        formatMoney(
                          computeTypeCounts(places).reduce(
                            (sum, r) => sum + r.count * (prixAdulteByTypePlaceId.get(String(r.typePlaceId)) ?? 0),
                            0,
                          ),
                        )
                      }}
                    </span>
                  </div>
                </div>
              </div>

              <div class="text-muted mt-2">Clique sur une place pour changer son type (cycle).</div>
            </div>

            <div v-if="saving" class="text-muted">Enregistrement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.place-config-panel {
  min-width: 240px;
  max-width: 360px;
  border-left: 1px solid #dee2e6;
  padding-left: 1rem;
}
</style>
