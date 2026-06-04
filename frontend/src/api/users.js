import client from './client'

export const getMe          = ()           => client.get('/api/users/me')
export const getAllUsers     = ()           => client.get('/api/users')
export const getUserById     = (id)        => client.get(`/api/users/${id}`)
export const updateProfile   = (data)      => client.put('/api/users/me', data)
export const getMySkills     = ()          => client.get('/api/users/me/skills')
export const addSkill        = (data)      => client.post('/api/users/me/skills', data)
export const removeSkill     = (skillId)   => client.delete(`/api/users/me/skills/${skillId}`)
