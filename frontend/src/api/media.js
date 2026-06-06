import client from './client'

const toForm = (file) => {
  const form = new FormData()
  form.append('file', file)
  return form
}

// Ne pas forcer Content-Type : Axios détecte FormData et laisse le navigateur
// ajouter le boundary automatiquement (ex: multipart/form-data; boundary=---xyz)
export const uploadAvatar = (file) =>
  client.post('/api/users/me/avatar', toForm(file))

export const uploadBanner = (file) =>
  client.post('/api/users/me/banner', toForm(file))
