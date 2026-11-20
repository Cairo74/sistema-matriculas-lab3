import { useEffect, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { AnimatePresence, motion } from 'motion/react'
import {
  BookOpenCheck,
  CalendarRange,
  GraduationCap,
  LibraryBig,
  LogOut,
  Menu,
  Receipt,
  ScrollText,
  Users,
  UsersRound,
  Presentation,
} from 'lucide-react'
import { useAuth } from '../auth.jsx'
import { Logo } from './ui.jsx'

const MENUS = {
  ALUNO: [
    { to: '/aluno/matricula', rotulo: 'Matrícula', icone: BookOpenCheck },
    { to: '/aluno/historico', rotulo: 'Minhas matrículas', icone: ScrollText },
    { to: '/aluno/cobranca', rotulo: 'Cobrança', icone: Receipt },
  ],
  PROFESSOR: [{ to: '/professor/turmas', rotulo: 'Minhas turmas', icone: Presentation }],
  SECRETARIA: [
    { to: '/secretaria/semestres', rotulo: 'Semestres', icone: CalendarRange },
    { to: '/secretaria/cursos', rotulo: 'Cursos', icone: GraduationCap },
    { to: '/secretaria/disciplinas', rotulo: 'Disciplinas', icone: LibraryBig },
    { to: '/secretaria/professores', rotulo: 'Professores', icone: UsersRound },
    { to: '/secretaria/alunos', rotulo: 'Alunos', icone: Users },
  ],
}

const ROTULO_PAPEL = { ALUNO: 'Aluno', PROFESSOR: 'Professor', SECRETARIA: 'Secretaria' }

export default function Layout() {
  const { user, logout } = useAuth()
  const [aberto, setAberto] = useState(false)
  const location = useLocation()

  useEffect(() => setAberto(false), [location.pathname])

  const iniciais = user.nome
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0])
    .join('')
    .toUpperCase()

  return (
    <div className="shell">
      <aside className={`sidebar ${aberto ? 'open' : ''}`}>
        <div className="brand">
          <Logo size={38} />
          <div>
            <strong>Meridiano</strong>
            <small>Matrículas</small>
          </div>
        </div>

        <nav className="nav" aria-label="Principal">
          <p className="nav-label">{ROTULO_PAPEL[user.papel]}</p>
          {MENUS[user.papel].map(({ to, rotulo, icone: Icone }) => (
            <NavLink key={to} to={to} className={({ isActive }) => (isActive ? 'active' : '')}>
              {({ isActive }) => (
                <>
                  {isActive ? (
                    <motion.span
                      layoutId="nav-pill"
                      className="pill"
                      transition={{ type: 'spring', stiffness: 420, damping: 34 }}
                    />
                  ) : null}
                  <Icone size={18} />
                  <span>{rotulo}</span>
                </>
              )}
            </NavLink>
          ))}
        </nav>

        <div className="me">
          <div className="avatar" aria-hidden="true">
            {iniciais}
          </div>
          <div>
            <strong>{user.nome}</strong>
            <small>{user.cursoNome ?? ROTULO_PAPEL[user.papel]}</small>
          </div>
          <button className="btn btn-icon" onClick={logout} aria-label="Sair" title="Sair">
            <LogOut size={16} />
          </button>
        </div>
      </aside>

      {aberto ? <div className="scrim" onClick={() => setAberto(false)} /> : null}

      <div>
        <div className="mobile-bar">
          <button className="btn btn-icon" onClick={() => setAberto(true)} aria-label="Abrir menu">
            <Menu size={18} />
          </button>
          Meridiano
        </div>
        <main className="main">
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.25, ease: [0.22, 1, 0.36, 1] }}
            >
              <Outlet />
            </motion.div>
          </AnimatePresence>
        </main>
      </div>
    </div>
  )
}
