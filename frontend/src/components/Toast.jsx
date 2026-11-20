import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import { CircleAlert, CircleCheck, Info } from 'lucide-react'

const ToastContext = createContext(() => {})

export function ToastProvider({ children }) {
  const [items, setItems] = useState([])

  const push = useCallback((mensagem, tipo = 'info') => {
    const id = crypto.randomUUID()
    setItems((atual) => [...atual, { id, mensagem, tipo }])
    setTimeout(() => setItems((atual) => atual.filter((t) => t.id !== id)), 4200)
  }, [])

  const api = useMemo(
    () => ({
      ok: (m) => push(m, 'ok'),
      erro: (m) => push(m, 'erro'),
      info: (m) => push(m, 'info'),
    }),
    [push],
  )

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        <AnimatePresence initial={false}>
          {items.map((t) => (
            <motion.div
              key={t.id}
              className={`toast ${t.tipo}`}
              layout
              initial={{ opacity: 0, y: 16, scale: 0.97 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, x: 24 }}
              transition={{ type: 'spring', stiffness: 420, damping: 32 }}
            >
              {t.tipo === 'ok' ? <CircleCheck size={18} color="var(--ok)" /> : null}
              {t.tipo === 'erro' ? <CircleAlert size={18} color="var(--bad)" /> : null}
              {t.tipo === 'info' ? <Info size={18} color="var(--blue-600)" /> : null}
              <span>{t.mensagem}</span>
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    </ToastContext.Provider>
  )
}

export const useToast = () => useContext(ToastContext)
