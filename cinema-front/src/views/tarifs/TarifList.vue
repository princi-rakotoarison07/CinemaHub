<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_TARIFS = `${API_BASE_URL}/api/tarifs`
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`

const toast = useToast()

const tarifs = ref([])
const typePlaces = ref([])
const categories = ref([])
const loading = ref(false)
const error = ref('')

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

const getTypePlaceLabel = (typePlace) => {
  if (!typePlace) return ''
  if (typePlace.libelle) return typePlace.libelle
  const id = typePlace.id
  const tp = id != null ? typePlaceById.value.get(String(id)) : null
  return tp?.libelle ?? (id != null ? String(id) : '')
}

const getCategorieLabel = (categorieClient) => {
  if (!categorieClient) return ''
  if (categorieClient.libelle) return categorieClient.libelle
  const id = categorieClient.id
  const c = id != null ? categorieById.value.get(String(id)) : null
  return c?.libelle ?? (id != null ? String(id) : '')
}

const loadRefs = async () => {
  const [tpRes, catRes] = await Promise.all([fetch(API_TYPE_PLACES), fetch(API_CATEGORIES)])
  if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)
  if (!catRes.ok) throw new Error(`Catégories HTTP ${catRes.status}`)
  typePlaces.value = await tpRes.json()
  categories.value = await catRes.json()
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    await loadRefs()
    const res = await fetch(API_TARIFS)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    tarifs.value = await res.json()
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
    <h1>Tarifs</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Grille tarifaire</h5>

            <div class="d-flex gap-2 mb-3">
              <button class="btn btn-outline-primary" type="button" @click="load">Rafraîchir</button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Type place</th>
                    <th>Catégorie client</th>
                    <th>Prix</th>
                    <th>Actif</th>
                    <th>Date début</th>
                    <th>Date fin</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="t in tarifs" :key="t.id">
                    <td>{{ t.id }}</td>
                    <td>{{ getTypePlaceLabel(t.typePlace) }}</td>
                    <td>{{ getCategorieLabel(t.categorieClient) }}</td>
                    <td>{{ t.prix }}</td>
                    <td>{{ t.actif }}</td>
                    <td>{{ t.dateDebut }}</td>
                    <td>{{ t.dateFin }}</td>
                  </tr>
                  <tr v-if="tarifs.length === 0">
                    <td colspan="7" class="text-center text-muted">Aucun élément</td>
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
