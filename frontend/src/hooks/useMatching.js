import { useState, useEffect, useCallback } from 'react'
import { getCandidates } from '../api/matching'
import { mapCandidateToCard } from '../utils/mappers'

// Encapsule tout ce qui touche au matching :
// appel API, état loading/error, transformation en format SwipeCard.
// DiscoverPage ne sait pas d'où viennent les données — il appelle juste ce hook.
//
// Pour changer le moteur de matching plus tard :
//   → modifier getCandidates() dans api/matching.js
//   → ou changer la logique de tri/filtrage ici
//   → DiscoverPage ne change pas
export function useMatching() {
  const [candidates, setCandidates] = useState([])
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState(null)

  const load = useCallback(() => {
    setLoading(true)
    setError(null)
    getCandidates()
      .then(res => setCandidates(res.data.map(mapCandidateToCard)))
      .catch(() => setError('Could not load candidates. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  return { candidates, loading, error, reload: load }
}
