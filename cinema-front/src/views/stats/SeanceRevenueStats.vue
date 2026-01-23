<script setup>
import { ref, onMounted } from 'vue'
import { API_BASE_URL } from '../../config/api'

const loading = ref(false)
const revenues = ref([])

const fetchRevenues = async () => {
  loading.value = true
  try {
    const response = await fetch(`${API_BASE_URL}/api/stats/seance-revenues`)
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    revenues.value = await response.json()
  } catch (error) {
    console.error('Error fetching seance revenues:', error)
  } finally {
    loading.value = false
  }
}

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 Ar'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MGA' }).format(value)
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('fr-FR')
}

onMounted(() => {
  fetchRevenues()
})
</script>

<template>
  <div class="pagetitle">
    <h1>Statistiques des Séances</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><RouterLink to="/">Home</RouterLink></li>
        <li class="breadcrumb-item active">Statistiques</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-lg-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Tableau de Bord des Revenus par Séance</h5>

            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
              </div>
            </div>

            <div v-else class="table-responsive">
              <table class="table table-striped table-hover datatable">
                <thead>
                  <tr>
                    <th scope="col">Film</th>
                    <th scope="col">Date</th>
                    <th scope="col">Heure</th>
                    <th scope="col" class="text-end">Pub (Total)</th>
                    <th scope="col" class="text-end">Pub (Réel/Payé)</th>
                    <th scope="col" class="text-end">Tickets</th>
                    <th scope="col" class="text-end">CA Total</th>
                    <th scope="col" class="text-end">CA Réel</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(rev, index) in revenues" :key="index">
                    <td>{{ rev.filmTitre }}</td>
                    <td>{{ formatDate(rev.dateDiffusion) }}</td>
                    <td>{{ rev.heureDiffusion }}</td>
                    <td class="text-end">{{ formatCurrency(rev.montantTotalPublicite) }}</td>
                    <td class="text-end">{{ formatCurrency(rev.montantReelPublicite) }}</td>
                    <td class="text-end">{{ formatCurrency(rev.montantTickets) }}</td>
                    <td class="text-end fw-bold">{{ formatCurrency(rev.chiffreAffaireTotal) }}</td>
                    <td class="text-end fw-bold text-success">{{ formatCurrency(rev.chiffreAffaireReel) }}</td>
                  </tr>
                  <tr v-if="revenues.length === 0">
                    <td colspan="8" class="text-center">Aucune donnée disponible</td>
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
