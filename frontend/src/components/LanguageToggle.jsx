import { Languages } from 'lucide-react'
import { useT } from '../i18n/LanguageContext'

export default function LanguageToggle({ className = '' }) {
  const { lang, setLang } = useT()
  return (
    <button
      onClick={() => setLang(lang === 'en' ? 'hi' : 'en')}
      className={`inline-flex h-9 items-center gap-1.5 rounded-lg border border-ink-200 bg-white px-2.5 text-sm font-medium text-ink-700 hover:bg-ink-50 ${className}`}
      aria-label="Change language"
    >
      <Languages className="h-4 w-4" />
      {lang === 'en' ? 'हिन्दी' : 'English'}
    </button>
  )
}
