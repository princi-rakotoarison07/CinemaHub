<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_TICKETS = `${API_BASE_URL}/api/tickets`
const API_RESERVATIONS = `${API_BASE_URL}/api/reservations`
const API_CLIENTS = `${API_BASE_URL}/api/clients`
const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_FILMS = `${API_BASE_URL}/api/films`
const API_PLACES = `${API_BASE_URL}/api/places`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`
const API_SALLES = `${API_BASE_URL}/api/salles`

const toast = useToast()

const tickets = ref([])
const reservations = ref([])
const clients = ref([])
const seances = ref([])
const films = ref([])
const places = ref([])
const categories = ref([])
const salles = ref([])
const loading = ref(false)
const error = ref('')

const reservationById = computed(() => {
  const m = new Map()
  for (const r of reservations.value ?? []) {
    if (r?.id != null) m.set(String(r.id), r)
  }
  return m
})

const clientById = computed(() => {
  const m = new Map()
  for (const c of clients.value ?? []) {
    if (c?.id != null) m.set(String(c.id), c)
  }
  return m
})

const seanceById = computed(() => {
  const m = new Map()
  for (const s of seances.value ?? []) {
    if (s?.id != null) m.set(String(s.id), s)
  }
  return m
})

const filmById = computed(() => {
  const m = new Map()
  for (const f of films.value ?? []) {
    if (f?.id != null) m.set(String(f.id), f)
  }
  return m
})

const placeById = computed(() => {
  const m = new Map()
  for (const p of places.value ?? []) {
    if (p?.id != null) m.set(String(p.id), p)
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

const salleById = computed(() => {
  const m = new Map()
  for (const s of salles.value ?? []) {
    if (s?.id != null) m.set(String(s.id), s)
  }
  return m
})

const getReservationLabel = (reservationRef) => {
  const id = reservationRef?.id
  if (id == null) return ''
  const r = reservationById.value.get(String(id))

  const clientId = r?.client?.id
  const seanceId = r?.seance?.id
  const statut = r?.statut ?? ''

  const client = clientId != null ? clientById.value.get(String(clientId)) : null
  const clientNom = client ? `${client?.nom ?? ''} ${client?.prenom ?? ''}`.trim() : ''

  const seance = seanceId != null ? seanceById.value.get(String(seanceId)) : null
  const filmId = seance?.film?.id
  const film = filmId != null ? filmById.value.get(String(filmId)) : null
  const filmTitre = film?.titre ?? (filmId != null ? `Film ${filmId}` : '')

  const salleId = seance?.salle?.id
  const salleNom = salleId != null ? salleById.value.get(String(salleId))?.nom : ''

  const dateHeure = seance?.dateHeure
  const dateLabel = dateHeure ? new Date(dateHeure).toLocaleString() : ''

  const parts = [
    clientNom || (clientId != null ? `Client ${clientId}` : null),
    filmTitre || null,
    dateLabel || null,
    salleNom || (salleId != null ? `Salle ${salleId}` : null),
  ].filter(Boolean)

  const details = [parts.join(' - '), statut ? `(${statut})` : null].filter(Boolean).join(' ')
  return details ? `#${id} - ${details}` : `#${id}`
}

const getPlaceLabel = (placeRef) => {
  const id = placeRef?.id
  if (id == null) return ''
  const p = placeById.value.get(String(id))
  const salleId = p?.salle?.id
  const salleNom = salleId != null ? salleById.value.get(String(salleId))?.nom : null
  const rangee = p?.rangee ?? ''
  const numero = p?.numero != null ? String(p.numero) : ''
  const seat = `${rangee}${numero}`.trim()
  const parts = [salleNom ? salleNom : salleId != null ? `Salle ${salleId}` : null, seat || null].filter(Boolean)
  return parts.length ? parts.join(' - ') : `#${id}`
}

const getCategorieLabel = (categorieRef) => {
  const id = categorieRef?.id
  if (id == null) return ''
  const c = categorieById.value.get(String(id))
  return c?.libelle ?? String(id)
}

const loadRefs = async () => {
  const [rRes, clRes, seRes, fRes, pRes, cRes, sRes] = await Promise.all([
    fetch(API_RESERVATIONS),
    fetch(API_CLIENTS),
    fetch(API_SEANCES),
    fetch(API_FILMS),
    fetch(API_PLACES),
    fetch(API_CATEGORIES),
    fetch(API_SALLES),
  ])
  if (!rRes.ok) throw new Error(`Réservations HTTP ${rRes.status}`)
  if (!clRes.ok) throw new Error(`Clients HTTP ${clRes.status}`)
  if (!seRes.ok) throw new Error(`Séances HTTP ${seRes.status}`)
  if (!fRes.ok) throw new Error(`Films HTTP ${fRes.status}`)
  if (!pRes.ok) throw new Error(`Places HTTP ${pRes.status}`)
  if (!cRes.ok) throw new Error(`Catégories HTTP ${cRes.status}`)
  if (!sRes.ok) throw new Error(`Salles HTTP ${sRes.status}`)

  reservations.value = await rRes.json()
  clients.value = await clRes.json()
  seances.value = await seRes.json()
  films.value = await fRes.json()
  places.value = await pRes.json()
  categories.value = await cRes.json()
  salles.value = await sRes.json()
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    await loadRefs()
    const res = await fetch(API_TICKETS)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    tickets.value = await res.json()
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
    <h1>Tickets</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Liste des tickets</h5>

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
                    <th>Réservation</th>
                    <th>Place</th>
                    <th>Catégorie client</th>
                    <th>Prix</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="t in tickets" :key="t.id">
                    <td>{{ t.id }}</td>
                    <td>{{ getReservationLabel(t.reservation) }}</td>
                    <td>{{ getPlaceLabel(t.place) }}</td>
                    <td>{{ getCategorieLabel(t.categorieClient) }}</td>
                    <td>{{ t.prix }}</td>
                  </tr>
                  <tr v-if="tickets.length === 0">
                    <td colspan="5" class="text-center text-muted">Aucun élément</td>
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
