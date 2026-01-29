<script setup>
import { computed, onMounted, ref } from 'vue'
import { API_BASE_URL } from '../../config/api'
import { useToast } from '../../composables/useToast'

const toast = useToast()

const API_CA_SEANCES = `${API_BASE_URL}/api/seances/chiffres-affaire`
const API_VENTES = `${API_BASE_URL}/api/ventes`

const loading = ref(false)
const error = ref('')

const mois = ref('') // YYYY-MM

const caTicket = ref(0)
const caPublicites = ref(0)
const dejaPayePub = ref(0)
const resteAPayerPub = ref(0)
const caVentes = ref(0)

const totalCA = computed(() =>
  Number(caTicket.value || 0) +
  Number(caPublicites.value || 0) +
  Number(caVentes.value || 0),
)

const formatMoney = (v) => {
  const n = Number(v ?? 0)
  if (!Number.isFinite(n)) return '0'
  return n.toLocaleString('fr-FR', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
}

const monthRangeFromKey = (mk) => {
  const key = String(mk ?? '')
  if (!key) return null
  const [yyyyStr, mmStr] = key.split('-')
  const yyyy = Number(yyyyStr)
  const mm = Number(mmStr)
  if (!Number.isFinite(yyyy) || !Number.isFinite(mm) || mm < 1 || mm > 12) return null

  const start = new Date(Date.UTC(yyyy, mm - 1, 1, 0, 0, 0, 0))
  const end = new Date(Date.UTC(yyyy, mm, 0, 0, 0, 0, 0))

  const toDateInput = (d) => d.toISOString().slice(0, 10)
  return { dateDebut: toDateInput(start), dateFin: toDateInput(end) }
}

const load = async () => {
  const range = monthRangeFromKey(mois.value)
  if (!range) {
    toast.error('Choisir un mois')
    return
  }

  loading.value = true
  error.value = ''

  caTicket.value = 0
  caPublicites.value = 0
  dejaPayePub.value = 0
  resteAPayerPub.value = 0
  caVentes.value = 0

  try {
    const params = new URLSearchParams()
    params.set('dateDebut', range.dateDebut)
    params.set('dateFin', range.dateFin)

    const [resSeances, resVentes] = await Promise.all([
      fetch(`${API_CA_SEANCES}?${params.toString()}`),
      fetch(API_VENTES),
    ])

    if (!resSeances.ok) {
      const msg = await resSeances.text().catch(() => '')
      throw new Error(msg || `CA séances HTTP ${resSeances.status}`)
    }
    if (!resVentes.ok) {
      const msg = await resVentes.text().catch(() => '')
      throw new Error(msg || `Ventes HTTP ${resVentes.status}`)
    }

    const seances = await resSeances.json()
    const ventes = await resVentes.json()

    const listSeances = Array.isArray(seances) ? seances : []
    caTicket.value = listSeances.reduce((sum, r) => sum + Number(r?.montantTickets ?? 0), 0)
    caPublicites.value = listSeances.reduce((sum, r) => sum + Number(r?.montantPublicite ?? 0), 0)
    dejaPayePub.value = listSeances.reduce((sum, r) => sum + Number(r?.montantPublicitePaye ?? 0), 0)
    resteAPayerPub.value = listSeances.reduce((sum, r) => sum + Number(r?.montantPubliciteReste ?? 0), 0)

    const listVentes = Array.isArray(ventes) ? ventes : []
    const start = new Date(`${range.dateDebut}T00:00:00Z`).getTime()
    const end = new Date(`${range.dateFin}T23:59:59Z`).getTime()

    caVentes.value = listVentes
      .filter((v) => {
        const raw = v?.dateVente ?? v?.date_vente ?? v?.date
        if (!raw) return false
        const t = new Date(raw).getTime()
        return Number.isFinite(t) && t >= start && t <= end
      })
      .reduce((sum, v) => sum + Number(v?.montantTotal ?? v?.montant_total ?? 0), 0)
  } catch (e) {
    error.value = e?.message ?? 'Erreur'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const clear = () => {
  mois.value = ''
  caTicket.value = 0
  caPublicites.value = 0
  dejaPayePub.value = 0
  resteAPayerPub.value = 0
  caVentes.value = 0
}

onMounted(() => {
  const d = new Date()
  mois.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  load()
})
</script>

<template>
  <div class="pagetitle">
    <h1>Chiffre d'affaire mensuel</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">CA mensuel (tickets + publicités + ventes)</h5>

            <div class="row g-3 mb-3">
              <div class="col-12 col-md-4">
                <label class="form-label">Mois</label>
                <input v-model="mois" type="month" class="form-control" />
              </div>
              <div class="col-12 col-md-8 d-flex align-items-end gap-2">
                <button class="btn btn-primary" type="button" :disabled="loading" @click="load">Filtrer</button>
                <button class="btn btn-outline-secondary" type="button" :disabled="loading" @click="clear">Réinitialiser</button>
              </div>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else>
              <div class="row mb-3">
                <div class="col-12">
                  <div class="border rounded p-3 bg-light d-flex justify-content-between">
                    <span class="text-muted">Total CA</span>
                    <span class="fw-bold">{{ formatMoney(totalCA) }} Ar</span>
                  </div>
                </div>
              </div>

              <div class="table-responsive shadow-sm rounded border">
                <table class="table table-striped table-hover align-middle mb-0">
                  <thead class="bg-white">
                    <tr>
                      <th>Mois</th>
                      <th class="text-end">CA Ticket</th>
                      <th class="text-end">CA Publicites</th>
                      <th class="text-end">Déjà payé pub</th>
                      <th class="text-end">Reste à payer pub</th>
                      <th class="text-end">CA Ventes</th>
                      <th class="text-end">Total CA</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td>{{ mois || '-' }}</td>
                      <td class="text-end">{{ formatMoney(caTicket) }} Ar</td>
                      <td class="text-end">{{ formatMoney(caPublicites) }} Ar</td>
                      <td class="text-end">{{ formatMoney(dejaPayePub) }} Ar</td>
                      <td class="text-end">{{ formatMoney(resteAPayerPub) }} Ar</td>
                      <td class="text-end">{{ formatMoney(caVentes) }} Ar</td>
                      <td class="text-end fw-bold">{{ formatMoney(totalCA) }} Ar</td>
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
