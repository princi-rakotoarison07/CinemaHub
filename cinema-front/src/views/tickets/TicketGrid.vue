<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_TICKETS = `${API_BASE_URL}/api/tickets`
const API_RESERVATIONS = `${API_BASE_URL}/api/reservations`
const API_CLIENTS = `${API_BASE_URL}/api/clients`
const API_SEANCES = `${API_BASE_URL}/api/seances`
const API_FILMS = `${API_BASE_URL}/api/films`
const API_PLACES = `${API_BASE_URL}/api/places`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`
const API_SALLES = `${API_BASE_URL}/api/salles`

const loading = ref(false)
const saving = ref(false)
const error = ref('')

const ticketsOriginal = ref([])
const rows = ref([])

const newRowCounter = ref(0)

const reservations = ref([])
const clients = ref([])
const seances = ref([])
const films = ref([])
const places = ref([])
const categories = ref([])
const salles = ref([])

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

const getReservationLabel = (reservationId) => {
  if (!reservationId) return ''
  const r = reservationById.value.get(String(reservationId))

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
  return details ? `#${reservationId} - ${details}` : `#${reservationId}`
}

const getPlaceLabel = (placeId) => {
  if (!placeId) return ''
  const p = placeById.value.get(String(placeId))
  const salleId = p?.salle?.id
  const salleNom = salleId != null ? salleById.value.get(String(salleId))?.nom : null
  const rangee = p?.rangee ?? ''
  const numero = p?.numero != null ? String(p.numero) : ''
  const seat = `${rangee}${numero}`.trim()
  const parts = [salleNom ? salleNom : salleId != null ? `Salle ${salleId}` : null, seat || null].filter(Boolean)
  return parts.length ? parts.join(' - ') : `#${placeId}`
}

const getCategorieLabel = (categorieId) => {
  if (!categorieId) return ''
  const c = categorieById.value.get(String(categorieId))
  return c?.libelle ?? String(categorieId)
}

const toRow = (t) => ({
  id: t.id,
  reservationId: t?.reservation?.id != null ? String(t.reservation.id) : '',
  placeId: t?.place?.id != null ? String(t.place.id) : '',
  categorieClientId: t?.categorieClient?.id != null ? String(t.categorieClient.id) : '',
  prix: t?.prix != null ? String(t.prix) : '',
  isNew: false,
})

const createNewRow = () => {
  newRowCounter.value += 1
  return {
    id: `new-${newRowCounter.value}`,
    reservationId: '',
    placeId: '',
    categorieClientId: '',
    prix: '',
    isNew: true,
  }
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
    if (!res.ok) throw new Error(`Tickets HTTP ${res.status}`)
    const list = await res.json()

    ticketsOriginal.value = Array.isArray(list) ? list : []
    rows.value = ticketsOriginal.value.map(toRow)
    newRowCounter.value = 0
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const originalRowById = computed(() => {
  const m = new Map()
  for (const t of ticketsOriginal.value ?? []) {
    if (t?.id != null) m.set(String(t.id), toRow(t))
  }
  return m
})

const isDirty = (row) => {
  if (!row?.id) return false
  if (row.isNew) return true
  const orig = originalRowById.value.get(String(row.id))
  if (!orig) return false
  return (
    String(row.reservationId ?? '') !== String(orig.reservationId ?? '') ||
    String(row.placeId ?? '') !== String(orig.placeId ?? '') ||
    String(row.categorieClientId ?? '') !== String(orig.categorieClientId ?? '') ||
    String(row.prix ?? '') !== String(orig.prix ?? '')
  )
}

const dirtyRows = computed(() => rows.value.filter(isDirty))

const addRow = () => {
  rows.value = [...rows.value, createNewRow()]
}

const removeRow = (rowId) => {
  rows.value = rows.value.filter((r) => String(r.id) !== String(rowId))
}

const cancel = () => {
  rows.value = ticketsOriginal.value.map(toRow)
}

const save = async () => {
  saving.value = true
  error.value = ''
  try {
    const toSave = dirtyRows.value
    if (toSave.length === 0) {
      toast.success('Aucune modification')
      return
    }

    for (const r of toSave) {
      if (!r.reservationId || !r.placeId || !r.categorieClientId || r.prix === '') {
        throw new Error(`Champs obligatoires manquants (ticket ${r.id})`)
      }

      const payload = {
        reservation: { id: Number(r.reservationId) },
        place: { id: Number(r.placeId) },
        categorieClient: { id: Number(r.categorieClientId) },
        prix: r.prix,
      }

      const isNew = Boolean(r.isNew)
      const url = isNew ? API_TICKETS : `${API_TICKETS}/${r.id}`
      const method = isNew ? 'POST' : 'PUT'

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(isNew ? payload : { id: Number(r.id), ...payload }),
      })

      if (!res.ok) throw new Error(`${isNew ? 'Création' : 'Ticket'} ${r.id} HTTP ${res.status}`)
    }

    toast.success('Grille mise à jour')
    await load()
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    saving.value = false
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
            <h5 class="card-title">Tickets (édition multiple)</h5>

            <div class="d-flex gap-2 mb-3">
              <button class="btn btn-outline-primary" type="button" :disabled="loading || saving" @click="load">
                Rafraîchir
              </button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive">
              <table class="table table-striped align-middle">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Réservation</th>
                    <th>Place</th>
                    <th>Catégorie client</th>
                    <th>Prix</th>
                    <th>Modifié</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in rows" :key="r.id">
                    <td>{{ r.id }}</td>

                    <td style="min-width: 260px">
                      <select v-model="r.reservationId" class="form-select form-select-sm">
                        <option value="" disabled>Sélectionner...</option>
                        <option v-for="res in reservations" :key="res.id" :value="String(res.id)">
                          {{ getReservationLabel(String(res.id)) }}
                        </option>
                      </select>
                    </td>

                    <td style="min-width: 220px">
                      <select v-model="r.placeId" class="form-select form-select-sm">
                        <option value="" disabled>Sélectionner...</option>
                        <option v-for="p in places" :key="p.id" :value="String(p.id)">
                          {{ getPlaceLabel(String(p.id)) }}
                        </option>
                      </select>
                    </td>

                    <td style="min-width: 220px">
                      <select v-model="r.categorieClientId" class="form-select form-select-sm">
                        <option value="" disabled>Sélectionner...</option>
                        <option v-for="c in categories" :key="c.id" :value="String(c.id)">
                          {{ getCategorieLabel(String(c.id)) }}
                        </option>
                      </select>
                    </td>

                    <td style="min-width: 140px">
                      <input v-model="r.prix" class="form-control form-control-sm" type="number" step="0.01" min="0" />
                    </td>

                    <td>
                      <span v-if="isDirty(r)" class="badge bg-warning text-dark">Oui</span>
                      <span v-else class="text-muted">Non</span>
                    </td>

                    <td class="text-end">
                      <button
                        v-if="r.isNew"
                        class="btn btn-sm btn-outline-danger"
                        type="button"
                        :disabled="loading || saving"
                        @click="removeRow(r.id)"
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>

                  <tr v-if="rows.length === 0">
                    <td colspan="7" class="text-center text-muted">Aucun élément</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="d-flex gap-2 mt-3">
              <button class="btn btn-outline-primary" type="button" :disabled="loading || saving" @click="addRow">
                Ajouter ligne
              </button>
              <button class="btn btn-primary" type="button" :disabled="loading || saving" @click="save">
                Enregistrer
              </button>
              <button class="btn btn-secondary" type="button" :disabled="loading || saving" @click="cancel">
                Annuler
              </button>
              <button class="btn btn-outline-secondary" type="button" :disabled="loading || saving" @click="router.push('/tickets')">
                Retour liste
              </button>
              <div class="text-muted align-self-center ms-2">Modifiés: {{ dirtyRows.length }}</div>
            </div>

            <div v-if="saving" class="text-muted mt-2">Enregistrement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
