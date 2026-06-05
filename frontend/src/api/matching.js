import client from './client'

export const getCandidates = () => client.get('/api/matching/candidates')
