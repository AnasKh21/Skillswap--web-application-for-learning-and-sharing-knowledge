import client from './client'

// Laisse un avis sur un autre utilisateur après une session COMPLETED
// data = { sessionId, targetId, score, comment }
export const createRating       = (data)   => client.post('/api/ratings', data)

// Récupère tous les avis reçus par un utilisateur (page profil publique)
export const getRatingsForUser  = (userId) => client.get(`/api/ratings/user/${userId}`)
