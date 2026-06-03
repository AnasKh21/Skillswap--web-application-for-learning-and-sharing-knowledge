export default function Avatar({ user, size = 'md', className = '' }) {
  const sizes = {
    sm:   'w-8  h-8  text-xs',
    md:   'w-12 h-12 text-sm',
    lg:   'w-16 h-16 text-lg',
    xl:   'w-24 h-24 text-2xl',
    '2xl':'w-32 h-32 text-3xl',
  }

  const bg = user?.avatarBg ?? 'from-primary to-accent'

  return (
    <div className={`
      ${sizes[size]} rounded-full flex items-center justify-center
      bg-gradient-to-br ${bg} text-white font-bold flex-shrink-0
      ${className}
    `}>
      {user?.avatarInitials ?? '?'}
    </div>
  )
}
