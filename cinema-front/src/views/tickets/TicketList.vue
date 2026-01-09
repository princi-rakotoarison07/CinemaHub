<script setup>
import { onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/tickets`

const toast = useToast()

const tickets = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_BASE)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    tickets.value = await res.json()
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
    <h1>Tickets</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Liste des tickets</h5>

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
                    <th>Réservation (id)</th>
                    <th>Place (id)</th>
                    <th>Catégorie client (id)</th>
                    <th>Prix</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="t in tickets" :key="t.id">
                    <td>{{ t.id }}</td>
                    <td>{{ t.reservation?.id }}</td>
                    <td>{{ t.place?.id }}</td>
                    <td>{{ t.categorieClient?.id }}</td>
                    <td>{{ t.prix }}</td>
                  </tr>
                  <tr v-if="tickets.length === 0">
                    <td colspan="5" class="text-center text-muted">Aucun élément</td>
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
