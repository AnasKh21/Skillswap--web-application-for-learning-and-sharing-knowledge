import { Video, MapPin, CheckCircle2, XCircle, Star } from 'lucide-react'
import Avatar from '../ui/Avatar'
import Button from '../ui/Button'

export default function SessionCard({ session, onComplete, onCancel, onRate }) {
  const { otherUser, offeredSkill, wantedSkill, scheduledAt, mode, location, status, rated } = session

  const formatDate = (iso) =>
    new Date(iso).toLocaleDateString('en-US', {
      weekday: 'short', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
    })

  const statusBadge = {
    SCHEDULED: { label: 'Upcoming', cls: 'bg-blue-100 text-blue-700' },
    COMPLETED:  { label: 'Completed', cls: 'bg-green-100 text-green-700' },
    CANCELLED:  { label: 'Cancelled', cls: 'bg-gray-100 text-muted' },
  }[status]

  return (
    <div className="bg-white rounded-2xl border border-orange-100 shadow-sm p-4 space-y-3 hover:shadow-card transition-shadow">
      <div className="flex items-start gap-3">
        <Avatar user={otherUser} size="md" />
        <div className="flex-1 min-w-0">
          <p className="font-semibold text-dark">{otherUser.displayName}</p>
          <p className="text-xs text-muted">{formatDate(scheduledAt)}</p>
        </div>
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full flex-shrink-0 ${statusBadge.cls}`}>
          {statusBadge.label}
        </span>
      </div>

      {/* Skills */}
      <div className="flex items-center gap-2 text-sm">
        <span className="font-medium text-primary">{offeredSkill}</span>
        <span className="text-muted">↔</span>
        <span className="font-medium text-blue-600">{wantedSkill}</span>
      </div>

      {/* Mode */}
      <div className="flex items-center gap-1.5 text-xs text-muted">
        {mode === 'ONLINE'
          ? <><Video size={12} /> Online session</>
          : <><MapPin size={12} /> {location ?? 'In-person'}</>
        }
      </div>

      {/* Actions */}
      {status === 'SCHEDULED' && (
        <div className="flex gap-2 pt-1">
          <Button size="sm" className="flex-1" onClick={() => onComplete(session.id)}>
            <CheckCircle2 size={14} /> Mark complete
          </Button>
          <Button size="sm" variant="ghost" className="text-danger border-red-200 hover:bg-red-50"
            onClick={() => onCancel(session.id)}>
            <XCircle size={14} />
          </Button>
        </div>
      )}

      {status === 'COMPLETED' && !rated && (
        <Button size="sm" variant="outline" className="w-full" onClick={() => onRate(session)}>
          <Star size={14} /> Rate this session
        </Button>
      )}
    </div>
  )
}
