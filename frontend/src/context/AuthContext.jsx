import { createContext, useContext, useEffect, useState } from 'react'
import * as authApi from '../api/auth'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [loading, setLoading] = useState(true)  // vrai pendant la vérification initiale

  // Au démarrage de l'app : vérifie si un cookie de session existe déjà
  // Si oui → l'utilisateur reste connecté sans re-saisir son mot de passe
  useEffect(() => {
    authApi.getMe()
      .then(res => setUser(res.data))
      .catch(() => setUser(null))   // 401 = pas de session active, c'est normal
      .finally(() => setLoading(false))
  }, [])

  const login = async (email, password) => {
    const res = await authApi.login({ email, password })
    setUser(res.data.user)   // le token est dans le cookie, pas dans res.data.token
  }

  const register = async (displayName, email, password) => {
    await authApi.register({ displayName, email, password })
    // Après l'inscription, on connecte automatiquement
    const res = await authApi.login({ email, password })
    setUser(res.data.user)
  }

  // ─────────────────────────────────────────────────────────────
  // EXERCICE 6 — Implémente la déconnexion
  //
  // Quand l'utilisateur clique "Logout" :
  //   1. Appelle authApi.logout()  (try/catch — si ça échoue, on continue quand même)
  //   2. Remet user à null  → l'app redirige vers /login automatiquement
  //
  // Hints :
  //   try {
  //     await authApi.logout()
  //   } catch (_) { /* ignore les erreurs réseau */ }
  //   setUser(null)
  // ─────────────────────────────────────────────────────────────
  const logout = async () => {
    // TODO Exercice 6 — complète ici
    try {
      await authApi.logout()
    } catch (_) {
      /* ignore les erreurs réseau */
    }
    setUser(null)
  }

  // Si la vérification initiale est en cours, on n'affiche rien encore
  // (évite un flash de la page de login alors que l'utilisateur est connecté)
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-surface">
        <div className="w-8 h-8 border-2 border-primary border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  const updateUser = (patches) => setUser(prev => ({ ...prev, ...patches }))

  return (
    <AuthContext.Provider value={{ user, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
