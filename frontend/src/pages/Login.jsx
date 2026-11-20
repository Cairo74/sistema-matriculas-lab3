import { useState } from 'react'
import { Navigate } from 'react-router-dom'
import { CalendarCheck, ReceiptText, ShieldCheck } from 'lucide-react'
import { HOME_POR_PAPEL, useAuth } from '../auth.jsx'
import { BlurText, Button, Logo, trackPointer } from '../components/ui.jsx'

const DEMO = [
  ['secretaria', 'Secretaria'],
  ['helena', 'Professora'],
  ['ana', 'Aluna'],
]

export default function Login() {
  const { user, login } = useAuth()
  const [loginName, setLoginName] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  if (user) return <Navigate to={HOME_POR_PAPEL[user.papel]} replace />

  async function entrar(e) {
    e.preventDefault()
    setErro('')
    setEnviando(true)
    try {
      await login(loginName, senha)
    } catch (err) {
      setErro(err.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="login">
      <section className="login-art" onPointerMove={trackPointer}>
        <div className="brand">
          <Logo size={42} />
          <div>
            <strong>Meridiano</strong>
            <small>Universidade</small>
          </div>
        </div>

        <div>
          <h1>
            <BlurText text="Sua matrícula," />
            <br />
            <em>
              <BlurText text="sem fila." delay={0.25} />
            </em>
          </h1>
          <p className="lead">
            Monte seu semestre, acompanhe as vagas em tempo real e veja a cobrança atualizar a cada decisão.
          </p>
        </div>

        <ul>
          <li>
            <CalendarCheck size={18} /> Períodos de matrícula abertos pela secretaria
          </li>
          <li>
            <ShieldCheck size={18} /> Acesso individual por perfil: aluno, professor e secretaria
          </li>
          <li>
            <ReceiptText size={18} /> Cobrança do semestre notificada automaticamente
          </li>
        </ul>
      </section>

      <section className="login-form">
        <form onSubmit={entrar}>
          <h2>Entrar</h2>
          <p className="sub">Use o login e a senha fornecidos pela secretaria.</p>

          <div className="field">
            <label htmlFor="login">Login</label>
            <input
              id="login"
              className="input"
              autoComplete="username"
              autoFocus
              required
              value={loginName}
              onChange={(e) => setLoginName(e.target.value)}
            />
          </div>
          <div className="field">
            <label htmlFor="senha">Senha</label>
            <input
              id="senha"
              type="password"
              className="input"
              autoComplete="current-password"
              required
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
            />
          </div>

          {erro ? (
            <p className="error-text" role="alert">
              {erro}
            </p>
          ) : null}

          <Button type="submit" className="btn-primary" carregando={enviando} style={{ height: 46 }}>
            Entrar
          </Button>

          {import.meta.env.DEV ? (
            <div className="demo">
              <b>Ambiente de demonstração</b>
              <div>
                {DEMO.map(([l, r]) => (
                  <button key={l} type="button" onClick={() => setLoginName(l)}>
                    {r}: {l}
                  </button>
                ))}
              </div>
              <span>A senha padrão está no README.</span>
            </div>
          ) : null}
        </form>
      </section>
    </div>
  )
}
