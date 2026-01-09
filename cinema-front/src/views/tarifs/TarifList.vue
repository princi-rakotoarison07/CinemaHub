<script setup>
import { onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/tarifs`

const toast = useToast()

const tarifs = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_BASE)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    tarifs.value = await res.json()
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
    <h1>Tarifs</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Grille tarifaire</h5>

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
                    <th>Type place (id)</th>
                    <th>Catégorie client (id)</th>
                    <th>Prix</th>
                    <th>Actif</th>
                    <th>Date début</th>
                    <th>Date fin</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="t in tarifs" :key="t.id">
                    <td>{{ t.id }}</td>
                    <td>{{ t.typePlace?.id }}</td>
                    <td>{{ t.categorieClient?.id }}</td>
                    <td>{{ t.prix }}</td>
                    <td>{{ t.actif }}</td>
                    <td>{{ t.dateDebut }}</td>
                    <td>{{ t.dateFin }}</td>
                  </tr>
                  <tr v-if="tarifs.length === 0">
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
