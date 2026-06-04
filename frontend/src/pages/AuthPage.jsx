import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { Zap, Mail, Lock, User, ArrowRight, Eye, EyeOff } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import Button from '../components/ui/Button'

const Field = ({ icon: Icon, type = 'text', placeholder, value, onChange, rightEl }) => (
  <div className="relative">
    <Icon size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-muted pointer-events-none" />
    <input
      type={type}
      placeholder={placeholder}
      value={value}
      onChange={onChange}
      className="w-full pl-11 pr-12 py-3.5 rounded-2xl border border-gray-200 focus:border-primary focus:ring-2 focus:ring-orange-200 outline-none transition bg-white text-dark placeholder:text-gray-400 text-sm"
    />
    {rightEl && <div className="absolute right-4 top-1/2 -translate-y-1/2">{rightEl}</div>}
  </div>
)

export default function AuthPage() {
  const [tab, setTab]         = useState('login')
  const [showPass, setShowPass] = useState(false)
  const [email, setEmail]     = useState('')
  const [password, setPassword] = useState('')
  const [name, setName]       = useState('')
  const [error, setError]     = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { login, register }   = useAuth()
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      if (tab === 'login') {
        await login(email, password)
      } else {
        // ─────────────────────────────────────────────────────────
        // EXERCICE 7 — Appelle register() avec les bons paramètres
        //
        // La fonction register() dans AuthContext attend :
        //   register(displayName, email, password)
        //
        // Le formulaire a un champ "name" (variable : name)
        // mais l'API attend "displayName".
        //
        // Hints :
        //   - Le premier argument de register() est le displayName
        //   - La variable du formulaire s'appelle "name"
        //   - Donc : await register(???, email, password)
        //
        // ─────────────────────────────────────────────────────────
        // TODO Exercice 7 — remplace la ligne ci-dessous
        await register(name, email, password)
      }
      navigate('/discover')
    } catch (err) {
      const msg = err.response?.data?.message ?? err.message ?? 'Something went wrong'
      setError(msg)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* ── Left panel (desktop only) ─── */}
      <div className="hidden md:flex flex-1 bg-gradient-hero items-center justify-center p-12 relative overflow-hidden">
        <div className="absolute -top-20 -left-20 w-80 h-80 bg-white/10 rounded-full" />
        <div className="absolute -bottom-10 -right-10 w-60 h-60 bg-white/10 rounded-full" />
        <div className="relative z-10 text-white text-center">
          <div className="w-20 h-20 rounded-3xl bg-white/20 backdrop-blur flex items-center justify-center mx-auto mb-6 shadow-2xl">
            <Zap size={40} className="text-white fill-white" />
          </div>
          <h1 className="text-5xl font-black mb-4 tracking-tight">SkillSwap</h1>
          <p className="text-xl text-white/80 max-w-xs leading-relaxed">
            Trade your talent, grow without limits.
          </p>
          <div className="mt-10 flex flex-col gap-3 text-left">
            {['Find people with skills you want to learn',
              'Offer what you know in return',
              'Schedule sessions, grow together'].map((t) => (
              <div key={t} className="flex items-center gap-3 text-white/90 text-sm">
                <span className="w-6 h-6 rounded-full bg-white/20 flex items-center justify-center text-xs font-bold flex-shrink-0">✓</span>
                {t}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* ── Right panel (form) ─── */}
      <div className="flex-1 flex items-center justify-center p-6 bg-surface">
        <div className="w-full max-w-sm">
          <div className="md:hidden text-center mb-8">
            <div className="w-14 h-14 rounded-2xl bg-gradient-brand flex items-center justify-center mx-auto mb-3 shadow-card">
              <Zap size={28} className="text-white fill-white" />
            </div>
            <h1 className="text-2xl font-black text-dark">Skill<span className="text-primary">Swap</span></h1>
          </div>

          <h2 className="text-2xl font-bold text-dark mb-1">
            {tab === 'login' ? 'Welcome back 👋' : 'Join SkillSwap 🚀'}
          </h2>
          <p className="text-muted text-sm mb-6">
            {tab === 'login' ? 'Sign in to continue swapping skills' : 'Create your account for free'}
          </p>

          <div className="flex gap-1 bg-orange-50 p-1 rounded-2xl mb-6">
            {['login', 'register'].map((t) => (
              <button
                key={t}
                onClick={() => { setTab(t); setError(null) }}
                className={`flex-1 py-2 rounded-xl text-sm font-semibold transition-all
                  ${tab === t ? 'bg-white text-primary shadow-sm' : 'text-muted hover:text-primary'}`}
              >
                {t === 'login' ? 'Sign In' : 'Sign Up'}
              </button>
            ))}
          </div>

          {error && (
            <div className="mb-4 px-4 py-3 rounded-2xl bg-red-50 border border-red-200 text-red-600 text-sm">
              {error}
            </div>
          )}

          <AnimatePresence mode="wait">
            <motion.form
              key={tab}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -8 }}
              transition={{ duration: 0.15 }}
              onSubmit={handleSubmit}
              className="space-y-4"
            >
              {tab === 'register' && (
                <Field icon={User} placeholder="Display name" value={name} onChange={(e) => setName(e.target.value)} />
              )}
              <Field icon={Mail} type="email" placeholder="Email address" value={email} onChange={(e) => setEmail(e.target.value)} />
              <Field
                icon={Lock}
                type={showPass ? 'text' : 'password'}
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                rightEl={
                  <button type="button" onClick={() => setShowPass(!showPass)} className="text-muted hover:text-dark">
                    {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                  </button>
                }
              />
              <Button type="submit" size="lg" className="w-full mt-2" disabled={submitting}>
                {submitting ? 'Loading...' : tab === 'login' ? 'Sign In' : 'Create Account'}
                {!submitting && <ArrowRight size={18} />}
              </Button>
            </motion.form>
          </AnimatePresence>
        </div>
      </div>
    </div>
  )
}
