import { useRef } from 'react'
import { Camera } from 'lucide-react'

// Enveloppe n'importe quel élément pour le rendre cliquable et déclenchant un upload.
// Affiche une overlay avec une icône caméra au survol.
export default function ImageUpload({ onUpload, children, className = '', rounded = 'rounded-full' }) {
  const inputRef = useRef(null)

  const handleChange = (e) => {
    const file = e.target.files?.[0]
    if (file) {
      onUpload(file)
      e.target.value = ''   // reset pour pouvoir re-uploader le même fichier
    }
  }

  return (
    <div className={`relative cursor-pointer group ${className}`} onClick={() => inputRef.current?.click()}>
      {children}
      <div className={`
        absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100
        transition-opacity flex items-center justify-center ${rounded}
      `}>
        <Camera size={20} className="text-white drop-shadow" />
      </div>
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp,image/gif"
        className="hidden"
        onChange={handleChange}
      />
    </div>
  )
}
