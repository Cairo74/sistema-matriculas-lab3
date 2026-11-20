import { Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { HOME_POR_PAPEL, useAuth } from './auth.jsx'
import Layout from './components/Layout.jsx'
import Login from './pages/Login.jsx'
import Matricula from './pages/aluno/Matricula.jsx'
import Historico from './pages/aluno/Historico.jsx'
import Cobranca from './pages/aluno/Cobranca.jsx'
import Turmas from './pages/professor/Turmas.jsx'
import Semestres from './pages/secretaria/Semestres.jsx'
import { Alunos, Cursos, Disciplinas, Professores } from './pages/secretaria/Cadastros.jsx'

/** Exige sessão e, opcionalmente, um perfil específico. */
function Protegida({ papel }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="fullscreen-center"><span className="spinner" style={{ borderColor: 'var(--blue-200)', borderTopColor: 'var(--blue-600)' }} /></div>
  if (!user) return <Navigate to="/login" replace />
  if (papel && user.papel !== papel) return <Navigate to={HOME_POR_PAPEL[user.papel]} replace />
  return <Outlet />
}

function Inicio() {
  const { user, loading } = useAuth()
  if (loading) return null
  return <Navigate to={user ? HOME_POR_PAPEL[user.papel] : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route element={<Protegida />}>
        <Route element={<Layout />}>
          <Route element={<Protegida papel="ALUNO" />}>
            <Route path="/aluno/matricula" element={<Matricula />} />
            <Route path="/aluno/historico" element={<Historico />} />
            <Route path="/aluno/cobranca" element={<Cobranca />} />
          </Route>
          <Route element={<Protegida papel="PROFESSOR" />}>
            <Route path="/professor/turmas" element={<Turmas />} />
          </Route>
          <Route element={<Protegida papel="SECRETARIA" />}>
            <Route path="/secretaria/semestres" element={<Semestres />} />
            <Route path="/secretaria/cursos" element={<Cursos />} />
            <Route path="/secretaria/disciplinas" element={<Disciplinas />} />
            <Route path="/secretaria/professores" element={<Professores />} />
            <Route path="/secretaria/alunos" element={<Alunos />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Inicio />} />
    </Routes>
  )
}
