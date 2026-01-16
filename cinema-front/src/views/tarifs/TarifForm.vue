<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_TARIFS = `${API_BASE_URL}/api/tarifs`
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`

const typePlaces = ref([])
const categories = ref([])

const loading = ref(false)
const error = ref('')

const form = ref({
  typePlaceId: '',
  categorieClientId: '',
  prix: '',
  actif: true,
  dateDebut: '',
  dateFin: '',
})

const canSubmit = computed(() =>
  Boolean(form.value.typePlaceId && form.value.categorieClientId && form.value.prix),
)

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [tpRes, catRes] = await Promise.all([fetch(API_TYPE_PLACES), fetch(API_CATEGORIES)])
    if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)
    if (!catRes.ok) throw new Error(`Catégories HTTP ${catRes.status}`)

    typePlaces.value = await tpRes.json()
    categories.value = await catRes.json()
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
    const payload = {
      typePlace: { id: Number(form.value.typePlaceId) },
      categorieClient: { id: Number(form.value.categorieClientId) },
      prix: form.value.prix,
      actif: Boolean(form.value.actif),
      dateDebut: form.value.dateDebut || null,
      dateFin: form.value.dateFin || null,
    }

    const res = await fetch(API_TARIFS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Tarif créé')
    await router.push('/tarifs')
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
    <h1>Tarifs</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Nouveau tarif</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12 col-md-6">
                <label class="form-label">Type place</label>
                <select v-model="form.typePlaceId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="tp in typePlaces" :key="tp.id" :value="String(tp.id)">
                    {{ tp.libelle }} ({{ tp.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Catégorie client</label>
                <select v-model="form.categorieClientId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="c in categories" :key="c.id" :value="String(c.id)">
                    {{ c.libelle }} ({{ c.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Prix</label>
                <input v-model="form.prix" class="form-control" type="number" step="0.01" min="0" required />
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Actif</label>
                <select v-model="form.actif" class="form-select">
                  <option :value="true">Oui</option>
                  <option :value="false">Non</option>
                </select>
              </div>

              <div class="col-12 col-md-4"></div>

              <div class="col-12 col-md-6">
                <label class="form-label">Date début</label>
                <input v-model="form.dateDebut" class="form-control" type="date" />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Date fin</label>
                <input v-model="form.dateFin" class="form-control" type="date" />
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">
                  Enregistrer
                </button>
                <RouterLink class="btn btn-secondary" to="/tarifs">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
