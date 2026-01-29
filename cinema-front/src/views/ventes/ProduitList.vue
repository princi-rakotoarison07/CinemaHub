<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/produits-extra`
const API_BASE_WITH_PRIX = `${API_BASE_URL}/api/produits-extra/with-prix`
const API_TARIFS = `${API_BASE_URL}/api/produit-tarifs`

const toast = useToast()

const original = ref([])
const produits = ref([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')

const toRow = (p) => ({
  id: p?.id,
  nom: p?.nom ?? '',
  description: p?.description ?? '',
  actif: Boolean(p?.actif),
  prix: p?.prix ?? null,
  prixEdit: p?.prix ?? null,
})

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_BASE_WITH_PRIX)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const list = await res.json()
    original.value = Array.isArray(list) ? list : []
    produits.value = original.value
      .slice()
      .sort((a, b) => (Number(a?.id ?? 0) || 0) - (Number(b?.id ?? 0) || 0))
      .map(toRow)
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const originalRowById = computed(() => {
  const m = new Map()
  for (const p of original.value ?? []) {
    if (p?.id != null) m.set(String(p.id), toRow(p))
  }
  return m
})

const isDirty = (r) => {
  if (!r?.id) return false
  const orig = originalRowById.value.get(String(r.id))
  if (!orig) return false
  const a = Number(r.prixEdit)
  const b = Number(orig.prix)
  return Number.isFinite(a) && a !== b
}

const dirtyRows = computed(() => produits.value.filter(isDirty))

const cancel = () => {
  produits.value = (original.value ?? []).map(toRow)
}

const save = async () => {
  saving.value = true
  error.value = ''
  try {
    const toSave = dirtyRows.value
    if (toSave.length === 0) {
      toast.success('Aucune modification')
      return
    }

    const today = new Date().toISOString().slice(0, 10)

    for (const r of toSave) {
      const prix = Number(r.prixEdit)
      if (!Number.isFinite(prix) || prix < 0) {
        throw new Error(`Prix invalide (produit #${r.id})`)
      }

      const payload = {
        idProduitExtra: Number(r.id),
        prix,
        dateDebut: today,
        dateFin: null,
      }

      const res = await fetch(API_TARIFS, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })
      if (!res.ok) {
        const msg = await res.text().catch(() => '')
        throw new Error(msg || `HTTP ${res.status}`)
      }
    }

    toast.success('Enregistré')
    await load()
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    saving.value = false
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
    <h1>Produits</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#">Home</a></li>
        <li class="breadcrumb-item">Vente</li>
        <li class="breadcrumb-item active">Produits</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Grille des produits</h5>

            <div class="d-flex gap-2 mb-3">
              <RouterLink class="btn btn-primary" to="/ventes/produits/new">Nouveau</RouterLink>
              <button class="btn btn-outline-primary" type="button" :disabled="loading || saving" @click="load">
                Rafraîchir
              </button>
              <button class="btn btn-primary" type="button" :disabled="saving" @click="save">Enregistrer</button>
              <button class="btn btn-secondary" type="button" :disabled="saving" @click="cancel">Annuler</button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Nom</th>
                    <th>Description</th>
                    <th class="text-end">Prix</th>
                    <th>Actif</th>
                    <th class="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in produits" :key="p.id">
                    <td>{{ p.id }}</td>
                    <td>{{ p.nom }}</td>
                    <td>{{ p.description }}</td>
                    <td class="text-end" style="max-width: 160px">
                      <input v-model="p.prixEdit" class="form-control form-control-sm text-end" type="number" min="0" />
                    </td>
                    <td>
                      <span :class="['badge', p.actif ? 'bg-success' : 'bg-secondary']">
                        {{ p.actif ? 'Oui' : 'Non' }}
                      </span>
                    </td>
                    <td class="text-end">
                      <RouterLink
                        class="btn btn-sm btn-outline-secondary me-2"
                        :to="`/ventes/produits/${p.id}/edit`"
                      >
                        Modifier
                      </RouterLink>
                      <button class="btn btn-sm btn-outline-danger" type="button" @click="remove(p.id)">
                        Supprimer
                      </button>
                    </td>
                  </tr>
                  <tr v-if="produits.length === 0">
                    <td colspan="6" class="text-center text-muted">Aucun élément</td>
                  </tr>
                </tbody>
              </table>
              <div class="text-muted small mt-2">Modifications: {{ dirtyRows.length }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
