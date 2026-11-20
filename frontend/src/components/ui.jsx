import { useEffect, useRef, useState } from 'react'
import { AnimatePresence, animate, motion, useReducedMotion } from 'motion/react'
import { X } from 'lucide-react'

export function Logo({ size = 40 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" fill="none" aria-hidden="true">
      <rect width="64" height="64" rx="16" fill="#1f4fe0" />
      <path
        d="M14 46V20.5c0-1.4 1.8-2 2.6-.9L32 38l15.4-18.4c.8-1.1 2.6-.5 2.6.9V46"
        stroke="#fff"
        strokeWidth="5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </svg>
  )
}

/** Acompanha o cursor para o efeito de spotlight (ver .spot no CSS). */
export function trackPointer(e) {
  const r = e.currentTarget.getBoundingClientRect()
  e.currentTarget.style.setProperty('--mx', `${e.clientX - r.left}px`)
  e.currentTarget.style.setProperty('--my', `${e.clientY - r.top}px`)
}

export function Spot({ as: Tag = 'div', className = '', children, ...rest }) {
  return (
    <Tag className={`card spot ${className}`} onPointerMove={trackPointer} {...rest}>
      {children}
    </Tag>
  )
}

/** Número que "conta" até o valor final. */
export function CountUp({ value, duration = 0.9 }) {
  const reduzir = useReducedMotion()
  const [n, setN] = useState(reduzir ? value : 0)
  const anterior = useRef(0)

  useEffect(() => {
    if (reduzir) {
      setN(value)
      return undefined
    }
    const controle = animate(anterior.current, value, {
      duration,
      ease: [0.22, 1, 0.36, 1],
      onUpdate: (v) => setN(Math.round(v)),
    })
    anterior.current = value
    return () => controle.stop()
  }, [value, duration, reduzir])

  return <span>{n}</span>
}

/** Texto que entra palavra a palavra com desfoque. */
export function BlurText({ text, className, delay = 0 }) {
  const reduzir = useReducedMotion()
  const palavras = text.split(' ')
  return (
    <span className={className} aria-label={text}>
      {palavras.map((p, i) => (
        <motion.span
          key={`${p}-${i}`}
          aria-hidden="true"
          style={{ display: 'inline-block', whiteSpace: 'pre' }}
          initial={reduzir ? false : { opacity: 0, y: 14, filter: 'blur(10px)' }}
          animate={{ opacity: 1, y: 0, filter: 'blur(0px)' }}
          transition={{ duration: 0.7, delay: delay + i * 0.07, ease: [0.22, 1, 0.36, 1] }}
        >
          {p}
          {i < palavras.length - 1 ? ' ' : ''}
        </motion.span>
      ))}
    </span>
  )
}

export function Badge({ tom = '', children }) {
  return <span className={`badge ${tom}`}>{children}</span>
}

export function Progress({ valor, max }) {
  const pct = Math.min(100, Math.round((valor / max) * 100))
  const tom = pct >= 100 ? 'full' : pct >= 85 ? 'warn' : ''
  return (
    <div className={`progress ${tom}`} role="progressbar" aria-valuenow={valor} aria-valuemax={max}>
      <span style={{ width: `${pct}%` }} />
    </div>
  )
}

export function PageHeader({ eyebrow, titulo, descricao, children }) {
  return (
    <header className="page-head">
      <div>
        {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
        <h1>{titulo}</h1>
        {descricao ? <p>{descricao}</p> : null}
      </div>
      {children}
    </header>
  )
}

export function Empty({ icone: Icone, titulo, children }) {
  return (
    <div className="empty">
      <div className="ico">
        <Icone size={24} />
      </div>
      <strong>{titulo}</strong>
      {children ? <p>{children}</p> : null}
    </div>
  )
}

export function Skeletons({ n = 3 }) {
  return (
    <div className="grid-cards" aria-busy="true">
      {Array.from({ length: n }, (_, i) => (
        <div key={i} className="skeleton" />
      ))}
    </div>
  )
}

export function Modal({ aberto, titulo, descricao, onFechar, children }) {
  useEffect(() => {
    if (!aberto) return undefined
    const esc = (e) => e.key === 'Escape' && onFechar()
    window.addEventListener('keydown', esc)
    return () => window.removeEventListener('keydown', esc)
  }, [aberto, onFechar])

  return (
    <AnimatePresence>
      {aberto ? (
        <motion.div
          className="overlay"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          onMouseDown={(e) => e.target === e.currentTarget && onFechar()}
        >
          <motion.div
            className="modal"
            role="dialog"
            aria-modal="true"
            aria-label={titulo}
            initial={{ opacity: 0, y: 24, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 12, scale: 0.98 }}
            transition={{ type: 'spring', stiffness: 380, damping: 30 }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12 }}>
              <h2>{titulo}</h2>
              <button className="btn btn-icon" onClick={onFechar} aria-label="Fechar">
                <X size={16} />
              </button>
            </div>
            {descricao ? <p className="sub">{descricao}</p> : null}
            {children}
          </motion.div>
        </motion.div>
      ) : null}
    </AnimatePresence>
  )
}

export function Button({ carregando, children, className = '', ...rest }) {
  return (
    <button className={`btn ${className}`} disabled={carregando || rest.disabled} {...rest}>
      {carregando ? <span className="spinner" /> : null}
      {children}
    </button>
  )
}

export const brl = (v) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v)
export const dataCurta = (iso) =>
  new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })

export const STATUS_SEMESTRE = {
  PLANEJADO: ['neutral', 'Planejado'],
  MATRICULAS_ABERTAS: ['ok', 'Matrículas abertas'],
  ENCERRADO: ['', 'Encerrado'],
}
export const STATUS_OFERTA = {
  ABERTA: ['warn', 'Em matrícula'],
  ATIVA: ['ok', 'Confirmada'],
  CANCELADA: ['bad', 'Cancelada'],
}
export const STATUS_MATRICULA = {
  ATIVA: ['ok', 'Ativa'],
  CANCELADA: ['neutral', 'Cancelada'],
  DISCIPLINA_CANCELADA: ['bad', 'Disciplina cancelada'],
}
