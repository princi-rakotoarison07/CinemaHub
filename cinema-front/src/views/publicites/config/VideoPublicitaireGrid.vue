<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../../composables/useToast'
import { API_BASE_URL } from '../../../config/api'

const toast = useToast()

const API_SOCIETES = `${API_BASE_URL}/api/societes`
const API_VIDEOS = `${API_BASE_URL}/api/videos-publicitaires`

const loading = ref(false)
const saving = ref(false)
const error = ref('')

const societes = ref([])
const original = ref([])
const rows = ref([])
const newRowCounter = ref(0)

const toRow = (v) => ({
  id: v?.id,
  societeId: v?.societe?.id != null ? String(v.societe.id) : '',
  titre: v?.titre ?? '',
  dureeSecondes: v?.dureeSecondes != null ? String(v.dureeSecondes) : '',
  dateCreation: v?.dateCreation ?? '',
  isNew: false,
})

const createNewRow = () => {
  newRowCounter.value += 1
  return {
    id: `new-${newRowCounter.value}`,
    societeId: '',
    titre: '',
    dureeSecondes: '',
    dateCreation: '',
    isNew: true,
  }
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, vRes] = await Promise.all([fetch(API_SOCIETES), fetch(API_VIDEOS)])
    if (!sRes.ok) throw new Error(`Sociétés HTTP ${sRes.status}`)
    if (!vRes.ok) throw new Error(`Vidéos pub HTTP ${vRes.status}`)

    societes.value = await sRes.json()

    const list = await vRes.json()
    original.value = Array.isArray(list) ? list : []
    rows.value = [...original.value]
      .sort((a, b) => (Number(a?.id ?? 0) || 0) - (Number(b?.id ?? 0) || 0))
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
  for (const v of original.value ?? []) {
    if (v?.id != null) m.set(String(v.id), toRow(v))
  }
  return m
})

const isDirty = (r) => {
  if (!r?.id) return false
  if (r.isNew) return true
  const orig = originalRowById.value.get(String(r.id))
  if (!orig) return false
  return (
    String(r.societeId ?? '') !== String(orig.societeId ?? '') ||
    String(r.titre ?? '') !== String(orig.titre ?? '') ||
    String(r.dureeSecondes ?? '') !== String(orig.dureeSecondes ?? '') ||
    String(r.dateCreation ?? '') !== String(orig.dateCreation ?? '')
  )
}

const dirtyRows = computed(() => rows.value.filter(isDirty))

const addRow = () => {
  rows.value = [...rows.value, createNewRow()]
}

const cancel = () => {
  rows.value = original.value.map(toRow)
}

const removeRow = async (row) => {
  const id = String(row?.id ?? '')
  if (!id) return

  if (String(id).startsWith('new-')) {
    rows.value = rows.value.filter((x) => String(x.id) !== String(id))
    return
  }

  try {
    const res = await fetch(`${API_VIDEOS}/${encodeURIComponent(id)}`, { method: 'DELETE' })
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `Suppression HTTP ${res.status}`)
    }
    toast.success('Vidéo supprimée')
    await load()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur suppression')
  }
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
      if (!r.societeId) throw new Error(`Société obligatoire (ligne ${r.id})`)
      if (!String(r.titre ?? '').trim()) throw new Error(`Titre obligatoire (ligne ${r.id})`)
      const duree = Number(r.dureeSecondes)
      if (!Number.isFinite(duree) || duree <= 0) throw new Error(`Durée invalide (ligne ${r.id})`)

      const payload = {
        societe: { id: Number(r.societeId) },
        titre: String(r.titre ?? '').trim(),
        dureeSecondes: duree,
        dateCreation: r.dateCreation || null,
      }

      const isNew = Boolean(r.isNew)
      const url = isNew ? API_VIDEOS : `${API_VIDEOS}/${r.id}`
      const method = isNew ? 'POST' : 'PUT'

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(isNew ? payload : { id: Number(r.id), ...payload }),
      })

      if (!res.ok) {
        const msg = await res.text().catch(() => '')
        throw new Error(msg || `${isNew ? 'Création' : 'Mise à jour'} HTTP ${res.status}`)
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

onMounted(load)
</script>

<template>
  <div class="pagetitle">
    <h1>Publicité</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Configuration - Vidéos publicitaires</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>
            <div v-else-if="loading" class="text-muted">Chargement...</div>

            <div v-else>
              <div class="d-flex gap-2 mb-3">
                <button class="btn btn-outline-primary" type="button" @click="addRow">Ajouter</button>
                <button class="btn btn-primary" type="button" :disabled="saving" @click="save">Enregistrer</button>
                <button class="btn btn-secondary" type="button" :disabled="saving" @click="cancel">Annuler</button>
              </div>

              <div class="table-responsive shadow-sm rounded border">
                <table class="table table-striped table-hover align-middle mb-0">
                  <thead class="bg-white">
                    <tr>
                      <th>#</th>
                      <th>Société</th>
                      <th>Titre</th>
                      <th>Durée (s)</th>
                      <th>Date création</th>
                      <th class="text-end">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in rows" :key="String(r.id)">
                      <td>{{ r.isNew ? '' : `#${r.id}` }}</td>
                      <td>
                        <select v-model="r.societeId" class="form-select form-select-sm">
                          <option value="" disabled>Sélectionner...</option>
                          <option v-for="s in societes" :key="s.id" :value="String(s.id)">
                            {{ s.nom }} ({{ s.id }})
                          </option>
                        </select>
                      </td>
                      <td><input v-model="r.titre" class="form-control form-control-sm" type="text" /></td>
                      <td>
                        <input v-model="r.dureeSecondes" class="form-control form-control-sm" type="number" min="1" step="1" />
                      </td>
                      <td><input v-model="r.dateCreation" class="form-control form-control-sm" type="date" /></td>
                      <td class="text-end">
                        <button class="btn btn-sm btn-outline-danger" type="button" @click="removeRow(r)">
                          Supprimer
                        </button>
                      </td>
                    </tr>
                    <tr v-if="rows.length === 0">
                      <td colspan="6" class="text-center text-muted">Aucune vidéo</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="text-muted small mt-2">Modifications: {{ dirtyRows.length }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
