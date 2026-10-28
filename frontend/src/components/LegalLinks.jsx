import { Link } from 'react-router-dom'
import { useT } from '../i18n/LanguageContext'

export default function LegalLinks({ className = '' }) {
  const { t } = useT()
  return (
    <nav className={`flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-ink-400 ${className}`}>
      <Link to="/privacy" className="hover:text-ink-700">{t('Privacy policy')}</Link>
      <Link to="/terms" className="hover:text-ink-700">{t('Terms of use')}</Link>
      <Link to="/support" className="hover:text-ink-700">{t('Support')}</Link>
    </nav>
  )
}
