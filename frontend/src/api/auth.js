import client from './client'

// POST /api/auth/register → { displayName, email, password }
export const register = (data) => client.post('/api/auth/register', data)

// POST /api/auth/login → { email, password }
// Le cookie access_token est posé automatiquement par le Set-Cookie du serveur
export const login = (data) => client.post('/api/auth/login', data)

// POST /api/auth/logout → efface les cookies côté serveur
export const logout = () => client.post('/api/auth/logout')

// POST /api/auth/refresh → renouvelle le access_token depuis le refresh_token cookie
export const refresh = () => client.post('/api/auth/refresh')

// GET /api/users/me → profil de l'utilisateur connecté (pour vérifier la session au démarrage)
export const getMe = () => client.get('/api/users/me')
