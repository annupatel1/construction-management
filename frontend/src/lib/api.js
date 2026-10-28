import axios from 'axios'

export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
export const TOKEN_KEY = 'cm_token'

const api = axios.create({ baseURL: API_URL })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || ''
    // An expired / invalid token on a protected call logs the user out.
    if (err.response?.status === 401 && !url.includes('/api/auth/')) {
      localStorage.removeItem(TOKEN_KEY)
      window.dispatchEvent(new Event('auth:logout'))
    }
    return Promise.reject(err)
  },
)

export function errorMessage(err, fallback = 'Something went wrong. Please try again.') {
  let message = fallback
  if (err?.response?.data?.message) message = err.response.data.message
  else if (err?.request && !err.response) message = 'Cannot reach the server. Is the backend running?'
  const requestId = err?.response?.data?.requestId
  return requestId ? `${message} (ref: ${requestId})` : message
}

/**
 * Downloads a protected file with the auth header. The API client cannot use a plain
 * anchor for exports because the JWT lives in localStorage.
 */
export async function downloadFile(path, fallbackName = 'buildtrack-export.csv') {
  let response
  try {
    response = await api.get(path, { responseType: 'blob' })
  } catch (err) {
    // Blob responses hide the JSON error body; parse it so the toast keeps the
    // server message and request id.
    if (err?.response?.data instanceof Blob) {
      try {
        err.response.data = JSON.parse(await err.response.data.text())
      } catch {
        // Keep the original blob/error when the body is not JSON.
      }
    }
    throw err
  }
  const disposition = response.headers['content-disposition'] || ''
  const match = disposition.match(/filename="?([^";]+)"?/i)
  const filename = match?.[1] || fallbackName
  const url = URL.createObjectURL(new Blob([response.data], {
    type: response.headers['content-type'] || 'text/csv;charset=utf-8',
  }))
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

export const errorCode = (err) => err?.response?.data?.code

export default api
