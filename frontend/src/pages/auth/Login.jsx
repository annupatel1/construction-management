import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import api, { errorCode, errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useT } from '../../i18n/LanguageContext'
import AuthShell from '../../components/AuthShell'
import OtpInput from '../../components/OtpInput'
import useCountdown from '../../components/useCountdown'
import { Alert, Button, Input } from '../../components/ui'

export default function Login() {
  const { t } = useT()
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const goHome = () => navigate(location.state?.from || '/', { replace: true })

  const [mode, setMode] = useState('password') // password | otp
  const [identifier, setIdentifier] = useState('')
  const [password, setPassword] = useState('')
  const [otpStep, setOtpStep] = useState(false)
  const [otp, setOtp] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [left, restart] = useCountdown(0)

  const switchMode = (m) => { setMode(m); setError(''); setOtpStep(false); setOtp('') }

  const submitPassword = async (e) => {
    e.preventDefault()
    setError(''); setBusy(true)
    try {
      const { data } = await api.post('/api/auth/login', { identifier, password })
      login(data.token, data.user)
      goHome()
    } catch (err) {
      if (errorCode(err) === 'PHONE_NOT_VERIFIED') {
        // account exists but the mobile number was never verified: send a fresh code and continue there
        try { await api.post('/api/auth/resend-otp', { identifier }) } catch { /* cooldown: a code was just sent */ }
        navigate('/verify', { state: { phone: identifier } })
        return
      }
      setError(errorMessage(err))
    } finally { setBusy(false) }
  }

  const requestOtp = async (e) => {
    e?.preventDefault()
    setError(''); setBusy(true)
    try {
      const { data } = await api.post('/api/auth/login-otp/request', { identifier })
      setOtpStep(true); setOtp(''); restart(data.resendAfterSeconds || 30)
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  const verifyOtp = async (e) => {
    e.preventDefault()
    setError(''); setBusy(true)
    try {
      const { data } = await api.post('/api/auth/login-otp/verify', { identifier, otp })
      login(data.token, data.user)
      goHome()
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <AuthShell
      title={t('Welcome back')}
      subtitle={t('Log in to manage your sites')}
      footer={<>{t("Don't have an account?")} <Link to="/register" className="font-medium text-brand-700 hover:underline">{t('Register')}</Link></>}
    >
      <div className="mb-5 grid grid-cols-2 gap-1 rounded-xl bg-ink-100 p-1 text-sm font-medium">
        {[['password', 'Password'], ['otp', 'OTP code']].map(([id, label]) => (
          <button key={id} type="button" onClick={() => switchMode(id)}
            className={`rounded-lg py-2 ${mode === id ? 'bg-white text-ink-900 shadow-sm' : 'text-ink-600'}`}>
            {t(label)}
          </button>
        ))}
      </div>

      {error && <Alert className="mb-4">{error}</Alert>}

      {mode === 'password' && (
        <form onSubmit={submitPassword} className="space-y-4">
          <Input label={t('Mobile number or email')} value={identifier} autoComplete="username"
            onChange={(e) => setIdentifier(e.target.value)} placeholder="98765 43210" required />
          <Input label={t('Password')} type="password" value={password} autoComplete="current-password"
            onChange={(e) => setPassword(e.target.value)} required />
          <div className="text-right">
            <Link to="/forgot-password" className="text-sm font-medium text-brand-700 hover:underline">
              {t('Forgot password?')}
            </Link>
          </div>
          <Button type="submit" size="lg" className="w-full" loading={busy}>{t('Log in')}</Button>
        </form>
      )}

      {mode === 'otp' && !otpStep && (
        <form onSubmit={requestOtp} className="space-y-4">
          <Input label={t('Mobile number or email')} value={identifier}
            onChange={(e) => setIdentifier(e.target.value)} placeholder="98765 43210" required
            hint={t("We'll send a 6-digit code. Email works only if it is verified.")} />
          <Button type="submit" size="lg" className="w-full" loading={busy}>{t('Send code')}</Button>
        </form>
      )}

      {mode === 'otp' && otpStep && (
        <form onSubmit={verifyOtp} className="space-y-5">
          <p className="text-center text-sm text-ink-600">{t('Enter the 6-digit code sent to')} <b>{identifier}</b></p>
          <OtpInput value={otp} onChange={setOtp} />
          {import.meta.env.DEV && (
            <p className="text-center text-xs text-ink-400">Dev mode: the code is printed in the backend console.</p>
          )}
          <Button type="submit" size="lg" className="w-full" loading={busy} disabled={otp.length !== 6}>{t('Verify and log in')}</Button>
          <div className="text-center text-sm">
            {left > 0
              ? <span className="text-ink-500">{t('Resend code in {s}s', { s: left })}</span>
              : <button type="button" onClick={requestOtp} className="font-medium text-brand-700 hover:underline">{t('Resend code')}</button>}
          </div>
        </form>
      )}
    </AuthShell>
  )
}
