import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronRight, HardHat, Plus, Receipt, Wallet } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { formatDate, formatINR, formatNumber, todayISO } from '../../lib/format'
import { sum } from '../../lib/calc'
import StatusBadge from '../../components/Status'
import AssignLabourModal from '../../components/AssignLabourModal'
import { Alert, Badge, Button, Card, EmptyState, Input, Modal, Progress, Select, Textarea } from '../../components/ui'

/* =========================== LABOUR =========================== */
export function LabourTab({ project, assignments, role, onChange }) {
  const { t } = useT()
  const [open, setOpen] = useState(false)
  const canAssign = role === 'CONTRACTOR'

  return (
    <div className="space-y-4">
      {canAssign && (
        <div className="flex justify-end">
          <Button onClick={() => setOpen(true)}><Plus className="h-4 w-4" /> {t('Assign labour')}</Button>
        </div>
      )}
      {assignments.length === 0 ? (
        <EmptyState icon={HardHat} title={t('No labour on this project')}
          text={canAssign ? t('Assign labour to track attendance and wages.') : t('The contractor has not assigned any labour yet.')} />
      ) : (
        <Card className="divide-y divide-ink-100">
          {assignments.map((a) => (
            <Link key={a.id} to={`/assignments/${a.id}`} className="flex items-center justify-between gap-3 p-4 hover:bg-ink-50">
              <div className="min-w-0">
                <p className="truncate font-medium text-ink-900">{a.labour.name}</p>
                <p className="text-xs text-ink-500">{formatINR(a.dailyRate)} / {t('day')} · {t('Since')} {formatDate(a.assignedDate)}</p>
              </div>
              <div className="flex shrink-0 items-center gap-2">
                <StatusBadge group="assignmentStatus" value={a.status} />
                <ChevronRight className="h-4 w-4 text-ink-400" />
              </div>
            </Link>
          ))}
        </Card>
      )}
      {canAssign && <AssignLabourModal open={open} onClose={() => setOpen(false)} project={project} assignments={assignments} onDone={onChange} />}
    </div>
  )
}

/* =========================== EXPENSES =========================== */
const DEFAULT_UNIT = { CEMENT: 'bags', BRICK: 'pieces', AGGREGATE: 'cft', STEEL: 'kg', SAND: 'cft', OTHER: 'units' }

export function ExpensesTab({ project, expenses, role, onChange }) {
  const { t } = useT()
  const [open, setOpen] = useState(false)
  const canAdd = role === 'CONTRACTOR'
  const total = sum(expenses, (e) => e.amount)
  const sorted = [...expenses].sort((a, b) => b.purchaseDate.localeCompare(a.purchaseDate))
  const byCategory = options('category')
    .map((c) => ({ c, v: sum(expenses.filter((e) => e.category === c), (e) => e.amount) }))
    .filter((x) => x.v > 0)

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="text-xs uppercase tracking-wide text-ink-500">{t('Total material expenses')}</p>
          <p className="text-2xl font-semibold text-ink-900">{formatINR(total)}</p>
        </div>
        {canAdd && <Button onClick={() => setOpen(true)}><Plus className="h-4 w-4" /> {t('Add expense')}</Button>}
      </div>

      {project.contractType === 'LABOUR_ONLY' && (
        <Alert tone="info">{t('This is a "Labour only" contract, so material is usually supplied by the owner. These expenses are not counted in the contractor\'s profit.')}</Alert>
      )}

      {byCategory.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {byCategory.map(({ c, v }) => <Badge key={c} tone="neutral">{t(LABELS.category[c])}: {formatINR(v)}</Badge>)}
        </div>
      )}

      {expenses.length === 0 ? (
        <EmptyState icon={Receipt} title={t('No expenses recorded')} text={canAdd ? t('Add cement, brick, steel and other purchases here.') : undefined} />
      ) : (
        <Card className="divide-y divide-ink-100">
          {sorted.map((e) => (
            <div key={e.id} className="flex items-center justify-between gap-3 p-4">
              <div>
                <p className="font-medium text-ink-900">{t(LABELS.category[e.category])}</p>
                <p className="text-xs text-ink-500">{formatNumber(e.quantity)} {e.unit} · {formatDate(e.purchaseDate)}</p>
              </div>
              <p className="font-semibold text-ink-900">{formatINR(e.amount)}</p>
            </div>
          ))}
        </Card>
      )}
      {canAdd && <ExpenseModal open={open} onClose={() => setOpen(false)} project={project} onDone={onChange} />}
    </div>
  )
}

function ExpenseModal({ open, onClose, project, onDone }) {
  const { t } = useT()
  const toast = useToast()
  const blank = { category: 'CEMENT', amount: '', quantity: '', unit: 'bags', purchaseDate: todayISO() }
  const [form, setForm] = useState(blank)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async () => {
    setError(''); setBusy(true)
    try {
      await api.post(`/api/expenses/${project.id}`, { ...form, amount: Number(form.amount), quantity: Number(form.quantity) })
      toast.success(t('Expense added'))
      setForm(blank); onClose(); onDone()
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title={t('Add expense')}
      footer={<><Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button><Button onClick={submit} loading={busy}>{t('Save')}</Button></>}>
      <div className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <Select label={t('Material')} value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value, unit: DEFAULT_UNIT[e.target.value] })}>
          {options('category').map((c) => <option key={c} value={c}>{t(LABELS.category[c])}</option>)}
        </Select>
        <div className="grid grid-cols-2 gap-3">
          <Input label={t('Quantity')} type="number" inputMode="decimal" min="0.01" step="any" value={form.quantity}
            onChange={(e) => setForm({ ...form, quantity: e.target.value })} />
          <Input label={t('Unit')} value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })} />
        </div>
        <Input label={t('Total amount (₹)')} type="number" inputMode="decimal" min="1" step="any" value={form.amount}
          onChange={(e) => setForm({ ...form, amount: e.target.value })} />
        <Input label={t('Purchase date')} type="date" max={todayISO()} value={form.purchaseDate}
          onChange={(e) => setForm({ ...form, purchaseDate: e.target.value })} />
      </div>
    </Modal>
  )
}

/* =========================== PAYMENTS (owner -> contractor) =========================== */
export function PaymentsTab({ project, payments, fin, role, onChange }) {
  const { t } = useT()
  const [open, setOpen] = useState(false)
  const canAdd = role === 'HOUSE_OWNER'
  const sorted = [...payments].sort((a, b) => b.paymentDate.localeCompare(a.paymentDate))

  return (
    <div className="space-y-4">
      <Card className="p-4">
        <div className="mb-2 flex flex-wrap justify-between gap-2 text-sm">
          <span className="text-ink-600">{t('Paid')} <b className="text-ink-900">{formatINR(fin.received)}</b> {t('of')} {formatINR(fin.estimated)}</span>
          <span className="text-ink-600">{t('Balance due')} <b className="text-ink-900">{formatINR(fin.due)}</b></span>
        </div>
        <Progress value={fin.estimated ? (fin.received / fin.estimated) * 100 : 0} />
      </Card>

      {canAdd && (
        <div className="flex justify-end">
          <Button onClick={() => setOpen(true)}><Plus className="h-4 w-4" /> {t('Record payment')}</Button>
        </div>
      )}

      {payments.length === 0 ? (
        <EmptyState icon={Wallet} title={t('No payments yet')} text={canAdd ? t('Record each payment you make to the contractor.') : undefined} />
      ) : (
        <Card className="divide-y divide-ink-100">
          {sorted.map((p) => (
            <div key={p.id} className="flex items-center justify-between gap-3 p-4">
              <div className="min-w-0">
                <p className="font-medium text-ink-900">{formatDate(p.paymentDate)}</p>
                <p className="truncate text-xs text-ink-500">{t(LABELS.paymentMode[p.mode])}{p.remarks ? ` · ${p.remarks}` : ''}</p>
              </div>
              <p className="font-semibold text-emerald-700">{formatINR(p.amount)}</p>
            </div>
          ))}
        </Card>
      )}
      {canAdd && <PaymentModal open={open} onClose={() => setOpen(false)} project={project} due={fin.due} onDone={onChange} />}
    </div>
  )
}

function PaymentModal({ open, onClose, project, due, onDone }) {
  const { t } = useT()
  const toast = useToast()
  const blank = { amount: '', paymentDate: todayISO(), mode: 'UPI', remarks: '' }
  const [form, setForm] = useState(blank)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async () => {
    setError(''); setBusy(true)
    try {
      await api.post(`/api/payments/${project.id}`, { ...form, amount: Number(form.amount), remarks: form.remarks || null })
      toast.success(t('Payment recorded'))
      setForm(blank); onClose(); onDone()
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title={t('Record payment')}
      footer={<><Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button><Button onClick={submit} loading={busy}>{t('Save')}</Button></>}>
      <div className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <Input label={t('Amount (₹)')} type="number" inputMode="decimal" min="1" step="any" value={form.amount}
          onChange={(e) => setForm({ ...form, amount: e.target.value })} hint={`${t('Balance due')}: ${formatINR(due)}`} />
        <div className="grid grid-cols-2 gap-3">
          <Input label={t('Date')} type="date" max={todayISO()} value={form.paymentDate} onChange={(e) => setForm({ ...form, paymentDate: e.target.value })} />
          <Select label={t('Mode')} value={form.mode} onChange={(e) => setForm({ ...form, mode: e.target.value })}>
            {options('paymentMode').map((m) => <option key={m} value={m}>{t(LABELS.paymentMode[m])}</option>)}
          </Select>
        </div>
        <Textarea label={t('Remarks (optional)')} value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />
      </div>
    </Modal>
  )
}
