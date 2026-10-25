import { NavLink, Outlet, Link } from 'react-router-dom'
import {
  Building2, ClipboardList, LayoutDashboard, LogOut, Search, Users, UserCircle, Wrench,
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useT } from '../i18n/LanguageContext'
import { LABELS } from '../lib/labels'
import { initials } from '../lib/format'
import Logo from './Logo'
import LanguageToggle from './LanguageToggle'

const NAV = {
  HOUSE_OWNER: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/projects', label: 'Projects', icon: Building2 },
    { to: '/requirements', label: 'Requirements', icon: ClipboardList },
    { to: '/contractors', label: 'Find contractors', icon: Search },
  ],
  CONTRACTOR: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/my-labour', label: 'My labour', icon: Users },
    { to: '/projects', label: 'Projects', icon: Building2 },
    { to: '/requirements', label: 'Requirements', icon: ClipboardList },
  ],
  LABOUR: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/work', label: 'My work', icon: Wrench },
  ],
  ADMIN: [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
    { to: '/users', label: 'Users', icon: Users },
    { to: '/projects', label: 'Projects', icon: Building2 },
  ],
}

export default function Layout() {
  const { user, logout } = useAuth()
  const { t } = useT()
  const items = NAV[user.roleName] || NAV.LABOUR

  const sideLink = ({ isActive }) =>
    `flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition-colors ${
      isActive ? 'bg-brand-600 text-white' : 'text-ink-300 hover:bg-ink-800 hover:text-white'
    }`

  return (
    <div className="min-h-screen lg:flex">
      {/* Desktop sidebar */}
      <aside className="sticky top-0 hidden h-screen w-64 shrink-0 flex-col bg-ink-900 p-4 lg:flex">
        <div className="px-2 py-2"><Logo light /></div>
        <nav className="mt-6 flex flex-1 flex-col gap-1">
          {items.map(({ to, label, icon: Icon, end }) => (
            <NavLink key={to} to={to} end={end} className={sideLink}>
              <Icon className="h-5 w-5" /> {t(label)}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-ink-700 pt-3">
          <Link to="/profile" className="flex items-center gap-3 rounded-xl px-2 py-2 hover:bg-ink-800">
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-600 text-sm font-semibold text-white">
              {initials(user.name)}
            </span>
            <span className="min-w-0">
              <span className="block truncate text-sm font-medium text-white">{user.name}</span>
              <span className="block text-xs text-ink-400">{t(LABELS.role[user.roleName])}</span>
            </span>
          </Link>
          <button onClick={logout}
            className="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-ink-300 hover:bg-ink-800 hover:text-white">
            <LogOut className="h-5 w-5" /> {t('Log out')}
          </button>
        </div>
      </aside>

      <div className="flex min-w-0 flex-1 flex-col">
        {/* Top bar */}
        <header className="sticky top-0 z-30 flex h-14 items-center justify-between border-b border-ink-200 bg-white/90 px-4 backdrop-blur lg:justify-end">
          <div className="lg:hidden"><Logo /></div>
          <div className="flex items-center gap-2">
            <LanguageToggle />
            <Link to="/profile" className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-600 text-sm font-semibold text-white lg:hidden"
              aria-label="Profile">
              {initials(user.name)}
            </Link>
          </div>
        </header>

        <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-5 pb-24 sm:px-6 lg:pb-8">
          <Outlet />
        </main>
      </div>

      {/* Mobile bottom navigation */}
      <nav className="fixed inset-x-0 bottom-0 z-30 flex border-t border-ink-200 bg-white lg:hidden">
        {[...items, { to: '/profile', label: 'Profile', icon: UserCircle }].map(({ to, label, icon: Icon, end }) => (
          <NavLink key={to} to={to} end={end}
            className={({ isActive }) =>
              `flex flex-1 flex-col items-center gap-0.5 py-2 text-[11px] font-medium ${isActive ? 'text-brand-600' : 'text-ink-500'}`}>
            <Icon className="h-5 w-5" />
            <span className="max-w-full truncate px-1">{t(label)}</span>
          </NavLink>
        ))}
      </nav>
    </div>
  )
}
