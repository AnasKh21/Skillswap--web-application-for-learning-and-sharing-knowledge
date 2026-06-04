import client from './client'

// Retourne tous les skills du catalogue global
export const getAllSkills = () => client.get('/api/skills')

// ─────────────────────────────────────────────────────────────────
// EXERCICE 1 — Filtre les skills par catégorie
//
// L'API accepte : GET /api/skills?category=Programming
//
// Complète la fonction ci-dessous.
//
// Hint :
//   - Regarde getAllSkills() juste au-dessus → même chose mais avec un paramètre
//   - Axios accepte les query params via l'option { params: { category } }
//   - Syntaxe : client.get('/api/skills', { params: { category } })
// ─────────────────────────────────────────────────────────────────
export const getSkillsByCategory = (category) => {
  return client.get('/api/skills', { params: { category } })
}
