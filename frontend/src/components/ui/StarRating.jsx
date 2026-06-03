import { Star } from 'lucide-react'
import { useState } from 'react'

export function StarDisplay({ rating, size = 16 }) {
  return (
    <div className="flex items-center gap-0.5">
      {[1, 2, 3, 4, 5].map((n) => (
        <Star
          key={n}
          size={size}
          className={n <= Math.round(rating) ? 'text-accent fill-accent' : 'text-gray-300 fill-gray-100'}
        />
      ))}
      <span className="ml-1 text-sm font-semibold text-dark">
        {rating?.toFixed(1) ?? '—'}
      </span>
    </div>
  )
}

export function StarPicker({ value, onChange }) {
  const [hovered, setHovered] = useState(0)
  const active = hovered || value

  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          onClick={() => onChange(n)}
          onMouseEnter={() => setHovered(n)}
          onMouseLeave={() => setHovered(0)}
          className="transition-transform hover:scale-125 active:scale-95"
        >
          <Star
            size={28}
            className={n <= active ? 'text-accent fill-accent' : 'text-gray-300 fill-gray-100'}
          />
        </button>
      ))}
    </div>
  )
}
