<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_SOCIETES = `${API_BASE_URL}/api/societes`
const API_CONTRATS_PUBLICITE = `${API_BASE_URL}/api/contrats-publicite`
const API_PAIEMENTS = `${API_BASE_URL}/api/paiements-contrats-publicite`

const societes = ref([])
const contrats = ref([])

const loading = ref(false)
const error = ref('')

const form = ref({
  societeId: '',
  contratId: '',
  datePaiement: '',
  montant: '',
  mode: '',
  reference: '',
})

const paiementsContrat = ref([])

const contratsBySociete = computed(() => {
  const sid = String(form.value.societeId ?? '')
  const list = contrats.value ?? []
  if (!sid) return list
  return list.filter((c) => String(c?.videoPublicitaire?.societe?.id ?? '') === sid)
})

const selectedContrat = computed(() =>
  (contrats.value ?? []).find((c) => String(c?.id ?? '') === String(form.value.contratId ?? '')),
)

const totalContrat = computed(() => Number(selectedContrat.value?.montantTotal ?? 0) || 0)

const dejaPaye = computed(() =>
  (paiementsContrat.value ?? []).reduce((sum, p) => sum + (Number(p?.montant ?? 0) || 0), 0),
)

const resteAPayer = computed(() => Math.max(0, totalContrat.value - dejaPaye.value))

const canSubmit = computed(() => {
  const contratOk = Boolean(form.value.contratId)
  const montant = Number(form.value.montant)
  const montantOk = Number.isFinite(montant) && montant > 0 && montant <= resteAPayer.value
  return contratOk && montantOk
})

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, cRes] = await Promise.all([fetch(API_SOCIETES), fetch(API_CONTRATS_PUBLICITE)])
    if (!sRes.ok) throw new Error(`Sociétés HTTP ${sRes.status}`)
    if (!cRes.ok) throw new Error(`Contrats pub HTTP ${cRes.status}`)

    societes.value = await sRes.json()
    contrats.value = await cRes.json()

    const firstSoc = (societes.value ?? [])[0]
    if (!form.value.societeId && firstSoc?.id != null) form.value.societeId = String(firstSoc.id)

    const firstContrat = (contratsBySociete.value ?? [])[0]
    if (!form.value.contratId && firstContrat?.id != null) form.value.contratId = String(firstContrat.id)
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const loadPaiementsForContrat = async () => {
  paiementsContrat.value = []
  const id = String(form.value.contratId ?? '')
  if (!id) return
  try {
    const res = await fetch(`${API_PAIEMENTS}?contratId=${encodeURIComponent(id)}`)
    if (!res.ok) throw new Error(`Paiements HTTP ${res.status}`)
    paiementsContrat.value = await res.json()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement paiements')
  }
}

watch(
  () => form.value.societeId,
  () => {
    const first = (contratsBySociete.value ?? [])[0]
    form.value.contratId = first?.id != null ? String(first.id) : ''
  },
)

watch(
  () => form.value.contratId,
  async () => {
    form.value.montant = ''
    await loadPaiementsForContrat()
  },
)

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    const montant = Number(form.value.montant)
    if (!Number.isFinite(montant) || montant <= 0) throw new Error('Montant invalide')
    if (montant > resteAPayer.value) throw new Error('Montant dépasse le reste à payer')

    const payload = {
      contratPublicite: { id: Number(form.value.contratId) },
      montant,
      mode: form.value.mode || null,
      reference: form.value.reference || null,
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

    toast.success('Paiement enregistré')
    await router.push('/publicites/restes')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadRefs)
onMounted(loadPaiementsForContrat)
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
            <h5 class="card-title">Nouveau paiement (contrat publicité)</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12 col-md-6">
                <label class="form-label">Société</label>
                <select v-model="form.societeId" class="form-select">
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="s in societes" :key="s.id" :value="String(s.id)">
                    {{ s.nom }} ({{ s.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Contrat</label>
                <select v-model="form.contratId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="c in contratsBySociete" :key="c.id" :value="String(c.id)">
                    #{{ c.id }} - total {{ c.montantTotal }} Ar
                  </option>
                </select>
              </div>

              <div class="col-12">
                <div class="row g-2">
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
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Date paiement</label>
                <input v-model="form.datePaiement" class="form-control" type="date" />
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Montant</label>
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

              <div class="col-12 col-md-4">
                <label class="form-label">Mode</label>
                <input v-model="form.mode" class="form-control" type="text" placeholder="CASH / MOBILE_MONEY / VIREMENT" />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Référence</label>
                <input v-model="form.reference" class="form-control" type="text" placeholder="REF-001" />
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/publicites/restes">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
