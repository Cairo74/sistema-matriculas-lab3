import { useCallback, useEffect, useState } from 'react'
import { api } from './api.js'
import { useToast } from './components/Toast.jsx'

/** Carrega vários endpoints em paralelo e permite recarregar. */
export function useCarregar(caminhos) {
  const toast = useToast()
  const chave = caminhos.join('|')
  const [dados, setDados] = useState(null)
  const [carregando, setCarregando] = useState(true)

  const recarregar = useCallback(async () => {
    try {
      setDados(await Promise.all(caminhos.map((c) => api.get(c))))
    } catch (e) {
      toast.erro(e.message)
    } finally {
      setCarregando(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [chave])

  useEffect(() => {
    recarregar()
  }, [recarregar])

  return { dados, carregando, recarregar }
}
