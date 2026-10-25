import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Banknote, HardHat, MapPin, PencilRuler, Receipt, TrendingUp, Trash2, UserPlus, Wallet } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { formatDate, formatINR, formatNumber } from '../../lib/format'
import { projectFinance, wageSummary } from '../../lib/calc'
import CallButton from '../../components/Contact'
import StatusBadge from '../../components/Status'
import { Alert, Button, Card, ErrorState, PageLoader, Progress, Row, StatCard, Tabs } from '../../components/ui'
import { ExpensesTab, LabourTab, PaymentsTab } from './ProjectTabs'

const get = (url) => api.get(url).then((r) => r.data)

export default function ProjectDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const { t } = useT()
  const toast = useToast()
  const role = user.roleName
  const canViewFinance = role === 'HOUSE_OWNER' || role === 'CONTRACTOR' || role === 'ADMIN'
  const canViewEngineers = canViewFinance
  const canInviteEngineers = role === 'HOUSE_OWNER' || role === 'CONTRACTOR'
  const isEngineer = role === 'ENGINEER'
  const [tab, setTab] = useState('overview')

  const { data, loading, error, reload } = useFetch(async () => {
    const project = await get(`/api/projects/${id}`)
    if (!canViewFinance) return { project }
    const [payments, expenses, assignments, engineers] = await Promise.all([
      get(`/api/payments/project/${id}`),
      get(`/api/expenses/project/${id}`),
      get(`/api/assignments/project/${id}`),
      canViewEngineers ? get(`/api/projects/${id}/engineers`) : Promise.resolve([]),
    ])
    // wage figures are only visible to the contractor (house owners may not see wages)
    let wages = []
    if (role === 'CONTRACTOR' || role === 'ADMIN') {
      wages = await Promise.all(assignments.map(async (a) => {
        const [att, lp] = await Promise.all([get(`/api/attendance/assignment/${a.id}`), get(`/api/labour-payments/assignment/${a.id}`)])
        return wageSummary(a, att, lp)
      }))
    }
    return { project, payments, expenses, assignments, wages, engineers }
  }, [id, role, canViewFinance, canViewEngineers])

  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const { project, payments = [], expenses = [], assignments = [], wages = [], engineers = [] } = data
  const fin = projectFinance(project, payments, expenses, wages)
  const canManageStatus = role === 'HOUSE_OWNER' || role === 'CONTRACTOR'
  const isContractor = role === 'CONTRACTOR'

  const changeStatus = async (status) => {
    try {
      await api.patch(`/api/projects/${id}/status`, { status })
      toast.success(t('Status updated'))
      reload()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  const tabs = [{ id: 'overview', label: t('Overview') }]
  if (canViewFinance) {
    tabs.push(
      { id: 'labour', label: `${t('Labour')} (${assignments.length})` },
      { id: 'expenses', label: t('Expenses') },
      { id: 'payments', label: t('Payments') },
    )
  }

  const removeEngineer = async (member) => {
    if (!window.confirm(t('Remove {name} from this project?', { name: member.name }))) return
    try {
      await api.delete(`/api/projects/${id}/engineers/${member.membershipId}`)
      toast.success(t('Engineer removed'))
      reload()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  return (
    <>
      <Link to="/projects" className="mb-3 inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
        <ArrowLeft className="h-4 w-4" /> {t('Projects')}
      </Link>

      <div className="mb-5 flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <h1 className="text-xl font-semibold text-ink-900 sm:text-2xl">{project.title}</h1>
          <p className="mt-1 flex items-center gap-1.5 text-sm text-ink-500"><MapPin className="h-4 w-4" /> {project.location}</p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {canInviteEngineers && (
            <Link to="/engineers"><Button size="sm" variant="secondary"><UserPlus className="h-4 w-4" /> {t('Find engineer')}</Button></Link>
          )}
          {canManageStatus ? (
            <select value={project.status} onChange={(e) => changeStatus(e.target.value)}
              className="h-10 rounded-xl border border-ink-200 bg-white px-3 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-brand-400">
              {options('projectStatus').map((s) => <option key={s} value={s}>{t(LABELS.projectStatus[s])}</option>)}
            </select>
          ) : <StatusBadge group="projectStatus" value={project.status} />}
        </div>
      </div>

      <Tabs tabs={tabs} value={tab} onChange={setTab} />

      {tab === 'overview' && (
        <div className="space-y-5">
          {canViewFinance && (
            <>
              <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
                <StatCard label={t('Estimated cost')} value={formatINR(fin.estimated)} icon={Banknote} tone="ink" />
                <StatCard label={t('Received from owner')} value={formatINR(fin.received)} icon={Wallet} tone="green" />
                <StatCard label={t('Balance due')} value={formatINR(fin.due)} icon={Receipt} tone="amber" />
                {isContractor
                  ? <StatCard label={t('Estimated profit')} value={formatINR(fin.profit)} icon={TrendingUp} tone={fin.profit >= 0 ? 'brand' : 'red'} />
                  : <StatCard label={t('Material expenses')} value={formatINR(fin.materials)} icon={Receipt} tone="brand" />}
              </div>
              <Card className="p-4">
                <div className="mb-2 flex justify-between text-sm">
                  <span className="text-ink-600">{t('Payment progress')}</span>
                  <span className="font-medium text-ink-900">{fin.estimated ? Math.round((fin.received / fin.estimated) * 100) : 0}%</span>
                </div>
                <Progress value={fin.estimated ? (fin.received / fin.estimated) * 100 : 0} />
              </Card>
              {isContractor && (
                <Alert tone="info">
                  {t('Profit = received from owner − wages earned')}
                  {project.contractType === 'LABOUR_MATERIAL' ? ` − ${t('material expenses')}` : ''}.{' '}
                  {t('Wages earned so far')}: <b>{formatINR(fin.wagesEarned)}</b>
                </Alert>
              )}
            </>
          )}

          <div className="grid gap-4 md:grid-cols-2">
            <Card className="p-4">
              <h3 className="mb-1 font-semibold text-ink-900">{t('Project details')}</h3>
              <div className="divide-y divide-ink-100">
                <Row label={t('Area')}>{formatNumber(project.areaSqFt)} sq ft</Row>
                {canViewFinance && <Row label={t('Rate')}>{formatINR(project.ratePerSqFt)} / sq ft</Row>}
                <Row label={t('Contract type')}>{t(LABELS.contractType[project.contractType])}</Row>
                <Row label={t('Start date')}>{formatDate(project.startDate)}</Row>
                <Row label={t('End date')}>{formatDate(project.endDate)}</Row>
              </div>
            </Card>
            <Card className="p-4">
              <h3 className="mb-1 font-semibold text-ink-900">{t('People')}</h3>
              <div className="divide-y divide-ink-100">
                {project.houseOwner
                  ? <Person label={t('House owner')} person={project.houseOwner} />
                  : <Row label={t('House owner')}>{t('Self-managed site')}</Row>}
                <Person label={t('Contractor')} person={project.contractor} />
              </div>
              {role === 'LABOUR' && <p className="mt-3 flex items-center gap-1.5 text-xs text-ink-500"><HardHat className="h-4 w-4" /> {t('You are working on this site')}</p>}
              {isEngineer && <p className="mt-3 flex items-center gap-1.5 text-xs text-ink-500"><PencilRuler className="h-4 w-4" /> {t('You are an engineer on this project')}</p>}
            </Card>
          </div>

          {canViewEngineers && (
            <Card className="p-4">
              <div className="mb-2 flex flex-wrap items-center justify-between gap-2">
                <h3 className="font-semibold text-ink-900">{t('Project engineers')} ({engineers.length})</h3>
                <Link to="/engineers" className="text-sm font-medium text-brand-700 hover:underline">{t('Find engineer')} →</Link>
              </div>
              {engineers.length === 0 ? (
                <p className="text-sm text-ink-500">{t('No engineer has joined this project yet.')}</p>
              ) : (
                <div className="divide-y divide-ink-100">
                  {engineers.map((member) => (
                    <div key={member.membershipId} className="flex flex-wrap items-center justify-between gap-2 py-2.5">
                      <div className="min-w-0">
                        <p className="truncate font-medium text-ink-900">{member.name}</p>
                        <p className="truncate text-xs text-ink-500">
                          {member.specialty || t('Engineer')} · {member.experienceYears}+ {t('years')} · {member.city || '-'}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        <CallButton phone={member.phone} label={t('Call')} />
                        <Button size="sm" variant="ghost" onClick={() => removeEngineer(member)} aria-label={t('Remove engineer')}>
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Card>
          )}

          {isEngineer && (
            <Alert tone="info">{t('Your engineer membership is read-only. Financial details are hidden.')}</Alert>
          )}
        </div>
      )}

      {tab === 'labour' && canViewFinance && <LabourTab project={project} assignments={assignments} role={role} onChange={reload} />}
      {tab === 'expenses' && canViewFinance && <ExpensesTab project={project} expenses={expenses} role={role} onChange={reload} />}
      {tab === 'payments' && canViewFinance && <PaymentsTab project={project} payments={payments} fin={fin} role={role} onChange={reload} />}
    </>
  )
}

function Person({ label, person }) {
  if (!person) return null
  return (
    <div className="flex items-center justify-between gap-3 py-2.5">
      <div className="min-w-0">
        <p className="text-xs text-ink-500">{label}</p>
        <p className="truncate font-medium text-ink-900">{person.name}</p>
      </div>
      <CallButton phone={person.phone} label="Call" />
    </div>
  )
}
