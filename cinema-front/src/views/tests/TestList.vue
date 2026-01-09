<script setup>
import { onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/tests`

const toast = useToast()

const tests = ref([])
const loading = ref(false)
const error = ref('')

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_BASE)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    tests.value = await res.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const remove = async (id) => {
  if (!confirm('Supprimer cet élément ?')) return
  try {
    const res = await fetch(`${API_BASE}/${id}`, { method: 'DELETE' })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    toast.success('Supprimé avec succès')
    await load()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur lors de la suppression')
  }
}

onMounted(load)
</script>

<template>
  <div class="pagetitle">
    <h1>Test</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#">Home</a></li>
        <li class="breadcrumb-item active">Test</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Liste des tests</h5>

            <div class="d-flex gap-2 mb-3">
              <RouterLink class="btn btn-primary" to="/tests/new">Nouveau</RouterLink>
              <button class="btn btn-outline-primary" type="button" @click="load">Rafraîchir</button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>IP</th>
                    <th>Port</th>
                    <th class="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="t in tests" :key="t.id">
                    <td>{{ t.id }}</td>
                    <td>{{ t.ip }}</td>
                    <td>{{ t.port }}</td>
                    <td class="text-end">
                      <RouterLink class="btn btn-sm btn-outline-secondary me-2" :to="`/tests/${t.id}/edit`">
                        Modifier
                      </RouterLink>
                      <button class="btn btn-sm btn-outline-danger" type="button" @click="remove(t.id)">
                        Supprimer
                      </button>
                    </td>
                  </tr>
                  <tr v-if="tests.length === 0">
                    <td colspan="4" class="text-center text-muted">Aucun élément</td>
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
