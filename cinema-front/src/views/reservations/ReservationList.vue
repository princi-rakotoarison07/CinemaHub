<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/reservations`
const API_CLIENTS = `${API_BASE_URL}/api/clients`
const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_FILMS = `${API_BASE_URL}/api/films`
const API_SALLES = `${API_BASE_URL}/api/salles`
const API_DETAILS_RESERVATION = `${API_BASE_URL}/api/details-reservations`

const toast = useToast()

const reservations = ref([])
const clients = ref([])
const seances = ref([])
const films = ref([])
const salles = ref([])
const expandedCaBuckets = ref({})
const detailsByReservationId = ref({})
const loadingDetailsByReservationId = ref({})
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

const reservationDetailsSummary = (detailsList) => {
  const list = Array.isArray(detailsList) ? detailsList : []
  const map = new Map()

  for (const it of list) {
    const type = String(it?.place?.typePlaceLibelle ?? it?.place?.typePlaceId ?? 'Type').trim()
    const cat = String(it?.categorieClient?.libelle ?? it?.categorieClient?.id ?? 'Catégorie').trim()
    const key = `${type}||${cat}`
    map.set(key, {
      type,
      categorie: cat,
      count: (map.get(key)?.count ?? 0) + 1,
    })
  }

  const out = Array.from(map.values())
  out.sort((a, b) => {
    if (a.type !== b.type) return a.type.localeCompare(b.type)
    return a.categorie.localeCompare(b.categorie)
  })
  return out
}

const expandedReservationDetails = ref({})

const isReservationDetailsExpanded = (reservationId) =>
  Boolean(expandedReservationDetails.value[String(reservationId ?? '')])

const toggleReservationDetails = async (reservationId) => {
  const key = String(reservationId ?? '')
  if (!key) return

  const next = !expandedReservationDetails.value[key]
  expandedReservationDetails.value = {
    ...expandedReservationDetails.value,
    [key]: next,
  }

  if (!next) return
  if (detailsByReservationId.value[key]) return
  if (loadingDetailsByReservationId.value[key]) return

  loadingDetailsByReservationId.value = {
    ...loadingDetailsByReservationId.value,
    [key]: true,
  }

  try {
    const res = await fetch(`${API_DETAILS_RESERVATION}?reservationId=${encodeURIComponent(key)}`)
    if (!res.ok) throw new Error(`Détails réservation HTTP ${res.status}`)
    const list = await res.json()
    detailsByReservationId.value = {
      ...detailsByReservationId.value,
      [key]: Array.isArray(list) ? list : [],
    }
  } catch (e) {
    toast.error(e?.message ?? 'Erreur lors du chargement des détails')
  } finally {
    loadingDetailsByReservationId.value = {
      ...loadingDetailsByReservationId.value,
      [key]: false,
    }
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

const chiffreAffaireParHeureParSeance = computed(() => {
  const map = new Map()

  for (const r of reservations.value) {
    if (r?.statut !== 'PAYEE') continue

    const seanceId = r?.seance?.id
    const dateHeure = getSeanceDateHeure(seanceId)
    const bucket = hourBucketLabel(dateHeure)
    const filmSalle = getFilmSalleLabelBySeanceId(seanceId)
    const amount = Number.parseFloat(String(r?.montantTotal ?? 0)) || 0

    const key = `${bucket}||${filmSalle}`
    if (!map.has(key)) {
      map.set(key, {
        key,
        dateHeure: bucket,
        filmSalle,
        total: 0,
        details: [],
      })
    }

    const entry = map.get(key)
    entry.total += amount
    entry.details.push({
      id: r?.id,
      client: getClientLabel(r?.client?.id),
      montant: amount,
      expiration: r?.dateExpiration,
    })
  }

  const list = Array.from(map.values())
  list.sort((a, b) => {
    const da = a.dateHeure || ''
    const db = b.dateHeure || ''
    if (da !== db) return da.localeCompare(db)
    return String(a.filmSalle || '').localeCompare(String(b.filmSalle || ''))
  })

  for (const x of list) {
    x.details.sort((a, b) => String(a.client || '').localeCompare(String(b.client || '')))
  }

  return list
})

const toggleCaBucket = (bucketKey) => {
  const key = String(bucketKey ?? '')
  expandedCaBuckets.value = {
    ...expandedCaBuckets.value,
    [key]: !expandedCaBuckets.value[key],
  }
}

const isCaBucketExpanded = (bucketKey) => Boolean(expandedCaBuckets.value[String(bucketKey ?? '')])

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
    expandedCaBuckets.value = {}
    detailsByReservationId.value = {}
    loadingDetailsByReservationId.value = {}
    expandedReservationDetails.value = {}
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
                    <th>Client</th>
                    <th>Date/Heure séance</th>
                    <th>Film - Salle</th>
                    <th>Statut</th>
                    <th>Nombre de places</th>
                    <th>Montant</th>
                    <th>Expiration</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in reservations" :key="r.id">
                    <td>{{ getClientLabel(r.client?.id) }}</td>
                    <td>{{ formatDate(getSeanceDateHeure(r.seance?.id)) }}</td>
                    <td>{{ getFilmSalleLabelBySeanceId(r.seance?.id) }}</td>
                    <td>{{ r.statut }}</td>
                    <td>{{ r.nbPlace }}</td>
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

            <div v-if="chiffreAffaireParHeureParSeance.length" class="mt-4">
              <h6>Chiffre d'affaire (par heure, par film) - PAYEE</h6>
              <div class="table-responsive">
                <table class="table table-sm">
                  <thead>
                    <tr>
                      <th style="width: 1%"></th>
                      <th>Date heure</th>
                      <th>Film - Salle</th>
                      <th>Chiffre d'affaire</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="row in chiffreAffaireParHeureParSeance" :key="row.key">
                      <tr>
                        <td>
                          <button
                            class="btn btn-sm btn-outline-primary"
                            type="button"
                            @click="toggleCaBucket(row.key)"
                          >
                            {{ isCaBucketExpanded(row.key) ? '-' : '+' }}
                          </button>
                        </td>
                        <td>{{ row.dateHeure }}</td>
                        <td>{{ row.filmSalle }}</td>
                        <td>{{ row.total.toFixed(2) }}</td>
                      </tr>
                      <tr v-if="isCaBucketExpanded(row.key)">
                        <td colspan="4">
                          <div class="table-responsive">
                            <table class="table table-sm mb-0">
                              <thead>
                                <tr>
                                  <th style="width: 1%"></th>
                                  <th>Client</th>
                                  <th>Montant</th>
                                  <th>Expiration</th>
                                </tr>
                              </thead>
                              <tbody>
                                <template v-for="d in row.details" :key="row.key + '|' + d.id">
                                  <tr>
                                    <td>
                                      <button
                                        class="btn btn-sm btn-outline-primary"
                                        type="button"
                                        @click="toggleReservationDetails(d.id)"
                                      >
                                        {{ isReservationDetailsExpanded(d.id) ? '-' : '+' }}
                                      </button>
                                    </td>
                                    <td>{{ d.client }}</td>
                                    <td>{{ d.montant.toFixed(2) }}</td>
                                    <td>{{ formatDate(d.expiration) }}</td>
                                  </tr>
                                  <tr v-if="isReservationDetailsExpanded(d.id)">
                                    <td colspan="4">
                                      <div v-if="loadingDetailsByReservationId[String(d.id)]" class="text-muted">Chargement détails...</div>
                                      <div v-else>
                                        <div v-if="(detailsByReservationId[String(d.id)] ?? []).length === 0" class="text-muted">
                                          Aucun détail
                                        </div>

                                        <div v-else class="table-responsive">
                                          <div class="row g-3">
                                            <div class="col-12 col-md-4">
                                              <div class="fw-semibold mb-2">Résumé</div>
                                              <div
                                                v-for="s in reservationDetailsSummary(detailsByReservationId[String(d.id)])"
                                                :key="String(d.id) + '|' + s.type + '|' + s.categorie"
                                                class="small"
                                              >
                                                {{ s.type }} {{ s.categorie }} {{ s.count }}
                                              </div>
                                            </div>

                                            <div class="col-12 col-md-8">
                                              <table class="table table-sm mb-0">
                                                <thead>
                                                  <tr>
                                                    <th>Place</th>
                                                    <th>Type</th>
                                                    <th>Catégorie</th>
                                                    <th class="text-end">Prix</th>
                                                  </tr>
                                                </thead>
                                                <tbody>
                                                  <tr
                                                    v-for="it in detailsByReservationId[String(d.id)]"
                                                    :key="String(d.id) + '|' + String(it.id)"
                                                  >
                                                    <td>{{ it?.place?.label ?? it?.place?.id }}</td>
                                                    <td>{{ it?.place?.typePlaceLibelle ?? it?.place?.typePlaceId }}</td>
                                                    <td>{{ it?.categorieClient?.libelle ?? it?.categorieClient?.id }}</td>
                                                    <td class="text-end">{{ it?.prix }}</td>
                                                  </tr>
                                                </tbody>
                                              </table>
                                            </div>
                                          </div>
                                        </div>
                                      </div>
                                    </td>
                                  </tr>
                                </template>
                              </tbody>
                            </table>
                          </div>
                        </td>
                      </tr>
                    </template>
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
