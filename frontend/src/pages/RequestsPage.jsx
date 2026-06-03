import { useState } from 'react'
import { motion } from 'framer-motion'
import { Inbox, Send } from 'lucide-react'
import RequestCard from '../components/cards/RequestCard'
import { mockRequests } from '../data/mockData'

export default function RequestsPage() {
  const [tab, setTab] = useState('incoming')
  const [requests, setRequests] = useState(mockRequests)

  const incoming = requests.filter((r) => r.status === 'PENDING')
  const outgoing  = []

  const handleAccept = (id) => {
    setRequests((prev) => prev.filter((r) => r.id !== id))
    alert('Request accepted! A session has been scheduled.')
  }

  const handleDecline = (id) => {
    setRequests((prev) => prev.filter((r) => r.id !== id))
  }

  const Empty = ({ icon: Icon, label }) => (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="w-16 h-16 rounded-full bg-orange-50 flex items-center justify-center mb-4">
        <Icon size={28} className="text-primary" />
      </div>
      <p className="text-muted text-sm">{label}</p>
    </div>
  )

  const list = tab === 'incoming' ? incoming : outgoing

  return (
    <div className="page-enter">
      <div className="mb-5">
        <h1 className="text-2xl font-bold text-dark">Requests</h1>
        <p className="text-muted text-sm">Manage your skill-swap invitations</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 bg-orange-50 p-1 rounded-2xl mb-5">
        <button
          onClick={() => setTab('incoming')}
          className={`flex-1 py-2 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2
            ${tab === 'incoming' ? 'bg-white text-primary shadow-sm' : 'text-muted hover:text-primary'}`}
        >
          <Inbox size={14} /> Incoming
          {incoming.length > 0 && (
            <span className="w-5 h-5 rounded-full bg-primary text-white text-[10px] font-bold flex items-center justify-center">
              {incoming.length}
            </span>
          )}
        </button>
        <button
          onClick={() => setTab('outgoing')}
          className={`flex-1 py-2 rounded-xl text-sm font-semibold transition-all flex items-center justify-center gap-2
            ${tab === 'outgoing' ? 'bg-white text-primary shadow-sm' : 'text-muted hover:text-primary'}`}
        >
          <Send size={14} /> Outgoing
        </button>
      </div>

      {/* List */}
      {list.length === 0 ? (
        <Empty
          icon={tab === 'incoming' ? Inbox : Send}
          label={tab === 'incoming'
            ? 'No pending invitations — keep swiping!'
            : 'You haven\'t sent any requests yet.'}
        />
      ) : (
        <div className="space-y-3">
          {list.map((req, i) => (
            <motion.div
              key={req.id}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.06 }}
            >
              <RequestCard
                request={req}
                onAccept={handleAccept}
                onDecline={handleDecline}
                isIncoming={tab === 'incoming'}
              />
            </motion.div>
          ))}
        </div>
      )}
    </div>
  )
}
