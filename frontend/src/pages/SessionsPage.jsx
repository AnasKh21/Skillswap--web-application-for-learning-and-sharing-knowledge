import { useEffect, useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { CalendarDays, CheckCircle2, X } from 'lucide-react'
import SessionCard from '../components/cards/SessionCard'
import { StarPicker } from '../components/ui/StarRating'
import Button from '../components/ui/Button'
import { getMySessions, completeSession, cancelSession } from '../api/sessions'
import { createRating } from '../api/ratings'
import { splitSessions, mapApiSessionToCard } from '../utils/mappers'
import { useAuth } from '../context/AuthContext'

function RatingModal({ session, onClose, onSubmit }) {
  const [score, setScore]     = useState(0)
  const [comment, setComment] = useState('')
  return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-end md:items-center justify-center z-50 p-4"
      onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div initial={{ y: 60, opacity: 0 }} animate={{ y: 0, opacity: 1 }} exit={{ y: 60, opacity: 0 }}
        className="bg-white rounded-3xl p-6 w-full max-w-sm shadow-2xl">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-bold text-dark">Rate the session</h3>
          <button onClick={onClose} className="text-muted hover:text-dark"><X size={20} /></button>
        </div>
        <p className="text-sm text-muted mb-4">
          How was your session with <strong>{session.otherUser.displayName}</strong>?
        </p>
        <div className="flex justify-center mb-4">
          <StarPicker value={score} onChange={setScore} />
        </div>
        <textarea placeholder="Leave a comment (optional)..." value={comment}
          onChange={e => setComment(e.target.value)} rows={3}
          className="w-full px-4 py-3 rounded-2xl border border-gray-200 focus:border-primary focus:ring-2 focus:ring-orange-200 outline-none text-sm resize-none mb-4" />
        <Button className="w-full" disabled={score === 0} onClick={() => onSubmit(session, score, comment)}>
          Submit Rating
        </Button>
      </motion.div>
    </motion.div>
  )
}

export default function SessionsPage() {
  const { user }                          = useAuth()
  const [sessions, setSessions]           = useState([])
  const [tab, setTab]                     = useState('upcoming')
  const [ratingTarget, setRatingTarget]   = useState(null)
  const [loading, setLoading]             = useState(true)

  useEffect(() => {
    getMySessions()
      .then(res => {
        // mapApiSessionToCard : transforme le format API → format SessionCard
        // user est nécessaire pour trouver "l'autre" participant
        const mapped = res.data.map(s => mapApiSessionToCard(s, user))
        setSessions(mapped)
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [user])

  // Exercice 3 : splitSessions sépare les sessions planifiées de l'historique
  const { upcoming, history } = sessions.length > 0
    ? splitSessions(sessions)
    : { upcoming: [], history: [] }

  const handleComplete = async (id) => {
    await completeSession(id)
    setSessions(prev => prev.map(s => s.id === id ? { ...s, status: 'COMPLETED' } : s))
  }

  const handleCancel = async (id) => {
    await cancelSession(id)
    setSessions(prev => prev.map(s => s.id === id ? { ...s, status: 'CANCELLED' } : s))
  }

  const handleSubmitRating = async (session, score, comment) => {
    await createRating({
      sessionId: session.id,
      targetId: session.otherUser.id,
      score,
      comment,
    })
    setSessions(prev => prev.map(s => s.id === session.id ? { ...s, rated: true } : s))
    setRatingTarget(null)
  }

  const list = tab === 'upcoming' ? upcoming : history

  const Empty = () => (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="w-16 h-16 rounded-full bg-orange-50 flex items-center justify-center mb-4">
        <CalendarDays size={28} className="text-primary" />
      </div>
      <p className="text-muted text-sm">
        {tab === 'upcoming' ? 'No upcoming sessions. Accept a request!' : 'No completed sessions yet.'}
      </p>
    </div>
  )

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="page-enter">
      <div className="mb-5">
        <h1 className="text-2xl font-bold text-dark">Sessions</h1>
        <p className="text-muted text-sm">Track your skill-exchange appointments</p>
      </div>

      <div className="flex gap-1 bg-orange-50 p-1 rounded-2xl mb-5">
        {[['upcoming', 'Upcoming'], ['history', 'History']].map(([key, label]) => (
          <button key={key} onClick={() => setTab(key)}
            className={`flex-1 py-2 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2
              ${tab === key ? 'bg-white text-primary shadow-sm' : 'text-muted hover:text-primary'}`}>
            {key === 'upcoming' ? <CalendarDays size={14} /> : <CheckCircle2 size={14} />}
            {label}
            {key === 'upcoming' && upcoming.length > 0 && (
              <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-bold flex items-center justify-center">
                {upcoming.length}
              </span>
            )}
          </button>
        ))}
      </div>

      {list.length === 0 ? <Empty /> : (
        <div className="space-y-3">
          {list.map((s, i) => (
            <motion.div key={s.id} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.06 }}>
              <SessionCard session={s} onComplete={handleComplete} onCancel={handleCancel} onRate={setRatingTarget} />
            </motion.div>
          ))}
        </div>
      )}

      <AnimatePresence>
        {ratingTarget && (
          <RatingModal session={ratingTarget} onClose={() => setRatingTarget(null)} onSubmit={handleSubmitRating} />
        )}
      </AnimatePresence>
    </div>
  )
}
