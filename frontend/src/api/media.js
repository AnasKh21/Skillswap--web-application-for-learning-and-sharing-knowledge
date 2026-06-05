import client from './client'

const toForm = (file) => {
  const form = new FormData()
  form.append('file', file)
  return form
}

export const uploadAvatar = (file) =>
  client.post('/api/users/me/avatar', toForm(file), {
    headers: { 'Content-Type': 'multipart/form-data' },
  })

export const uploadBanner = (file) =>
  client.post('/api/users/me/banner', toForm(file), {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
