<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/seances`

const router = useRouter()
const toast = useToast()

const seances = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_BASE)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    seances.value = await res.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const formatDate = (v) => {
  if (!v) return ''
  try {
    return new Date(v).toLocaleString()
  } catch {
    return String(v)
  }
}

onMounted(load)

const goToPlaces = async (id) => {
  await router.push(`/seances/${id}/places`)
}
</script>

<template>
  <div class="pagetitle">
    <h1>Séances</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Liste des séances</h5>

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
                    <th>Date/Heure</th>
                    <th>Version</th>
                    <th>Langue</th>
                    <th>Film (id)</th>
                    <th>Salle (id)</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="s in seances" :key="s.id">
                    <td>{{ s.id }}</td>
                    <td>{{ formatDate(s.dateHeure) }}</td>
                    <td>{{ s.version }}</td>
                    <td>{{ s.langue }}</td>
                    <td>{{ s.film?.id }}</td>
                    <td>{{ s.salle?.id }}</td>
                    <td class="text-end">
                      <button class="btn btn-sm btn-outline-secondary" type="button" @click="goToPlaces(s.id)">
                        Places
                      </button>
                    </td>
                  </tr>
                  <tr v-if="seances.length === 0">
                    <td colspan="7" class="text-center text-muted">Aucun élément</td>
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
