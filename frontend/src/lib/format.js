const inr = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 })

/** ₹12,50,000.00 style (Indian digit grouping) */
export const formatINR = (n) => inr.format(Number(n) || 0)

/** ₹12.5 L / ₹1.25 Cr for dashboards */
export function formatINRShort(n) {
  const v = Number(n) || 0
  const abs = Math.abs(v)
  const sign = v < 0 ? '-' : ''
  if (abs >= 1e7) return `${sign}₹${trim(abs / 1e7)} Cr`
  if (abs >= 1e5) return `${sign}₹${trim(abs / 1e5)} L`
  return formatINR(v)
}
const trim = (x) => String(Math.round(x * 100) / 100)

export const formatNumber = (n) => new Intl.NumberFormat('en-IN', { maximumFractionDigits: 2 }).format(Number(n) || 0)

export function formatDate(iso) {
  if (!iso) return '-'
  const d = new Date(iso + 'T00:00:00')
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}

export function formatDateTime(iso) {
  if (!iso) return '-'
  const normalized = String(iso).replace(/(\.\d{3})\d+/, '$1')
  return new Date(normalized).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

/** yyyy-MM-dd in the user's own timezone (what <input type="date"> and the API expect) */
export const todayISO = () => new Date().toLocaleDateString('en-CA')

export const initials = (name = '') =>
  name.trim().split(/\s+/).slice(0, 2).map((p) => p[0]?.toUpperCase()).join('') || '?'
