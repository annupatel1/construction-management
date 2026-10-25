import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../../lib/api'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import AuthShell from '../../components/AuthShell'
import OtpInput from '../../components/OtpInput'
import useCountdown from '../../components/useCountdown'
import { Alert, Button, Input } from '../../components/ui'

export default function ForgotPassword() {
  const { t } = useT()
  const toast = useToast()
  const navigate = useNavigate()
  const [step, setStep] = useState(1)
  const [identifier, setIdentifier] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [left, restart] = useCountdown(0)

  const sendCode = async (e) => {
    e?.preventDefault()
    setError(''); setBusy(true)
    try {
      const { data } = await api.post('/api/auth/forgot-password', { identifier })
      setStep(2); restart(data.resendAfterSeconds || 30)
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  const reset = async (e) => {
    e.preventDefault()
    setError(''); setBusy(true)
    try {
      await api.post('/api/auth/reset-password', { identifier, otp, newPassword })
      toast.success(t('Password changed. You can now log in with your new password.'))
      navigate('/login', { replace: true })
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <AuthShell
      title={t('Reset your password')}
      subtitle={step === 1 ? t("Enter your mobile number or verified email and we'll send a code") : t('Enter the code and choose a new password')}
      footer={<Link to="/login" className="font-medium text-brand-700 hover:underline">{t('Back to log in')}</Link>}
    >
      {error && <Alert className="mb-4">{error}</Alert>}

      {step === 1 ? (
        <form onSubmit={sendCode} className="space-y-4">
          <Input label={t('Mobile number or email')} value={identifier} onChange={(e) => setIdentifier(e.target.value)}
            placeholder="98765 43210" required />
          <Button type="submit" size="lg" className="w-full" loading={busy}>{t('Send code')}</Button>
        </form>
      ) : (
        <form onSubmit={reset} className="space-y-5">
          <p className="text-center text-sm text-ink-600">
            {t('If this account exists, a code has been sent.')}
          </p>
          <OtpInput value={otp} onChange={setOtp} />
          {import.meta.env.DEV && (
            <p className="text-center text-xs text-ink-400">Dev mode: the code is printed in the backend console.</p>
          )}
          <Input label={t('New password')} type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)}
            minLength={8} autoComplete="new-password" required hint={t('At least 8 characters')} />
          <Button type="submit" size="lg" className="w-full" loading={busy} disabled={otp.length !== 6}>{t('Change password')}</Button>
          <div className="text-center text-sm">
            {left > 0
              ? <span className="text-ink-500">{t('Resend code in {s}s', { s: left })}</span>
              : <button type="button" onClick={sendCode} className="font-medium text-brand-700 hover:underline">{t('Resend code')}</button>}
          </div>
        </form>
      )}
    </AuthShell>
  )
}
