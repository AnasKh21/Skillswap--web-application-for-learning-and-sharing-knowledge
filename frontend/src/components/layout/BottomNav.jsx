import { NavLink } from 'react-router-dom'
import { Flame, Inbox, CalendarDays, User } from 'lucide-react'

const links = [
  { to: '/discover',  icon: Flame,        label: 'Discover' },
  { to: '/requests',  icon: Inbox,        label: 'Requests' },
  { to: '/sessions',  icon: CalendarDays, label: 'Sessions' },
  { to: '/profile',   icon: User,         label: 'Profile'  },
]

export default function BottomNav() {
  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-white/90 backdrop-blur border-t border-orange-100 flex md:hidden z-50">
      {links.map(({ to, icon: Icon, label }) => (
        <NavLink
          key={to}
          to={to}
          className={({ isActive }) =>
            `flex-1 flex flex-col items-center py-3 gap-0.5 transition-colors
             ${isActive ? 'text-primary' : 'text-muted hover:text-primary'}`
          }
        >
          {({ isActive }) => (
            <>
              <Icon size={22} className={isActive ? 'fill-orange-100' : ''} />
              <span className="text-[10px] font-semibold">{label}</span>
            </>
          )}
        </NavLink>
      ))}
    </nav>
  )
}
