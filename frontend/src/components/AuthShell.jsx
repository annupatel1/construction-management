import { HardHat, ShieldCheck, Wallet } from 'lucide-react'
import Logo from './Logo'
import LanguageToggle from './LanguageToggle'
import { useT } from '../i18n/LanguageContext'

export default function AuthShell({ title, subtitle, children, footer }) {
  const { t } = useT()
  const points = [
    { icon: HardHat, text: 'Daily attendance and wages for every labourer' },
    { icon: Wallet, text: 'Payments and expenses tracked in rupees' },
    { icon: ShieldCheck, text: 'Your site data stays private and secure' },
  ]
  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[1.05fr_1fr]">
      <div className="relative hidden flex-col justify-between bg-ink-900 p-12 text-white lg:flex">
        <Logo light size="lg" />
        <div>
          <h2 className="max-w-md text-3xl font-semibold leading-tight">
            {t('Run your construction sites from one simple app')}
          </h2>
          <ul className="mt-8 space-y-4">
            {points.map(({ icon: Icon, text }) => (
              <li key={text} className="flex items-center gap-3 text-ink-200">
                <span className="flex h-9 w-9 items-center justify-center rounded-xl bg-brand-600/20 text-brand-300">
                  <Icon className="h-5 w-5" />
                </span>
                {t(text)}
              </li>
            ))}
          </ul>
        </div>
        <p className="text-sm text-ink-400">{t('For house owners, contractors and labour across India')}</p>
      </div>

      <div className="flex min-h-screen flex-col px-5 py-6 sm:px-10">
        <div className="flex items-center justify-between lg:justify-end">
          <div className="lg:hidden"><Logo /></div>
          <LanguageToggle />
        </div>
        <div className="mx-auto flex w-full max-w-md flex-1 flex-col justify-center py-8">
          <h1 className="text-2xl font-semibold text-ink-900">{title}</h1>
          {subtitle && <p className="mt-1.5 text-sm text-ink-500">{subtitle}</p>}
          <div className="mt-6">{children}</div>
          {footer && <div className="mt-6 text-center text-sm text-ink-600">{footer}</div>}
        </div>
      </div>
    </div>
  )
}
