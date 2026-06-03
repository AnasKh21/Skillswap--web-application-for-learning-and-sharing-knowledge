const categoryColors = {
  Music:       'bg-purple-100 text-purple-700 border-purple-200',
  Programming: 'bg-blue-100  text-blue-700  border-blue-200',
  Art:         'bg-pink-100  text-pink-700  border-pink-200',
  Sport:       'bg-green-100 text-green-700 border-green-200',
  Language:    'bg-yellow-100 text-yellow-700 border-yellow-200',
  Lifestyle:   'bg-orange-100 text-orange-700 border-orange-200',
  Design:      'bg-cyan-100  text-cyan-700  border-cyan-200',
}

export default function SkillTag({ name, category, size = 'sm' }) {
  const color = categoryColors[category] ?? 'bg-gray-100 text-gray-700 border-gray-200'
  const sizeClass = size === 'sm' ? 'px-2.5 py-1 text-xs' : 'px-4 py-1.5 text-sm'

  return (
    <span className={`
      inline-flex items-center rounded-full border font-medium
      ${color} ${sizeClass}
    `}>
      {name}
    </span>
  )
}
