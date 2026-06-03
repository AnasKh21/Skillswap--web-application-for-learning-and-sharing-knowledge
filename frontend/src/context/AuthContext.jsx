import { createContext, useContext, useState } from 'react'
import { mockCurrentUser } from '../data/mockData'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  // Mock auth: replace with real JWT logic when Spring Security is configured.
  const [user, setUser] = useState(null)

  const login = (email, _password) => {
    setUser({ ...mockCurrentUser, email })
  }

  const register = (email, displayName, _password) => {
    setUser({ ...mockCurrentUser, email, displayName })
  }

  const logout = () => setUser(null)

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
