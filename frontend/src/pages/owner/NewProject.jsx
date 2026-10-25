import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Check, Search } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { formatINR, todayISO } from '../../lib/format'
import CallButton from '../../components/Contact'
import { Alert, Button, Card, Input, PageHeader, Select, Spinner } from '../../components/ui'

export default function NewProject() {
  const { t } = useT()
  const { user } = useAuth()
  const toast = useToast()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const isOwner = user.roleName === 'HOUSE_OWNER'

  const [q, setQ] = useState('')
  const [contractors, setContractors] = useState(null)
  const [selected, setSelected] = useState(null)
  const [form, setForm] = useState({
    title: '', location: '', areaSqFt: '', ratePerSqFt: '', contractType: 'LABOUR_MATERIAL', startDate: todayISO(), endDate: '',
  })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  // search contractors (debounced) - only a house owner needs to pick one
  useEffect(() => {
    if (!isOwner) return
    const id = setTimeout(() => {
      api.get('/api/directory/contractors', { params: { q } })
        .then((r) => {
          setContractors(r.data)
          const pre = Number(params.get('contractor'))
          if (pre && !selected) setSelected(r.data.find((c) => c.id === pre) || null)
        })
        .catch((err) => setError(errorMessage(err)))
    }, 300)
    return () => clearTimeout(id)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [q, isOwner])

  const estimate = useMemo(() => (Number(form.areaSqFt) || 0) * (Number(form.ratePerSqFt) || 0), [form.areaSqFt, form.ratePerSqFt])

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    if (isOwner && !selected) return setError(t('Please choose a contractor'))
    setBusy(true)
    try {
      const body = {
        ...form,
        areaSqFt: Number(form.areaSqFt),
        ratePerSqFt: Number(form.ratePerSqFt),
        endDate: form.endDate || null,
      }
      const { data } = isOwner
        ? await api.post(`/api/projects/${selected.id}`, body)
        : await api.post('/api/projects', body)
      toast.success(t('Project created'))
      navigate(`/projects/${data.id}`)
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <>
      <PageHeader
        title={isOwner ? t('Start a project') : t('New site')}
        subtitle={isOwner ? t('Choose a contractor and enter the project details') : t('Create a site you will run as the contractor')}
      />
      <div className={isOwner ? 'grid gap-5 lg:grid-cols-[1fr_1.1fr]' : 'mx-auto max-w-2xl'}>
        {/* 1. contractor (house owner only) */}
        {isOwner && (
          <Card className="p-4">
            <h2 className="mb-3 font-semibold text-ink-900">1. {t('Choose contractor')}</h2>
            <div className="relative mb-3">
              <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-ink-400" />
              <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={t('Search by name or city')}
                className="h-10 w-full rounded-xl border border-ink-200 pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400" />
            </div>
            {!contractors ? <div className="flex justify-center py-6"><Spinner /></div>
              : contractors.length === 0 ? <p className="py-6 text-center text-sm text-ink-500">{t('No contractors found')}</p>
              : (
                <ul className="max-h-96 space-y-2 overflow-y-auto">
                  {contractors.map((c) => (
                    <li key={c.id}>
                      <button type="button" onClick={() => setSelected(c)}
                        className={`flex w-full items-center justify-between gap-3 rounded-xl border p-3 text-left ${selected?.id === c.id ? 'border-brand-500 bg-brand-50' : 'border-ink-200 hover:bg-ink-50'}`}>
                        <span className="min-w-0">
                          <span className="block truncate font-medium text-ink-900">{c.name}</span>
                          <span className="block truncate text-xs text-ink-500">{c.address}</span>
                        </span>
                        {selected?.id === c.id && <Check className="h-5 w-5 shrink-0 text-brand-600" />}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            {selected && <div className="mt-3"><CallButton phone={selected.phone} label={`${t('Call')} ${selected.name}`} /></div>}
          </Card>
        )}

        {/* details */}
        <Card className="p-4">
          <h2 className="mb-3 font-semibold text-ink-900">{isOwner ? '2. ' : ''}{t('Project details')}</h2>
          <form onSubmit={submit} className="space-y-4">
            {error && <Alert>{error}</Alert>}
            {!isOwner && <Alert tone="info">{t('This site will belong to you as the contractor. You can add labour and track wages here.')}</Alert>}
            <Input label={t('Project title')} value={form.title} onChange={set('title')} placeholder="2BHK house construction" required />
            <Input label={t('Site location')} value={form.location} onChange={set('location')} placeholder="Plot 14, Vijay Nagar, Indore" required />
            <div className="grid grid-cols-2 gap-3">
              <Input label={t('Area (sq ft)')} type="number" inputMode="decimal" min="1" step="any" value={form.areaSqFt} onChange={set('areaSqFt')} required />
              <Input label={t('Rate per sq ft (₹)')} type="number" inputMode="decimal" min="1" step="any" value={form.ratePerSqFt} onChange={set('ratePerSqFt')} required />
            </div>
            <div className="rounded-xl bg-brand-50 px-4 py-3 text-sm">
              <span className="text-ink-600">{t('Estimated cost')}: </span>
              <span className="font-semibold text-brand-800">{formatINR(estimate)}</span>
            </div>
            <Select label={t('Contract type')} value={form.contractType} onChange={set('contractType')}
              hint={form.contractType === 'LABOUR_ONLY' ? t('You supply the material, the contractor supplies labour') : t('The contractor supplies both labour and material')}>
              {options('contractType').map((o) => <option key={o} value={o}>{t(LABELS.contractType[o])}</option>)}
            </Select>
            <div className="grid grid-cols-2 gap-3">
              <Input label={t('Start date')} type="date" value={form.startDate} onChange={set('startDate')} required />
              <Input label={t('End date (optional)')} type="date" min={form.startDate} value={form.endDate} onChange={set('endDate')} />
            </div>
            <Button type="submit" size="lg" className="w-full" loading={busy}>{t('Create project')}</Button>
          </form>
        </Card>
      </div>
    </>
  )
}
