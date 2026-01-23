<script setup>
import { computed, onMounted, ref } from 'vue'
import { API_BASE_URL } from '../../config/api'
import { useToast } from '../../composables/useToast'

const toast = useToast()

const API_CA_SEANCES = `${API_BASE_URL}/api/seances/chiffres-affaire`
const API_CA_SEANCE_DETAILS = `${API_BASE_URL}/api/seances`
const API_CONTRATS_FACTURES = `${API_BASE_URL}/api/contrats-publicite/factures`
const API_CONTRATS_FACTURE_DETAILS = `${API_BASE_URL}/api/contrats-publicite`

const loading = ref(false)
const error = ref('')

const dateDebut = ref('')
const dateFin = ref('')

const rows = ref([])

const expandedSeanceIds = ref([])
const detailsBySeanceId = ref({})
const loadingDetailsBySeanceId = ref({})

const isExpanded = (seanceId) => expandedSeanceIds.value.includes(String(seanceId))

const loadDetails = async (seanceId) => {
  const id = String(seanceId)
  loadingDetailsBySeanceId.value = { ...loadingDetailsBySeanceId.value, [id]: true }
  try {
    const res = await fetch(`${API_CA_SEANCE_DETAILS}/${encodeURIComponent(id)}/chiffres-affaire-details`)
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }
    const data = await res.json()
    detailsBySeanceId.value = { ...detailsBySeanceId.value, [id]: data }
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement détails')
  } finally {
    loadingDetailsBySeanceId.value = { ...loadingDetailsBySeanceId.value, [id]: false }
  }
}

const toggleExpand = async (seanceId) => {
  const id = String(seanceId)
  if (isExpanded(id)) {
    expandedSeanceIds.value = expandedSeanceIds.value.filter((x) => x !== id)
    return
  }
  expandedSeanceIds.value = [...expandedSeanceIds.value, id]
  if (!detailsBySeanceId.value[id]) {
    await loadDetails(id)
  }
}

const formatMoney = (v) => {
  const n = Number(v ?? 0)
  if (!Number.isFinite(n)) return '0.00'
  return n.toLocaleString('fr-FR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatDate = (isoInstant) => {
  if (!isoInstant) return ''
  const d = new Date(isoInstant)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleDateString('fr-FR')
}

const formatTime = (isoInstant) => {
  if (!isoInstant) return ''
  const d = new Date(isoInstant)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
}

const formatMoneyWithSuffix = (v) => `${formatMoney(v)} Ar`

const contratsFactures = ref([])
const loadingFactures = ref(false)
const openMenuForContrat = ref(null)
const expandedContratIds = ref([])
const factureDetailsByContratId = ref({})
const loadingFactureDetailsByContratId = ref({})

const isContratExpanded = (contratId) => expandedContratIds.value.includes(String(contratId))

const loadFactures = async () => {
  loadingFactures.value = true
  try {
    const res = await fetch(API_CONTRATS_FACTURES)
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }
    const list = await res.json()
    contratsFactures.value = Array.isArray(list) ? list : []
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement factures')
  } finally {
    loadingFactures.value = false
  }
}

const loadFactureDetails = async (contratId) => {
  const id = String(contratId)
  loadingFactureDetailsByContratId.value = { ...loadingFactureDetailsByContratId.value, [id]: true }
  try {
    const res = await fetch(`${API_CONTRATS_FACTURE_DETAILS}/${encodeURIComponent(id)}/facture-details`)
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }
    const data = await res.json()
    factureDetailsByContratId.value = { ...factureDetailsByContratId.value, [id]: data }
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement détails facture')
  } finally {
    loadingFactureDetailsByContratId.value = { ...loadingFactureDetailsByContratId.value, [id]: false }
  }
}

const toggleContratDetails = async (contratId) => {
  const id = String(contratId)
  if (isContratExpanded(id)) {
    expandedContratIds.value = expandedContratIds.value.filter((x) => x !== id)
    return
  }
  expandedContratIds.value = [...expandedContratIds.value, id]
  if (!factureDetailsByContratId.value[id]) {
    await loadFactureDetails(id)
  }
}

const totalTickets = computed(() =>
  rows.value.reduce((sum, r) => sum + Number(r?.montantTickets ?? 0), 0),
)

const totalPublicite = computed(() =>
  rows.value.reduce((sum, r) => sum + Number(r?.montantPublicite ?? 0), 0),
)

const totalGlobal = computed(() => totalTickets.value + totalPublicite.value)

const buildUrl = () => {
  const params = new URLSearchParams()
  if (dateDebut.value) params.set('dateDebut', dateDebut.value)
  if (dateFin.value) params.set('dateFin', dateFin.value)
  const q = params.toString()
  return q ? `${API_CA_SEANCES}?${q}` : API_CA_SEANCES
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(buildUrl())
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }
    const list = await res.json()
    rows.value = Array.isArray(list)
      ? list.map((x) => ({
          seanceId: x?.seanceId,
          film: x?.film ?? '',
          dateHeure: x?.dateHeure,
          montantTickets: x?.montantTickets ?? x?.montantTickets ?? 0,
          montantPublicite: x?.montantPublicite ?? x?.montantPublicite ?? 0,
          montantPublicitePaye: x?.montantPublicitePaye ?? 0,
          montantPubliciteReste: x?.montantPubliciteReste ?? 0,
          montantTotal: x?.montantTotal ?? 0,
        }))
      : []
  } catch (e) {
    error.value = e?.message ?? 'Erreur'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const clearFilters = async () => {
  dateDebut.value = ''
  dateFin.value = ''
  await load()
}

onMounted(load)
onMounted(loadFactures)
</script>

<template>
  <div class="pagetitle">
    <h1>Chiffre d'affaire</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Chiffre d'affaire par séance</h5>

            <div class="row g-3 mb-3">
              <div class="col-12 col-md-4">
                <label class="form-label">Date début</label>
                <input v-model="dateDebut" type="date" class="form-control" />
              </div>
              <div class="col-12 col-md-4">
                <label class="form-label">Date fin</label>
                <input v-model="dateFin" type="date" class="form-control" />
              </div>
              <div class="col-12 col-md-4 d-flex align-items-end gap-2">
                <button class="btn btn-primary" type="button" :disabled="loading" @click="load">
                  Filtrer
                </button>
                <button class="btn btn-outline-secondary" type="button" :disabled="loading" @click="clearFilters">
                  Tous
                </button>
              </div>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else>
              <div class="row mb-3">
                <div class="col-12 col-md-4">
                  <div class="border rounded p-3 bg-light">
                    <div class="text-muted">Total Tickets</div>
                    <div class="fw-bold">{{ formatMoney(totalTickets) }} Ar</div>
                  </div>
                </div>
                <div class="col-12 col-md-4">
                  <div class="border rounded p-3 bg-light">
                    <div class="text-muted">Total Publicité</div>
                    <div class="fw-bold">{{ formatMoney(totalPublicite) }} Ar</div>
                  </div>
                </div>
                <div class="col-12 col-md-4">
                  <div class="border rounded p-3 bg-light">
                    <div class="text-muted">Total CA</div>
                    <div class="fw-bold">{{ formatMoney(totalGlobal) }} Ar</div>
                  </div>
                </div>
              </div>

              <div class="table-responsive shadow-sm rounded border">
                <table class="table table-striped table-hover align-middle mb-0">
                  <thead class="bg-white">
                    <tr>
                      <th style="width: 60px"></th>
                      <th>Film</th>
                      <th>Date diffusion</th>
                      <th>Heure diff</th>
                      <th class="text-end">Montant tickets</th>
                      <th class="text-end">Montant publicité</th>
                      <th class="text-end">Payé pub</th>
                      <th class="text-end">Reste pub</th>
                      <th class="text-end">CA total</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-for="r in rows" :key="String(r.seanceId)">
                      <tr>
                        <td>
                          <button
                            class="btn btn-sm btn-outline-secondary"
                            type="button"
                            :disabled="loadingDetailsBySeanceId[String(r.seanceId)]"
                            @click="toggleExpand(r.seanceId)"
                          >
                            {{ isExpanded(r.seanceId) ? '-' : '+' }}
                          </button>
                        </td>
                        <td>{{ r.film }}</td>
                        <td>{{ formatDate(r.dateHeure) }}</td>
                        <td>{{ formatTime(r.dateHeure) }}</td>
                        <td class="text-end">{{ formatMoney(r.montantTickets) }} Ar</td>
                        <td class="text-end">{{ formatMoney(r.montantPublicite) }} Ar</td>
                        <td class="text-end">{{ formatMoney(r.montantPublicitePaye) }} Ar</td>
                        <td class="text-end">{{ formatMoney(r.montantPubliciteReste) }} Ar</td>
                        <td class="text-end fw-bold">{{ formatMoney(r.montantTotal) }} Ar</td>
                      </tr>

                      <tr v-if="isExpanded(r.seanceId)">
                        <td colspan="9">
                          <div v-if="loadingDetailsBySeanceId[String(r.seanceId)]" class="text-muted">
                            Chargement des détails...
                          </div>

                          <div v-else>
                            <div class="fw-semibold mb-2">Détails publicité</div>

                            <div class="table-responsive mb-3">
                              <table class="table table-sm table-bordered align-middle mb-0">
                                <thead>
                                  <tr>
                                    <th>Société</th>
                                    <th>Vidéo</th>
                                    <th class="text-end">Nombre</th>
                                    <th class="text-end">Prix/diffusion</th>
                                    <th class="text-end">Montant diffusion</th>
                                    <th class="text-end">Reste contrat</th>
                                  </tr>
                                </thead>
                                <tbody>
                                  <tr
                                    v-for="p in (detailsBySeanceId[String(r.seanceId)]?.diffusionsPublicite ?? [])"
                                    :key="String(p.contratId) + '|' + String(p.video)"
                                  >
                                    <td>{{ p.societe }}</td>
                                    <td>{{ p.video }}</td>
                                    <td class="text-end">{{ p.nombrePub }}</td>
                                    <td class="text-end">{{ formatMoneyWithSuffix(p.prixParDiffusion) }}</td>
                                    <td class="text-end">{{ formatMoneyWithSuffix(p.montantDiffusion) }}</td>
                                    <td class="text-end">{{ formatMoneyWithSuffix(p.montantReste) }}</td>
                                  </tr>
                                  <tr
                                    v-if="(detailsBySeanceId[String(r.seanceId)]?.diffusionsPublicite ?? []).length === 0"
                                  >
                                    <td colspan="6" class="text-center text-muted">Aucune diffusion publicité</td>
                                  </tr>
                                </tbody>
                              </table>
                            </div>

                            <div class="pt-2 border-top d-flex justify-content-between">
                              <span class="text-muted">Montant tickets</span>
                              <span class="fw-semibold">
                                {{ formatMoneyWithSuffix(detailsBySeanceId[String(r.seanceId)]?.montantTickets ?? 0) }}
                              </span>
                            </div>
                          </div>
                        </td>
                      </tr>
                    </template>
                    <tr v-if="rows.length === 0">
                      <td colspan="9" class="text-center text-muted">Aucune séance</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="mt-4">
                <h5 class="card-title mb-2">Factures contrats publicité</h5>

                <div v-if="loadingFactures" class="text-muted">Chargement des factures...</div>

                <div v-else class="table-responsive shadow-sm rounded border">
                  <table class="table table-striped table-hover align-middle mb-0">
                    <thead class="bg-white">
                      <tr>
                        <th>#</th>
                        <th>Société</th>
                        <th>Vidéo</th>
                        <th class="text-end">Nb diffusions</th>
                        <th class="text-end">Montant total</th>
                        <th class="text-end">Payé</th>
                        <th class="text-end">Reste</th>
                        <th style="width: 180px">% payé</th>
                        <th class="text-end" style="width: 80px">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      <template v-for="c in contratsFactures" :key="String(c.contratId)">
                        <tr>
                          <td>#{{ c.contratId }}</td>
                          <td>{{ c.societe }}</td>
                          <td>{{ c.video }}</td>
                          <td class="text-end">{{ c.nbDiffusions }}</td>
                          <td class="text-end">{{ formatMoneyWithSuffix(c.montantTotal) }}</td>
                          <td class="text-end">{{ formatMoneyWithSuffix(c.montantPaye) }}</td>
                          <td class="text-end">{{ formatMoneyWithSuffix(c.montantReste) }}</td>
                          <td>
                            <div class="progress" style="height: 16px">
                              <div
                                class="progress-bar"
                                role="progressbar"
                                :style="{ width: `${Math.min(100, Math.max(0, Number(c.pourcentagePaye ?? 0)))}%` }"
                                :aria-valuenow="Number(c.pourcentagePaye ?? 0)"
                                aria-valuemin="0"
                                aria-valuemax="100"
                              >
                                {{ Number(c.pourcentagePaye ?? 0).toFixed(0) }}%
                              </div>
                            </div>
                          </td>
                          <td class="text-end">
                            <div class="dropdown">
                              <button
                                class="btn btn-sm btn-light"
                                type="button"
                                @click="openMenuForContrat = openMenuForContrat === c.contratId ? null : c.contratId"
                              >
                                <i class="bi bi-three-dots-vertical"></i>
                              </button>
                              <ul class="dropdown-menu dropdown-menu-end" :class="{ show: openMenuForContrat === c.contratId }">
                                <li>
                                  <button
                                    class="dropdown-item"
                                    type="button"
                                    @click="toggleContratDetails(c.contratId); openMenuForContrat = null"
                                  >
                                    Détails
                                  </button>
                                </li>
                              </ul>
                            </div>
                          </td>
                        </tr>

                        <tr v-if="isContratExpanded(c.contratId)">
                          <td colspan="9">
                            <div v-if="loadingFactureDetailsByContratId[String(c.contratId)]" class="text-muted">
                              Chargement détails...
                            </div>

                            <div v-else>
                              <div class="fw-semibold mb-2">Détails diffusions (répartition au même % payé)</div>
                              <div class="table-responsive">
                                <table class="table table-sm table-bordered align-middle mb-0">
                                  <thead>
                                    <tr>
                                      <th>Séance</th>
                                      <th>Film</th>
                                      <th>Date</th>
                                      <th>Heure</th>
                                      <th class="text-end">Nombre pub</th>
                                      <th class="text-end">Prix/diffusion</th>
                                      <th class="text-end">Montant</th>
                                      <th class="text-end">% payé</th>
                                      <th class="text-end">Payé</th>
                                      <th class="text-end">Reste</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    <tr
                                      v-for="d in (factureDetailsByContratId[String(c.contratId)]?.diffusions ?? [])"
                                      :key="String(d.seanceId)"
                                    >
                                      <td>#{{ d.seanceId }}</td>
                                      <td>{{ d.film }}</td>
                                      <td>{{ formatDate(d.dateHeure) }}</td>
                                      <td>{{ formatTime(d.dateHeure) }}</td>
                                      <td class="text-end">{{ d.nombrePub }}</td>
                                      <td class="text-end">{{ formatMoneyWithSuffix(d.prixParDiffusion) }}</td>
                                      <td class="text-end">{{ formatMoneyWithSuffix(d.montantDiffusion) }}</td>
                                      <td class="text-end">{{ Number(d.pourcentagePaye ?? 0).toFixed(2) }}%</td>
                                      <td class="text-end">{{ formatMoneyWithSuffix(d.montantPaye) }}</td>
                                      <td class="text-end">{{ formatMoneyWithSuffix(d.montantReste) }}</td>
                                    </tr>
                                    <tr v-if="(factureDetailsByContratId[String(c.contratId)]?.diffusions ?? []).length === 0">
                                      <td colspan="10" class="text-center text-muted">Aucune diffusion</td>
                                    </tr>
                                  </tbody>
                                </table>
                              </div>

                              <div class="pt-2 border-top mt-2 d-flex justify-content-between">
                                <span class="text-muted">Résumé contrat</span>
                                <span class="fw-semibold">
                                  {{ formatMoneyWithSuffix(factureDetailsByContratId[String(c.contratId)]?.montantPaye ?? 0) }}
                                  /
                                  {{ formatMoneyWithSuffix(factureDetailsByContratId[String(c.contratId)]?.montantTotal ?? 0) }}
                                </span>
                              </div>
                            </div>
                          </td>
                        </tr>
                      </template>

                      <tr v-if="contratsFactures.length === 0">
                        <td colspan="9" class="text-center text-muted">Aucune facture</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
