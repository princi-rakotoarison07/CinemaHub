<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_CONTRATS_PUBLICITE = `${API_BASE_URL}/api/contrats-publicite`
const API_VIDEOS_PUBLICITAIRES = `${API_BASE_URL}/api/videos-publicitaires`
const API_SEANCES_INTERVAL = `${API_BASE_URL}/api/seances/by-interval`

const videos = ref([])
const seances = ref([])

const loading = ref(false)
const error = ref('')

const form = ref({
  videoPublicitaireId: '',
  nbDiffusions: 1,
  dateDebut: '',
  dateFin: '',
})

const nombrePubBySeanceId = ref({})

const totalPubs = computed(() => {
  let sum = 0
  const map = nombrePubBySeanceId.value || {}
  for (const s of seances.value ?? []) {
    const n = Number(map[String(s?.id ?? '')] ?? 0)
    if (Number.isFinite(n) && n > 0) sum += n
  }
  return sum
})

const canSubmit = computed(() => {
  const videoOk = Boolean(form.value.videoPublicitaireId)
  const nb = Number(form.value.nbDiffusions)
  const nbOk = Number.isFinite(nb) && nb > 0
  const dateOk = Boolean(form.value.dateDebut) && Boolean(form.value.dateFin)
  const sumOk = totalPubs.value === nb
  return videoOk && nbOk && dateOk && sumOk
})

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const vRes = await fetch(API_VIDEOS_PUBLICITAIRES)
    if (!vRes.ok) throw new Error(`Vidéos pub HTTP ${vRes.status}`)
    videos.value = await vRes.json()

    const firstVideo = (videos.value ?? [])[0]
    if (!form.value.videoPublicitaireId && firstVideo?.id != null) {
      form.value.videoPublicitaireId = String(firstVideo.id)
    }
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const loadSeances = async () => {
  seances.value = []
  nombrePubBySeanceId.value = {}

  const dd = String(form.value.dateDebut ?? '')
  const df = String(form.value.dateFin ?? '')
  if (!dd || !df) return

  try {
    const url = `${API_SEANCES_INTERVAL}?dateDebut=${encodeURIComponent(dd)}&dateFin=${encodeURIComponent(df)}`
    const res = await fetch(url)
    if (!res.ok) throw new Error(`Séances HTTP ${res.status}`)
    const list = await res.json()
    seances.value = list

    const init = {}
    for (const s of list ?? []) {
      if (s?.id != null) init[String(s.id)] = 0
    }
    nombrePubBySeanceId.value = init
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement séances')
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

watch(
  () => [form.value.dateDebut, form.value.dateFin],
  async () => {
    await loadSeances()
  },
)

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    const diffusions = (seances.value ?? []).map((s) => {
      const sid = String(s?.id ?? '')
      return {
        seanceId: Number(sid),
        nombrePub: Number(nombrePubBySeanceId.value?.[sid] ?? 0),
      }
    })

    const payload = {
      videoPublicitaireId: Number(form.value.videoPublicitaireId),
      nbDiffusions: Number(form.value.nbDiffusions),
      dateDebut: form.value.dateDebut,
      dateFin: form.value.dateFin,
      diffusions,
    }

    const res = await fetch(`${API_CONTRATS_PUBLICITE}/with-diffusions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }

    toast.success('Contrat publicité créé (avec détails)')
    await router.push('/publicites/contrats')
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
                <label class="form-label">Vidéo (Société - Titre)</label>
                <select v-model="form.videoPublicitaireId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="v in videos" :key="v.id" :value="String(v.id)">
                    {{ v?.societe?.nom }} - {{ v?.titre }} (#{{ v?.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Somme des pubs</label>
                <input
                  class="form-control"
                  type="text"
                  :value="`${totalPubs} / ${Number(form.nbDiffusions || 0)}`"
                  disabled
                />
                <div class="form-text" :class="totalPubs === Number(form.nbDiffusions || 0) ? 'text-success' : 'text-danger'">
                  La somme des pubs par séance doit être égale au nombre de diffusions.
                </div>
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
                <input v-model="form.dateFin" class="form-control" type="date" required />
              </div>

              <div class="col-12">
                <div class="table-responsive shadow-sm rounded border" style="max-height: 500px; overflow-y: auto;">
                  <table class="table table-sm table-striped table-hover align-middle mb-0">
                    <thead class="sticky-top bg-white" style="z-index: 1;">
                      <tr>
                        <th>Séance</th>
                        <th>Film</th>
                        <th>Salle</th>
                        <th style="width: 160px;">Nombre pubs</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="s in seances" :key="String(s?.id)">
                        <td class="small">{{ s?.dateHeure }}</td>
                        <td class="small">#{{ s?.film?.id }}</td>
                        <td class="small">#{{ s?.salle?.id }}</td>
                        <td>
                          <input
                            v-model.number="nombrePubBySeanceId[String(s?.id)]"
                            class="form-control form-control-sm"
                            type="number"
                            min="0"
                            step="1"
                          />
                        </td>
                      </tr>
                      <tr v-if="seances.length === 0">
                        <td colspan="4" class="text-center text-muted">Aucune séance dans l'intervalle</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/publicites/contrats">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
