import { Link } from 'react-router-dom'
import Logo from './Logo'
import LanguageToggle from './LanguageToggle'
import LegalLinks from './LegalLinks'
import { useAuth } from '../context/AuthContext'
import { useT } from '../i18n/LanguageContext'

export default function LegalShell({ title, children }) {
  const { user } = useAuth()
  const { t } = useT()
  return (
    <div className="min-h-screen bg-ink-50">
      <header className="border-b border-ink-200 bg-white">
        <div className="mx-auto flex max-w-3xl items-center justify-between gap-4 px-5 py-4 sm:px-8">
          <Logo />
          <div className="flex items-center gap-2">
            <LanguageToggle />
            <Link to={user ? '/' : '/login'}
              className="rounded-lg px-2 py-1.5 text-sm font-medium text-brand-600 hover:bg-brand-50">
              {t(user ? 'Back to app' : 'Log in')}
            </Link>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-5 py-8 sm:px-8 sm:py-12">
        <div className="rounded-2xl border border-ink-200 bg-white p-6 shadow-sm sm:p-10">
          <p className="text-xs font-semibold uppercase tracking-wide text-brand-600">BuildTrack pilot</p>
          <h1 className="mt-2 text-2xl font-semibold text-ink-900 sm:text-3xl">{t(title)}</h1>
          <p className="mt-2 text-sm text-ink-500">{t('Last updated')}: 26 October 2026</p>
          <div className="mt-8 space-y-7 text-sm leading-6 text-ink-700">{children}</div>
        </div>
      </main>

      <footer className="mx-auto max-w-3xl px-5 pb-8 sm:px-8">
        <LegalLinks />
      </footer>
    </div>
  )
}
