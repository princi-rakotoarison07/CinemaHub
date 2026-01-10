<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_TARIFS = `${API_BASE_URL}/api/tarifs`
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`
const API_CATEGORIES = `${API_BASE_URL}/api/categories-clients`

const loading = ref(false)
const saving = ref(false)
const error = ref('')

const tarifsOriginal = ref([])
const rows = ref([])

const typePlaces = ref([])
const categories = ref([])

const loadRefs = async () => {
  const [tpRes, catRes] = await Promise.all([fetch(API_TYPE_PLACES), fetch(API_CATEGORIES)])
  if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)
  if (!catRes.ok) throw new Error(`Catégories HTTP ${catRes.status}`)
  typePlaces.value = await tpRes.json()
  categories.value = await catRes.json()
}

const toRow = (t) => ({
  id: t.id,
  typePlaceId: t?.typePlace?.id != null ? String(t.typePlace.id) : '',
  categorieClientId: t?.categorieClient?.id != null ? String(t.categorieClient.id) : '',
  prix: t?.prix != null ? String(t.prix) : '',
  actif: t?.actif != null ? Boolean(t.actif) : true,
  dateDebut: t?.dateDebut ?? '',
  dateFin: t?.dateFin ?? '',
})

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    await loadRefs()
    const res = await fetch(API_TARIFS)
    if (!res.ok) throw new Error(`Tarifs HTTP ${res.status}`)
    const list = await res.json()

    tarifsOriginal.value = Array.isArray(list) ? list : []
    rows.value = tarifsOriginal.value.map(toRow)
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const originalRowById = computed(() => {
  const m = new Map()
  for (const t of tarifsOriginal.value ?? []) {
    if (t?.id != null) m.set(String(t.id), toRow(t))
  }
  return m
})

const isDirty = (row) => {
  if (!row?.id) return false
  const orig = originalRowById.value.get(String(row.id))
  if (!orig) return false
  return (
    String(row.typePlaceId ?? '') !== String(orig.typePlaceId ?? '') ||
    String(row.categorieClientId ?? '') !== String(orig.categorieClientId ?? '') ||
    String(row.prix ?? '') !== String(orig.prix ?? '') ||
    Boolean(row.actif) !== Boolean(orig.actif) ||
    String(row.dateDebut ?? '') !== String(orig.dateDebut ?? '') ||
    String(row.dateFin ?? '') !== String(orig.dateFin ?? '')
  )
}

const dirtyRows = computed(() => rows.value.filter(isDirty))

const cancel = () => {
  rows.value = tarifsOriginal.value.map(toRow)
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

    for (const r of toSave) {
      if (!r.typePlaceId || !r.categorieClientId || r.prix === '') {
        throw new Error(`Champs obligatoires manquants (tarif ${r.id})`)
      }

      const payload = {
        id: Number(r.id),
        typePlace: { id: Number(r.typePlaceId) },
        categorieClient: { id: Number(r.categorieClientId) },
        prix: r.prix,
        actif: Boolean(r.actif),
        dateDebut: r.dateDebut || null,
        dateFin: r.dateFin || null,
      }

      const res = await fetch(`${API_TARIFS}/${r.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })

      if (!res.ok) throw new Error(`Tarif ${r.id} HTTP ${res.status}`)
    }

    toast.success('Grille mise à jour')
    await load()
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    saving.value = false
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
            <h5 class="card-title">Grille tarifaire (édition multiple)</h5>

            <div class="d-flex gap-2 mb-3">
              <button class="btn btn-outline-primary" type="button" :disabled="loading || saving" @click="load">
                Rafraîchir
              </button>
            </div>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else class="table-responsive">
              <table class="table table-striped align-middle">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Type place</th>
                    <th>Catégorie client</th>
                    <th>Prix</th>
                    <th>Actif</th>
                    <th>Date début</th>
                    <th>Date fin</th>
                    <th>Modifié</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in rows" :key="r.id">
                    <td>{{ r.id }}</td>

                    <td style="min-width: 200px">
                      <select v-model="r.typePlaceId" class="form-select form-select-sm">
                        <option value="" disabled>Sélectionner...</option>
                        <option v-for="tp in typePlaces" :key="tp.id" :value="String(tp.id)">
                          {{ tp.libelle }}
                        </option>
                      </select>
                    </td>

                    <td style="min-width: 220px">
                      <select v-model="r.categorieClientId" class="form-select form-select-sm">
                        <option value="" disabled>Sélectionner...</option>
                        <option v-for="c in categories" :key="c.id" :value="String(c.id)">
                          {{ c.libelle }}
                        </option>
                      </select>
                    </td>

                    <td style="min-width: 140px">
                      <input v-model="r.prix" class="form-control form-control-sm" type="number" step="0.01" min="0" />
                    </td>

                    <td>
                      <select v-model="r.actif" class="form-select form-select-sm" style="min-width: 90px">
                        <option :value="true">Oui</option>
                        <option :value="false">Non</option>
                      </select>
                    </td>

                    <td style="min-width: 150px">
                      <input v-model="r.dateDebut" class="form-control form-control-sm" type="date" />
                    </td>

                    <td style="min-width: 150px">
                      <input v-model="r.dateFin" class="form-control form-control-sm" type="date" />
                    </td>

                    <td>
                      <span v-if="isDirty(r)" class="badge bg-warning text-dark">Oui</span>
                      <span v-else class="text-muted">Non</span>
                    </td>
                  </tr>

                  <tr v-if="rows.length === 0">
                    <td colspan="8" class="text-center text-muted">Aucun élément</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="d-flex gap-2 mt-3">
              <button class="btn btn-primary" type="button" :disabled="loading || saving" @click="save">
                Enregistrer
              </button>
              <button class="btn btn-secondary" type="button" :disabled="loading || saving" @click="cancel">
                Annuler
              </button>
              <button class="btn btn-outline-secondary" type="button" :disabled="loading || saving" @click="router.push('/tarifs')">
                Retour liste
              </button>
              <div class="text-muted align-self-center ms-2">
                Modifiés: {{ dirtyRows.length }}
              </div>
            </div>

            <div v-if="saving" class="text-muted mt-2">Enregistrement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
