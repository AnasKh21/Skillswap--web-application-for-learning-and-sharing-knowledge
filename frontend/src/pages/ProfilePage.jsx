import { useState } from 'react'
import { motion } from 'framer-motion'
import { Edit2, Plus, Trash2, BookOpen, Lightbulb, Star } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import Avatar from '../components/ui/Avatar'
import { StarDisplay } from '../components/ui/StarRating'
import Button from '../components/ui/Button'
import { mockCurrentUser, mockSkills } from '../data/mockData'

function SkillSection({ title, icon: Icon, color, skills, onRemove, onAdd }) {
  return (
    <div className="bg-white rounded-2xl border border-orange-100 shadow-sm p-4">
      <div className={`flex items-center gap-2 mb-3 text-sm font-bold uppercase tracking-wide ${color}`}>
        <Icon size={14} /> {title}
      </div>
      <div className="flex flex-wrap gap-2 mb-3">
        {skills.length === 0 && (
          <p className="text-xs text-muted italic">No skills added yet.</p>
        )}
        {skills.map((skill) => (
          <div
            key={skill}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-orange-50 border border-orange-200 rounded-full text-sm font-semibold text-primary group"
          >
            {skill}
            <button
              onClick={() => onRemove(skill)}
              className="opacity-0 group-hover:opacity-100 transition-opacity text-muted hover:text-danger"
            >
              <Trash2 size={12} />
            </button>
          </div>
        ))}
      </div>
      <button
        onClick={onAdd}
        className="flex items-center gap-1.5 text-xs text-muted hover:text-primary transition-colors font-medium"
      >
        <Plus size={14} /> Add skill
      </button>
    </div>
  )
}

function AddSkillModal({ onClose, onAdd }) {
  const [selected, setSelected] = useState(null)

  return (
    <div
      className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-end md:items-center justify-center z-50 p-4"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <motion.div
        initial={{ y: 60, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="bg-white rounded-3xl p-5 w-full max-w-sm shadow-2xl"
      >
        <h3 className="text-lg font-bold text-dark mb-4">Add a skill</h3>
        <div className="flex flex-wrap gap-2 mb-5 max-h-48 overflow-y-auto no-scrollbar">
          {mockSkills.map((s) => (
            <button
              key={s.id}
              onClick={() => setSelected(s.name)}
              className={`px-3 py-1.5 rounded-full text-sm font-semibold border transition-all
                ${selected === s.name
                  ? 'bg-primary text-white border-primary'
                  : 'bg-gray-50 text-dark border-gray-200 hover:border-primary hover:text-primary'}`}
            >
              {s.name}
            </button>
          ))}
        </div>
        <div className="flex gap-2">
          <Button variant="ghost" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1" disabled={!selected} onClick={() => { onAdd(selected); onClose() }}>
            Add
          </Button>
        </div>
      </motion.div>
    </div>
  )
}

export default function ProfilePage() {
  const { user } = useAuth()
  const profile = user ?? mockCurrentUser

  const [editMode, setEditMode] = useState(false)
  const [bio, setBio] = useState(profile.bio)
  const [teachSkills, setTeachSkills] = useState([...mockCurrentUser.teachSkills])
  const [learnSkills, setLearnSkills] = useState([...mockCurrentUser.learnSkills])
  const [modal, setModal] = useState(null) // 'teach' | 'learn' | null

  return (
    <div className="page-enter space-y-5">
      {/* Hero card */}
      <div className="bg-white rounded-3xl border border-orange-100 shadow-sm overflow-hidden">
        <div className={`h-24 bg-gradient-to-br ${profile.avatarBg}`} />
        <div className="px-5 pb-5">
          <div className="-mt-10 mb-3 flex items-end justify-between">
            <Avatar user={profile} size="xl" className="border-4 border-white shadow-lg" />
            <Button
              variant={editMode ? 'primary' : 'ghost'}
              size="sm"
              onClick={() => setEditMode(!editMode)}
            >
              <Edit2 size={14} /> {editMode ? 'Save' : 'Edit'}
            </Button>
          </div>

          <h1 className="text-xl font-bold text-dark">{profile.displayName}</h1>
          <p className="text-sm text-muted">{profile.email}</p>

          <div className="flex items-center gap-1 mt-2">
            <StarDisplay rating={profile.averageRating} />
          </div>

          <div className="flex gap-4 mt-3 text-center">
            <div>
              <p className="font-bold text-dark">{profile.sessionsCompleted ?? 0}</p>
              <p className="text-xs text-muted">Sessions</p>
            </div>
            <div>
              <p className="font-bold text-dark">{teachSkills.length}</p>
              <p className="text-xs text-muted">Teaching</p>
            </div>
            <div>
              <p className="font-bold text-dark">{learnSkills.length}</p>
              <p className="text-xs text-muted">Learning</p>
            </div>
          </div>
        </div>
      </div>

      {/* Bio */}
      <div className="bg-white rounded-2xl border border-orange-100 shadow-sm p-4">
        <p className="text-xs font-bold text-muted uppercase tracking-wide mb-2">About</p>
        {editMode ? (
          <textarea
            value={bio}
            onChange={(e) => setBio(e.target.value)}
            rows={3}
            className="w-full px-3 py-2 rounded-xl border border-gray-200 focus:border-primary focus:ring-2 focus:ring-orange-200 outline-none text-sm resize-none"
          />
        ) : (
          <p className="text-sm text-dark leading-relaxed">{bio || 'No bio yet.'}</p>
        )}
      </div>

      {/* Skills */}
      <SkillSection
        title="I teach"
        icon={BookOpen}
        color="text-primary"
        skills={teachSkills}
        onRemove={(s) => setTeachSkills((prev) => prev.filter((x) => x !== s))}
        onAdd={() => setModal('teach')}
      />

      <SkillSection
        title="I want to learn"
        icon={Lightbulb}
        color="text-blue-500"
        skills={learnSkills}
        onRemove={(s) => setLearnSkills((prev) => prev.filter((x) => x !== s))}
        onAdd={() => setModal('learn')}
      />

      {/* Add skill modal */}
      {modal && (
        <AddSkillModal
          onClose={() => setModal(null)}
          onAdd={(skill) => {
            if (modal === 'teach') setTeachSkills((p) => [...p, skill])
            else setLearnSkills((p) => [...p, skill])
          }}
        />
      )}
    </div>
  )
}
