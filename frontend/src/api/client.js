import axios from 'axios'

// Axios instance — toutes les requêtes API passent par ici
const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  // withCredentials = true : envoie les cookies HttpOnly automatiquement
  // sans ça, le cookie access_token n'est jamais envoyé avec les requêtes
  withCredentials: true,
})

// Intercepteur de réponse : redirige vers /auth si le token expire en cours de session
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirige uniquement si on est sur une page protégée
      // (pas sur la landing page / ni sur /auth)
      const protectedPaths = ['/discover', '/requests', '/sessions', '/profile']
      const onProtectedPage = protectedPaths.some(p => window.location.pathname.startsWith(p))
      if (onProtectedPage) {
        window.location.href = '/auth'
      }
    }
    return Promise.reject(error)
  }
)

export default client
