<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_PLACES = `${API_BASE_URL}/api/places`
const API_SALLES = `${API_BASE_URL}/api/salles`
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`

const toast = useToast()

const places = ref([])
const salles = ref([])
const typePlaces = ref([])
const loading = ref(false)
const error = ref('')

const salleById = computed(() => {
  const m = new Map()
  for (const s of salles.value ?? []) {
    if (s?.id != null) m.set(String(s.id), s)
  }
  return m
})

const typePlaceById = computed(() => {
  const m = new Map()
  for (const tp of typePlaces.value ?? []) {
    if (tp?.id != null) m.set(String(tp.id), tp)
  }
  return m
})

const getSalleLabel = (salle) => {
  if (!salle) return ''
  if (salle.nom) return salle.nom
  const id = salle.id
  const s = id != null ? salleById.value.get(String(id)) : null
  return s?.nom ?? (id != null ? String(id) : '')
}

const getTypePlaceLabel = (typePlace) => {
  if (!typePlace) return ''
  if (typePlace.libelle) return typePlace.libelle
  const id = typePlace.id
  const tp = id != null ? typePlaceById.value.get(String(id)) : null
  return tp?.libelle ?? (id != null ? String(id) : '')
}

const loadRefs = async () => {
  const [sRes, tpRes] = await Promise.all([fetch(API_SALLES), fetch(API_TYPE_PLACES)])
  if (!sRes.ok) throw new Error(`Salles HTTP ${sRes.status}`)
  if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)
  salles.value = await sRes.json()
  typePlaces.value = await tpRes.json()
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    await loadRefs()
    const res = await fetch(API_PLACES)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    places.value = await res.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(load)
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
            <h5 class="card-title">Liste des places</h5>

            <div class="d-flex gap-2 mb-3">
              <button class="btn btn-outline-primary" type="button" @click="load">Rafraîchir</button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th>Salle</th>
                    <th>Rangée</th>
                    <th>Numéro</th>
                    <th>Type place</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in places" :key="p.id">
                    <td>{{ getSalleLabel(p.salle) }}</td>
                    <td>{{ p.rangee }}</td>
                    <td>{{ p.numero }}</td>
                    <td>{{ getTypePlaceLabel(p.typePlace) }}</td>
                  </tr>
                  <tr v-if="places.length === 0">
                    <td colspan="4" class="text-center text-muted">Aucun élément</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
