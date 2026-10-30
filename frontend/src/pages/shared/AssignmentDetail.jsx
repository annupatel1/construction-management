import { useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, CalendarCheck, CalendarDays, HandCoins, Plus, Wallet } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { formatDate, formatINR, formatNumber, todayISO } from '../../lib/format'
import { wageSummary } from '../../lib/calc'
import CallButton from '../../components/Contact'
import StatusBadge from '../../components/Status'
import LabourPaymentModal from '../../components/LabourPaymentModal'
import { Alert, Badge, Button, Card, EmptyState, ErrorState, Input, PageLoader, SectionTitle, StatCard } from '../../components/ui'

const get = (url) => api.get(url).then((r) => r.data)

export default function AssignmentDetail() {
  const { id } = useParams()
  const { user } = useAuth()
  const { t } = useT()
  const role = user.roleName
  const isContractor = role === 'CONTRACTOR'
  const canSeeWages = role !== 'HOUSE_OWNER'

  const { data, loading, error, reload } = useFetch(async () => {
    const mine = await get('/api/assignments')
    const assignment = mine.find((a) => String(a.id) === id)
    if (!assignment) return { missing: true }
    const [attendance, payments] = await Promise.all([
      get(`/api/attendance/assignment/${id}`),
      canSeeWages ? get(`/api/labour-payments/assignment/${id}`) : Promise.resolve([]),
    ])
    return { assignment, attendance, payments }
  }, [id, role])

  const [payTarget, setPayTarget] = useState(null)

  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />
  if (data.missing) return <ErrorState message={t('This assignment was not found')} />

  const { assignment, attendance, payments } = data
  const sheet = wageSummary(assignment, attendance, payments)
  const sortedAtt = [...attendance].sort((a, b) => b.date.localeCompare(a.date))
  const active = assignment.status === 'ASSIGNED'

  return (
    <>
      <Link to={role === 'LABOUR' ? '/work' : `/projects/${assignment.project.id}`}
        className="mb-3 inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
        <ArrowLeft className="h-4 w-4" /> {role === 'LABOUR' ? t('My work') : assignment.project.title}
      </Link>

      <div className="mb-5 flex flex-wrap items-start justify-between gap-3">
        <div className="min-w-0">
          <h1 className="text-xl font-semibold text-ink-900 sm:text-2xl">{role === 'LABOUR' ? assignment.project.title : assignment.labour.name}</h1>
          <p className="mt-1 text-sm text-ink-500">
            {role === 'LABOUR' ? assignment.project.location : assignment.project.title} · {formatINR(assignment.dailyRate)} / {t('day')} · {t('Since')} {formatDate(assignment.assignedDate)}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <StatusBadge group="assignmentStatus" value={assignment.status} />
          <CallButton phone={role === 'LABOUR' ? assignment.project.contractor.phone : assignment.labour.phone} label={t('Call')} />
        </div>
      </div>

      {canSeeWages && (
        <>
          <div className="mb-2 grid grid-cols-2 gap-3 lg:grid-cols-5">
            <StatCard label={t('Days worked')} value={formatNumber(sheet.days)} icon={CalendarDays} tone="ink"
              sub={sheet.overtimeHours ? `+ ${formatNumber(sheet.overtimeHours)} ${t('overtime hrs')}` : undefined} />
            <StatCard label={t('Wages earned')} value={formatINR(sheet.earned)} icon={Wallet} tone="brand" />
            <StatCard label={t('Advance')} value={formatINR(sheet.advance)} icon={HandCoins} tone="amber" />
            <StatCard label={t('Paid')} value={formatINR(sheet.paid)} icon={Wallet} tone="green" />
            <div className="col-span-2 lg:col-span-1">
              <StatCard label={sheet.balance < 0 ? t('Overpaid') : t('Balance due')} value={formatINR(Math.abs(sheet.balance))}
                icon={Wallet} tone={sheet.balance < 0 ? 'red' : 'green'} />
            </div>
          </div>
          <p className="mb-6 text-xs text-ink-500">{t('Wages = days worked × daily rate + overtime hours × (daily rate ÷ 8). Balance = wages − advance − paid.')}</p>
        </>
      )}

      {isContractor && active && <MarkAttendance assignment={assignment} attendance={attendance} onDone={reload} />}

      <section className="mb-6">
        <SectionTitle>{t('Attendance')}</SectionTitle>
        {sortedAtt.length === 0 ? (
          <EmptyState icon={CalendarCheck} title={t('No attendance marked yet')} />
        ) : (
          <Card className="divide-y divide-ink-100">
            {sortedAtt.map((a) => (
              <div key={a.id} className="flex items-center justify-between gap-3 px-4 py-3">
                <p className="text-sm font-medium text-ink-900">{formatDate(a.date)}</p>
                <div className="flex items-center gap-2">
                  {a.status === 'OVERTIME' && <span className="text-xs text-ink-500">+{formatNumber(a.overtimeHours)} {t('hrs')}</span>}
                  <StatusBadge group="attendance" value={a.status} />
                </div>
              </div>
            ))}
          </Card>
        )}
      </section>

      {canSeeWages && (
        <section>
          <SectionTitle action={isContractor && (
            <div className="flex flex-wrap gap-2">
              <Button size="sm" variant="secondary" onClick={() => setPayTarget('ADVANCE')}>
                <HandCoins className="h-4 w-4" /> {t('Give advance')}
              </Button>
              <Button size="sm" onClick={() => setPayTarget('WAGE')}>
                <Plus className="h-4 w-4" /> {t('Pay wages')}
              </Button>
            </div>
          )}>{t('Wage payments')}</SectionTitle>
          {payments.length === 0 ? (
            <EmptyState icon={Wallet} title={t('No wage payments yet')} />
          ) : (
            <Card className="divide-y divide-ink-100">
              {[...payments].sort((a, b) => b.paymentDate.localeCompare(a.paymentDate)).map((p) => (
                <div key={p.id} className="flex items-center justify-between gap-3 px-4 py-3">
                  <div className="min-w-0">
                    <p className="flex items-center gap-2 text-sm font-medium text-ink-900">
                      {formatDate(p.paymentDate)}
                      <Badge tone={p.type === 'ADVANCE' ? 'warning' : 'success'}>
                        {p.type === 'ADVANCE' ? t('Advance') : t('Wage')}
                      </Badge>
                    </p>
                    <p className="truncate text-xs text-ink-500">{t(LABELS.paymentMode[p.mode])}{p.remarks ? ` · ${p.remarks}` : ''}</p>
                  </div>
                  <p className="font-semibold text-ink-900">{formatINR(p.amount)}</p>
                </div>
              ))}
            </Card>
          )}
        </section>
      )}

      {isContractor && payTarget && (
        <LabourPaymentModal open={!!payTarget} onClose={() => setPayTarget(null)} assignment={assignment}
          type={payTarget} pending={sheet.balance} onDone={() => { setPayTarget(null); reload() }} />
      )}
    </>
  )
}

/* ---------- Mark attendance (contractor) ---------- */
const ATT_STYLE = {
  FULL_DAY: 'border-emerald-500 bg-emerald-50 text-emerald-800',
  HALF_DAY: 'border-amber-500 bg-amber-50 text-amber-800',
  OVERTIME: 'border-brand-500 bg-brand-50 text-brand-800',
  ABSENT: 'border-red-500 bg-red-50 text-red-800',
}

function MarkAttendance({ assignment, attendance, onDone }) {
  const { t } = useT()
  const toast = useToast()
  const [date, setDate] = useState(todayISO())
  const [status, setStatus] = useState('FULL_DAY')
  const [overtime, setOvertime] = useState('2')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const existing = useMemo(() => attendance.find((a) => a.date === date), [attendance, date])

  const save = async () => {
    setError(''); setBusy(true)
    try {
      await api.post(`/api/attendance/${assignment.id}`, {
        date, status, overtimeHours: status === 'OVERTIME' ? Number(overtime || 0) : 0,
      })
      toast.success(t('Attendance saved'))
      onDone()
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <Card className="mb-6 p-4">
      <h2 className="mb-3 font-semibold text-ink-900">{t('Mark attendance')}</h2>
      {error && <Alert className="mb-3">{error}</Alert>}
      <div className="grid gap-4 sm:grid-cols-[200px_1fr]">
        <Input label={t('Date')} type="date" max={todayISO()} value={date} onChange={(e) => setDate(e.target.value)} />
        <div>
          <span className="mb-1.5 block text-sm font-medium text-ink-700">{t('Status')}</span>
          <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
            {options('attendance').map((s) => (
              <button key={s} type="button" onClick={() => setStatus(s)}
                className={`h-11 rounded-xl border text-sm font-medium transition-colors ${status === s ? ATT_STYLE[s] : 'border-ink-200 bg-white text-ink-600 hover:bg-ink-50'}`}>
                {t(LABELS.attendance[s])}
              </button>
            ))}
          </div>
        </div>
      </div>
      {status === 'OVERTIME' && (
        <Input className="mt-4 max-w-[200px]" label={t('Overtime hours')} type="number" inputMode="decimal" min="0" step="0.5"
          value={overtime} onChange={(e) => setOvertime(e.target.value)} />
      )}
      {existing && (
        <p className="mt-3 text-sm text-amber-700">{t('Attendance for this date is already marked as')} <b>{t(LABELS.attendance[existing.status])}</b>.</p>
      )}
      <Button className="mt-4" onClick={save} loading={busy} disabled={!!existing}>{t('Save attendance')}</Button>
    </Card>
  )
}
