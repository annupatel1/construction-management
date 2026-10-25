import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import api, { TOKEN_KEY } from '../lib/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(!!localStorage.getItem(TOKEN_KEY))

  const logout = useCallback(() => {
    const token = localStorage.getItem(TOKEN_KEY)
    // Revoke server-side before dropping the local token. A failure here (offline,
    // already-expired token) must not stop the local logout.
    if (token) {
      api.post('/api/auth/logout', null, { headers: { Authorization: `Bearer ${token}` } }).catch(() => {})
    }
    localStorage.removeItem(TOKEN_KEY)
    setUser(null)
  }, [])

  const login = useCallback((token, u) => {
    localStorage.setItem(TOKEN_KEY, token)
    setUser(u)
  }, [])

  const refreshUser = useCallback(async () => {
    const { data } = await api.get('/api/users/me')
    setUser(data)
    return data
  }, [])

  // restore the session on page load
  useEffect(() => {
    if (!localStorage.getItem(TOKEN_KEY)) return
    refreshUser()
      .catch((err) => { if (err.response?.status === 401) logout() })
      .finally(() => setLoading(false))
  }, [refreshUser, logout])

  useEffect(() => {
    const onLogout = () => setUser(null)
    window.addEventListener('auth:logout', onLogout)
    return () => window.removeEventListener('auth:logout', onLogout)
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
