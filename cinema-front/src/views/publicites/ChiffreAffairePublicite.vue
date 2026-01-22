<template>
  <div class="publicite-container">
    <!-- Header -->
    <div class="header">
      <h1>Publicité</h1>
      <p class="subtitle">Chiffre d'affaires et suivi des paiements</p>
    </div>

    <!-- Error Message -->
    <div v-if="error" class="error-card">
      <div class="error-icon">!</div>
      <div class="error-message">
        <strong>Erreur :</strong> {{ error }}
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-card">
      <div class="filter-header">
        <h3>Filtres</h3>
      </div>
      
      <div class="filters">
        <div class="filter-item">
          <label>Société</label>
          <select v-model="filters.societeId" class="filter-select">
            <option value="ALL">Toutes</option>
            <option v-for="s in societes" :key="s.id" :value="String(s.id)">
              {{ s.nom }}
            </option>
          </select>
        </div>
        
        <div class="filter-item">
          <label>Mois</label>
          <div class="month-input">
            <input v-model="filters.mois" type="month" class="filter-input">
            <button v-if="filters.mois" @click="resetMonthOnly" class="clear-btn" title="Effacer">
              ×
            </button>
          </div>
        </div>
        
        <div class="filter-actions">
          <button @click="refreshData" class="btn-refresh" :disabled="loading">
            {{ loading ? 'Chargement...' : 'Actualiser' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading">
      <div class="loader"></div>
      <p>Chargement des données...</p>
    </div>

    <!-- Main Content -->
    <div v-else class="main-content">
      <!-- Summary Cards -->
      <div class="summary-cards">
        <div class="summary-card">
          <div class="summary-icon">
            <svg viewBox="0 0 24 24">
              <path d="M11.8 10.9c-2.27-.59-3-1.2-3-2.15 0-1.09 1.01-1.85 2.7-1.85 1.78 0 2.44.85 2.5 2.1h2.21c-.07-1.72-1.12-3.3-3.21-3.81V3h-3v2.16c-1.94.42-3.5 1.68-3.5 3.61 0 2.31 1.91 3.46 4.7 4.13 2.5.6 3 1.48 3 2.41 0 .69-.49 1.79-2.7 1.79-2.06 0-2.87-.92-2.98-2.1h-2.2c.12 2.19 1.76 3.42 3.68 3.83V21h3v-2.15c1.95-.37 3.5-1.5 3.5-3.55 0-2.84-2.43-3.81-4.7-4.4z"/>
            </svg>
          </div>
          <div class="summary-content">
            <div class="summary-label">Chiffre d'affaires</div>
            <div class="summary-value">{{ formatCurrency(chiffreAffaire) }}</div>
            <div class="summary-info" v-if="filters.mois">
              Mois : {{ formatMonth(filters.mois) }}
            </div>
          </div>
        </div>
        
        <div class="summary-card">
          <div class="summary-icon">
            <svg viewBox="0 0 24 24">
              <path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/>
            </svg>
          </div>
          <div class="summary-content">
            <div class="summary-label">Montants restants</div>
            <div class="summary-value" :class="{ 'zero': totalReste === 0 }">
              {{ formatCurrency(totalReste) }}
            </div>
            <div class="summary-info">{{ rowsFiltered.length }} contrat(s)</div>
          </div>
        </div>
      </div>

      <!-- Table -->
      <div class="table-section">
        <div class="table-header">
          <h3>Contrats publicitaires</h3>
          <div class="table-info">
            <span>{{ rowsFiltered.length }} résultat(s)</span>
          </div>
        </div>
        
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>Contrat</th>
                <th>Société</th>
                <th>Période</th>
                <th class="text-right">Total</th>
                <th class="text-right">Payé</th>
                <th class="text-right">Reste</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in rowsFiltered" :key="String(r.contrat?.id)" 
                  class="table-row" @click="toggleRowDetails(r)">
                <td>
                  <div class="contract-info">
                    <div class="contract-id">#{{ r.contrat?.id }}</div>
                    <div class="contract-title">{{ r.contrat?.videoPublicitaire?.titre || '-' }}</div>
                  </div>
                </td>
                <td>
                  <div class="societe-info">
                    {{ r.contrat?.videoPublicitaire?.societe?.nom || '-' }}
                  </div>
                </td>
                <td>
                  <div class="period-info">
                    <div>{{ formatDate(r.contrat?.dateDebut) }}</div>
                    <div class="period-to">→ {{ formatDate(r.contrat?.dateFin) }}</div>
                  </div>
                </td>
                <td class="text-right amount">
                  {{ formatCurrency(r.total) }}
                </td>
                <td class="text-right amount">
                  <div class="paid-amount">
                    {{ formatCurrency(r.paid) }}
                    <div class="progress-container">
                      <div class="progress-bar">
                        <div class="progress-fill" :style="{ width: `${(r.paid / r.total) * 100}%` }"></div>
                      </div>
                    </div>
                  </div>
                </td>
                <td class="text-right">
                  <div class="reste-amount" :class="{ 'fully-paid': r.reste === 0 }">
                    {{ formatCurrency(r.reste) }}
                  </div>
                </td>
              </tr>
              
              <!-- Empty State -->
              <tr v-if="rowsFiltered.length === 0">
                <td colspan="6" class="empty-state">
                  <div class="empty-content">
                    Aucun contrat trouvé
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- Summary -->
        <div v-if="rowsFiltered.length > 0" class="table-footer">
          <div class="footer-content">
            <div class="footer-label">Totaux généraux</div>
            <div class="footer-values">
              <div class="footer-value">
                <span>CA total :</span>
                <strong>{{ formatCurrency(chiffreAffaire) }}</strong>
              </div>
              <div class="footer-value">
                <span>Reste total :</span>
                <strong :class="{ 'zero': totalReste === 0 }">{{ formatCurrency(totalReste) }}</strong>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useToast } from '../../composables/useToast'
import { API_BASE_URL } from '../../config/api'

const toast = useToast()

const API_SOCIETES = `${API_BASE_URL}/api/societes`
const API_CONTRATS_PUBLICITE = `${API_BASE_URL}/api/contrats-publicite`
const API_PAIEMENTS = `${API_BASE_URL}/api/paiements-contrats-publicite`

const societes = ref([])
const contrats = ref([])
const paiements = ref([])

const loading = ref(false)
const error = ref('')

const filters = ref({
  societeId: 'ALL',
  mois: '', // YYYY-MM
})

const monthRangeFromKey = (mk) => {
  const key = String(mk ?? '')
  if (!key) return null
  const [yyyyStr, mmStr] = key.split('-')
  const yyyy = Number(yyyyStr)
  const mm = Number(mmStr)
  if (!Number.isFinite(yyyy) || !Number.isFinite(mm) || mm < 1 || mm > 12) return null

  const start = new Date(Date.UTC(yyyy, mm - 1, 1, 0, 0, 0, 0))
  const end = new Date(Date.UTC(yyyy, mm, 0, 23, 59, 59, 999))
  return { start, end }
}

const parseLocalDateAsUTC = (d) => {
  const s = String(d ?? '')
  if (!s) return null
  const dt = new Date(`${s}T00:00:00Z`)
  return Number.isNaN(dt.getTime()) ? null : dt
}

const loadData = async () => {
  loading.value = true
  error.value = ''
  try {
    const [sRes, cRes, pRes] = await Promise.all([
      fetch(API_SOCIETES),
      fetch(API_CONTRATS_PUBLICITE),
      fetch(API_PAIEMENTS),
    ])

    if (!sRes.ok) throw new Error(`Sociétés HTTP ${sRes.status}`)
    if (!cRes.ok) throw new Error(`Contrats pub HTTP ${cRes.status}`)
    if (!pRes.ok) throw new Error(`Paiements pub HTTP ${pRes.status}`)

    societes.value = await sRes.json()
    contrats.value = await cRes.json()
    paiements.value = await pRes.json()
  } catch (e) {
    error.value = e?.message ?? 'Erreur lors du chargement'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

const refreshData = () => {
  loadData()
}

const paiementsByContratId = computed(() => {
  const map = new Map()
  for (const p of paiements.value ?? []) {
    const cid = String(p?.contratPublicite?.id ?? '')
    if (!cid) continue
    if (!map.has(cid)) map.set(cid, [])
    map.get(cid).push(p)
  }

  for (const arr of map.values()) {
    arr.sort((a, b) => String(a?.datePaiement ?? '').localeCompare(String(b?.datePaiement ?? '')))
  }

  return map
})

const rowsAll = computed(() => {
  return (contrats.value ?? []).map((c) => {
    const cid = String(c?.id ?? '')
    const list = paiementsByContratId.value.get(cid) ?? []

    const total = Number(c?.montantTotal ?? 0) || 0
    const paid = list.reduce((sum, p) => sum + (Number(p?.montant ?? 0) || 0), 0)
    const reste = Math.max(0, total - paid)

    return {
      contrat: c,
      total,
      paid,
      reste,
    }
  })
})

const rowsFiltered = computed(() => {
  const sid = String(filters.value.societeId ?? 'ALL')
  const mk = String(filters.value.mois ?? '')
  const range = monthRangeFromKey(mk)

  return rowsAll.value.filter((r) => {
    const matchSociete = sid === 'ALL' || String(r?.contrat?.videoPublicitaire?.societe?.id ?? '') === sid

    if (!range) return matchSociete

    const dd = parseLocalDateAsUTC(r?.contrat?.dateDebut)
    const df = parseLocalDateAsUTC(r?.contrat?.dateFin)

    const end = df ?? dd
    if (!dd || !end) return false

    const overlap = dd <= range.end && end >= range.start
    return matchSociete && overlap
  })
})

const chiffreAffaire = computed(() => {
  return rowsFiltered.value.reduce((sum, r) => sum + (Number(r.total) || 0), 0)
})

const totalReste = computed(() => {
  return rowsFiltered.value.reduce((sum, r) => sum + (Number(r.reste) || 0), 0)
})

// Helper functions for display
const formatCurrency = (value) => {
  if (typeof value !== 'number') return '-'
  return new Intl.NumberFormat('fr-FR', { 
    style: 'currency', 
    currency: 'MGA',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(value)
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  try {
    return new Date(dateString).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    })
  } catch {
    return dateString
  }
}

const formatMonth = (monthString) => {
  if (!monthString) return ''
  try {
    const [year, month] = monthString.split('-')
    const date = new Date(parseInt(year), parseInt(month) - 1, 1)
    return date.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' })
  } catch {
    return monthString
  }
}

const toggleRowDetails = (row) => {
  // Simple interactivité - pourrait être étendu
  console.log('Détails du contrat:', row)
}

const resetMonthOnly = () => {
  filters.value.mois = ''
}

watch(
  () => filters.value.societeId,
  () => {
    // computed reacts
  },
)

onMounted(loadData)
</script>

<style scoped>
.publicite-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Header */
.header {
  margin-bottom: 32px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 16px;
}

.header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1a237e;
  margin: 0;
}

.subtitle {
  color: #5c6bc0;
  font-size: 14px;
  margin-top: 4px;
}

/* Error Card */
.error-card {
  background: #ffebee;
  border: 1px solid #f44336;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.error-icon {
  width: 24px;
  height: 24px;
  background: #f44336;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.error-message {
  color: #b71c1c;
  font-size: 14px;
}

/* Filter Card */
.filter-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-header h3 {
  margin: 0 0 16px 0;
  color: #1a237e;
  font-size: 16px;
  font-weight: 600;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  align-items: end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-size: 13px;
  color: #5c6bc0;
  font-weight: 500;
}

.filter-select,
.filter-input {
  padding: 10px 12px;
  border: 1px solid #c5cae9;
  border-radius: 6px;
  font-size: 14px;
  color: #1a237e;
  background: white;
  transition: all 0.2s;
}

.filter-select:focus,
.filter-input:focus {
  outline: none;
  border-color: #3949ab;
  box-shadow: 0 0 0 2px rgba(57, 73, 171, 0.1);
}

.month-input {
  position: relative;
}

.clear-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #5c6bc0;
  font-size: 18px;
  cursor: pointer;
  padding: 0 8px;
  line-height: 1;
}

.clear-btn:hover {
  color: #3949ab;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}

.btn-refresh {
  padding: 10px 24px;
  background: #3949ab;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-refresh:hover:not(:disabled) {
  background: #303f9f;
  transform: translateY(-1px);
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Loading */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loader {
  width: 40px;
  height: 40px;
  border: 3px solid #c5cae9;
  border-top-color: #3949ab;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading p {
  color: #5c6bc0;
  font-size: 14px;
  margin: 0;
}

/* Summary Cards */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.summary-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.summary-icon {
  width: 48px;
  height: 48px;
  background: #e8eaf6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.summary-icon svg {
  width: 24px;
  height: 24px;
  fill: #3949ab;
}

.summary-content {
  flex: 1;
}

.summary-label {
  font-size: 14px;
  color: #5c6bc0;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
  color: #1a237e;
  margin-bottom: 8px;
}

.summary-value.zero {
  color: #4caf50;
}

.summary-info {
  font-size: 13px;
  color: #7986cb;
}

/* Table Section */
.table-section {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.table-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  margin: 0;
  color: #1a237e;
  font-size: 16px;
  font-weight: 600;
}

.table-info span {
  font-size: 13px;
  color: #5c6bc0;
  background: #e8eaf6;
  padding: 4px 12px;
  border-radius: 20px;
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.data-table th {
  padding: 16px 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: #5c6bc0;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9ff;
}

.data-table th.text-right {
  text-align: right;
}

.data-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.data-table tbody tr:hover {
  background-color: #f8f9ff;
}

.data-table td {
  padding: 16px 20px;
  vertical-align: middle;
}

/* Table Cells */
.contract-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contract-id {
  font-size: 13px;
  font-weight: 600;
  color: #3949ab;
  background: #e8eaf6;
  padding: 2px 8px;
  border-radius: 4px;
  width: fit-content;
}

.contract-title {
  font-size: 14px;
  color: #1a237e;
}

.societe-info {
  font-size: 14px;
  color: #1a237e;
}

.period-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 13px;
  color: #5c6bc0;
}

.period-to {
  font-size: 12px;
  color: #7986cb;
}

.amount {
  font-weight: 600;
  color: #1a237e;
  font-size: 14px;
}

.paid-amount {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}

.progress-container {
  width: 80px;
}

.progress-bar {
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #3949ab;
  transition: width 0.3s ease;
}

.reste-amount {
  font-weight: 600;
  color: #f44336;
  font-size: 14px;
}

.reste-amount.fully-paid {
  color: #4caf50;
}

/* Empty State */
.empty-state {
  padding: 40px 20px;
}

.empty-content {
  text-align: center;
  color: #7986cb;
  font-size: 14px;
}

/* Table Footer */
.table-footer {
  padding: 16px 20px;
  border-top: 1px solid #e0e0e0;
  background: #f8f9ff;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-label {
  font-size: 14px;
  color: #5c6bc0;
  font-weight: 500;
}

.footer-values {
  display: flex;
  gap: 32px;
}

.footer-value {
  display: flex;
  align-items: center;
  gap: 8px;
}

.footer-value span {
  font-size: 14px;
  color: #5c6bc0;
}

.footer-value strong {
  font-size: 16px;
  color: #1a237e;
}

.footer-value strong.zero {
  color: #4caf50;
}

/* Responsive */
@media (max-width: 768px) {
  .publicite-container {
    padding: 16px;
  }
  
  .summary-cards {
    grid-template-columns: 1fr;
  }
  
  .filters {
    grid-template-columns: 1fr;
  }
  
  .filter-actions {
    justify-content: flex-start;
  }
  
  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .footer-content {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .footer-values {
    flex-direction: column;
    gap: 8px;
  }
}
</style>