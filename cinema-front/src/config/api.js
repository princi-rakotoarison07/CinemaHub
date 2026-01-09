const _rawApiBase = import.meta.env.VITE_API_BASE_URL
if (!_rawApiBase) {
	// avertissement utile en dev si la variable d'env n'est pas définie
	// (évite de récupérer du HTML et d'avoir "JSON.parse" errors)
	// Vous pouvez créer un fichier `.env` dans `cinema-front` avec:
	// VITE_API_BASE_URL=http://localhost:8080
	// ou redémarrer le serveur Vite après modification.
	// fallback sûr vers le backend local
	// eslint-disable-next-line no-console
	console.warn('VITE_API_BASE_URL is not defined — falling back to http://localhost:8080')
}
export const API_BASE_URL = _rawApiBase ? _rawApiBase.replace(/\/$/, '') : 'http://localhost:8080'
