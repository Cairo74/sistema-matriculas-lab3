const BASE = import.meta.env.VITE_API_URL ?? '/api'

export class ApiError extends Error {
  constructor(message, status) {
    super(message)
    this.status = status
  }
}

let onUnauthorized = () => {}
export const setUnauthorizedHandler = (fn) => {
  onUnauthorized = fn
}

async function request(method, path, body) {
  let res
  try {
    res = await fetch(BASE + path, {
      method,
      credentials: 'include',
      headers: body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError('Não foi possível conectar ao servidor.', 0)
  }
  if (res.status === 204) return null
  const data = await res.json().catch(() => null)
  if (!res.ok) {
    if (res.status === 401 && path !== '/auth/login' && path !== '/auth/me') onUnauthorized()
    throw new ApiError(data?.mensagem ?? 'Erro inesperado. Tente novamente.', res.status)
  }
  return data
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body ?? {}),
  put: (path, body) => request('PUT', path, body),
  del: (path) => request('DELETE', path),
}
