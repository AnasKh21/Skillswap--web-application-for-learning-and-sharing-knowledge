import { CheckCircle2, XCircle, Clock, ArrowRightLeft } from 'lucide-react'
import Avatar from '../ui/Avatar'
import Button from '../ui/Button'

export default function RequestCard({ request, onAccept, onDecline, isIncoming = true }) {
  const { initiator, offeredSkill, wantedSkill, message, createdAt } = request

  const timeAgo = (iso) => {
    const diff = (Date.now() - new Date(iso)) / 1000
    if (diff < 3600)  return `${Math.floor(diff / 60)}m ago`
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
    return `${Math.floor(diff / 86400)}d ago`
  }

  return (
    <div className="bg-white rounded-2xl border border-orange-100 shadow-sm p-4 space-y-4 hover:shadow-card transition-shadow">
      {/* Header */}
      <div className="flex items-start gap-3">
        <Avatar user={initiator} size="md" />
        <div className="flex-1 min-w-0">
          <p className="font-semibold text-dark">{initiator.displayName}</p>
          <p className="text-xs text-muted flex items-center gap-1">
            <Clock size={11} /> {timeAgo(createdAt)}
          </p>
        </div>
      </div>

      {/* Skill swap display */}
      <div className="flex items-center justify-center gap-3 py-2 px-4 bg-orange-50 rounded-xl">
        <span className="px-3 py-1 bg-primary text-white text-xs font-bold rounded-full">
          {offeredSkill}
        </span>
        <ArrowRightLeft size={16} className="text-primary flex-shrink-0" />
        <span className="px-3 py-1 bg-blue-500 text-white text-xs font-bold rounded-full">
          {wantedSkill}
        </span>
      </div>

      {/* Message */}
      {message && (
        <p className="text-sm text-muted italic leading-relaxed">
          "{message}"
        </p>
      )}

      {/* Actions */}
      {isIncoming && (
        <div className="flex gap-2 pt-1">
          <Button
            variant="primary"
            size="sm"
            className="flex-1"
            onClick={() => onAccept(request.id)}
          >
            <CheckCircle2 size={15} /> Accept
          </Button>
          <Button
            variant="ghost"
            size="sm"
            className="flex-1 text-danger border-red-200 hover:bg-red-50"
            onClick={() => onDecline(request.id)}
          >
            <XCircle size={15} /> Decline
          </Button>
        </div>
      )}
    </div>
  )
}
