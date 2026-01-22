<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const toast = useToast()

const API_SOCIETES = `${API_BASE_URL}/api/societes`
const API_CONTRATS_PUBLICITE = `${API_BASE_URL}/api/contrats-publicite`
const API_PAIEMENTS = `${API_BASE_URL}/api/paiements-contrats-publicite`

const societes = ref([])
const contrats = ref([])
const paiements = ref([])

const loading = ref(false)
const error = ref('')

const filters = ref({
  societeId: '',
  mois: '', // YYYY-MM
})

const monthKeyFromDate = (d) => {
  if (!d) return ''
  try {
    const dt = new Date(d)
    const yyyy = dt.getFullYear()
    const mm = String(dt.getMonth() + 1).padStart(2, '0')
    return `${yyyy}-${mm}`
  } catch {
    return ''
  }
}

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, cRes, pRes] = await Promise.all([
      fetch(API_SOCIETES),
      fetch(API_CONTRATS_PUBLICITE),
      fetch(API_PAIEMENTS),
    ])
    if (!sRes.ok) throw new Error(`Sociétés HTTP ${sRes.status}`)
    if (!cRes.ok) throw new Error(`Contrats pub HTTP ${cRes.status}`)
    if (!pRes.ok) throw new Error(`Paiements pub HTTP ${pRes.status}`)

    societes.value = await sRes.json()
    contrats.value = await cRes.json()
    paiements.value = await pRes.json()

    const firstSoc = (societes.value ?? [])[0]
    if (!filters.value.societeId && firstSoc?.id != null) filters.value.societeId = String(firstSoc.id)

    if (!filters.value.mois) {
      const anyContrat = (contrats.value ?? [])[0]
      const mk = monthKeyFromDate(anyContrat?.dateContrat)
      filters.value.mois = mk || '2025-12'
    }
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const contratsForSociete = computed(() => {
  const sid = String(filters.value.societeId ?? '')
  return (contrats.value ?? []).filter((c) => String(c?.videoPublicitaire?.societe?.id ?? '') === sid)
})

const paiementsForSociete = computed(() => {
  const sid = String(filters.value.societeId ?? '')
  return (paiements.value ?? []).filter((p) => String(p?.contratPublicite?.videoPublicitaire?.societe?.id ?? '') === sid)
})

const paiementsByContratId = computed(() => {
  const map = new Map()
  for (const p of paiementsForSociete.value ?? []) {
    const cid = String(p?.contratPublicite?.id ?? '')
    if (!cid) continue
    if (!map.has(cid)) map.set(cid, [])
    map.get(cid).push(p)
  }
  for (const arr of map.values()) {
    arr.sort((a, b) => String(a?.datePaiement ?? '').localeCompare(String(b?.datePaiement ?? '')))
  }
  return map
})

const contratRows = computed(() => {
  return (contratsForSociete.value ?? []).map((c) => {
    const cid = String(c?.id ?? '')
    const list = paiementsByContratId.value.get(cid) ?? []
    const total = Number(c?.montantTotal ?? 0) || 0
    const paid = list.reduce((sum, p) => sum + (Number(p?.montant ?? 0) || 0), 0)
    const reste = Math.max(0, total - paid)
    return {
      contrat: c,
      paiements: list,
      total,
      paid,
      reste,
    }
  })
})

const totalResteSociete = computed(() => contratRows.value.reduce((sum, r) => sum + (Number(r.reste) || 0), 0))

const caTheoriqueMois = computed(() => {
  const mk = String(filters.value.mois ?? '')
  if (!mk) return 0
  return (contratsForSociete.value ?? [])
    .filter((c) => monthKeyFromDate(c?.dateContrat) === mk)
    .reduce((sum, c) => sum + (Number(c?.montantTotal ?? 0) || 0), 0)
})

const caPayeMois = computed(() => {
  const mk = String(filters.value.mois ?? '')
  if (!mk) return 0
  return (paiementsForSociete.value ?? [])
    .filter((p) => monthKeyFromDate(p?.datePaiement) === mk)
    .reduce((sum, p) => sum + (Number(p?.montant ?? 0) || 0), 0)
})

watch(
  () => filters.value.societeId,
  () => {
    // no-op, computed lists will update
  },
)

onMounted(loadData)
</script>

<template>
  <div class="pagetitle">
    <h1>Publicité</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Reste à payer (par société)</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else>
              <div class="row g-3 align-items-end mb-3">
                <div class="col-12 col-md-6">
                  <label class="form-label">Société</label>
                  <select v-model="filters.societeId" class="form-select">
                    <option value="" disabled>Sélectionner...</option>
                    <option v-for="s in societes" :key="s.id" :value="String(s.id)">
                      {{ s.nom }} ({{ s.id }})
                    </option>
                  </select>
                </div>

                <div class="col-12 col-md-3">
                  <label class="form-label">Mois (CA)</label>
                  <input v-model="filters.mois" class="form-control" type="month" />
                </div>

                <div class="col-12 col-md-3 d-flex gap-2">
                  <RouterLink class="btn btn-primary" to="/publicites/contrats">Contrats</RouterLink>
                </div>
              </div>

              <div class="alert alert-info">
                <strong>Reste total à payer :</strong> {{ totalResteSociete.toFixed(2) }} Ar
              </div>

              <div class="table-responsive shadow-sm rounded border" style="max-height: 900px; overflow-y: auto;">
                <table class="table table-striped table-hover align-middle mb-0">
                  <thead class="sticky-top bg-white" style="z-index: 1;">
                    <tr>
                      <th>Contrat</th>
                      <th>Total</th>
                      <th>Payé</th>
                      <th>Reste</th>
                      <th>Détails paiements</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in contratRows" :key="String(r.contrat?.id)">
                      <td>#{{ r.contrat?.id }}</td>
                      <td>{{ r.total.toFixed(2) }}</td>
                      <td>{{ r.paid.toFixed(2) }}</td>
                      <td class="fw-semibold" :class="r.reste > 0 ? 'text-danger' : 'text-success'">
                        {{ r.reste.toFixed(2) }}
                      </td>
                      <td>
                        <div v-if="r.paiements.length === 0" class="text-muted">Aucun paiement</div>
                        <div v-else>
                          <div v-for="p in r.paiements" :key="String(p.id)" class="small">
                            {{ p.datePaiement }} - {{ Number(p.montant).toFixed(2) }} Ar
                            <span v-if="p.mode">({{ p.mode }})</span>
                            <span v-if="p.reference">#{{ p.reference }}</span>
                          </div>
                        </div>
                      </td>
                    </tr>
                    <tr v-if="contratRows.length === 0">
                      <td colspan="5" class="text-center text-muted">Aucun contrat</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="mt-4">
                <h6>Chiffre d'affaire - {{ filters.mois }}</h6>
                <div class="row g-3">
                  <div class="col-12 col-md-6">
                    <div class="card border shadow-none">
                      <div class="card-body">
                        <div class="text-muted small">CA théorique (si tout payé) - contrats du mois (date_contrat)</div>
                        <div class="fs-4 fw-bold">{{ caTheoriqueMois.toFixed(2) }} Ar</div>
                      </div>
                    </div>
                  </div>
                  <div class="col-12 col-md-6">
                    <div class="card border shadow-none">
                      <div class="card-body">
                        <div class="text-muted small">CA payé - paiements du mois (date_paiement)</div>
                        <div class="fs-4 fw-bold">{{ caPayeMois.toFixed(2) }} Ar</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
