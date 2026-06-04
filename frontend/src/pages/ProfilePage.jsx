import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { Edit2, Plus, Trash2, BookOpen, Lightbulb } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import Avatar from '../components/ui/Avatar'
import { StarDisplay } from '../components/ui/StarRating'
import Button from '../components/ui/Button'
import { getMySkills, addSkill, removeSkill, updateProfile } from '../api/users'
import { getAllSkills } from '../api/skills'
import { mapSkillsToUI } from '../utils/mappers'

function SkillSection({ title, icon: Icon, color, skills, onRemove, onAdd }) {
  return (
    <div className="bg-white rounded-2xl border border-orange-100 shadow-sm p-4">
      <div className={`flex items-center gap-2 mb-3 text-sm font-bold uppercase tracking-wide ${color}`}>
        <Icon size={14} /> {title}
      </div>
      <div className="flex flex-wrap gap-2 mb-3">
        {skills.length === 0 && <p className="text-xs text-muted italic">No skills added yet.</p>}
        {skills.map((skill) => (
          <div key={skill.name ?? skill}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-orange-50 border border-orange-200 rounded-full text-sm font-semibold text-primary group">
            {skill.name ?? skill}
            <button onClick={() => onRemove(skill)}
              className="opacity-0 group-hover:opacity-100 transition-opacity text-muted hover:text-danger">
              <Trash2 size={12} />
            </button>
          </div>
        ))}
      </div>
      <button onClick={onAdd} className="flex items-center gap-1.5 text-xs text-muted hover:text-primary transition-colors font-medium">
        <Plus size={14} /> Add skill
      </button>
    </div>
  )
}

function AddSkillModal({ onClose, onAdd, allSkills }) {
  const [selected, setSelected] = useState(null)
  return (
    <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-end md:items-center justify-center z-50 p-4"
      onClick={e => e.target === e.currentTarget && onClose()}>
      <motion.div initial={{ y: 60, opacity: 0 }} animate={{ y: 0, opacity: 1 }}
        className="bg-white rounded-3xl p-5 w-full max-w-sm shadow-2xl">
        <h3 className="text-lg font-bold text-dark mb-4">Add a skill</h3>
        <div className="flex flex-wrap gap-2 mb-5 max-h-48 overflow-y-auto no-scrollbar">
          {allSkills.map(s => (
            <button key={s.id} onClick={() => setSelected(s)}
              className={`px-3 py-1.5 rounded-full text-sm font-semibold border transition-all
                ${selected?.id === s.id ? 'bg-primary text-white border-primary' : 'bg-gray-50 text-dark border-gray-200 hover:border-primary hover:text-primary'}`}>
              {s.name}
            </button>
          ))}
        </div>
        <div className="flex gap-2">
          <Button variant="ghost" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1" disabled={!selected} onClick={() => { onAdd(selected); onClose() }}>Add</Button>
        </div>
      </motion.div>
    </div>
  )
}

export default function ProfilePage() {
  const { user }                              = useAuth()
  const [editMode, setEditMode]               = useState(false)
  const [bio, setBio]                         = useState(user?.bio ?? '')
  const [teachSkills, setTeachSkills]         = useState([])
  const [learnSkills, setLearnSkills]         = useState([])
  const [allSkills, setAllSkills]             = useState([])
  const [userSkillIds, setUserSkillIds]       = useState([])
  const [modal, setModal]                     = useState(null)
  const [loading, setLoading]                 = useState(true)

  useEffect(() => {
    Promise.all([getMySkills(), getAllSkills()])
      .then(([skillsRes, allSkillsRes]) => {
        const apiSkills = skillsRes.data
        // Exercice 4 : mapSkillsToUI sépare OFFERED → teach et WANTED → learn
        const { teachSkills: teach, learnSkills: learn } = mapSkillsToUI(apiSkills)
        setTeachSkills(teach.map(name => ({ name })))
        setLearnSkills(learn.map(name => ({ name })))
        setUserSkillIds(apiSkills)  // garde les IDs pour la suppression
        setAllSkills(allSkillsRes.data)
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  const handleSaveBio = async () => {
    await updateProfile({ displayName: user.displayName, bio })
    setEditMode(false)
  }

  const handleAddSkill = async (skill, type) => {
    await addSkill({ skillId: skill.id, type, level: 1 })
    if (type === 'OFFERED') setTeachSkills(p => [...p, { name: skill.name }])
    else setLearnSkills(p => [...p, { name: skill.name }])
  }

  const handleRemoveSkill = async (skill, type) => {
    const entry = userSkillIds.find(s => s.skillName === (skill.name ?? skill))
    if (entry) {
      await removeSkill(entry.id)
      if (type === 'teach') setTeachSkills(p => p.filter(s => s.name !== (skill.name ?? skill)))
      else setLearnSkills(p => p.filter(s => s.name !== (skill.name ?? skill)))
    }
  }

  const avatarUser = {
    ...user,
    avatarBg: 'from-orange-400 to-pink-500',
    avatarInitials: user?.displayName?.charAt(0).toUpperCase(),
  }

  if (loading || !user) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="page-enter space-y-5">
      <div className="bg-white rounded-3xl border border-orange-100 shadow-sm overflow-hidden">
        <div className={`h-24 bg-gradient-to-br ${avatarUser.avatarBg}`} />
        <div className="px-5 pb-5">
          <div className="-mt-10 mb-3 flex items-end justify-between">
            <Avatar user={avatarUser} size="xl" className="border-4 border-white shadow-lg" />
            <Button variant={editMode ? 'primary' : 'ghost'} size="sm"
              onClick={() => editMode ? handleSaveBio() : setEditMode(true)}>
              <Edit2 size={14} /> {editMode ? 'Save' : 'Edit'}
            </Button>
          </div>
          <h1 className="text-xl font-bold text-dark">{user.displayName}</h1>
          <p className="text-sm text-muted">{user.email}</p>
          <div className="flex items-center gap-1 mt-2">
            <StarDisplay rating={user.averageRating ?? 0} />
          </div>
          <div className="flex gap-4 mt-3 text-center">
            <div><p className="font-bold text-dark">{teachSkills.length}</p><p className="text-xs text-muted">Teaching</p></div>
            <div><p className="font-bold text-dark">{learnSkills.length}</p><p className="text-xs text-muted">Learning</p></div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-2xl border border-orange-100 shadow-sm p-4">
        <p className="text-xs font-bold text-muted uppercase tracking-wide mb-2">About</p>
        {editMode ? (
          <textarea value={bio} onChange={e => setBio(e.target.value)} rows={3}
            className="w-full px-3 py-2 rounded-xl border border-gray-200 focus:border-primary focus:ring-2 focus:ring-orange-200 outline-none text-sm resize-none" />
        ) : (
          <p className="text-sm text-dark leading-relaxed">{bio || 'No bio yet.'}</p>
        )}
      </div>

      <SkillSection title="I teach" icon={BookOpen} color="text-primary" skills={teachSkills}
        onRemove={s => handleRemoveSkill(s, 'teach')} onAdd={() => setModal('teach')} />
      <SkillSection title="I want to learn" icon={Lightbulb} color="text-blue-500" skills={learnSkills}
        onRemove={s => handleRemoveSkill(s, 'learn')} onAdd={() => setModal('learn')} />

      {modal && (
        <AddSkillModal allSkills={allSkills} onClose={() => setModal(null)}
          onAdd={skill => handleAddSkill(skill, modal === 'teach' ? 'OFFERED' : 'WANTED')} />
      )}
    </div>
  )
}
