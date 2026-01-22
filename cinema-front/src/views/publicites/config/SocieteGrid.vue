<script setup>
import { computed, onMounted, ref } from 'vue'
import { useToast } from '../../../composables/useToast'
import { API_BASE_URL } from '../../../config/api'

const toast = useToast()

const API_SOCIETES = `${API_BASE_URL}/api/societes`

const loading = ref(false)
const saving = ref(false)
const error = ref('')

const original = ref([])
const rows = ref([])
const newRowCounter = ref(0)

const toRow = (s) => ({
  id: s?.id,
  nom: s?.nom ?? '',
  contact: s?.contact ?? '',
  email: s?.email ?? '',
  telephone: s?.telephone ?? '',
  isNew: false,
})

const createNewRow = () => {
  newRowCounter.value += 1
  return {
    id: `new-${newRowCounter.value}`,
    nom: '',
    contact: '',
    email: '',
    telephone: '',
    isNew: true,
  }
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_SOCIETES)
    if (!res.ok) throw new Error(`Sociétés HTTP ${res.status}`)
    const list = await res.json()

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
  for (const s of original.value ?? []) {
    if (s?.id != null) m.set(String(s.id), toRow(s))
  }
  return m
})

const isDirty = (r) => {
  if (!r?.id) return false
  if (r.isNew) return true
  const orig = originalRowById.value.get(String(r.id))
  if (!orig) return false
  return (
    String(r.nom ?? '') !== String(orig.nom ?? '') ||
    String(r.contact ?? '') !== String(orig.contact ?? '') ||
    String(r.email ?? '') !== String(orig.email ?? '') ||
    String(r.telephone ?? '') !== String(orig.telephone ?? '')
  )
}

const dirtyRows = computed(() => rows.value.filter(isDirty))

const addRow = () => {
  rows.value = [...rows.value, createNewRow()]
}

const removeRow = async (row) => {
  const id = String(row?.id ?? '')
  if (!id) return

  if (String(id).startsWith('new-')) {
    rows.value = rows.value.filter((x) => String(x.id) !== String(id))
    return
  }

  try {
    const res = await fetch(`${API_SOCIETES}/${encodeURIComponent(id)}`, { method: 'DELETE' })
    if (!res.ok) {
      const msg = await res.text().catch(() => '')
      throw new Error(msg || `Suppression HTTP ${res.status}`)
    }
    toast.success('Société supprimée')
    await load()
  } catch (e) {
    toast.error(e?.message ?? 'Erreur suppression')
  }
}

const cancel = () => {
  rows.value = original.value.map(toRow)
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
      if (!String(r.nom ?? '').trim()) throw new Error(`Nom obligatoire (ligne ${r.id})`)

      const payload = {
        nom: String(r.nom ?? '').trim(),
        contact: r.contact || null,
        email: r.email || null,
        telephone: r.telephone || null,
      }

      const isNew = Boolean(r.isNew)
      const url = isNew ? API_SOCIETES : `${API_SOCIETES}/${r.id}`
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
            <h5 class="card-title">Configuration - Sociétés</h5>

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
                      <th>Nom</th>
                      <th>Contact</th>
                      <th>Email</th>
                      <th>Téléphone</th>
                      <th class="text-end">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="r in rows" :key="String(r.id)">
                      <td>{{ r.isNew ? '' : `#${r.id}` }}</td>
                      <td><input v-model="r.nom" class="form-control form-control-sm" type="text" /></td>
                      <td><input v-model="r.contact" class="form-control form-control-sm" type="text" /></td>
                      <td><input v-model="r.email" class="form-control form-control-sm" type="text" /></td>
                      <td><input v-model="r.telephone" class="form-control form-control-sm" type="text" /></td>
                      <td class="text-end">
                        <button class="btn btn-sm btn-outline-danger" type="button" @click="removeRow(r)">
                          Supprimer
                        </button>
                      </td>
                    </tr>
                    <tr v-if="rows.length === 0">
                      <td colspan="6" class="text-center text-muted">Aucune société</td>
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
