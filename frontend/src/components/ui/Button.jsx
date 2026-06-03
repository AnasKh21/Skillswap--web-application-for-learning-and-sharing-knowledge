const variants = {
  primary:  'bg-gradient-brand text-white shadow-card hover:shadow-card-hover hover:scale-[1.02]',
  danger:   'bg-gradient-danger text-white shadow-md hover:scale-[1.02]',
  ghost:    'bg-white/80 border border-gray-200 text-dark hover:bg-orange-50 hover:border-primary/30',
  outline:  'border-2 border-primary text-primary hover:bg-primary hover:text-white',
}

const sizes = {
  sm:   'px-4 py-2 text-sm rounded-xl',
  md:   'px-6 py-3 text-base rounded-2xl',
  lg:   'px-8 py-4 text-lg rounded-2xl',
  icon: 'p-3 rounded-full',
}

export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  className = '',
  ...props
}) {
  return (
    <button
      className={`
        inline-flex items-center justify-center gap-2
        font-semibold transition-all duration-200 active:scale-95
        disabled:opacity-50 disabled:pointer-events-none
        ${variants[variant]} ${sizes[size]} ${className}
      `}
      {...props}
    >
      {children}
    </button>
  )
}
