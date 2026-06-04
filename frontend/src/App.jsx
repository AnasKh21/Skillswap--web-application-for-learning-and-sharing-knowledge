import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Layout from './components/layout/Layout'
import LandingPage from './pages/LandingPage'
import AuthPage from './pages/AuthPage'
import DiscoverPage from './pages/DiscoverPage'
import RequestsPage from './pages/RequestsPage'
import SessionsPage from './pages/SessionsPage'
import ProfilePage from './pages/ProfilePage'

function ProtectedRoute({ children }) {
  const { user } = useAuth()
  return user ? children : <Navigate to="/auth" replace />
}

export default function App() {
  const { user } = useAuth()

  return (
    <Routes>
      <Route path="/" element={user ? <Navigate to="/discover" replace /> : <LandingPage />} />
      <Route
        path="/auth"
        element={user ? <Navigate to="/discover" replace /> : <AuthPage />}
      />

      <Route
        path="/*"
        element={
          <ProtectedRoute>
            <Layout>
              <Routes>
                <Route path="/discover"  element={<DiscoverPage />} />
                <Route path="/requests"  element={<RequestsPage />} />
                <Route path="/sessions"  element={<SessionsPage />} />
                <Route path="/profile"   element={<ProfilePage />} />
                <Route path="*"          element={<Navigate to="/discover" replace />} />
              </Routes>
            </Layout>
          </ProtectedRoute>
        }
      />
    </Routes>
  )
}
