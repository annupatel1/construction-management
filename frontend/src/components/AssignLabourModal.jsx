import { useEffect, useState } from 'react'
import { Check, Plus, Search, UserPlus } from 'lucide-react'
import api, { errorMessage } from '../lib/api'
import { useToast } from '../context/ToastContext'
import { useT } from '../i18n/LanguageContext'
import { todayISO } from '../lib/format'
import { Alert, Button, Input, Modal, Select, Spinner } from './ui'

const blankForm = { dailyRate: '', advanceTaken: '0', assignedDate: todayISO() }
const blankWorker = { name: '', phone: '', address: '' }

/**
 * Assign a labourer to a site. Works from a project (project fixed) or from the
 * contractor ledger (project chosen from a list). A worker who is not on the app
 * can be created inline with just a name and phone number.
 */
export default function AssignLabourModal({ open, onClose, project, projects = [], assignments = [], onDone }) {
  const { t } = useT()
  const toast = useToast()
  const [q, setQ] = useState('')
  const [list, setList] = useState(null)
  const [picked, setPicked] = useState(null)
  const [siteId, setSiteId] = useState('')
  const [form, setForm] = useState(blankForm)
  const [showNew, setShowNew] = useState(false)
  const [worker, setWorker] = useState(blankWorker)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  // reset the form each time the modal opens
  useEffect(() => {
    if (!open) return
    setQ(''); setPicked(null); setForm(blankForm); setShowNew(false); setWorker(blankWorker); setError('')
    setSiteId(project ? String(project.id) : (projects[0] ? String(projects[0].id) : ''))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open])

  useEffect(() => {
    if (!open) return
    const id = setTimeout(() => {
      api.get('/api/directory/labourers', { params: { q } })
        .then((r) => setList(r.data))
        .catch((e) => setError(errorMessage(e)))
    }, 300)
    return () => clearTimeout(id)
  }, [open, q])

  const onSite = new Set(assignments.filter((a) => a.status === 'ASSIGNED').map((a) => a.labour.id))

  const createWorker = async () => {
    setError('')
    if (!worker.name.trim() || !worker.phone.trim()) return setError(t('Enter the worker name and mobile number'))
    setBusy(true)
    try {
      const { data } = await api.post('/api/directory/labourers', worker)
      setList((old) => [data, ...(old || []).filter((w) => w.id !== data.id)])
      setPicked(data)
      setShowNew(false)
      setWorker(blankWorker)
      toast.success(t('Worker added'))
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  const submit = async () => {
    setError('')
    if (!siteId) return setError(t('Please choose a site'))
    if (!picked) return setError(t('Please choose a labourer'))
    setBusy(true)
    try {
      await api.post(`/api/assignments/${picked.id}/${siteId}`, {
        dailyRate: Number(form.dailyRate),
        advanceTaken: Number(form.advanceTaken || 0),
        assignedDate: form.assignedDate,
      })
      toast.success(t('Labour assigned'))
      onClose()
      onDone?.()
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title={t('Assign labour')}
      footer={<><Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button><Button onClick={submit} loading={busy}>{t('Assign')}</Button></>}>
      <div className="space-y-4">
        {error && <Alert>{error}</Alert>}

        {!project && (
          projects.length === 0
            ? <Alert tone="warning">{t('Create a site first, then add labour to it.')}</Alert>
            : (
              <Select label={t('Site')} value={siteId} onChange={(e) => setSiteId(e.target.value)}>
                {projects.map((p) => <option key={p.id} value={p.id}>{p.title} · {p.location}</option>)}
              </Select>
            )
        )}

        <div>
          <div className="mb-2 flex items-center justify-between">
            <span className="text-sm font-medium text-ink-700">{t('Choose labourer')}</span>
            <button type="button" onClick={() => setShowNew((v) => !v)}
              className="inline-flex items-center gap-1 text-sm font-medium text-brand-700 hover:text-brand-800">
              <UserPlus className="h-4 w-4" /> {t('Add new worker')}
            </button>
          </div>

          {showNew && (
            <div className="mb-3 space-y-3 rounded-xl border border-brand-200 bg-brand-50 p-3">
              <p className="text-xs text-brand-800">{t('Add a worker who does not use the app yet. They can register later with the same mobile number.')}</p>
              <Input label={t('Worker name')} value={worker.name} onChange={(e) => setWorker({ ...worker, name: e.target.value })} />
              <div className="grid grid-cols-2 gap-3">
                <Input label={t('Mobile number')} inputMode="numeric" maxLength={10} value={worker.phone}
                  onChange={(e) => setWorker({ ...worker, phone: e.target.value.replace(/\D/g, '').slice(0, 10) })} />
                <Input label={t('Address (optional)')} value={worker.address} onChange={(e) => setWorker({ ...worker, address: e.target.value })} />
              </div>
              <Button size="sm" onClick={createWorker} loading={busy}><Plus className="h-4 w-4" /> {t('Add worker')}</Button>
            </div>
          )}

          <div className="relative mb-2">
            <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-ink-400" />
            <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={t('Search by name or city')}
              className="h-10 w-full rounded-xl border border-ink-200 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400" />
          </div>
          {!list ? <div className="flex justify-center py-4"><Spinner /></div>
            : list.length === 0 ? <p className="py-4 text-center text-sm text-ink-500">{t('No labour found')}</p>
            : (
              <ul className="max-h-52 space-y-1.5 overflow-y-auto">
                {list.map((l) => {
                  const already = project ? onSite.has(l.id) : false
                  return (
                    <li key={l.id}>
                      <button type="button" disabled={already} onClick={() => setPicked(l)}
                        className={`flex w-full items-center justify-between gap-3 rounded-xl border p-2.5 text-left disabled:opacity-50 ${picked?.id === l.id ? 'border-brand-500 bg-brand-50' : 'border-ink-200 hover:bg-ink-50'}`}>
                        <span className="min-w-0">
                          <span className="block truncate text-sm font-medium text-ink-900">{l.name}</span>
                          <span className="block truncate text-xs text-ink-500">
                            {already ? t('Already on this site') : `${l.phone}${l.address ? ` · ${l.address}` : ''}`}
                          </span>
                        </span>
                        {picked?.id === l.id && <Check className="h-4 w-4 shrink-0 text-brand-600" />}
                      </button>
                    </li>
                  )
                })}
              </ul>
            )}
        </div>

        <div className="grid grid-cols-2 gap-3">
          <Input label={t('Daily rate (₹)')} type="number" inputMode="decimal" min="1" step="any" value={form.dailyRate}
            onChange={(e) => setForm({ ...form, dailyRate: e.target.value })} required />
          <Input label={t('Advance given (₹)')} type="number" inputMode="decimal" min="0" step="any" value={form.advanceTaken}
            onChange={(e) => setForm({ ...form, advanceTaken: e.target.value })} />
        </div>
        <Input label={t('Start date')} type="date" max={todayISO()} value={form.assignedDate}
          onChange={(e) => setForm({ ...form, assignedDate: e.target.value })} />
      </div>
    </Modal>
  )
}
