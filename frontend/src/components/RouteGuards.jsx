import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { PageLoader } from './ui'

/** Must be logged in */
export function RequireAuth() {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <PageLoader />
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}

/** Only for the listed roles */
export function RequireRole({ roles }) {
  const { user } = useAuth()
  if (!roles.includes(user.roleName)) return <Navigate to="/" replace />
  return <Outlet />
}

/** Login / register pages: bounce to the app if already logged in */
export function PublicOnly() {
  const { user, loading } = useAuth()
  if (loading) return <PageLoader />
  if (user) return <Navigate to="/" replace />
  return <Outlet />
}
