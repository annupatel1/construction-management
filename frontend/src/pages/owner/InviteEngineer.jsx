import { useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { ArrowLeft, Send, ShieldCheck } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { todayISO } from '../../lib/format'
import { Alert, Button, Card, ErrorState, Input, PageHeader, PageLoader, Textarea } from '../../components/ui'

const get = (url) => api.get(url).then((r) => r.data)

export default function InviteEngineer() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const engineerId = params.get('engineer')
  const navigate = useNavigate()
  const { t } = useT()
  const toast = useToast()

  const { data, loading, error, reload } = useFetch(async () => {
    if (!engineerId) throw new Error('Choose an engineer from the directory first')
    const [project, engineer] = await Promise.all([
      get(`/api/projects/${id}`),
      get(`/api/engineers/${engineerId}`),
    ])
    return { project, engineer }
  }, [id, engineerId])

  const [form, setForm] = useState({
    scope: '', startDate: todayISO(), endDate: '', feeTerms: '', responseDeadline: '',
  })
  const [formError, setFormError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  if (loading) return <PageLoader />
  if (error) return <ErrorState message={!engineerId ? t('Choose an engineer from the directory first') : error} onRetry={reload} />

  const { project, engineer } = data

  const submit = async (e) => {
    e.preventDefault()
    setFormError('')
    setBusy(true)
    try {
      await api.post(`/api/projects/${project.id}/engineer-invitations`, {
        engineerId: Number(engineerId),
        scope: form.scope,
        startDate: form.startDate,
        endDate: form.endDate,
        feeTerms: form.feeTerms || null,
        responseDeadline: form.responseDeadline || null,
        requestedPermissions: 'PROJECT_READ',
      })
      toast.success(t('Invitation sent'))
      navigate(`/projects/${project.id}`)
    } catch (err) { setFormError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <>
      <Link to={`/engineers/${engineerId}`} className="mb-3 inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
        <ArrowLeft className="h-4 w-4" /> {t('Engineer profile')}
      </Link>
      <PageHeader
        title={t('Invite engineer')}
        subtitle={`${engineer.name} · ${project.title}`}
      />

      <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <Card className="p-5">
          <form onSubmit={submit} className="space-y-4">
            {formError && <Alert>{formError}</Alert>}
            <Textarea label={t('Scope of work')} value={form.scope} onChange={set('scope')} rows={4} required
              placeholder={t('What should the engineer do? e.g. supervise RCC work, check quality, review drawings')} />
            <div className="grid gap-3 sm:grid-cols-2">
              <Input label={t('Start date')} type="date" value={form.startDate} onChange={set('startDate')} required />
              <Input label={t('End date')} type="date" value={form.endDate} onChange={set('endDate')} required />
            </div>
            <Input label={t('Fee terms (optional)')} value={form.feeTerms} onChange={set('feeTerms')}
              placeholder={t('e.g. ₹40,000 per month')} />
            <Input label={t('Response deadline (optional)')} type="date" value={form.responseDeadline} onChange={set('responseDeadline')} />
            <Button type="submit" loading={busy} size="lg" className="w-full">
              <Send className="h-4 w-4" /> {t('Send invitation')}
            </Button>
          </form>
        </Card>

        <div className="space-y-4">
          <Card className="p-4">
            <h2 className="mb-2 flex items-center gap-2 font-semibold text-ink-900"><ShieldCheck className="h-4 w-4" /> {t('Access is granted on acceptance')}</h2>
            <p className="text-sm leading-6 text-ink-600">
              {t('The engineer will not see this project until they accept. On acceptance they get scoped site access for progress, tasks, milestones and issues; financial details stay hidden.')}
            </p>
          </Card>
          <Card className="p-4">
            <h2 className="mb-1 font-semibold text-ink-900">{engineer.name}</h2>
            <p className="text-sm text-ink-600">{engineer.specialty} · {engineer.experienceYears}+ {t('years')}</p>
            <p className="mt-1 text-sm text-ink-500">{engineer.city}</p>
          </Card>
        </div>
      </div>
    </>
  )
}
