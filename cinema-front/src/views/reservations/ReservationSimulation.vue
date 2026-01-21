<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const props = defineProps(['id'])
const router = useRouter()
const toast = useToast()

const API_RESERVATIONS = `${API_BASE_URL}/api/reservations`
const API_CLIENTS = `${API_BASE_URL}/api/clients`
const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_FILMS = `${API_BASE_URL}/api/films`
const API_SALLES = `${API_BASE_URL}/api/salles`

const loading = ref(false)
const error = ref('')
const reservation = ref(null)
const simulation = ref(null)
const clients = ref([])
const seances = ref([])
const films = ref([])
const salles = ref([])

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [rRes, sRes, clRes, seRes, fRes, saRes] = await Promise.all([
      fetch(`${API_RESERVATIONS}/${props.id}`),
      fetch(`${API_RESERVATIONS}/${props.id}/pay-preview`),
      fetch(API_CLIENTS),
      fetch(API_SEANCES),
      fetch(API_FILMS),
      fetch(API_SALLES)
    ])

    if (!rRes.ok) throw new Error('Erreur lors du chargement de la réservation')
    if (!sRes.ok) throw new Error('Erreur lors du chargement de la simulation')

    reservation.value = await rRes.json()
    simulation.value = await sRes.json()
    clients.value = await clRes.json()
    seances.value = await seRes.json()
    films.value = await fRes.json()
    salles.value = await saRes.json()
  } catch (e) {
    error.value = e.message
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const getClientLabel = (id) => {
  const c = clients.value.find(x => String(x.id) === String(id))
  return c ? `${c.nom} ${c.prenom}` : id
}

const getSeanceLabel = (id) => {
  const s = seances.value.find(x => String(x.id) === String(id))
  if (!s) return id
  const film = films.value.find(f => String(f.id) === String(s.film?.id))
  const salle = salles.value.find(sa => String(sa.id) === String(s.salle?.id))
  return `${film?.titre ?? 'Film'} - ${salle?.nom ?? 'Salle'} (${new Date(s.dateHeure).toLocaleString()})`
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

onMounted(loadData)
</script>

<template>
  <div class="pagetitle">
    <h1>Simulation de prix</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><RouterLink to="/">Home</RouterLink></li>
        <li class="breadcrumb-item"><RouterLink to="/reservations">Réservations</RouterLink></li>
        <li class="breadcrumb-item active">Simulation</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8 mx-auto">
        <div class="card shadow-sm">
          <div class="card-body pt-3">
            <div class="d-flex justify-content-between align-items-center mb-4">
              <h5 class="card-title p-0 m-0">Détails de la simulation #{{ id }}</h5>
              <button class="btn btn-outline-secondary btn-sm" @click="router.back()">
                <i class="bi bi-arrow-left me-1"></i> Retour
              </button>
            </div>

            <div v-if="loading" class="text-center py-5">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Chargement...</span>
              </div>
              <div class="mt-2 text-muted">Calcul en cours...</div>
            </div>

            <div v-else-if="error" class="alert alert-danger">
              {{ error }}
            </div>

            <div v-else-if="reservation && simulation">
              <div class="row g-3 mb-4">
                <div class="col-md-6">
                  <label class="small text-muted d-block">Client</label>
                  <div class="fw-bold">{{ getClientLabel(reservation.client?.id) }}</div>
                </div>
                <div class="col-md-6">
                  <label class="small text-muted d-block">Séance</label>
                  <div class="fw-bold">{{ getSeanceLabel(reservation.seance?.id) }}</div>
                </div>
                <div class="col-md-4">
                  <label class="small text-muted d-block">Date réservation</label>
                  <div class="fw-bold">{{ formatDate(reservation.dateReservation) }}</div>
                </div>
                <div class="col-md-4">
                  <label class="small text-muted d-block">Statut actuel</label>
                  <div class="badge" :class="reservation.statut === 'PAYEE' ? 'bg-success' : 'bg-warning'">
                    {{ reservation.statut }}
                  </div>
                </div>
                <div class="col-md-4">
                  <label class="small text-muted d-block">Nombre de places</label>
                  <div class="fw-bold">{{ reservation.nbPlace }} place(s)</div>
                </div>
              </div>

              <h6 class="border-bottom pb-2 mb-3">Détail du calcul</h6>
              <div class="table-responsive">
                <table class="table table-hover align-middle">
                  <thead class="table-light">
                    <tr>
                      <th>Place</th>
                      <th>Type de place</th>
                      <th>Catégorie client</th>
                      <th class="text-end">Prix unitaire</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(line, idx) in simulation.lines" :key="idx">
                      <td>
                        <span class="badge bg-primary rounded-pill">
                          {{ line.place?.label || '?' }}
                        </span>
                      </td>
                      <td>{{ line.place?.typePlaceLibelle || '-' }}</td>
                      <td>{{ line.categorieClient?.libelle || '-' }}</td>
                      <td class="text-end fw-semibold">{{ line.prix?.toFixed(2) }} Ar</td>
                    </tr>
                  </tbody>
                  <tfoot>
                    <tr class="table-primary border-top-2">
                      <td colspan="3" class="text-end fw-bold">TOTAL À PAYER</td>
                      <td class="text-end fw-bold fs-5">{{ simulation.total?.toFixed(2) }} Ar</td>
                    </tr>
                  </tfoot>
                </table>
              </div>

              <div class="alert alert-info mt-4 d-flex align-items-center">
                <i class="bi bi-info-circle fs-4 me-3"></i>
                <div>
                  Cette simulation est basée sur les tarifs actuels configurés pour le film, la salle et les catégories de clients sélectionnées.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.card-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #012970;
}
.table tfoot tr {
  border-top: 2px solid #dee2e6;
}
</style>
