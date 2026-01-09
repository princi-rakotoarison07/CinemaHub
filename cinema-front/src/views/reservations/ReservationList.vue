<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/reservations`
const API_CLIENTS = `${API_BASE_URL}/api/clients`
const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_FILMS = `${API_BASE_URL}/api/films`
const API_SALLES = `${API_BASE_URL}/api/salles`

const toast = useToast()

const reservations = ref([])
const clients = ref([])
const seances = ref([])
const films = ref([])
const salles = ref([])
const loading = ref(false)
const error = ref('')

const getClientLabel = (clientId) => {
  const c = clients.value.find((x) => String(x.id) === String(clientId))
  if (!c) return clientId ? `Client ${clientId}` : ''
  return `${c.nom ?? ''} ${c.prenom ?? ''}`.trim() || `Client ${clientId}`
}

const payReservation = async (id) => {
  try {
    const res = await fetch(`${API_BASE}/${id}/pay`, { method: 'PUT' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    toast.success('Payée')
    await load()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur paiement')
  }
}

const getFilmTitreBySeanceId = (seanceId) => {
  const s = seances.value.find((x) => String(x.id) === String(seanceId))
  const filmId = s?.film?.id
  const f = films.value.find((x) => String(x.id) === String(filmId))
  return f?.titre ?? (filmId ? `Film ${filmId}` : 'Film')
}

const getSalleLabelBySeanceId = (seanceId) => {
  const s = seances.value.find((x) => String(x.id) === String(seanceId))
  const salleId = s?.salle?.id
  const sa = salles.value.find((x) => String(x.id) === String(salleId))
  return sa?.nom ?? (salleId ? `Salle ${salleId}` : 'Salle')
}

const getFilmSalleLabelBySeanceId = (seanceId) => {
  const film = getFilmTitreBySeanceId(seanceId)
  const salle = getSalleLabelBySeanceId(seanceId)
  return `${film} - ${salle}`
}

const getSeanceDateHeure = (seanceId) => {
  const s = seances.value.find((x) => String(x.id) === String(seanceId))
  return s?.dateHeure
}

const hourBucketLabel = (dateStr) => {
  if (!dateStr) return ''
  try {
    const d = new Date(dateStr)
    d.setMinutes(0, 0, 0)
    const yyyy = d.getFullYear()
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    return `${yyyy}-${mm}-${dd} ${hh}:00:00`
  } catch {
    return String(dateStr)
  }
}

const chiffreAffaireParHeureParFilm = computed(() => {
  const map = new Map()

  for (const r of reservations.value) {
    if (r?.statut !== 'PAYEE') continue

    const seanceId = r?.seance?.id
    const dateHeure = getSeanceDateHeure(seanceId)
    const bucket = hourBucketLabel(dateHeure)
    const film = getFilmTitreBySeanceId(seanceId)
    const amount = Number.parseFloat(String(r?.montantTotal ?? 0)) || 0

    const key = `${bucket}||${film}`
    const prev = map.get(key)
    map.set(key, {
      dateHeure: bucket,
      film,
      total: (prev?.total ?? 0) + amount,
    })
  }

  return Array.from(map.values()).sort((a, b) => {
    const da = a.dateHeure || ''
    const db = b.dateHeure || ''
    if (da !== db) return da.localeCompare(db)
    return a.film.localeCompare(b.film)
  })
})

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const [rRes, cRes, sRes, fRes, saRes] = await Promise.all([
      fetch(API_BASE),
      fetch(API_CLIENTS),
      fetch(API_SEANCES),
      fetch(API_FILMS),
      fetch(API_SALLES),
    ])
    if (!rRes.ok) throw new Error(`Réservations HTTP ${rRes.status}`)
    if (!cRes.ok) throw new Error(`Clients HTTP ${cRes.status}`)
    if (!sRes.ok) throw new Error(`Séances HTTP ${sRes.status}`)
    if (!fRes.ok) throw new Error(`Films HTTP ${fRes.status}`)
    if (!saRes.ok) throw new Error(`Salles HTTP ${saRes.status}`)
    reservations.value = await rRes.json()
    clients.value = await cRes.json()
    seances.value = await sRes.json()
    films.value = await fRes.json()
    salles.value = await saRes.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const formatDate = (v) => {
  if (!v) return ''
  try {
    return new Date(v).toLocaleString()
  } catch {
    return String(v)
  }
}

onMounted(load)
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
            <h5 class="card-title">Liste des réservations</h5>

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
                    <th>Client</th>
                    <th>Date/Heure séance</th>
                    <th>Film - Salle</th>
                    <th>Statut</th>
                    <th>Montant</th>
                    <th>Expiration</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in reservations" :key="r.id">
                    <td>{{ r.id }}</td>
                    <td>{{ getClientLabel(r.client?.id) }}</td>
                    <td>{{ formatDate(getSeanceDateHeure(r.seance?.id)) }}</td>
                    <td>{{ getFilmSalleLabelBySeanceId(r.seance?.id) }}</td>
                    <td>{{ r.statut }}</td>
                    <td>{{ r.montantTotal }}</td>
                    <td>{{ formatDate(r.dateExpiration) }}</td>
                    <td>
                      <button
                        class="btn btn-sm btn-success"
                        type="button"
                        :disabled="r.statut === 'PAYEE' || r.statut === 'ANNULEE'"
                        @click="payReservation(r.id)"
                      >
                        Payer
                      </button>
                    </td>
                  </tr>
                  <tr v-if="reservations.length === 0">
                    <td colspan="8" class="text-center text-muted">Aucun élément</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div v-if="chiffreAffaireParHeureParFilm.length" class="mt-4">
              <h6>Chiffre d'affaire (par heure, par film) - PAYEE</h6>
              <div class="table-responsive">
                <table class="table table-sm">
                  <thead>
                    <tr>
                      <th>Date heure</th>
                      <th>Film</th>
                      <th>Chiffre d'affaire</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="row in chiffreAffaireParHeureParFilm" :key="row.dateHeure + '|' + row.film">
                      <td>{{ row.dateHeure }}</td>
                      <td>{{ row.film }}</td>
                      <td>{{ row.total.toFixed(2) }}</td>
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
