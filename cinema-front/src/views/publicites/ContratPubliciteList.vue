<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { API_BASE_URL } from '../../config/api'
import { useToast } from '../../composables/useToast'

const router = useRouter()
const toast = useToast()

const API_CONTRATS_PUBLICITE = `${API_BASE_URL}/api/contrats-publicite`

const contrats = ref([])
const loading = ref(false)
const error = ref('')

const openMenuFor = ref(null)

const rows = computed(() => {
  return (contrats.value ?? []).map((c) => {
    const societeNom = c?.videoPublicitaire?.societe?.nom ?? ''
    const videoTitre = c?.videoPublicitaire?.titre ?? ''
    return {
      id: c?.id,
      societeNom,
      videoTitre,
      nbDiffusions: c?.nbDiffusions,
      montantTotal: c?.montantTotal,
      dateDebut: c?.dateDebut,
      dateFin: c?.dateFin,
    }
  })
})

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_CONTRATS_PUBLICITE)
    if (!res.ok) throw new Error(`Contrats pub HTTP ${res.status}`)
    contrats.value = await res.json()
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
    <h1>Publicité</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Contrats publicité</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive shadow-sm rounded border">
              <table class="table table-striped table-hover align-middle mb-0">
                <thead class="bg-white">
                  <tr>
                    <th>#</th>
                    <th>Société</th>
                    <th>Vidéo</th>
                    <th>Nb diffusions</th>
                    <th>Date début</th>
                    <th>Date fin</th>
                    <th>Montant total</th>
                    <th class="text-end">Action</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in rows" :key="String(r.id)">
                    <td>#{{ r.id }}</td>
                    <td>{{ r.societeNom }}</td>
                    <td>{{ r.videoTitre }}</td>
                    <td>{{ r.nbDiffusions }}</td>
                    <td>{{ r.dateDebut }}</td>
                    <td>{{ r.dateFin }}</td>
                    <td>{{ r.montantTotal }}</td>
                    <td class="text-end">
                      <div class="dropdown">
                        <button
                          class="btn btn-sm btn-light"
                          type="button"
                          @click="openMenuFor = openMenuFor === r.id ? null : r.id"
                        >
                          <i class="bi bi-three-dots-vertical"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end" :class="{ show: openMenuFor === r.id }">
                          <li>
                            <button
                              class="dropdown-item"
                              type="button"
                              @click="router.push(`/publicites/contrats/${r.id}/payer`); openMenuFor = null"
                            >
                              Payer
                            </button>
                          </li>
                        </ul>
                      </div>
                    </td>
                  </tr>
                  <tr v-if="rows.length === 0">
                    <td colspan="8" class="text-center text-muted">Aucun contrat</td>
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
