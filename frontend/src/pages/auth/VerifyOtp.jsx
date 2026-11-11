import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import AuthShell from '../../components/AuthShell'
import OtpInput from '../../components/OtpInput'
import useCountdown from '../../components/useCountdown'
import { Alert, Button } from '../../components/ui'

export default function VerifyOtp() {
  const { t } = useT()
  const { login } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const locationState = useLocation().state || {}
  const identifier = locationState.identifier || locationState.email || locationState.phone
  const isEmail = identifier && identifier.includes('@')

  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [left, restart] = useCountdown(30)

  if (!identifier) return <Navigate to="/register" replace />

  const submit = async (e) => {
    e.preventDefault()
    setError(''); setBusy(true)
    try {
      const { data } = await api.post('/api/auth/verify-otp', { identifier, otp })
      login(data.token, data.user)
      toast.success(t('Account verified. Welcome!'))
      navigate('/', { replace: true })
    } catch (err) { setError(errorMessage(err)); setOtp('') } finally { setBusy(false) }
  }

  const resend = async () => {
    setError('')
    try {
      const { data } = await api.post('/api/auth/resend-otp', { identifier })
      restart(data.resendAfterSeconds || 30)
      toast.success(t('A new code has been sent'))
    } catch (err) { setError(errorMessage(err)) }
  }

  return (
    <AuthShell
      title={isEmail ? t('Verify your email') : t('Verify your mobile number')}
      subtitle={isEmail
        ? `${t('Enter the 6-digit code sent to')} ${identifier}`
        : `${t('Enter the 6-digit code sent to')} +91 ${identifier}`}
      footer={<Link to="/register" className="font-medium text-brand-700 hover:underline">{t('Need to make changes? Go back')}</Link>}
    >
      <form onSubmit={submit} className="space-y-5">
        {error && <Alert>{error}</Alert>}
        <OtpInput value={otp} onChange={setOtp} />
        {import.meta.env.DEV && (
          <p className="text-center text-xs text-ink-400">Dev mode: the code is printed in the backend console.</p>
        )}
        <Button type="submit" size="lg" className="w-full" loading={busy} disabled={otp.length !== 6}>
          {t('Verify')}
        </Button>
        <div className="text-center text-sm">
          {left > 0
            ? <span className="text-ink-500">{t('Resend code in {s}s', { s: left })}</span>
            : <button type="button" onClick={resend} className="font-medium text-brand-700 hover:underline">{t('Resend code')}</button>}
        </div>
      </form>
    </AuthShell>
  )
}
