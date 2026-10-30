import { useState } from 'react'
import { BadgeCheck, LogOut, Mail, Phone, MapPin } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS } from '../../lib/labels'
import { initials } from '../../lib/format'
import LanguageToggle from '../../components/LanguageToggle'
import OtpInput from '../../components/OtpInput'
import useCountdown from '../../components/useCountdown'
import { Alert, Badge, Button, Card, Input, PageHeader } from '../../components/ui'

export default function Profile() {
  const { user, logout, refreshUser } = useAuth()
  const { t } = useT()
  const toast = useToast()

  const [email, setEmail] = useState(user.email || '')
  const [sent, setSent] = useState(false)
  const [editing, setEditing] = useState(false)
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [left, restart] = useCountdown(0)

  const needsEmailVerify = user.email && !user.emailVerified
  const showOtp = sent

  const sendCode = async () => {
    setError(''); setBusy(true)
    try {
      const { data } = await api.post('/api/account/email', { email })
      setSent(true); setOtp(''); restart(data.resendAfterSeconds || 30)
      await refreshUser()
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  const verify = async () => {
    setError(''); setBusy(true)
    try {
      await api.post('/api/account/email/verify', { otp })
      await refreshUser()
      setSent(false); setEditing(false); setOtp('')
      toast.success(t('Email verified'))
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <>
      <PageHeader title={t('Profile')} />
      <div className="grid max-w-3xl gap-4">
        <Card className="p-5">
          <div className="flex items-center gap-4">
            <span className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-600 text-xl font-semibold text-white">{initials(user.name)}</span>
            <div>
              <h2 className="text-lg font-semibold text-ink-900">{user.name}</h2>
              <Badge tone="brand">{t(LABELS.role[user.roleName])}</Badge>
            </div>
          </div>
          <div className="mt-5 space-y-3 text-sm">
            <p className="flex items-center gap-3"><Phone className="h-4 w-4 text-ink-400" /> +91 {user.phone} <BadgeCheck className="h-4 w-4 text-emerald-600" /></p>
            <p className="flex items-center gap-3"><MapPin className="h-4 w-4 text-ink-400" /> {user.address}</p>
          </div>
        </Card>

        <Card className="p-5">
          <h3 className="mb-1 flex items-center gap-2 font-semibold text-ink-900"><Mail className="h-4 w-4" /> {t('Email')}</h3>
          <p className="mb-4 text-sm text-ink-500">{t('Verify your email to use it for login and password reset.')}</p>
          {error && <Alert className="mb-3">{error}</Alert>}

          {user.email && user.emailVerified && !sent && !editing ? (
            <div className="flex flex-wrap items-center justify-between gap-3">
              <p className="text-sm text-ink-800">{user.email} <Badge tone="success">{t('Verified')}</Badge></p>
              <Button variant="secondary" size="sm" onClick={() => { setEditing(true); setEmail(''); setError('') }}>{t('Change email')}</Button>
            </div>
          ) : (
            <div className="space-y-4">
              <Input label={t('Email address')} type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
              {needsEmailVerify && !showOtp && <Badge tone="warning">{t('Not verified')}</Badge>}
              {!showOtp ? (
                <Button onClick={sendCode} loading={busy} disabled={!email}>{t('Send verification code')}</Button>
              ) : (
                <div className="space-y-4">
                  <OtpInput value={otp} onChange={setOtp} />
                  {import.meta.env.DEV && <p className="text-center text-xs text-ink-400">Dev mode: the code is printed in the backend console.</p>}
                  <div className="flex flex-wrap items-center gap-3">
                    <Button onClick={verify} loading={busy} disabled={otp.length !== 6}>{t('Verify email')}</Button>
                    {left > 0
                      ? <span className="text-sm text-ink-500">{t('Resend code in {s}s', { s: left })}</span>
                      : <button type="button" onClick={sendCode} className="text-sm font-medium text-brand-700 hover:underline">{t('Resend code')}</button>}
                  </div>
                </div>
              )}
            </div>
          )}
        </Card>

        <Card className="flex flex-wrap items-center justify-between gap-3 p-5">
          <div className="flex items-center gap-3"><span className="text-sm font-medium text-ink-700">{t('Language')}</span><LanguageToggle /></div>
          <Button variant="secondary" onClick={logout}><LogOut className="h-4 w-4" /> {t('Log out')}</Button>
        </Card>
      </div>
    </>
  )
}
