<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_SOCIETES = `${API_BASE_URL}/api/societes`
const API_TARIFS_PUBLICITE = `${API_BASE_URL}/api/tarifs-publicite`
const API_CONTRATS_PUBLICITE = `${API_BASE_URL}/api/contrats-publicite`

const societes = ref([])
const tarifs = ref([])

const loading = ref(false)
const error = ref('')

const form = ref({
  societeId: '',
  tarifPubliciteId: '',
  nbDiffusions: 1,
  dateDebut: '',
  dateFin: '',
  actif: true,
})

const selectedTarif = computed(() =>
  (tarifs.value ?? []).find((t) => String(t?.id) === String(form.value.tarifPubliciteId)),
)

const montantTotal = computed(() => {
  const prix = Number(selectedTarif.value?.prixParDiffusion ?? 0)
  const nb = Number(form.value.nbDiffusions ?? 0)
  if (!Number.isFinite(prix) || !Number.isFinite(nb) || nb < 0) return 0
  return prix * nb
})

const canSubmit = computed(() =>
  Boolean(form.value.societeId && form.value.tarifPubliciteId && Number(form.value.nbDiffusions) > 0 && form.value.dateDebut),
)

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, tRes] = await Promise.all([fetch(API_SOCIETES), fetch(API_TARIFS_PUBLICITE)])
    if (!sRes.ok) throw new Error(`Sociétés HTTP ${sRes.status}`)
    if (!tRes.ok) throw new Error(`Tarifs publicité HTTP ${tRes.status}`)

    societes.value = await sRes.json()
    tarifs.value = await tRes.json()

    const firstSociete = (societes.value ?? [])[0]
    const firstTarif = (tarifs.value ?? [])[0]

    if (!form.value.societeId && firstSociete?.id != null) form.value.societeId = String(firstSociete.id)
    if (!form.value.tarifPubliciteId && firstTarif?.id != null) form.value.tarifPubliciteId = String(firstTarif.id)
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

watch(
  () => form.value.nbDiffusions,
  () => {
    const v = Number(form.value.nbDiffusions)
    if (!Number.isFinite(v)) form.value.nbDiffusions = 1
    if (v < 1) form.value.nbDiffusions = 1
  },
)

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    const payload = {
      societe: { id: Number(form.value.societeId) },
      tarifPublicite: { id: Number(form.value.tarifPubliciteId) },
      nbDiffusions: Number(form.value.nbDiffusions),
      montantTotal: montantTotal.value,
      dateDebut: form.value.dateDebut,
      dateFin: form.value.dateFin || null,
      actif: Boolean(form.value.actif),
    }

    const res = await fetch(API_CONTRATS_PUBLICITE, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Contrat publicité créé')
    await router.push('/')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadRefs)
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
            <h5 class="card-title">Nouveau contrat publicité</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12 col-md-6">
                <label class="form-label">Société</label>
                <select v-model="form.societeId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="s in societes" :key="s.id" :value="String(s.id)">
                    {{ s.nom }} ({{ s.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Tarif publicité</label>
                <select v-model="form.tarifPubliciteId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="t in tarifs" :key="t.id" :value="String(t.id)">
                    {{ t.prixParDiffusion }} Ar / diffusion ({{ t.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Nombre diffusions</label>
                <input v-model.number="form.nbDiffusions" class="form-control" type="number" min="1" step="1" required />
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Date début</label>
                <input v-model="form.dateDebut" class="form-control" type="date" required />
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Date fin</label>
                <input v-model="form.dateFin" class="form-control" type="date" />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Montant total (calculé)</label>
                <input :value="String(montantTotal)" class="form-control" type="text" disabled />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Actif</label>
                <select v-model="form.actif" class="form-select">
                  <option :value="true">Oui</option>
                  <option :value="false">Non</option>
                </select>
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
