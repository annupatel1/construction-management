import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { HardHat, Home, Hammer } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useT } from '../../i18n/LanguageContext'
import AuthShell from '../../components/AuthShell'
import { Alert, Button, Input, Textarea } from '../../components/ui'

const ROLES = [
  { id: 'HOUSE_OWNER', icon: Home, title: 'House owner', text: 'I want to build or renovate' },
  { id: 'CONTRACTOR', icon: Hammer, title: 'Contractor', text: 'I run construction projects' },
  { id: 'LABOUR', icon: HardHat, title: 'Labour', text: 'I work on construction sites' },
]

export default function Register() {
  const { t } = useT()
  const navigate = useNavigate()
  const [form, setForm] = useState({ roleName: '', name: '', phone: '', email: '', password: '', address: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    if (!form.roleName) return setError(t('Please choose how you will use the app'))
    const phone = form.phone.replace(/\D/g, '').slice(-10)
    setBusy(true)
    try {
      await api.post('/api/auth/register', { ...form, phone, email: form.email.trim() || null })
      navigate('/verify', { state: { phone } })
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <AuthShell
      title={t('Create your account')}
      subtitle={t('It takes less than a minute')}
      footer={<>{t('Already have an account?')} <Link to="/login" className="font-medium text-brand-700 hover:underline">{t('Log in')}</Link></>}
    >
      <form onSubmit={submit} className="space-y-4">
        {error && <Alert>{error}</Alert>}

        <div>
          <span className="mb-1.5 block text-sm font-medium text-ink-700">{t('I am a')}</span>
          <div className="grid grid-cols-3 gap-2">
            {ROLES.map(({ id, icon: Icon, title, text }) => (
              <button key={id} type="button" onClick={() => setForm((f) => ({ ...f, roleName: id }))}
                className={`flex flex-col items-center rounded-xl border p-3 text-center transition-colors ${
                  form.roleName === id ? 'border-brand-500 bg-brand-50 ring-1 ring-brand-500' : 'border-ink-200 bg-white hover:bg-ink-50'}`}>
                <Icon className={`h-6 w-6 ${form.roleName === id ? 'text-brand-600' : 'text-ink-500'}`} />
                <span className="mt-1.5 text-sm font-medium text-ink-900">{t(title)}</span>
                <span className="mt-0.5 hidden text-[11px] leading-tight text-ink-500 sm:block">{t(text)}</span>
              </button>
            ))}
          </div>
        </div>

        <Input label={t('Full name')} value={form.name} onChange={set('name')} autoComplete="name" required />
        <Input label={t('Mobile number')} value={form.phone} onChange={set('phone')} prefix="+91"
          inputMode="numeric" maxLength={14} placeholder="98765 43210" autoComplete="tel" required
          hint={t('We will send a code to verify this number')} />
        <Input label={t('Email (optional)')} type="email" value={form.email} onChange={set('email')} autoComplete="email"
          hint={t('You can also use email to log in once you verify it')} />
        <Input label={t('Password')} type="password" value={form.password} onChange={set('password')}
          autoComplete="new-password" minLength={8} required hint={t('At least 8 characters')} />
        <Textarea label={t('Address (city, area)')} value={form.address} onChange={set('address')}
          placeholder="Vijay Nagar, Indore, Madhya Pradesh" required />

        <Button type="submit" size="lg" className="w-full" loading={busy}>{t('Continue')}</Button>
      </form>
    </AuthShell>
  )
}
