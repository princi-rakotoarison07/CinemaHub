<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const route = useRoute()
const router = useRouter()
const toast = useToast()

const API_CONTRATS_PUBLICITE = `${API_BASE_URL}/api/contrats-publicite`
const API_PAIEMENTS = `${API_BASE_URL}/api/paiements-contrats-publicite`

const contratId = computed(() => String(route.params.id ?? ''))

const contrat = ref(null)
const paiements = ref([])

const loading = ref(false)
const error = ref('')

const form = ref({
  datePaiement: '',
  montant: '',
})

const totalContrat = computed(() => Number(contrat.value?.montantTotal ?? 0) || 0)

const dejaPaye = computed(() =>
  (paiements.value ?? []).reduce((sum, p) => sum + (Number(p?.montant ?? 0) || 0), 0),
)

const resteAPayer = computed(() => Math.max(0, totalContrat.value - dejaPaye.value))

const canSubmit = computed(() => {
  const montant = Number(form.value.montant)
  const montantOk = Number.isFinite(montant) && montant > 0 && montant <= resteAPayer.value
  return Boolean(contratId.value) && montantOk
})

const loadContrat = async () => {
  contrat.value = null
  const id = contratId.value
  if (!id) return

  try {
    const res = await fetch(`${API_CONTRATS_PUBLICITE}/${encodeURIComponent(id)}`)
    if (!res.ok) throw new Error(`Contrat HTTP ${res.status}`)
    contrat.value = await res.json()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement contrat')
  }
}

const loadPaiements = async () => {
  paiements.value = []
  const id = contratId.value
  if (!id) return

  try {
    const res = await fetch(`${API_PAIEMENTS}?contratId=${encodeURIComponent(id)}`)
    if (!res.ok) throw new Error(`Paiements HTTP ${res.status}`)
    const list = await res.json()
    paiements.value = Array.isArray(list) ? list : []
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement paiements')
  }
}

const submit = async () => {
  loading.value = true
  error.value = ''

  try {
    const id = contratId.value
    if (!id) throw new Error('Contrat invalide')

    const montant = Number(form.value.montant)
    if (!Number.isFinite(montant) || montant <= 0) throw new Error('Montant invalide')
    if (montant > resteAPayer.value) throw new Error('Montant dépasse le reste à payer')

    const payload = {
      contratPublicite: { id: Number(id) },
      montant,
    }

    if (form.value.datePaiement) {
      payload.datePaiement = new Date(`${form.value.datePaiement}T00:00:00Z`).toISOString()
    }

    const res = await fetch(API_PAIEMENTS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }

    form.value.montant = ''
    toast.success('Paiement enregistré')
    await loadPaiements()
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

watch(
  () => contratId.value,
  async () => {
    form.value.montant = ''
    form.value.datePaiement = ''
    await loadContrat()
    await loadPaiements()
  },
)

onMounted(loadContrat)
onMounted(loadPaiements)
</script>

<template>
  <div class="pagetitle">
    <h1>Publicité</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Payer contrat publicité #{{ contratId }}</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <div class="row g-2 mb-3">
              <div class="col-12 col-md-4">
                <label class="form-label">Total contrat</label>
                <input :value="String(totalContrat)" class="form-control" type="text" disabled />
              </div>
              <div class="col-12 col-md-4">
                <label class="form-label">Déjà payé</label>
                <input :value="String(dejaPaye)" class="form-control" type="text" disabled />
              </div>
              <div class="col-12 col-md-4">
                <label class="form-label">Reste à payer</label>
                <input :value="String(resteAPayer)" class="form-control" type="text" disabled />
              </div>
            </div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12 col-md-6">
                <label class="form-label">Date paiement</label>
                <input v-model="form.datePaiement" class="form-control" type="date" />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Montant à payer</label>
                <input
                  v-model="form.montant"
                  class="form-control"
                  type="number"
                  min="0"
                  step="0.01"
                  :max="resteAPayer"
                  required
                />
                <div class="form-text">Max: {{ resteAPayer }}</div>
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">Enregistrer</button>
                <button class="btn btn-secondary" type="button" @click="router.push('/publicites/contrats')">Retour</button>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>

        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Détails des paiements</h5>

            <div v-if="paiements.length === 0" class="text-muted">Aucun paiement</div>

            <div v-else class="table-responsive shadow-sm rounded border">
              <table class="table table-striped table-hover align-middle mb-0">
                <thead class="bg-white">
                  <tr>
                    <th>#</th>
                    <th>Date paiement</th>
                    <th>Montant</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in paiements" :key="String(p.id)">
                    <td>#{{ p.id }}</td>
                    <td>{{ p.datePaiement ? new Date(p.datePaiement).toLocaleString() : '' }}</td>
                    <td>{{ p.montant }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <div class="col-12 col-lg-4">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Contrat</h5>
            <div class="mb-1"><strong>Société:</strong> {{ contrat?.videoPublicitaire?.societe?.nom ?? '' }}</div>
            <div class="mb-1"><strong>Vidéo:</strong> {{ contrat?.videoPublicitaire?.titre ?? '' }}</div>
            <div class="mb-1"><strong>Nb diffusions:</strong> {{ contrat?.nbDiffusions ?? '' }}</div>
            <div class="mb-1"><strong>Montant total:</strong> {{ contrat?.montantTotal ?? '' }}</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
