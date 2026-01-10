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

const newRowCounter = ref(0)

const typePlaces = ref([])
const categories = ref([])

const typePlaceById = computed(() => {
  const m = new Map()
  for (const tp of typePlaces.value ?? []) {
    if (tp?.id != null) m.set(String(tp.id), tp)
  }
  return m
})

const categorieById = computed(() => {
  const m = new Map()
  for (const c of categories.value ?? []) {
    if (c?.id != null) m.set(String(c.id), c)
  }
  return m
})

const getTypePlaceLabel = (typePlaceId) => {
  if (!typePlaceId) return ''
  const tp = typePlaceById.value.get(String(typePlaceId))
  return tp?.libelle ?? String(typePlaceId)
}

const getCategorieLabel = (categorieClientId) => {
  if (!categorieClientId) return ''
  const c = categorieById.value.get(String(categorieClientId))
  return c?.libelle ?? String(categorieClientId)
}

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
  isNew: false,
})

const createNewRow = () => {
  newRowCounter.value += 1
  return {
    id: `new-${newRowCounter.value}`,
    typePlaceId: '',
    categorieClientId: '',
    prix: '',
    actif: true,
    dateDebut: '',
    dateFin: '',
    isNew: true,
  }
}

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
    newRowCounter.value = 0
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
  if (row.isNew) return true
  const orig = originalRowById.value.get(String(row.id))
  if (!orig) return false
  return (
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

const addRow = () => {
  rows.value = [...rows.value, createNewRow()]
}

const removeRow = (rowId) => {
  rows.value = rows.value.filter((r) => String(r.id) !== String(rowId))
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
        typePlace: { id: Number(r.typePlaceId) },
        categorieClient: { id: Number(r.categorieClientId) },
        prix: r.prix,
        actif: Boolean(r.actif),
        dateDebut: r.dateDebut || null,
        dateFin: r.dateFin || null,
      }

      const isNew = Boolean(r.isNew)
      const url = isNew ? API_TARIFS : `${API_TARIFS}/${r.id}`
      const method = isNew ? 'POST' : 'PUT'

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(isNew ? payload : { id: Number(r.id), ...payload }),
      })

      if (!res.ok) throw new Error(`${isNew ? 'Création' : 'Tarif'} ${r.id} HTTP ${res.status}`)
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
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in rows" :key="r.id">
                    <td>{{ r.id }}</td>

                    <td style="min-width: 200px">
                      <template v-if="r.isNew">
                        <select v-model="r.typePlaceId" class="form-select form-select-sm">
                          <option value="" disabled>Sélectionner...</option>
                          <option v-for="tp in typePlaces" :key="tp.id" :value="String(tp.id)">
                            {{ tp.libelle }}
                          </option>
                        </select>
                      </template>
                      <template v-else>
                        {{ getTypePlaceLabel(r.typePlaceId) }}
                      </template>
                    </td>

                    <td style="min-width: 220px">
                      <template v-if="r.isNew">
                        <select v-model="r.categorieClientId" class="form-select form-select-sm">
                          <option value="" disabled>Sélectionner...</option>
                          <option v-for="c in categories" :key="c.id" :value="String(c.id)">
                            {{ c.libelle }}
                          </option>
                        </select>
                      </template>
                      <template v-else>
                        {{ getCategorieLabel(r.categorieClientId) }}
                      </template>
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

                    <td class="text-end">
                      <button
                        v-if="r.isNew"
                        class="btn btn-sm btn-outline-danger"
                        type="button"
                        :disabled="loading || saving"
                        @click="removeRow(r.id)"
                      >
                        Supprimer
                      </button>
                    </td>
                  </tr>

                  <tr v-if="rows.length === 0">
                    <td colspan="9" class="text-center text-muted">Aucun élément</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <div class="d-flex gap-2 mt-3">
              <button class="btn btn-outline-primary" type="button" :disabled="loading || saving" @click="addRow">
                Ajouter ligne
              </button>
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
