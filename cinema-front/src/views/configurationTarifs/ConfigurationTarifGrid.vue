<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_CONFIG_TARIFS = `${API_BASE_URL}/api/configuration-tarifs`
const API_TARIFS = `${API_BASE_URL}/api/tarifs`

const loading = ref(false)
const saving = ref(false)
const error = ref('')

const configsOriginal = ref([])
const rows = ref([])

const tarifs = ref([])
const newRowCounter = ref(0)

const tarifById = computed(() => {
  const m = new Map()
  for (const t of tarifs.value ?? []) {
    if (t?.id != null) m.set(String(t.id), t)
  }
  return m
})

const tarifLabelById = (tarifId) => {
  const t = tarifById.value.get(String(tarifId ?? ''))
  if (!t) return tarifId ? `Tarif ${tarifId}` : ''
  const tp = t?.typePlace?.libelle ?? t?.typePlace?.id ?? ''
  const cc = t?.categorieClient?.libelle ?? t?.categorieClient?.id ?? ''
  const prix = t?.prix != null ? String(t.prix) : ''
  return `${tp} - ${cc} (${prix})`
}

const loadRefs = async () => {
  const res = await fetch(API_TARIFS)
  if (!res.ok) throw new Error(`Tarifs HTTP ${res.status}`)
  const list = await res.json()
  tarifs.value = Array.isArray(list) ? list : []
}

const toRow = (c) => ({
  id: c.id,
  tarif1Id: c?.tarif1?.id != null ? String(c.tarif1.id) : '',
  tarif2Id: c?.tarif2?.id != null ? String(c.tarif2.id) : '',
  pourcentage: c?.pourcentage != null ? String(c.pourcentage) : '',
  actif: c?.actif != null ? Boolean(c.actif) : true,
  isNew: false,
})

const createNewRow = () => {
  newRowCounter.value += 1
  return {
    id: `new-${newRowCounter.value}`,
    tarif1Id: '',
    tarif2Id: '',
    pourcentage: '',
    actif: true,
    isNew: true,
  }
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    await loadRefs()
    const res = await fetch(API_CONFIG_TARIFS)
    if (!res.ok) throw new Error(`Configuration tarifs HTTP ${res.status}`)
    const list = await res.json()
    configsOriginal.value = Array.isArray(list) ? list : []
    rows.value = [...configsOriginal.value]
      .sort((a, b) => {
        const ai = a?.id != null ? Number(a.id) : Number.POSITIVE_INFINITY
        const bi = b?.id != null ? Number(b.id) : Number.POSITIVE_INFINITY
        return ai - bi
      })
      .map(toRow)
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
  for (const c of configsOriginal.value ?? []) {
    if (c?.id != null) m.set(String(c.id), toRow(c))
  }
  return m
})

const isDirty = (row) => {
  if (!row?.id) return false
  if (row.isNew) return true
  const orig = originalRowById.value.get(String(row.id))
  if (!orig) return false
  return String(row.pourcentage ?? '') !== String(orig.pourcentage ?? '') || Boolean(row.actif) !== Boolean(orig.actif)
}

const dirtyRows = computed(() => rows.value.filter(isDirty))

const cancel = () => {
  rows.value = configsOriginal.value.map(toRow)
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
      if ((!r.tarif1Id || !r.tarif2Id) && r.isNew) {
        throw new Error(`Champs obligatoires manquants (configuration ${r.id})`)
      }
      if (r.pourcentage === '') throw new Error(`Pourcentage manquant (configuration ${r.id})`)

      const payload = {
        tarif1: { id: Number(r.tarif1Id) },
        tarif2: { id: Number(r.tarif2Id) },
        pourcentage: r.pourcentage,
        actif: Boolean(r.actif),
      }

      const isNew = Boolean(r.isNew)
      const url = isNew ? API_CONFIG_TARIFS : `${API_CONFIG_TARIFS}/${r.id}`
      const method = isNew ? 'POST' : 'PUT'

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(isNew ? payload : { id: Number(r.id), ...payload }),
      })

      if (!res.ok) throw new Error(`${isNew ? 'Création' : 'Configuration'} ${r.id} HTTP ${res.status}`)
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
    <h1>Configuration tarifs</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Grille configuration tarifs (édition multiple)</h5>

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
                    <th>Tarif 1 (base)</th>
                    <th>Tarif 2 (cible)</th>
                    <th>Pourcentage</th>
                    <th>Actif</th>
                    <th>Modifié</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in rows" :key="r.id">
                    <td>{{ r.id }}</td>

                    <td style="min-width: 260px">
                      <template v-if="r.isNew">
                        <select v-model="r.tarif1Id" class="form-select form-select-sm">
                          <option value="" disabled>Sélectionner...</option>
                          <option v-for="t in tarifs" :key="t.id" :value="String(t.id)">
                            {{ tarifLabelById(t.id) }}
                          </option>
                        </select>
                      </template>
                      <template v-else>
                        {{ tarifLabelById(r.tarif1Id) }}
                      </template>
                    </td>

                    <td style="min-width: 260px">
                      <template v-if="r.isNew">
                        <select v-model="r.tarif2Id" class="form-select form-select-sm">
                          <option value="" disabled>Sélectionner...</option>
                          <option v-for="t in tarifs" :key="t.id" :value="String(t.id)">
                            {{ tarifLabelById(t.id) }}
                          </option>
                        </select>
                      </template>
                      <template v-else>
                        {{ tarifLabelById(r.tarif2Id) }}
                      </template>
                    </td>

                    <td style="min-width: 140px">
                      <input v-model="r.pourcentage" class="form-control form-control-sm" type="number" step="0.01" min="0" max="100" />
                    </td>

                    <td>
                      <select v-model="r.actif" class="form-select form-select-sm" style="min-width: 90px">
                        <option :value="true">Oui</option>
                        <option :value="false">Non</option>
                      </select>
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
                    <td colspan="7" class="text-center text-muted">Aucun élément</td>
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
              <button
                class="btn btn-outline-secondary"
                type="button"
                :disabled="loading || saving"
                @click="router.push('/configuration-tarifs/new')"
              >
                Nouveau
              </button>
              <div class="text-muted align-self-center ms-2">Modifiés: {{ dirtyRows.length }}</div>
            </div>

            <div v-if="saving" class="text-muted mt-2">Enregistrement...</div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
