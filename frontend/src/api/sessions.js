import client from './client'

// Retourne toutes mes sessions (à venir + historique)
export const getMySessions   = ()    => client.get('/api/sessions')

// Marque une session comme terminée (permet ensuite de laisser un avis)
export const completeSession = (id)  => client.put(`/api/sessions/${id}/complete`)

// Annule une session planifiée
export const cancelSession   = (id)  => client.put(`/api/sessions/${id}/cancel`)
