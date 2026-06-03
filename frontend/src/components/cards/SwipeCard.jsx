import { useRef, useState } from 'react'
import { motion, useMotionValue, useTransform, animate } from 'framer-motion'
import { MapPin, Star, BookOpen, Lightbulb } from 'lucide-react'
import Avatar from '../ui/Avatar'

const SWIPE_THRESHOLD = 120

export default function SwipeCard({ user, onLike, onPass, isTop }) {
  const cardRef = useRef(null)
  const x = useMotionValue(0)
  const y = useMotionValue(0)

  const rotate = useTransform(x, [-300, 300], [-20, 20])
  const likeOpacity  = useTransform(x, [0, SWIPE_THRESHOLD], [0, 1])
  const passOpacity  = useTransform(x, [-SWIPE_THRESHOLD, 0], [1, 0])

  const [dragging, setDragging] = useState(false)

  const flyOut = (direction) => {
    const target = direction === 'right' ? 600 : -600
    animate(x, target, { duration: 0.35, ease: 'easeOut' })
    setTimeout(() => {
      if (direction === 'right') onLike(user)
      else onPass(user)
    }, 300)
  }

  const handleDragEnd = (_, info) => {
    setDragging(false)
    if (info.offset.x > SWIPE_THRESHOLD)  flyOut('right')
    else if (info.offset.x < -SWIPE_THRESHOLD) flyOut('left')
    else {
      animate(x, 0, { type: 'spring', stiffness: 300, damping: 25 })
      animate(y, 0, { type: 'spring', stiffness: 300, damping: 25 })
    }
  }

  if (!isTop) {
    return (
      <div className="absolute inset-0 rounded-3xl bg-white border border-orange-100 shadow-sm scale-95 translate-y-4 pointer-events-none" />
    )
  }

  return (
    <motion.div
      ref={cardRef}
      className="absolute inset-0 rounded-3xl bg-white shadow-card overflow-hidden cursor-grab active:cursor-grabbing select-none"
      style={{ x, y, rotate }}
      drag
      dragConstraints={{ left: 0, right: 0, top: 0, bottom: 0 }}
      dragElastic={0.9}
      onDragStart={() => setDragging(true)}
      onDragEnd={handleDragEnd}
      whileTap={{ scale: 1.02 }}
    >
      {/* ── Hero gradient header ─── */}
      <div className={`h-56 bg-gradient-to-br ${user.avatarBg} relative flex items-center justify-center`}>
        <Avatar user={user} size="2xl" className="border-4 border-white shadow-lg" />

        {/* Like stamp */}
        <motion.div
          className="absolute top-6 left-6 border-4 border-green-400 text-green-400 font-black text-2xl px-4 py-1 rounded-2xl rotate-[-15deg]"
          style={{ opacity: likeOpacity }}
        >
          CONNECT ✓
        </motion.div>

        {/* Pass stamp */}
        <motion.div
          className="absolute top-6 right-6 border-4 border-danger text-danger font-black text-2xl px-4 py-1 rounded-2xl rotate-[15deg]"
          style={{ opacity: passOpacity }}
        >
          PASS ✗
        </motion.div>
      </div>

      {/* ── Card body ─── */}
      <div className="p-5 space-y-4 overflow-y-auto max-h-[calc(100%-14rem)] no-scrollbar">
        {/* Name + location + rating */}
        <div>
          <h2 className="text-xl font-bold text-dark">{user.displayName}</h2>
          <div className="flex items-center gap-3 mt-1 text-muted text-sm">
            {user.location && (
              <span className="flex items-center gap-1">
                <MapPin size={13} /> {user.location}
              </span>
            )}
            <span className="flex items-center gap-1">
              <Star size={13} className="text-accent fill-accent" />
              {user.averageRating?.toFixed(1)}
            </span>
            <span>{user.sessionsCompleted} sessions</span>
          </div>
        </div>

        {/* Bio */}
        {user.bio && (
          <p className="text-sm text-muted leading-relaxed line-clamp-3">{user.bio}</p>
        )}

        {/* Teaches */}
        <div>
          <div className="flex items-center gap-1.5 text-xs font-semibold text-primary uppercase tracking-wide mb-2">
            <BookOpen size={12} /> Teaches
          </div>
          <div className="flex flex-wrap gap-1.5">
            {user.teachSkills?.map((s) => (
              <span key={s} className="px-3 py-1 bg-orange-50 border border-orange-200 text-primary text-xs font-semibold rounded-full">
                {s}
              </span>
            ))}
          </div>
        </div>

        {/* Wants to learn */}
        <div>
          <div className="flex items-center gap-1.5 text-xs font-semibold text-blue-500 uppercase tracking-wide mb-2">
            <Lightbulb size={12} /> Wants to learn
          </div>
          <div className="flex flex-wrap gap-1.5">
            {user.learnSkills?.map((s) => (
              <span key={s} className="px-3 py-1 bg-blue-50 border border-blue-200 text-blue-600 text-xs font-semibold rounded-full">
                {s}
              </span>
            ))}
          </div>
        </div>
      </div>
    </motion.div>
  )
}
