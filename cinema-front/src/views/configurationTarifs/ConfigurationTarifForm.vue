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
const error = ref('')

const tarifs = ref([])

const form = ref({
  tarif1Id: '',
  tarif2Id: '',
  pourcentage: '50',
  actif: true,
})

const canSubmit = computed(() => Boolean(form.value.tarif1Id && form.value.tarif2Id && form.value.pourcentage))

const tarifLabel = (t) => {
  if (!t) return ''
  const tp = t?.typePlace?.libelle ?? t?.typePlace?.id ?? ''
  const cc = t?.categorieClient?.libelle ?? t?.categorieClient?.id ?? ''
  const prix = t?.prix != null ? String(t.prix) : ''
  return `${tp} - ${cc} (${prix})`
}

const loadRefs = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await fetch(API_TARIFS)
    if (!res.ok) throw new Error(`Tarifs HTTP ${res.status}`)
    const list = await res.json()
    tarifs.value = Array.isArray(list) ? list : []
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
    const payload = {
      tarif1: { id: Number(form.value.tarif1Id) },
      tarif2: { id: Number(form.value.tarif2Id) },
      pourcentage: form.value.pourcentage,
      actif: Boolean(form.value.actif),
    }

    const res = await fetch(API_CONFIG_TARIFS, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (!res.ok) throw new Error(`HTTP ${res.status}`)

    toast.success('Configuration tarif créée')
    await router.push('/configuration-tarifs/grille')
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
    <h1>Configuration tarifs</h1>
  </div>

  <section class="section">
    <div class="row">
      <div class="col-12 col-lg-8">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Nouvelle configuration tarif</h5>

            <div v-if="error" class="alert alert-danger">{{ error }}</div>

            <form class="row g-3" @submit.prevent="submit">
              <div class="col-12">
                <label class="form-label">Tarif 1 (base)</label>
                <select v-model="form.tarif1Id" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="t in tarifs" :key="t.id" :value="String(t.id)">
                    {{ tarifLabel(t) }}
                  </option>
                </select>
              </div>

              <div class="col-12">
                <label class="form-label">Tarif 2 (cible)</label>
                <select v-model="form.tarif2Id" class="form-select" required>
                  <option value="" disabled>Sélectionner...</option>
                  <option v-for="t in tarifs" :key="t.id" :value="String(t.id)">
                    {{ tarifLabel(t) }}
                  </option>
                </select>
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Pourcentage (%)</label>
                <input v-model="form.pourcentage" class="form-control" type="number" step="0.01" min="0" max="100" required />
              </div>

              <div class="col-12 col-md-4">
                <label class="form-label">Actif</label>
                <select v-model="form.actif" class="form-select">
                  <option :value="true">Oui</option>
                  <option :value="false">Non</option>
                </select>
              </div>

              <div class="col-12 d-flex gap-2">
                <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">
                  Enregistrer
                </button>
                <RouterLink class="btn btn-secondary" to="/configuration-tarifs/grille">Annuler</RouterLink>
              </div>

              <div v-if="loading" class="text-muted">Chargement...</div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
