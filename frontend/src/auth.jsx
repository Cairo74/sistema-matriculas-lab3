import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { api, setUnauthorizedHandler } from './api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api
      .get('/auth/me')
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false))
    setUnauthorizedHandler(() => setUser(null))
  }, [])

  const login = useCallback(async (loginName, senha) => {
    const u = await api.post('/auth/login', { login: loginName, senha })
    setUser(u)
    return u
  }, [])

  const logout = useCallback(async () => {
    await api.post('/auth/logout').catch(() => {})
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, loading, login, logout }), [user, loading, login, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)

export const HOME_POR_PAPEL = {
  ALUNO: '/aluno/matricula',
  PROFESSOR: '/professor/turmas',
  SECRETARIA: '/secretaria/semestres',
}
