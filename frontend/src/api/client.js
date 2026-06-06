import axios from 'axios'

// Axios instance — toutes les requêtes API passent par ici
// baseURL vide → les requêtes /api/** passent par le proxy Vite (localhost:3000 → localhost:8080)
// = same-origin depuis le navigateur, donc le cookie SameSite=Strict est envoyé sans problème
const client = axios.create({
  baseURL: '',
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
})

// Quand le body est du FormData, supprimer Content-Type pour que le navigateur
// le pose lui-même avec le boundary correct (multipart/form-data; boundary=--xyz)
client.interceptors.request.use(config => {
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

// Gère les tokens en cours de rafraîchissement : on met en file les requêtes qui
// arrivent pendant qu'un refresh est déjà en cours, puis on les rejoue toutes ensemble.
let isRefreshing = false
let queue = []

const flushQueue = (error) => {
  queue.forEach(({ resolve, reject }) => error ? reject(error) : resolve())
  queue = []
}

client.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config

    // On ne tente le refresh que pour un vrai 401 non encore retenté,
    // et jamais sur les endpoints d'auth eux-mêmes (évite la boucle infinie)
    const isAuthCall = original.url?.startsWith('/api/auth/')
    if (error.response?.status !== 401 || original._retry || isAuthCall) {
      return Promise.reject(error)
    }

    // Si un refresh est déjà en cours, on met la requête en attente
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        queue.push({ resolve, reject })
      }).then(() => client(original))
        .catch(() => Promise.reject(error))
    }

    original._retry = true
    isRefreshing = true

    try {
      await client.post('/api/auth/refresh')
      flushQueue(null)
      return client(original)
    } catch (_) {
      // Refresh échoué → session définitivement expirée → retour au login
      flushQueue(error)
      const protectedPaths = ['/discover', '/requests', '/sessions', '/profile']
      const onProtectedPage = protectedPaths.some(p => window.location.pathname.startsWith(p))
      if (onProtectedPage) window.location.href = '/auth'
      return Promise.reject(error)
    } finally {
      isRefreshing = false
    }
  }
)

export default client
