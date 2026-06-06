import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { Edit2, Trash2, BookOpen, Lightbulb } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import Avatar from '../components/ui/Avatar'
import ImageUpload from '../components/ui/ImageUpload'
import SkillPicker from '../components/ui/SkillPicker'
import { StarDisplay } from '../components/ui/StarRating'
import Button from '../components/ui/Button'
import { getMySkills, addSkill, removeSkill, updateProfile } from '../api/users'
import { uploadAvatar, uploadBanner } from '../api/media'
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
        + Add skill
      </button>
    </div>
  )
}

export default function ProfilePage() {
  const { user, updateUser }                  = useAuth()
  const [editMode, setEditMode]               = useState(false)
  const [bio, setBio]                         = useState(user?.bio ?? '')
  const [teachSkills, setTeachSkills]         = useState([])
  const [learnSkills, setLearnSkills]         = useState([])
  const [allSkills, setAllSkills]             = useState([])
  const [userSkillIds, setUserSkillIds]       = useState([])
  const [modal, setModal]                     = useState(null)   // 'teach' | 'learn' | null
  const [loading, setLoading]                 = useState(true)
  const [uploading, setUploading]             = useState(false)

  useEffect(() => {
    Promise.all([getMySkills(), getAllSkills()])
      .then(([skillsRes, allSkillsRes]) => {
        const apiSkills = skillsRes.data
        const { teachSkills: teach, learnSkills: learn } = mapSkillsToUI(apiSkills)
        setTeachSkills(teach.map(name => ({ name })))
        setLearnSkills(learn.map(name => ({ name })))
        setUserSkillIds(apiSkills)
        setAllSkills(allSkillsRes.data)
      })
      .catch(console.error)
      .finally(() => setLoading(false))
  }, [])

  const handleSaveBio = async () => {
    await updateProfile({ displayName: user.displayName, bio })
    updateUser({ bio })
    setEditMode(false)
  }

  const handleAddSkill = async (skill, type, level) => {
    await addSkill({ skillId: skill.id, type, level })
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

  const handleAvatarUpload = async (file) => {
    setUploading(true)
    try {
      const res = await uploadAvatar(file)
      updateUser({ avatarUrl: res.data.avatarUrl })
    } catch (e) {
      const status = e.response?.status
      const msg    = e.response?.data?.message ?? e.message
      alert(`Avatar upload failed (${status ?? 'network error'}): ${msg}`)
    } finally {
      setUploading(false)
    }
  }

  const handleBannerUpload = async (file) => {
    setUploading(true)
    try {
      const res = await uploadBanner(file)
      updateUser({ bannerUrl: res.data.bannerUrl })
    } catch (e) {
      const status = e.response?.status
      const msg    = e.response?.data?.message ?? e.message
      alert(`Banner upload failed (${status ?? 'network error'}): ${msg}`)
    } finally {
      setUploading(false)
    }
  }

  const avatarUser = {
    ...user,
    avatarBg: 'from-orange-400 to-pink-500',
    avatarInitials: user?.displayName?.charAt(0).toUpperCase(),
  }

  const bannerUrl = user?.bannerUrl ?? null

  if (loading || !user) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="page-enter space-y-5">
      <div className="bg-white rounded-3xl border border-orange-100 shadow-sm overflow-hidden">

        {/* Bannière cliquable */}
        <ImageUpload onUpload={handleBannerUpload} className="h-24 w-full" rounded="rounded-none">
          {bannerUrl
            ? <img src={bannerUrl} alt="banner" className="h-24 w-full object-cover" />
            : <div className={`h-24 bg-gradient-to-br ${avatarUser.avatarBg}`} />
          }
        </ImageUpload>

        <div className="px-5 pb-5">
          <div className="-mt-10 mb-3 flex items-end justify-between">

            {/* Avatar cliquable */}
            <ImageUpload onUpload={handleAvatarUpload} className="inline-flex">
              <Avatar user={avatarUser} size="xl" className="border-4 border-white shadow-lg" />
            </ImageUpload>

            <div className="flex items-center gap-2">
              {uploading && <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />}
              <Button variant={editMode ? 'primary' : 'ghost'} size="sm"
                onClick={() => editMode ? handleSaveBio() : setEditMode(true)}>
                <Edit2 size={14} /> {editMode ? 'Save' : 'Edit'}
              </Button>
            </div>
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
        <SkillPicker
          allSkills={allSkills}
          type={modal}
          onClose={() => setModal(null)}
          onAdd={(skill, level) => handleAddSkill(skill, modal === 'teach' ? 'OFFERED' : 'WANTED', level)}
        />
      )}
    </div>
  )
}
