<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_TICKETS = `${API_BASE_URL}/api/tickets`
const API_RESERVATIONS = `${API_BASE_URL}/api/reservations`
const API_PLACES = `${API_BASE_URL}/api/places`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`

const reservations = ref([])
const places = ref([])
const categories = ref([])

const loading = ref(false)
const error = ref('')

const form = ref({
  reservationId: '',
  placeId: '',
  categorieClientId: '',
  prix: '',
})

const canSubmit = computed(() =>
  Boolean(form.value.reservationId && form.value.placeId && form.value.categorieClientId && form.value.prix),
)

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [rRes, pRes, cRes] = await Promise.all([
      fetch(API_RESERVATIONS),
      fetch(API_PLACES),
      fetch(API_CATEGORIES),
    ])

    if (!rRes.ok) throw new Error(`Réservations HTTP ${rRes.status}`)
    if (!pRes.ok) throw new Error(`Places HTTP ${pRes.status}`)
    if (!cRes.ok) throw new Error(`Catégories HTTP ${cRes.status}`)

    reservations.value = await rRes.json()
    places.value = await pRes.json()
    categories.value = await cRes.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const getReservationLabel = (r) => {
  const clientId = r?.client?.id ?? r?.clientId ?? ''
  const seanceId = r?.seance?.id ?? r?.seanceId ?? ''
  return `#${r?.id} (client ${clientId}, séance ${seanceId})`
}

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    const payload = {
      reservation: { id: Number(form.value.reservationId) },
      place: { id: Number(form.value.placeId) },
      categorieClient: { id: Number(form.value.categorieClientId) },
      prix: form.value.prix,
    }

    const res = await fetch(API_TICKETS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Ticket créé')
    await router.push('/tickets')
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
    <h1>Tickets</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Nouveau ticket</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">Réservation</label>
                <select v-model="form.reservationId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="r in reservations" :key="r.id" :value="String(r.id)">
                    {{ getReservationLabel(r) }}
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Place</label>
                <select v-model="form.placeId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="p in places" :key="p.id" :value="String(p.id)">
                    {{ p.id }} (salle {{ p.salle?.id }}, {{ p.rangee }}-{{ p.numero }})
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

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">
                  Enregistrer
                </button>
                <RouterLink class="btn btn-secondary" to="/tickets">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
