<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_PRODUITS = `${API_BASE_URL}/api/produits-extra`
const API_TARIFS = `${API_BASE_URL}/api/produit-tarifs`

const route = useRoute()
const router = useRouter()

const toast = useToast()

const id = computed(() => route.params.id)
const isEdit = computed(() => !!id.value)

const form = ref({
  nom: '',
  description: '',
  actif: true,
})

const tarif = ref({
  prix: '',
  dateDebut: new Date().toISOString().slice(0, 10),
})

const loading = ref(false)
const error = ref('')

const loadOne = async () => {
  if (!isEdit.value) return
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(`${API_PRODUITS}/${id.value}`)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    if (!data) throw new Error('Introuvable')
    form.value.nom = data.nom ?? ''
    form.value.description = data.description ?? ''
    form.value.actif = !!data.actif
  } catch (e) {
    error.value = e?.message ?? 'Erreur'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  loading.value = true
  error.value = ''

  try {
    const payloadProduit = {
      nom: form.value.nom,
      description: form.value.description,
      actif: !!form.value.actif,
    }

    const url = isEdit.value ? `${API_PRODUITS}/${id.value}` : API_PRODUITS
    const method = isEdit.value ? 'PUT' : 'POST'

    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payloadProduit),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    const saved = await res.json().catch(() => null)
    const produitId = isEdit.value ? Number(id.value) : Number(saved?.id)

    const prix = tarif.value.prix === '' ? null : Number(tarif.value.prix)
    const dateDebut = tarif.value.dateDebut

    if (produitId && prix !== null && !Number.isNaN(prix) && dateDebut) {
      const payloadTarif = {
        idProduitExtra: produitId,
        prix,
        dateDebut,
        dateFin: null,
      }

      const resTarif = await fetch(API_TARIFS, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payloadTarif),
      })
      if (!resTarif.ok) throw new Error(`HTTP ${resTarif.status}`)
    }

    toast.success(isEdit.value ? 'Modifié avec succès' : 'Créé avec succès')
    await router.push('/ventes/produits')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadOne)
</script>

<template>
  <div class="pagetitle">
    <h1>Produits</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#">Home</a></li>
        <li class="breadcrumb-item">Vente</li>
        <li class="breadcrumb-item"><RouterLink to="/ventes/produits">Produits</RouterLink></li>
        <li class="breadcrumb-item active">{{ isEdit ? 'Modifier' : 'Nouveau' }}</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-7">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">{{ isEdit ? 'Modifier un produit' : 'Créer un produit' }}</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">Nom</label>
                <input v-model="form.nom" class="form-control" type="text" required />
              </div>

              <div class="col-12">
                <label class="form-label">Description</label>
                <textarea v-model="form.description" class="form-control" rows="3"></textarea>
              </div>

              <div class="col-12">
                <div class="form-check">
                  <input id="actif" v-model="form.actif" class="form-check-input" type="checkbox" />
                  <label class="form-check-label" for="actif">Actif</label>
                </div>
              </div>

              <div class="col-12">
                <hr />
                <h6 class="mb-3">Tarif (optionnel)</h6>

                <div class="row g-3">
                  <div class="col-12 col-md-6">
                    <label class="form-label">Prix (Ar)</label>
                    <input v-model="tarif.prix" class="form-control" type="number" min="0" step="1" />
                  </div>
                  <div class="col-12 col-md-6">
                    <label class="form-label">Date début</label>
                    <input v-model="tarif.dateDebut" class="form-control" type="date" />
                  </div>
                </div>
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading">Enregistrer</button>
                <RouterLink class="btn btn-secondary" to="/ventes/produits">Annuler</RouterLink>
              </div>
            </form>

            <div v-if="loading" class="text-muted mt-3">Chargement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
