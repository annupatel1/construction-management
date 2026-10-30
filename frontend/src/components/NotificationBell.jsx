import { useCallback, useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Bell, CheckCheck, Loader2 } from 'lucide-react'
import api, { errorMessage } from '../lib/api'
import { useT } from '../i18n/LanguageContext'
import { formatDateTime } from '../lib/format'

const POLL_MS = 60_000

export default function NotificationBell() {
  const { t } = useT()
  const [open, setOpen] = useState(false)
  const [items, setItems] = useState([])
  const [unread, setUnread] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const boxRef = useRef(null)

  const loadUnread = useCallback(async () => {
    try {
      const { data } = await api.get('/api/notifications/unread-count')
      setUnread(Number(data.count) || 0)
    } catch {
      // A failed poll should not interrupt the rest of the app.
    }
  }, [])

  const loadList = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const { data } = await api.get('/api/notifications', { params: { page: 0, size: 15 } })
      setItems(Array.isArray(data) ? data : [])
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadUnread()
    const timer = setInterval(loadUnread, POLL_MS)
    return () => clearInterval(timer)
  }, [loadUnread])

  useEffect(() => {
    if (!open) return
    const onPointerDown = (event) => {
      if (boxRef.current && !boxRef.current.contains(event.target)) setOpen(false)
    }
    document.addEventListener('mousedown', onPointerDown)
    return () => document.removeEventListener('mousedown', onPointerDown)
  }, [open])

  const toggle = () => {
    const next = !open
    setOpen(next)
    if (next) loadList()
  }

  const markRead = async (notification) => {
    if (notification.read) return
    setItems((current) => current.map((item) => (
      item.id === notification.id ? { ...item, read: true } : item
    )))
    setUnread((current) => Math.max(0, current - 1))
    try {
      await api.post(`/api/notifications/${notification.id}/read`)
    } catch {
      // The optimistic state is harmless; the next poll reconciles the count.
    }
  }

  const markAll = async () => {
    setItems((current) => current.map((item) => ({ ...item, read: true })))
    setUnread(0)
    try {
      await api.post('/api/notifications/read-all')
    } catch {
      // The next poll will restore the real count if this failed.
    }
  }

  return (
    <div className="relative" ref={boxRef}>
      <button
        type="button"
        onClick={toggle}
        aria-label={t('Notifications')}
        aria-expanded={open}
        className="relative flex h-9 w-9 items-center justify-center rounded-lg border border-ink-200 bg-white text-ink-700 hover:bg-ink-50"
      >
        <Bell className="h-4 w-4" />
        {unread > 0 && (
          <span className="absolute -right-1.5 -top-1.5 flex min-h-5 min-w-5 items-center justify-center rounded-full bg-red-600 px-1 text-[10px] font-semibold leading-5 text-white">
            {unread > 9 ? '9+' : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 z-50 mt-2 w-80 max-w-[calc(100vw-2rem)] overflow-hidden rounded-2xl border border-ink-200 bg-white shadow-xl">
          <div className="flex items-center justify-between border-b border-ink-100 px-4 py-3">
            <p className="text-sm font-semibold text-ink-900">{t('Notifications')}</p>
            {unread > 0 && (
              <button type="button" onClick={markAll}
                className="inline-flex items-center gap-1 text-xs font-medium text-brand-600 hover:text-brand-700">
                <CheckCheck className="h-3.5 w-3.5" /> {t('Mark all as read')}
              </button>
            )}
          </div>

          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <div className="flex justify-center py-8"><Loader2 className="h-5 w-5 animate-spin text-brand-600" /></div>
            ) : error ? (
              <p className="px-4 py-6 text-center text-sm text-red-600">{error}</p>
            ) : items.length === 0 ? (
              <div className="px-4 py-8 text-center">
                <p className="text-sm font-medium text-ink-700">{t('No notifications yet')}</p>
                <p className="mt-1 text-xs text-ink-500">{t("You're all caught up")}</p>
              </div>
            ) : (
              items.map((item) => (
                <Link
                  key={item.id}
                  to={item.link || '/'}
                  onClick={() => { markRead(item); setOpen(false) }}
                  className={`block border-b border-ink-50 px-4 py-3 last:border-0 hover:bg-ink-50 ${item.read ? '' : 'bg-brand-50/50'}`}
                >
                  <div className="flex items-start gap-2.5">
                    <span className={`mt-1.5 h-2 w-2 shrink-0 rounded-full ${item.read ? 'bg-ink-200' : 'bg-brand-600'}`} />
                    <span className="min-w-0">
                      <span className="block text-sm font-medium text-ink-900">{item.title}</span>
                      {item.body && <span className="mt-0.5 block text-xs leading-5 text-ink-600">{item.body}</span>}
                      <span className="mt-1 block text-[11px] text-ink-400">{formatDateTime(item.createdAt)}</span>
                    </span>
                  </div>
                </Link>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  )
}
