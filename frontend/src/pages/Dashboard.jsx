import { Link } from 'react-router-dom'
import {
  Building2, CalendarCheck, ClipboardList, HardHat, IndianRupee, PencilRuler, PlusCircle, Search, UserPlus, Users, Wallet,
} from 'lucide-react'
import api from '../lib/api'
import useFetch from '../hooks/useFetch'
import { useAuth } from '../context/AuthContext'
import { useT } from '../i18n/LanguageContext'
import { formatINR, formatINRShort } from '../lib/format'
import { sum, wageSummary } from '../lib/calc'
import { LABELS } from '../lib/labels'
import ProjectCard from '../components/ProjectCard'
import StatusBadge from '../components/Status'
import { Button, Card, EmptyState, ErrorState, PageHeader, PageLoader, SectionTitle, StatCard } from '../components/ui'

const get = (url) => api.get(url).then((r) => r.data)

export default function Dashboard() {
  const { user } = useAuth()
  const { t } = useT()
  const first = user.name.split(' ')[0]

  return (
    <>
      <PageHeader title={`${t('Namaste')}, ${first} 👋`} subtitle={t(LABELS.role[user.roleName])} />
      {user.roleName === 'HOUSE_OWNER' && <OwnerHome />}
      {user.roleName === 'CONTRACTOR' && <ContractorHome />}
      {user.roleName === 'LABOUR' && <LabourHome />}
      {user.roleName === 'ENGINEER' && <EngineerHome />}
      {user.roleName === 'ADMIN' && <AdminHome />}
    </>
  )
}

function QuickLink({ to, icon: Icon, label }) {
  return (
    <Link to={to}>
      <Card className="flex items-center gap-3 p-4 transition-shadow hover:shadow-md">
        <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-600"><Icon className="h-5 w-5" /></span>
        <span className="text-sm font-medium text-ink-900">{label}</span>
      </Card>
    </Link>
  )
}

/* ---------------- House owner ---------------- */
function OwnerHome() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(async () => {
    const [projects, requirements] = await Promise.all([get('/api/projects'), get('/api/requirements/my')])
    return { projects, requirements }
  })
  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />
  const { projects, requirements } = data
  const active = projects.filter((p) => p.status === 'ONGOING')

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
        <StatCard label={t('Active projects')} value={active.length} icon={Building2} />
        <StatCard label={t('Total project value')} value={formatINRShort(sum(projects, (p) => p.totalEstimatedCost))} icon={IndianRupee} tone="green" />
        <StatCard label={t('Open requirements')} value={requirements.filter((r) => r.status === 'OPEN').length} icon={ClipboardList} tone="amber" />
      </div>
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <QuickLink to="/requirements/new" icon={PlusCircle} label={t('Post a requirement')} />
        <QuickLink to="/contractors" icon={Search} label={t('Find a contractor')} />
        <QuickLink to="/engineers" icon={PencilRuler} label={t('Find an engineer')} />
        <QuickLink to="/projects/new" icon={Building2} label={t('Start a project')} />
      </div>
      <section>
        <SectionTitle action={<Link to="/projects" className="text-sm font-medium text-brand-700">{t('View all')}</Link>}>{t('Your projects')}</SectionTitle>
        {projects.length === 0
          ? <EmptyState icon={Building2} title={t('No projects yet')} text={t('Find a contractor and start your first project.')}
              action={<Link to="/contractors"><Button>{t('Find a contractor')}</Button></Link>} />
          : <div className="grid gap-3 md:grid-cols-2">{projects.slice(0, 4).map((p) => <ProjectCard key={p.id} project={p} />)}</div>}
      </section>
    </div>
  )
}

/* ---------------- Contractor ---------------- */
function ContractorHome() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(async () => {
    const [projects, assignments, requirements, ledger] = await Promise.all([
      get('/api/projects'),
      get('/api/assignments'),
      get('/api/requirements'),
      // optional: keeps the dashboard working until the backend is restarted with the ledger endpoint
      get('/api/labour-ledger').catch(() => []),
    ])
    return { projects, assignments, requirements, ledger }
  })
  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />
  const { projects, assignments, requirements, ledger } = data
  const working = assignments.filter((a) => a.status === 'ASSIGNED')
  const pendingWages = sum(ledger, (l) => Math.max(l.pending, 0))

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label={t('Active projects')} value={projects.filter((p) => p.status === 'ONGOING').length} icon={Building2} />
        <StatCard label={t('Labour on site')} value={working.length} icon={HardHat} tone="amber" />
        <StatCard label={t('Pending wages')} value={formatINR(pendingWages)} icon={Wallet} tone={pendingWages > 0 ? 'red' : 'green'} />
        <StatCard label={t('Open requirements')} value={requirements.length} icon={ClipboardList} tone="green" />
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <QuickLink to="/my-labour" icon={Users} label={t('My labour')} />
        <QuickLink to="/projects/new" icon={PlusCircle} label={t('New site')} />
        <QuickLink to="/engineers" icon={PencilRuler} label={t('Find an engineer')} />
        <QuickLink to="/invitations" icon={UserPlus} label={t('Invitations')} />
      </div>

      <section>
        <SectionTitle>{t("Mark today's attendance")}</SectionTitle>
        {working.length === 0
          ? <EmptyState icon={CalendarCheck} title={t('No labour assigned yet')} text={t('Open a project and assign labour to start tracking attendance.')} />
          : (
            <Card className="divide-y divide-ink-100">
              {working.slice(0, 8).map((a) => (
                <Link key={a.id} to={`/assignments/${a.id}`} className="flex items-center justify-between gap-3 p-3.5 hover:bg-ink-50">
                  <div className="min-w-0">
                    <p className="truncate font-medium text-ink-900">{a.labour.name}</p>
                    <p className="truncate text-xs text-ink-500">{a.project.title}</p>
                  </div>
                  <span className="shrink-0 text-sm font-medium text-brand-700">{t('Mark')} →</span>
                </Link>
              ))}
            </Card>
          )}
      </section>

      <section>
        <SectionTitle action={<Link to="/projects" className="text-sm font-medium text-brand-700">{t('View all')}</Link>}>{t('Your projects')}</SectionTitle>
        {projects.length === 0
          ? <EmptyState icon={Building2} title={t('No projects yet')} text={t('Browse open requirements to find work.')}
              action={<Link to="/requirements"><Button>{t('Browse requirements')}</Button></Link>} />
          : <div className="grid gap-3 md:grid-cols-2">{projects.slice(0, 4).map((p) => <ProjectCard key={p.id} project={p} />)}</div>}
      </section>
    </div>
  )
}

/* ---------------- Labour ---------------- */
function LabourHome() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(async () => {
    const assignments = await get('/api/assignments')
    const sheets = await Promise.all(assignments.map(async (a) => {
      const [att, pay] = await Promise.all([get(`/api/attendance/assignment/${a.id}`), get(`/api/labour-payments/assignment/${a.id}`)])
      return wageSummary(a, att, pay)
    }))
    return { assignments, sheets }
  })
  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />
  const { assignments, sheets } = data

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
        <StatCard label={t('Sites')} value={assignments.filter((a) => a.status === 'ASSIGNED').length} icon={HardHat} />
        <StatCard label={t('Days worked')} value={sum(sheets, (s) => s.days)} icon={CalendarCheck} tone="amber" />
        <StatCard label={t('Pay due to you')} value={formatINR(sum(sheets, (s) => s.balance))} icon={Wallet} tone="green" />
      </div>
      <section>
        <SectionTitle>{t('My work')}</SectionTitle>
        {assignments.length === 0
          ? <EmptyState icon={HardHat} title={t('No work assigned yet')} text={t('When a contractor adds you to a site it will show here.')} />
          : <div className="grid gap-3 md:grid-cols-2">{assignments.map((a) => <AssignmentCard key={a.id} a={a} />)}</div>}
      </section>
    </div>
  )
}

export function AssignmentCard({ a }) {
  const { t } = useT()
  return (
    <Link to={`/assignments/${a.id}`}>
      <Card className="p-4 transition-shadow hover:shadow-md">
        <div className="flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h3 className="truncate font-semibold text-ink-900">{a.project.title}</h3>
            <p className="truncate text-sm text-ink-500">{a.project.location}</p>
          </div>
          <StatusBadge group="assignmentStatus" value={a.status} />
        </div>
        <div className="mt-3 flex items-end justify-between border-t border-ink-100 pt-3 text-sm">
          <div><p className="text-xs text-ink-500">{t('Daily rate')}</p><p className="font-semibold text-ink-900">{formatINR(a.dailyRate)}</p></div>
          <div className="text-right"><p className="text-xs text-ink-500">{t('Contractor')}</p><p className="font-medium text-ink-800">{a.project.contractor.name}</p></div>
        </div>
      </Card>
    </Link>
  )
}

/* ---------------- Engineer ---------------- */
function EngineerHome() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(async () => {
    const [projects, invitations, profile] = await Promise.all([
      get('/api/projects'),
      get('/api/engineer-invitations/inbox'),
      get('/api/engineers/me').catch(() => null),
    ])
    return { projects, invitations, profile }
  })
  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const { projects, invitations, profile } = data
  const pending = invitations.filter((i) => i.status === 'PENDING')
  const profileState = !profile ? t('Not created') : profile.profileComplete ? t('Complete') : t('Incomplete')

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-2 gap-3 lg:grid-cols-3">
        <StatCard label={t('Active projects')} value={projects.filter((p) => p.status === 'ONGOING').length} icon={Building2} />
        <StatCard label={t('Pending invitations')} value={pending.length} icon={UserPlus} tone={pending.length > 0 ? 'amber' : 'green'} />
        <StatCard label={t('Profile')} value={profileState} icon={PencilRuler} tone={profile?.profileComplete ? 'green' : 'red'} />
      </div>

      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <QuickLink to="/engineer-profile" icon={PencilRuler} label={t('Engineer profile')} />
        <QuickLink to="/invitations" icon={UserPlus} label={t('Invitations')} />
        <QuickLink to="/projects" icon={Building2} label={t('Projects')} />
      </div>

      <section>
        <SectionTitle action={<Link to="/invitations" className="text-sm font-medium text-brand-700">{t('View all')}</Link>}>
          {t('Invitations waiting for you')}
        </SectionTitle>
        {pending.length === 0
          ? <EmptyState icon={UserPlus} title={t('No pending invitations')} text={t('Owners and contractors can invite you from your public profile.')} />
          : (
            <Card className="divide-y divide-ink-100">
              {pending.slice(0, 4).map((invitation) => (
                <Link key={invitation.id} to="/invitations" className="flex items-center justify-between gap-3 p-3.5 hover:bg-ink-50">
                  <div className="min-w-0">
                    <p className="truncate font-medium text-ink-900">{invitation.projectTitle}</p>
                    <p className="truncate text-xs text-ink-500">{invitation.inviter.name} · {invitation.scope}</p>
                  </div>
                  <span className="shrink-0 text-sm font-medium text-brand-700">{t('Review')} →</span>
                </Link>
              ))}
            </Card>
          )}
      </section>

      <section>
        <SectionTitle action={<Link to="/projects" className="text-sm font-medium text-brand-700">{t('View all')}</Link>}>{t('Your projects')}</SectionTitle>
        {projects.length === 0
          ? <EmptyState icon={Building2} title={t('No projects yet')} text={t('Accept an invitation to get project access.')} />
          : <div className="grid gap-3 md:grid-cols-2">{projects.slice(0, 4).map((p) => <ProjectCard key={p.id} project={p} />)}</div>}
      </section>
    </div>
  )
}

/* ---------------- Admin ---------------- */
function AdminHome() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(() => get('/api/users'))
  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />
  const count = (r) => data.filter((u) => u.roleName === r).length
  return (
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-5">
      <StatCard label={t('Total users')} value={data.length} icon={Users} tone="ink" />
      <StatCard label={t('House owners')} value={count('HOUSE_OWNER')} icon={Building2} />
      <StatCard label={t('Contractors')} value={count('CONTRACTOR')} icon={HardHat} tone="amber" />
      <StatCard label={t('Labour')} value={count('LABOUR')} icon={Users} tone="green" />
      <StatCard label={t('Engineers')} value={count('ENGINEER')} icon={PencilRuler} tone="brand" />
    </div>
  )
}
