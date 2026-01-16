<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const router = useRouter()
const toast = useToast()

const API_PLACES = `${API_BASE_URL}/api/places`
const API_SALLES = `${API_BASE_URL}/api/salles`
const API_TYPE_PLACES = `${API_BASE_URL}/api/type-places`

const salles = ref([])
const typePlaces = ref([])

const loading = ref(false)
const error = ref('')

const mode = ref('single')

const form = ref({
  salleId: '',
  rangee: '',
  numero: '',
  numeroDebut: '',
  numeroFin: '',
  typePlaceId: '',
})

const canSubmit = computed(() => {
  if (!form.value.salleId || !form.value.rangee || !form.value.typePlaceId) return false
  if (mode.value === 'range') {
    return Boolean(form.value.numeroDebut && form.value.numeroFin)
  }
  return Boolean(form.value.numero)
})

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, tpRes] = await Promise.all([fetch(API_SALLES), fetch(API_TYPE_PLACES)])
    if (!sRes.ok) throw new Error(`Salles HTTP ${sRes.status}`)
    if (!tpRes.ok) throw new Error(`Type places HTTP ${tpRes.status}`)

    salles.value = await sRes.json()
    typePlaces.value = await tpRes.json()
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
    if (!form.value.salleId) throw new Error('Salle obligatoire')
    if (!form.value.typePlaceId) throw new Error('Type place obligatoire')
    if (!form.value.rangee) throw new Error('Rangée obligatoire')

    const basePayload = {
      salle: { id: Number(form.value.salleId) },
      rangee: form.value.rangee,
      typePlace: { id: Number(form.value.typePlaceId) },
    }

    if (mode.value === 'range') {
      const debut = Number(form.value.numeroDebut)
      const fin = Number(form.value.numeroFin)
      if (!Number.isFinite(debut) || !Number.isFinite(fin)) throw new Error('Numéros invalides')
      if (debut <= 0 || fin <= 0) throw new Error('Les numéros doivent être >= 1')
      if (debut > fin) throw new Error('Numéro début doit être <= numéro fin')

      let created = 0
      for (let n = debut; n <= fin; n++) {
        const payload = { ...basePayload, numero: n }
        const res = await fetch(API_PLACES, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        })
        if (!res.ok) throw new Error(`HTTP ${res.status}`)
        created += 1
      }

      toast.success(`${created} place(s) créée(s)`) 
    } else {
      const numero = Number(form.value.numero)
      if (!Number.isFinite(numero)) throw new Error('Numéro invalide')
      if (numero <= 0) throw new Error('Le numéro doit être >= 1')

      const payload = { ...basePayload, numero }

      const res = await fetch(API_PLACES, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      })

      if (!res.ok) throw new Error(`HTTP ${res.status}`)

      toast.success('Place créée')
    }

    await router.push('/places')
  } catch (e) {
    error.value = e?.message ?? "Erreur lors de l'enregistrement"
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadRefs)
</script>

<template>
  <div class="pagetitle">
    <h1>Places</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Nouvelle place</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">Mode d'insertion</label>
                <div class="d-flex gap-3">
                  <div class="form-check">
                    <input
                      id="mode-single"
                      v-model="mode"
                      class="form-check-input"
                      type="radio"
                      value="single"
                    />
                    <label class="form-check-label" for="mode-single">Une place</label>
                  </div>
                  <div class="form-check">
                    <input
                      id="mode-range"
                      v-model="mode"
                      class="form-check-input"
                      type="radio"
                      value="range"
                    />
                    <label class="form-check-label" for="mode-range">Multiple (intervalle)</label>
                  </div>
                </div>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Salle</label>
                <select v-model="form.salleId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="s in salles" :key="s.id" :value="String(s.id)">
                    {{ s.nom }} ({{ s.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Type place</label>
                <select v-model="form.typePlaceId" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="tp in typePlaces" :key="tp.id" :value="String(tp.id)">
                    {{ tp.libelle }} ({{ tp.id }})
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-6">
                <label class="form-label">Rangée</label>
                <input v-model="form.rangee" class="form-control" type="text" required />
              </div>

              <div v-if="mode === 'single'" class="col-12 col-md-6">
                <label class="form-label">Numéro</label>
                <input v-model="form.numero" class="form-control" type="number" min="1" required />
              </div>

              <template v-else>
                <div class="col-12 col-md-3">
                  <label class="form-label">Numéro début</label>
                  <input v-model="form.numeroDebut" class="form-control" type="number" min="1" required />
                </div>

                <div class="col-12 col-md-3">
                  <label class="form-label">Numéro fin</label>
                  <input v-model="form.numeroFin" class="form-control" type="number" min="1" required />
                </div>

                <div class="col-12 col-md-6 d-flex align-items-end">
                  <div class="text-muted">Ex: Rangée A, 1 à 10</div>
                </div>
              </template>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">
                  Enregistrer
                </button>
                <RouterLink class="btn btn-secondary" to="/places">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
