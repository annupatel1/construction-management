import { useState } from 'react'
import api, { errorMessage } from '../lib/api'
import { useToast } from '../context/ToastContext'
import { useT } from '../i18n/LanguageContext'
import { LABELS, options } from '../lib/labels'
import { formatINR, todayISO } from '../lib/format'
import { Alert, Button, Input, Modal, Select, Textarea } from './ui'

const blank = { amount: '', paymentDate: todayISO(), mode: 'CASH', remarks: '' }

/** Record a wage payment or an advance against one assignment. */
export default function LabourPaymentModal({ open, onClose, assignment, type = 'WAGE', pending = 0, onDone }) {
  const { t } = useT()
  const toast = useToast()
  const [form, setForm] = useState(blank)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const isAdvance = type === 'ADVANCE'

  const submit = async () => {
    setError(''); setBusy(true)
    try {
      await api.post(`/api/labour-payments/${assignment.id}`, {
        ...form,
        amount: Number(form.amount),
        type,
        remarks: form.remarks || null,
      })
      toast.success(isAdvance ? t('Advance recorded') : t('Wage payment saved'))
      setForm(blank); onClose(); onDone?.()
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title={`${isAdvance ? t('Give advance') : t('Pay wages')} · ${assignment.labour.name}`}
      footer={<><Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button><Button onClick={submit} loading={busy}>{t('Save')}</Button></>}>
      <div className="space-y-4">
        {error && <Alert>{error}</Alert>}
        <div className="rounded-xl bg-ink-50 px-4 py-3 text-sm">
          <span className="text-ink-600">{t('Balance due')}: </span>
          <span className="font-semibold text-ink-900">{formatINR(Math.max(pending, 0))}</span>
        </div>
        <Input label={isAdvance ? t('Advance amount (₹)') : t('Amount (₹)')} type="number" inputMode="decimal" min="1" step="any"
          value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
        <div className="grid grid-cols-2 gap-3">
          <Input label={t('Date')} type="date" max={todayISO()} value={form.paymentDate}
            onChange={(e) => setForm({ ...form, paymentDate: e.target.value })} />
          <Select label={t('Mode')} value={form.mode} onChange={(e) => setForm({ ...form, mode: e.target.value })}>
            {options('paymentMode').map((m) => <option key={m} value={m}>{t(LABELS.paymentMode[m])}</option>)}
          </Select>
        </div>
        <Textarea label={t('Remarks (optional)')} value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} />
      </div>
    </Modal>
  )
}
