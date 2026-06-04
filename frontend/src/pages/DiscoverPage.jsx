import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Heart, X, RefreshCw, Zap } from 'lucide-react'
import SwipeCard from '../components/cards/SwipeCard'
import Button from '../components/ui/Button'
import { getAllUsers } from '../api/users'
import { mapApiUserToCard } from '../utils/mappers'

export default function DiscoverPage() {
  const [users, setUsers]           = useState([])
  const [liked, setLiked]           = useState([])
  const [passed, setPassed]         = useState([])
  const [lastAction, setLastAction] = useState(null)
  const [loading, setLoading]       = useState(true)
  const [error, setError]           = useState(null)

  const loadUsers = () => {
    setLoading(true)
    setError(null)
    getAllUsers()
      .then(res => {
        // Exercice 2 : mapApiUserToCard transforme chaque user API en format SwipeCard
        const cards = res.data.map(mapApiUserToCard)
        setUsers(cards)
      })
      .catch(() => setError('Could not load users. Is the backend running?'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { loadUsers() }, [])

  const topUser = users[users.length - 1]

  const handleLike  = (u) => { setLiked(p => [...p, u]);  setUsers(p => p.filter(x => x.id !== u.id)); setLastAction('like') }
  const handlePass  = (u) => { setPassed(p => [...p, u]); setUsers(p => p.filter(x => x.id !== u.id)); setLastAction('pass') }
  const handleReset = ()  => { loadUsers(); setLiked([]); setPassed([]); setLastAction(null) }

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
    </div>
  )

  if (error) return (
    <div className="flex flex-col items-center justify-center h-64 text-center gap-4">
      <p className="text-muted text-sm">{error}</p>
      <Button onClick={loadUsers} variant="outline" size="sm">Retry</Button>
    </div>
  )

  return (
    <div className="flex flex-col h-full min-h-[calc(100vh-8rem)] md:min-h-0 page-enter">
      <div className="mb-4">
        <h1 className="text-2xl font-bold text-dark">Discover</h1>
        <p className="text-muted text-sm">
          {users.length > 0 ? `${users.length} people ready to swap skills` : "You've seen everyone for now!"}
        </p>
      </div>

      <div className="flex gap-3 mb-5">
        <div className="flex-1 bg-orange-50 border border-orange-100 rounded-2xl px-4 py-3 text-center">
          <p className="text-xl font-bold text-primary">{liked.length}</p>
          <p className="text-xs text-muted font-medium">Connected</p>
        </div>
        <div className="flex-1 bg-red-50 border border-red-100 rounded-2xl px-4 py-3 text-center">
          <p className="text-xl font-bold text-danger">{passed.length}</p>
          <p className="text-xs text-muted font-medium">Passed</p>
        </div>
        <div className="flex-1 bg-yellow-50 border border-yellow-100 rounded-2xl px-4 py-3 text-center">
          <p className="text-xl font-bold text-accent">{users.length}</p>
          <p className="text-xs text-muted font-medium">Remaining</p>
        </div>
      </div>

      <div className="flex-1 flex flex-col items-center">
        {users.length === 0 ? (
          <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }}
            className="flex flex-col items-center justify-center flex-1 text-center px-8"
          >
            <div className="w-24 h-24 rounded-full bg-gradient-brand flex items-center justify-center mb-5 shadow-card">
              <Zap size={40} className="text-white fill-white" />
            </div>
            <h2 className="text-xl font-bold text-dark mb-2">You've seen them all!</h2>
            <p className="text-muted text-sm mb-6">
              You connected with <strong className="text-primary">{liked.length}</strong> people.
            </p>
            <Button onClick={handleReset} variant="outline"><RefreshCw size={16} /> Start over</Button>
          </motion.div>
        ) : (
          <>
            <div className="relative w-full max-w-sm" style={{ height: 480 }}>
              {users.length > 2 && <div className="absolute inset-0 rounded-3xl bg-orange-100 border border-orange-200 scale-90 translate-y-8 pointer-events-none" />}
              {users.length > 1 && <div className="absolute inset-0 rounded-3xl bg-white border border-orange-100 shadow-sm scale-95 translate-y-4 pointer-events-none" />}
              <AnimatePresence>
                {topUser && <SwipeCard key={topUser.id} user={topUser} onLike={handleLike} onPass={handlePass} isTop />}
              </AnimatePresence>
            </div>

            <div className="flex items-center gap-5 mt-6">
              <button onClick={() => topUser && handlePass(topUser)}
                className="w-14 h-14 rounded-full bg-white border-2 border-red-200 flex items-center justify-center text-danger shadow-sm hover:shadow-md hover:scale-110 active:scale-95 transition-all">
                <X size={24} strokeWidth={2.5} />
              </button>
              <button onClick={() => topUser && handleLike(topUser)}
                className="w-16 h-16 rounded-full bg-gradient-brand flex items-center justify-center text-white shadow-card hover:shadow-card-hover hover:scale-110 active:scale-95 transition-all">
                <Heart size={28} strokeWidth={2.5} className="fill-white" />
              </button>
            </div>
            <p className="text-xs text-muted mt-3">Swipe right to connect · Swipe left to pass</p>
          </>
        )}
      </div>
    </div>
  )
}
