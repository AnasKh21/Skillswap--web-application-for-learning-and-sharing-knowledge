import { NavLink, useNavigate } from 'react-router-dom'
import { Flame, Inbox, CalendarDays, User, LogOut, Zap } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import Avatar from '../ui/Avatar'

const links = [
  { to: '/discover',  icon: Flame,        label: 'Discover' },
  { to: '/requests',  icon: Inbox,        label: 'Requests' },
  { to: '/sessions',  icon: CalendarDays, label: 'Sessions' },
  { to: '/profile',   icon: User,         label: 'My Profile' },
]

export default function Sidebar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/auth')
  }

  return (
    <aside className="hidden md:flex flex-col w-64 bg-white border-r border-orange-100 h-screen sticky top-0 py-6 px-4">
      {/* Logo */}
      <div className="flex items-center gap-2.5 mb-8 px-2">
        <div className="w-10 h-10 rounded-2xl bg-gradient-brand flex items-center justify-center shadow-card">
          <Zap size={20} className="text-white fill-white" />
        </div>
        <span className="text-xl font-extrabold text-dark tracking-tight">
          Skill<span className="text-primary">Swap</span>
        </span>
      </div>

      {/* Nav links */}
      <nav className="flex-1 space-y-1">
        {links.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-3 rounded-2xl font-medium transition-all
               ${isActive
                 ? 'bg-orange-50 text-primary'
                 : 'text-muted hover:bg-orange-50 hover:text-primary'}`
            }
          >
            <Icon size={20} />
            {label}
          </NavLink>
        ))}
      </nav>

      {/* User footer */}
      <div className="border-t border-orange-100 pt-4 mt-4">
        <div className="flex items-center gap-3 px-2 mb-3">
          <Avatar user={user} size="sm" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-dark truncate">{user?.displayName}</p>
            <p className="text-xs text-muted truncate">{user?.email}</p>
          </div>
        </div>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 w-full px-4 py-2.5 rounded-xl text-sm text-muted hover:bg-red-50 hover:text-danger transition-colors"
        >
          <LogOut size={16} /> Sign out
        </button>
      </div>
    </aside>
  )
}
