import { useEffect } from 'react'
import { AlertCircle, Inbox, Loader2, X } from 'lucide-react'
import { useT } from '../i18n/LanguageContext'

export const cx = (...a) => a.filter(Boolean).join(' ')

/* ---------- Buttons ---------- */
const BTN = {
  primary: 'bg-brand-600 text-white hover:bg-brand-700 focus-visible:ring-brand-400',
  secondary: 'bg-white text-ink-800 border border-ink-200 hover:bg-ink-50 focus-visible:ring-ink-300',
  ghost: 'text-ink-700 hover:bg-ink-100 focus-visible:ring-ink-300',
  danger: 'bg-red-600 text-white hover:bg-red-700 focus-visible:ring-red-300',
}
const BTN_SIZE = { sm: 'h-8 px-3 text-sm', md: 'h-10 px-4 text-sm', lg: 'h-12 px-6 text-base' }

export function Button({ variant = 'primary', size = 'md', loading, disabled, className, children, ...rest }) {
  return (
    <button
      disabled={disabled || loading}
      className={cx(
        'inline-flex items-center justify-center gap-2 rounded-xl font-medium transition-colors',
        'focus:outline-none focus-visible:ring-2 disabled:cursor-not-allowed disabled:opacity-60',
        BTN[variant], BTN_SIZE[size], className,
      )}
      {...rest}
    >
      {loading && <Loader2 className="h-4 w-4 animate-spin" />}
      {children}
    </button>
  )
}

/* ---------- Form fields ---------- */
const fieldBase =
  'w-full rounded-xl border bg-white px-3 text-sm text-ink-900 placeholder:text-ink-400 ' +
  'focus:outline-none focus:ring-2 focus:ring-brand-400 focus:border-brand-400 disabled:bg-ink-100'

export function Field({ label, hint, error, children, className }) {
  return (
    <label className={cx('block', className)}>
      {label && <span className="mb-1.5 block text-sm font-medium text-ink-700">{label}</span>}
      {children}
      {error ? <span className="mt-1 block text-xs text-red-600">{error}</span>
        : hint ? <span className="mt-1 block text-xs text-ink-500">{hint}</span> : null}
    </label>
  )
}

export function Input({ label, hint, error, className, prefix, ...rest }) {
  return (
    <Field label={label} hint={hint} error={error} className={className}>
      <div className="relative">
        {prefix && (
          <span className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-sm text-ink-500">
            {prefix}
          </span>
        )}
        <input
          className={cx(fieldBase, 'h-11', error ? 'border-red-400' : 'border-ink-200', prefix && 'pl-10')}
          {...rest}
        />
      </div>
    </Field>
  )
}

export function Select({ label, hint, error, className, children, ...rest }) {
  return (
    <Field label={label} hint={hint} error={error} className={className}>
      <select className={cx(fieldBase, 'h-11', error ? 'border-red-400' : 'border-ink-200')} {...rest}>
        {children}
      </select>
    </Field>
  )
}

export function Textarea({ label, hint, error, className, ...rest }) {
  return (
    <Field label={label} hint={hint} error={error} className={className}>
      <textarea
        rows={3}
        className={cx(fieldBase, 'py-2.5', error ? 'border-red-400' : 'border-ink-200')}
        {...rest}
      />
    </Field>
  )
}

/* ---------- Surfaces ---------- */
export function Card({ className, children, ...rest }) {
  return (
    <div className={cx('rounded-2xl border border-ink-200 bg-white shadow-sm', className)} {...rest}>
      {children}
    </div>
  )
}

const TONES = {
  neutral: 'bg-ink-100 text-ink-600',
  brand: 'bg-brand-50 text-brand-700',
  success: 'bg-emerald-50 text-emerald-700',
  warning: 'bg-amber-50 text-amber-700',
  danger: 'bg-red-50 text-red-700',
}
export function Badge({ tone = 'neutral', className, children }) {
  return (
    <span className={cx('inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium', TONES[tone], className)}>
      {children}
    </span>
  )
}

export function StatCard({ label, value, icon: Icon, tone = 'brand', sub }) {
  const iconTone = {
    brand: 'bg-brand-50 text-brand-600',
    amber: 'bg-amber-50 text-amber-600',
    green: 'bg-emerald-50 text-emerald-600',
    red: 'bg-red-50 text-red-600',
    ink: 'bg-ink-100 text-ink-600',
  }[tone]
  return (
    <Card className="p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-xs font-medium uppercase tracking-wide text-ink-500">{label}</p>
          <p className="mt-1.5 truncate text-xl font-semibold text-ink-900 sm:text-2xl">{value}</p>
          {sub && <p className="mt-0.5 text-xs text-ink-500">{sub}</p>}
        </div>
        {Icon && (
          <span className={cx('flex h-10 w-10 shrink-0 items-center justify-center rounded-xl', iconTone)}>
            <Icon className="h-5 w-5" />
          </span>
        )}
      </div>
    </Card>
  )
}

export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
      <div className="min-w-0">
        <h1 className="text-xl font-semibold text-ink-900 sm:text-2xl">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-ink-500">{subtitle}</p>}
      </div>
      {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
    </div>
  )
}

export function SectionTitle({ children, action }) {
  return (
    <div className="mb-3 flex items-center justify-between">
      <h2 className="text-base font-semibold text-ink-900">{children}</h2>
      {action}
    </div>
  )
}

/* ---------- States ---------- */
export function Spinner({ className }) {
  return <Loader2 className={cx('h-5 w-5 animate-spin text-brand-600', className)} />
}

export function PageLoader() {
  return <div className="flex justify-center py-20"><Spinner className="h-7 w-7" /></div>
}

export function EmptyState({ icon: Icon = Inbox, title, text, action }) {
  return (
    <Card className="flex flex-col items-center px-6 py-12 text-center">
      <span className="mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-ink-100 text-ink-500">
        <Icon className="h-6 w-6" />
      </span>
      <p className="font-medium text-ink-900">{title}</p>
      {text && <p className="mt-1 max-w-sm text-sm text-ink-500">{text}</p>}
      {action && <div className="mt-4">{action}</div>}
    </Card>
  )
}

export function ErrorState({ message, onRetry }) {
  const { t } = useT()
  return (
    <Card className="flex flex-col items-center px-6 py-10 text-center">
      <AlertCircle className="mb-3 h-8 w-8 text-red-500" />
      <p className="text-sm text-ink-700">{message}</p>
      {onRetry && <Button variant="secondary" size="sm" className="mt-4" onClick={onRetry}>{t('Try again')}</Button>}
    </Card>
  )
}

export function Alert({ tone = 'danger', children, className }) {
  const cls = {
    danger: 'bg-red-50 text-red-700 border-red-200',
    info: 'bg-brand-50 text-brand-800 border-brand-200',
    warning: 'bg-amber-50 text-amber-800 border-amber-200',
  }[tone]
  return <div className={cx('rounded-xl border px-3.5 py-2.5 text-sm', cls, className)}>{children}</div>
}

/* ---------- Modal ---------- */
export function Modal({ open, onClose, title, children, footer }) {
  useEffect(() => {
    if (!open) return
    const onKey = (e) => e.key === 'Escape' && onClose()
    document.addEventListener('keydown', onKey)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.style.overflow = ''
    }
  }, [open, onClose])

  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      <div className="absolute inset-0 bg-ink-900/50" onClick={onClose} />
      <div className="animate-slide-up relative flex max-h-[92vh] w-full flex-col rounded-t-3xl bg-white shadow-xl sm:max-w-lg sm:rounded-2xl">
        <div className="flex items-center justify-between border-b border-ink-100 px-5 py-4">
          <h3 className="text-base font-semibold text-ink-900">{title}</h3>
          <button onClick={onClose} className="rounded-lg p-1.5 text-ink-500 hover:bg-ink-100" aria-label="Close">
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="overflow-y-auto px-5 py-4">{children}</div>
        {footer && <div className="flex justify-end gap-2 border-t border-ink-100 px-5 py-3">{footer}</div>}
      </div>
    </div>
  )
}

/* ---------- Tabs ---------- */
export function Tabs({ tabs, value, onChange }) {
  return (
    <div className="mb-5 flex gap-1 overflow-x-auto rounded-xl bg-ink-100 p-1">
      {tabs.map((tab) => (
        <button
          key={tab.id}
          onClick={() => onChange(tab.id)}
          className={cx(
            'whitespace-nowrap rounded-lg px-4 py-2 text-sm font-medium transition-colors',
            value === tab.id ? 'bg-white text-ink-900 shadow-sm' : 'text-ink-600 hover:text-ink-900',
          )}
        >
          {tab.label}
        </button>
      ))}
    </div>
  )
}

export function Progress({ value }) {
  const pct = Math.max(0, Math.min(100, value))
  return (
    <div className="h-2 w-full overflow-hidden rounded-full bg-ink-100">
      <div className="h-full rounded-full bg-brand-500 transition-all" style={{ width: `${pct}%` }} />
    </div>
  )
}

export function Row({ label, children }) {
  return (
    <div className="flex items-start justify-between gap-4 py-2 text-sm">
      <span className="text-ink-500">{label}</span>
      <span className="text-right font-medium text-ink-900">{children}</span>
    </div>
  )
}
