import client from './client'

// Récupère les demandes d'échange en attente que j'ai reçues
export const getIncomingRequests = ()     => client.get('/api/session-requests/incoming')

// Envoie une demande d'échange à un autre utilisateur
// data = { receiverId, offeredSkillId, wantedSkillId, message }
export const sendRequest = (data)         => client.post('/api/session-requests', data)

// Accepte une demande reçue → crée automatiquement une Session
// data = { scheduledAt, mode, location }
export const acceptRequest = (id, data)   => client.put(`/api/session-requests/${id}/accept`, data)

// Refuse une demande reçue
export const declineRequest = (id)        => client.put(`/api/session-requests/${id}/decline`)

// Annule une demande que j'ai envoyée
export const cancelRequest = (id)         => client.put(`/api/session-requests/${id}/cancel`)
