const styles = {
  orange: 'bg-orange-100 text-primary border border-orange-200',
  yellow: 'bg-yellow-100 text-yellow-700 border border-yellow-200',
  red:    'bg-red-100 text-danger border border-red-200',
  gray:   'bg-gray-100 text-muted border border-gray-200',
  green:  'bg-green-100 text-green-700 border border-green-200',
}

export default function Badge({ children, color = 'orange', className = '' }) {
  return (
    <span className={`
      inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold
      ${styles[color]} ${className}
    `}>
      {children}
    </span>
  )
}
