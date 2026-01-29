<script setup>
import { onMounted, ref } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const API_BASE = `${API_BASE_URL}/api/ventes`
const API_VENTE_LIGNES = (venteId) => `${API_BASE_URL}/api/ventes/${venteId}/lignes`
const API_LIGNE = (ligneId) => `${API_BASE_URL}/api/ventes/lignes/${ligneId}`

const toast = useToast()

const ventes = ref([])
const loading = ref(false)
const error = ref('')

const expandedVenteIds = ref([])
const detailsByVenteId = ref({})
const loadingDetailsByVenteId = ref({})
const savingLigneIds = ref([])

const isExpanded = (venteId) => expandedVenteIds.value.includes(String(venteId))
const isSaving = (ligneId) => savingLigneIds.value.includes(String(ligneId))

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_BASE)
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    ventes.value = await res.json()
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

const loadDetails = async (venteId) => {
  const id = String(venteId)
  loadingDetailsByVenteId.value = { ...loadingDetailsByVenteId.value, [id]: true }
  try {
    const res = await fetch(API_VENTE_LIGNES(encodeURIComponent(id)))
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }
    const data = await res.json()
    const lignes = Array.isArray(data?.lignes) ? data.lignes : []
    detailsByVenteId.value = {
      ...detailsByVenteId.value,
      [id]: {
        montantTotal: data?.montantTotal ?? null,
        lignes: lignes.map((l) => ({ ...l, _quantiteEdit: l?.quantite })),
      },
    }
  } catch (e) {
    toast.error(e?.message ?? 'Erreur chargement détails')
  } finally {
    loadingDetailsByVenteId.value = { ...loadingDetailsByVenteId.value, [id]: false }
  }
}

const toggleExpand = async (venteId) => {
  const id = String(venteId)
  if (isExpanded(id)) {
    expandedVenteIds.value = expandedVenteIds.value.filter((x) => x !== id)
    return
  }
  expandedVenteIds.value = [...expandedVenteIds.value, id]
  if (!detailsByVenteId.value[id]) {
    await loadDetails(id)
  }
}

const updateLigne = async (venteId, ligne) => {
  const ligneId = ligne?.id
  if (ligneId == null) return

  const q = Number(ligne?._quantiteEdit)
  if (!Number.isFinite(q) || q <= 0) {
    toast.error('Quantité invalide')
    return
  }

  const key = String(ligneId)
  if (isSaving(key)) return
  savingLigneIds.value = [...savingLigneIds.value, key]

  try {
    const res = await fetch(API_LIGNE(encodeURIComponent(key)), {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ quantite: q }),
    })
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }

    const data = await res.json()
    const lignes = Array.isArray(data?.lignes) ? data.lignes : []
    detailsByVenteId.value = {
      ...detailsByVenteId.value,
      [String(venteId)]: {
        montantTotal: data?.montantTotal ?? null,
        lignes: lignes.map((l) => ({ ...l, _quantiteEdit: l?.quantite })),
      },
    }

    const idx = ventes.value.findIndex((v) => String(v?.id) === String(venteId))
    if (idx >= 0) {
      const copy = [...ventes.value]
      copy[idx] = { ...copy[idx], montantTotal: data?.montantTotal ?? copy[idx]?.montantTotal }
      ventes.value = copy
    }

    toast.success('Quantité mise à jour')
  } catch (e) {
    toast.error(e?.message ?? 'Erreur mise à jour')
  } finally {
    savingLigneIds.value = savingLigneIds.value.filter((x) => x !== key)
  }
}

const deleteLigne = async (venteId, ligneId) => {
  if (!confirm('Supprimer cette ligne ?')) return
  const key = String(ligneId)
  if (!key) return
  if (isSaving(key)) return

  savingLigneIds.value = [...savingLigneIds.value, key]
  try {
    const res = await fetch(API_LIGNE(encodeURIComponent(key)), { method: 'DELETE' })
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `HTTP ${res.status}`)
    }

    const data = await res.json()
    const lignes = Array.isArray(data?.lignes) ? data.lignes : []
    detailsByVenteId.value = {
      ...detailsByVenteId.value,
      [String(venteId)]: {
        montantTotal: data?.montantTotal ?? null,
        lignes: lignes.map((l) => ({ ...l, _quantiteEdit: l?.quantite })),
      },
    }

    const idx = ventes.value.findIndex((v) => String(v?.id) === String(venteId))
    if (idx >= 0) {
      const copy = [...ventes.value]
      copy[idx] = { ...copy[idx], montantTotal: data?.montantTotal ?? copy[idx]?.montantTotal }
      ventes.value = copy
    }

    toast.success('Ligne supprimée')
  } catch (e) {
    toast.error(e?.message ?? 'Erreur suppression')
  } finally {
    savingLigneIds.value = savingLigneIds.value.filter((x) => x !== key)
  }
}

const formatDateTime = (value) => {
  if (!value) return ''
  try {
    return new Date(value).toLocaleString()
  } catch {
    return String(value)
  }
}

onMounted(load)
</script>

<template>
  <div class="pagetitle">
    <h1>Ventes</h1>
    <nav>
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#">Home</a></li>
        <li class="breadcrumb-item">Vente</li>
        <li class="breadcrumb-item active">Liste</li>
      </ol>
    </nav>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Liste des ventes</h5>

            <div class="d-flex gap-2 mb-3">
              <RouterLink class="btn btn-primary" to="/ventes/new">Nouvelle vente</RouterLink>
              <button class="btn btn-outline-primary" type="button" @click="load">Rafraîchir</button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th style="width: 60px"></th>
                    <th>ID</th>
                    <th>Date</th>
                    <th>Montant total</th>
                    <th class="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <template v-for="v in ventes" :key="v.id">
                    <tr>
                      <td>
                        <button class="btn btn-sm btn-outline-secondary" type="button" @click="toggleExpand(v.id)">
                          {{ isExpanded(v.id) ? '-' : '+' }}
                        </button>
                      </td>
                      <td>{{ v.id }}</td>
                      <td>{{ formatDateTime(v.dateVente ?? v.date_vente ?? v.date) }}</td>
                      <td>{{ v.montantTotal ?? v.montant_total ?? v.total ?? 0 }}</td>
                      <td class="text-end">
                        <button class="btn btn-sm btn-outline-danger" type="button" @click="remove(v.id)">
                          Supprimer
                        </button>
                      </td>
                    </tr>
                    <tr v-if="isExpanded(v.id)">
                      <td colspan="5" class="bg-light">
                        <div v-if="loadingDetailsByVenteId[String(v.id)]" class="text-muted">Chargement détails...</div>
                        <div v-else>
                          <div class="fw-semibold mb-2">
                            Détails vente #{{ v.id }} — Total: {{ detailsByVenteId[String(v.id)]?.montantTotal ?? (v.montantTotal ?? 0) }}
                          </div>

                          <div class="table-responsive">
                            <table class="table table-sm table-bordered mb-0">
                              <thead>
                                <tr>
                                  <th>Produit</th>
                                  <th style="width: 140px" class="text-end">Prix unitaire</th>
                                  <th style="width: 140px">Quantité</th>
                                  <th style="width: 160px" class="text-end">Montant</th>
                                  <th style="width: 180px" class="text-end">Actions</th>
                                </tr>
                              </thead>
                              <tbody>
                                <tr v-for="l in (detailsByVenteId[String(v.id)]?.lignes ?? [])" :key="l.id">
                                  <td>{{ l?.produit?.nom ?? l?.produit?.id ?? '-' }}</td>
                                  <td class="text-end">{{ l?.prixUnitaire ?? 0 }}</td>
                                  <td>
                                    <input v-model="l._quantiteEdit" type="number" min="1" class="form-control form-control-sm" />
                                  </td>
                                  <td class="text-end">
                                    {{ (Number(l?.prixUnitaire ?? 0) * Number(l?._quantiteEdit ?? l?.quantite ?? 0)) || 0 }}
                                  </td>
                                  <td class="text-end">
                                    <button
                                      class="btn btn-sm btn-outline-primary me-2"
                                      type="button"
                                      :disabled="isSaving(l.id)"
                                      @click="updateLigne(v.id, l)"
                                    >
                                      Enregistrer
                                    </button>
                                    <button
                                      class="btn btn-sm btn-outline-danger"
                                      type="button"
                                      :disabled="isSaving(l.id)"
                                      @click="deleteLigne(v.id, l.id)"
                                    >
                                      Supprimer
                                    </button>
                                  </td>
                                </tr>
                                <tr v-if="(detailsByVenteId[String(v.id)]?.lignes ?? []).length === 0">
                                  <td colspan="5" class="text-center text-muted">Aucune ligne</td>
                                </tr>
                              </tbody>
                            </table>
                          </div>
                        </div>
                      </td>
                    </tr>
                  </template>

                  <tr v-if="ventes.length === 0">
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
