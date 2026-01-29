<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_VENTES = `${API_BASE_URL}/api/ventes`
const API_PRODUITS = `${API_BASE_URL}/api/produits-extra`
const API_CLIENTS = `${API_BASE_URL}/api/clients`

const router = useRouter()
const toast = useToast()

const loading = ref(false)
const error = ref('')

const produits = ref([])
const clients = ref([])

const form = ref({
  dateVente: new Date().toISOString().slice(0, 10),
  idClient: '',
})

const lignes = ref([
  { idProduitExtra: '', quantite: 1 },
])

const loadProduits = async () => {
  try {
    const res = await fetch(API_PRODUITS)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    produits.value = await res.json()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement produits')
  }
}

const loadClients = async () => {
  try {
    const res = await fetch(API_CLIENTS)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    clients.value = await res.json()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement clients')
  }
}

const addLine = () => {
  lignes.value.push({ idProduitExtra: '', quantite: 1 })
}

const clientLabel = (c) => {
  if (!c) return ''
  const nom = c.nom ?? c.nomClient ?? ''
  const prenom = c.prenom ?? ''
  const full = `${nom} ${prenom}`.trim()
  return full || `#${c.id}`
}

const removeLine = (idx) => {
  if (lignes.value.length === 1) return
  lignes.value.splice(idx, 1)
}

const submit = async () => {
  loading.value = true
  error.value = ''

  try {
    const payload = {
      dateVente: form.value.dateVente,
      idClient: form.value.idClient ? Number(form.value.idClient) : null,
      lignes: lignes.value
        .filter((l) => l.idProduitExtra)
        .map((l) => ({
          idProduitExtra: Number(l.idProduitExtra),
          quantite: Number(l.quantite),
        })),
    }

    const res = await fetch(API_VENTES, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Vente créée avec succès')
    await router.push('/ventes')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadProduits)
onMounted(loadClients)
</script>

<template>
  <div class="pagetitle">
    <h1>Vente</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#">Home</a></li>
        <li class="breadcrumb-item">Vente</li>
        <li class="breadcrumb-item active">Nouvelle vente</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Créer une vente</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12 col-md-6">
                <label class="form-label">Date</label>
                <input v-model="form.dateVente" class="form-control" type="date" />
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Client</label>
                <select v-model="form.idClient" class="form-select" required>
                  <option value="" disabled>Choisir...</option>
                  <option v-for="c in clients" :key="c.id" :value="c.id">
                    {{ clientLabel(c) }}
                  </option>
                </select>
              </div>

              <div class="col-12">
                <hr />
                <h6 class="mb-3">Lignes</h6>

                <div v-for="(l, idx) in lignes" :key="idx" class="row g-2 align-items-end mb-2">
                  <div class="col-12 col-md-7">
                    <label class="form-label">Produit</label>
                    <select v-model="l.idProduitExtra" class="form-select" required>
                      <option value="" disabled>Choisir...</option>
                      <option v-for="p in produits" :key="p.id" :value="p.id">
                        {{ p.nom }}
                      </option>
                    </select>
                  </div>

                  <div class="col-8 col-md-3">
                    <label class="form-label">Quantité</label>
                    <input v-model="l.quantite" class="form-control" type="number" min="1" required />
                  </div>

                  <div class="col-4 col-md-2 d-flex">
                    <button
                      class="btn btn-outline-danger w-100"
                      type="button"
                      :disabled="lignes.length === 1"
                      @click="removeLine(idx)"
                    >
                      -
                    </button>
                  </div>
                </div>

                <div class="d-flex gap-2">
                  <button class="btn btn-outline-primary" type="button" @click="addLine">Ajouter ligne</button>
                </div>
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/ventes">Annuler</RouterLink>
              </div>
            </form>

            <div v-if="loading" class="text-muted mt-3">Chargement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
