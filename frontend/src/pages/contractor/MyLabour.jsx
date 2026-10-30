import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronDown, HandCoins, HardHat, Plus, Search, Users, Wallet } from 'lucide-react'
import api from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useT } from '../../i18n/LanguageContext'
import { formatINR, formatNumber, initials } from '../../lib/format'
import { sum } from '../../lib/calc'
import StatusBadge from '../../components/Status'
import AssignLabourModal from '../../components/AssignLabourModal'
import LabourPaymentModal from '../../components/LabourPaymentModal'
import { Badge, Button, Card, EmptyState, ErrorState, PageHeader, PageLoader, StatCard } from '../../components/ui'

const get = (url) => api.get(url).then((r) => r.data)

export default function MyLabour() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(async () => {
    const [ledger, projects] = await Promise.all([get('/api/labour-ledger'), get('/api/projects')])
    return { ledger, projects }
  })

  const [q, setQ] = useState('')
  const [assignOpen, setAssignOpen] = useState(false)
  const [expanded, setExpanded] = useState(null)
  const [payTarget, setPayTarget] = useState(null)

  const ledger = data?.ledger || []
  const projects = data?.projects || []

  const filtered = useMemo(() => {
    const needle = q.trim().toLowerCase()
    if (!needle) return ledger
    return ledger.filter((w) =>
      [w.name, w.phone, w.address, ...(w.sites || []).map((s) => `${s.projectTitle} ${s.projectLocation}`)]
        .filter(Boolean)
        .some((v) => v.toLowerCase().includes(needle)),
    )
  }, [ledger, q])

  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const totalPending = sum(ledger, (w) => Math.max(w.pending, 0))
  const totalAdvance = sum(ledger, (w) => w.advance)
  const totalPaid = sum(ledger, (w) => w.paid)

  return (
    <>
      <PageHeader
        title={t('My labour')}
        subtitle={t('Every worker on your sites, with daily rate, advance and pending money')}
        actions={<Button onClick={() => setAssignOpen(true)}><Plus className="h-4 w-4" /> {t('Add labour')}</Button>}
      />

      <div className="mb-5 grid grid-cols-2 gap-3 lg:grid-cols-4">
        <StatCard label={t('Workers')} value={ledger.length} icon={Users} tone="ink" />
        <StatCard label={t('Pending wages')} value={formatINR(totalPending)} icon={Wallet} tone={totalPending > 0 ? 'red' : 'green'} />
        <StatCard label={t('Total advance')} value={formatINR(totalAdvance)} icon={HandCoins} tone="amber" />
        <StatCard label={t('Total paid')} value={formatINR(totalPaid)} icon={Wallet} tone="green" />
      </div>

      <div className="relative mb-4 max-w-sm">
        <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-ink-400" />
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={t('Search worker or site')}
          className="h-10 w-full rounded-xl border border-ink-200 bg-white pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400" />
      </div>

      {ledger.length === 0 ? (
        <EmptyState icon={HardHat} title={t('No labour yet')}
          text={t('Add a worker to one of your sites to start tracking attendance, advance and wages.')}
          action={<Button onClick={() => setAssignOpen(true)}><Plus className="h-4 w-4" /> {t('Add labour')}</Button>} />
      ) : filtered.length === 0 ? (
        <EmptyState icon={Search} title={t('No worker matches your search')} />
      ) : (
        <div className="space-y-3">
          {filtered.map((worker) => {
            const open = expanded === worker.labourId
            return (
              <Card key={worker.labourId}>
                <button type="button" onClick={() => setExpanded(open ? null : worker.labourId)}
                  className="flex w-full items-start justify-between gap-3 p-4 text-left hover:bg-ink-50">
                  <div className="flex min-w-0 items-center gap-3">
                    <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-brand-50 font-semibold text-brand-700">
                      {initials(worker.name)}
                    </span>
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <p className="truncate font-semibold text-ink-900">{worker.name}</p>
                        <Badge tone={worker.registered ? 'success' : 'neutral'}>
                          {worker.registered ? t('Registered in app') : t('Added by you')}
                        </Badge>
                      </div>
                      <p className="truncate text-xs text-ink-500">
                        {worker.phone}{worker.address ? ` · ${worker.address}` : ''}
                      </p>
                      <p className="mt-1 text-xs text-ink-600">
                        {rateLabel(worker, t)} · {worker.activeSites} {t('active sites')} · {formatNumber(worker.daysWorked)} {t('days')}
                      </p>
                    </div>
                  </div>
                  <div className="flex shrink-0 flex-col items-end gap-1">
                    <span className={`text-sm font-semibold ${worker.pending > 0 ? 'text-red-600' : worker.pending < 0 ? 'text-emerald-700' : 'text-ink-900'}`}>
                      {formatINR(Math.abs(worker.pending))}
                    </span>
                    <span className="text-[11px] text-ink-500">{worker.pending < 0 ? t('Overpaid') : t('Pending')}</span>
                    <ChevronDown className={`h-4 w-4 text-ink-400 transition-transform ${open ? 'rotate-180' : ''}`} />
                  </div>
                </button>

                <div className="grid grid-cols-2 gap-2 border-t border-ink-100 px-4 py-3 text-sm sm:grid-cols-4">
                  <Money label={t('Wages earned')} value={worker.totalEarned} />
                  <Money label={t('Advance')} value={worker.totalAdvance} tone="amber" />
                  <Money label={t('Paid')} value={worker.totalPaid} tone="green" />
                  <Money label={t('Pending')} value={worker.pending} tone={worker.pending > 0 ? 'red' : 'ink'} />
                </div>

                {open && (
                  <div className="space-y-2 border-t border-ink-100 bg-ink-50/50 p-3">
                    {worker.sites.map((site) => (
                      <Card key={site.assignmentId} className="p-3">
                        <div className="flex flex-wrap items-start justify-between gap-2">
                          <div className="min-w-0">
                            <Link to={`/assignments/${site.assignmentId}`} className="truncate font-medium text-ink-900 hover:text-brand-700">
                              {site.projectTitle}
                            </Link>
                            <p className="truncate text-xs text-ink-500">{site.projectLocation}</p>
                          </div>
                          <StatusBadge group="assignmentStatus" value={site.status} />
                        </div>
                        <div className="mt-2 grid grid-cols-2 gap-x-4 gap-y-1 text-xs sm:grid-cols-4">
                          <span className="text-ink-500">{t('Daily rate')}: <b className="text-ink-900">{formatINR(site.dailyRate)}</b></span>
                          <span className="text-ink-500">{t('Days worked')}: <b className="text-ink-900">{formatNumber(site.days)}</b></span>
                          <span className="text-ink-500">{t('Advance')}: <b className="text-ink-900">{formatINR(site.advance)}</b></span>
                          <span className="text-ink-500">{t('Pending')}: <b className={site.pending > 0 ? 'text-red-600' : 'text-ink-900'}>{formatINR(site.pending)}</b></span>
                        </div>
                        <div className="mt-3 flex flex-wrap gap-2">
                          <Link to={`/assignments/${site.assignmentId}`}>
                            <Button size="sm" variant="secondary">{t('Attendance')}</Button>
                          </Link>
                          <Button size="sm" variant="secondary" onClick={() => setPayTarget({ site, worker, type: 'ADVANCE' })}>
                            <HandCoins className="h-4 w-4" /> {t('Give advance')}
                          </Button>
                          <Button size="sm" onClick={() => setPayTarget({ site, worker, type: 'WAGE' })}>
                            <Wallet className="h-4 w-4" /> {t('Pay wages')}
                          </Button>
                        </div>
                      </Card>
                    ))}
                  </div>
                )}
              </Card>
            )
          })}
        </div>
      )}

      <AssignLabourModal open={assignOpen} onClose={() => setAssignOpen(false)} projects={projects} onDone={reload} />

      {payTarget && (
        <LabourPaymentModal
          open={!!payTarget}
          onClose={() => setPayTarget(null)}
          assignment={{ id: payTarget.site.assignmentId, labour: payTarget.worker }}
          type={payTarget.type}
          pending={payTarget.site.pending}
          onDone={() => { setPayTarget(null); reload() }}
        />
      )}
    </>
  )
}

function Money({ label, value, tone = 'ink' }) {
  const cls = { ink: 'text-ink-900', amber: 'text-amber-700', green: 'text-emerald-700', red: 'text-red-600' }[tone]
  return (
    <div>
      <p className="text-[11px] uppercase tracking-wide text-ink-500">{label}</p>
      <p className={`font-semibold ${cls}`}>{formatINR(value)}</p>
    </div>
  )
}

function rateLabel(worker, t) {
  const rates = [...new Set(worker.sites.map((s) => s.dailyRate))]
  if (rates.length === 0) return '-'
  if (rates.length === 1) return `${formatINR(rates[0])} / ${t('day')}`
  return `${formatINR(Math.min(...rates))} – ${formatINR(Math.max(...rates))} / ${t('day')}`
}
