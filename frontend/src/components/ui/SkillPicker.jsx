import { useState, useMemo } from 'react'
import { motion } from 'framer-motion'
import { X } from 'lucide-react'
import Button from './Button'

// Regroupe les skills par catégorie
function groupByCategory(skills) {
  return skills.reduce((acc, s) => {
    if (!acc[s.category]) acc[s.category] = []
    acc[s.category].push(s)
    return acc
  }, {})
}

const LEVEL_LABELS = ['', 'Beginner', 'Elementary', 'Intermediate', 'Advanced', 'Expert']

export default function SkillPicker({ allSkills, type, onAdd, onClose }) {
  const grouped    = useMemo(() => groupByCategory(allSkills), [allSkills])
  const categories = useMemo(() => Object.keys(grouped).sort(), [grouped])

  const [category, setCategory] = useState(categories[0] ?? '')
  const [selected, setSelected] = useState(null)
  const [level, setLevel]       = useState(3)

  const skillsInCategory = grouped[category] ?? []

  const handleAdd = () => {
    if (!selected) return
    onAdd(selected, level)
    onClose()
  }

  return (
    <div
      className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-end md:items-center justify-center z-50 p-4"
      onClick={e => e.target === e.currentTarget && onClose()}
    >
      <motion.div
        initial={{ y: 60, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="bg-white rounded-3xl w-full max-w-sm shadow-2xl overflow-hidden"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-5 pt-5 pb-3">
          <h3 className="text-lg font-bold text-dark">
            {type === 'teach' ? 'What can you teach?' : 'What do you want to learn?'}
          </h3>
          <button onClick={onClose} className="text-muted hover:text-dark transition-colors">
            <X size={20} />
          </button>
        </div>

        {/* Category tabs */}
        <div className="flex gap-2 px-5 overflow-x-auto no-scrollbar pb-1">
          {categories.map(cat => (
            <button
              key={cat}
              onClick={() => { setCategory(cat); setSelected(null) }}
              className={`px-3 py-1.5 rounded-full text-xs font-semibold whitespace-nowrap transition-all flex-shrink-0
                ${category === cat
                  ? 'bg-primary text-white'
                  : 'bg-gray-100 text-muted hover:bg-orange-50 hover:text-primary'
                }`}
            >
              {cat}
            </button>
          ))}
        </div>

        {/* Skill grid */}
        <div className="px-5 py-3 max-h-44 overflow-y-auto no-scrollbar">
          <div className="flex flex-wrap gap-2">
            {skillsInCategory.map(s => (
              <button
                key={s.id}
                onClick={() => setSelected(s)}
                className={`px-3 py-1.5 rounded-full text-sm font-semibold border transition-all
                  ${selected?.id === s.id
                    ? 'bg-primary text-white border-primary'
                    : 'bg-gray-50 text-dark border-gray-200 hover:border-primary hover:text-primary'
                  }`}
              >
                {s.name}
              </button>
            ))}
          </div>
        </div>

        {/* Level selector */}
        <div className="px-5 pb-4">
          <p className="text-xs font-semibold text-muted uppercase tracking-wide mb-2">
            Level — <span className="text-primary normal-case font-bold">{LEVEL_LABELS[level]}</span>
          </p>
          <div className="flex gap-2">
            {[1, 2, 3, 4, 5].map(n => (
              <button
                key={n}
                onClick={() => setLevel(n)}
                className={`flex-1 py-2 rounded-xl text-sm font-bold border transition-all
                  ${level === n
                    ? 'bg-primary text-white border-primary'
                    : 'bg-gray-50 text-muted border-gray-200 hover:border-primary hover:text-primary'
                  }`}
              >
                {n}
              </button>
            ))}
          </div>
        </div>

        {/* Actions */}
        <div className="flex gap-2 px-5 pb-5">
          <Button variant="ghost" className="flex-1" onClick={onClose}>Cancel</Button>
          <Button className="flex-1" disabled={!selected} onClick={handleAdd}>
            Add {selected ? `"${selected.name}"` : ''}
          </Button>
        </div>
      </motion.div>
    </div>
  )
}
