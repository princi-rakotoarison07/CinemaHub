<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const API_CLIENTS = `${API_BASE_URL}/api/clients`

const loading = ref(false)
const error = ref('')

const clientId = route.params?.id

const form = ref({
  nom: '',
  prenom: '',
  email: '',
  telephone: '',
})

const loadIfEdit = async () => {
  if (!clientId) return
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${API_CLIENTS}/${clientId}`)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const c = await res.json()
    form.value.nom = c?.nom ?? ''
    form.value.prenom = c?.prenom ?? ''
    form.value.email = c?.email ?? ''
    form.value.telephone = c?.telephone ?? ''
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    const payload = {
      nom: form.value.nom,
      prenom: form.value.prenom,
      email: form.value.email || null,
      telephone: form.value.telephone || null,
    }

    const url = clientId ? `${API_CLIENTS}/${clientId}` : API_CLIENTS
    const method = clientId ? 'PUT' : 'POST'

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success(clientId ? 'Client mis à jour' : 'Client créé')
    await router.push('/clients')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadIfEdit)
</script>

<template>
  <div class="pagetitle">
    <h1>Clients</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ clientId ? 'Modifier client' : 'Nouveau client' }}</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12 col-md-6">
                <label class="form-label">Nom</label>
                <input v-model="form.nom" class="form-control" type="text" required />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Prénom</label>
                <input v-model="form.prenom" class="form-control" type="text" required />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Email</label>
                <input v-model="form.email" class="form-control" type="email" />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Téléphone</label>
                <input v-model="form.telephone" class="form-control" type="text" />
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/clients">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
