import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CalendarDays, ClipboardList, MapPin, Plus, Ruler, Search } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS } from '../../lib/labels'
import { formatDate, formatNumber } from '../../lib/format'
import CallButton from '../../components/Contact'
import StatusBadge from '../../components/Status'
import { Badge, Button, Card, EmptyState, ErrorState, PageHeader, PageLoader } from '../../components/ui'

export default function Requirements() {
  const { user } = useAuth()
  const { t } = useT()
  const toast = useToast()
  const isOwner = user.roleName === 'HOUSE_OWNER'

  const [city, setCity] = useState('')
  const [list, setList] = useState(null)
  const [error, setError] = useState('')

  const load = () => {
    const req = isOwner ? api.get('/api/requirements/my') : api.get('/api/requirements', { params: { city } })
    return req.then((r) => { setList(r.data); setError('') }).catch((e) => setError(errorMessage(e)))
  }

  useEffect(() => {
    const id = setTimeout(load, isOwner ? 0 : 300)
    return () => clearTimeout(id)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [city, isOwner])

  const close = async (id) => {
    try { await api.patch(`/api/requirements/${id}/close`); toast.success(t('Requirement closed')); load() }
    catch (e) { toast.error(errorMessage(e)) }
  }

  return (
    <>
      <PageHeader
        title={isOwner ? t('My requirements') : t('Open requirements')}
        subtitle={isOwner ? t('Work you have posted for contractors') : t('Find construction work near you and call the owner')}
        actions={isOwner && <Link to="/requirements/new"><Button><Plus className="h-4 w-4" /> {t('Post requirement')}</Button></Link>}
      />

      {!isOwner && (
        <div className="relative mb-4 max-w-sm">
          <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-ink-400" />
          <input value={city} onChange={(e) => setCity(e.target.value)} placeholder={t('Filter by city, e.g. Indore')}
            className="h-10 w-full rounded-xl border border-ink-200 bg-white pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400" />
        </div>
      )}

      {error ? <ErrorState message={error} onRetry={load} /> : !list ? <PageLoader /> : list.length === 0 ? (
        <EmptyState icon={ClipboardList} title={t('No requirements found')}
          text={isOwner ? t('Post what you want built and contractors will contact you.') : t('Try a different city or check again later.')}
          action={isOwner && <Link to="/requirements/new"><Button>{t('Post requirement')}</Button></Link>} />
      ) : (
        <div className="grid gap-3 md:grid-cols-2">
          {list.map((r) => (
            <Card key={r.id} className="p-4">
              <div className="flex items-start justify-between gap-3">
                <h3 className="flex items-center gap-1.5 font-semibold text-ink-900"><MapPin className="h-4 w-4 text-brand-600" /> {r.area}, {r.city} - {r.pincode}</h3>
                {isOwner && <StatusBadge group="requirementStatus" value={r.status} />}
              </div>
              <div className="mt-2 flex flex-wrap gap-2">
                <Badge tone="neutral"><Ruler className="mr-1 h-3 w-3" /> {formatNumber(r.areaSqFt)} sq ft</Badge>
                <Badge tone="brand">{t(LABELS.contractType[r.contractType])}</Badge>
                <Badge tone="neutral"><CalendarDays className="mr-1 h-3 w-3" /> {formatDate(r.postedDate)}</Badge>
              </div>
              {r.description && <p className="mt-3 text-sm text-ink-600">{r.description}</p>}
              <div className="mt-4 flex items-center justify-between gap-3 border-t border-ink-100 pt-3">
                {isOwner
                  ? (r.status === 'OPEN' ? <Button variant="secondary" size="sm" onClick={() => close(r.id)}>{t('Close requirement')}</Button> : <span />)
                  : <span className="min-w-0 truncate text-sm text-ink-600">{r.houseOwner.name}</span>}
                {!isOwner && <CallButton phone={r.houseOwner.phone} label={t('Call owner')} />}
              </div>
            </Card>
          ))}
        </div>
      )}
    </>
  )
}
