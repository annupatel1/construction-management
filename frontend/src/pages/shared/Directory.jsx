import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { MapPin, Search, Users } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useT } from '../../i18n/LanguageContext'
import { initials } from '../../lib/format'
import CallButton from '../../components/Contact'
import { Button, Card, EmptyState, ErrorState, PageHeader, PageLoader } from '../../components/ui'

/** Searchable list of contractors (for owners) or labourers (for contractors). */
function Directory({ endpoint, title, subtitle, emptyText, renderAction }) {
  const { t } = useT()
  const [q, setQ] = useState('')
  const [list, setList] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const id = setTimeout(() => {
      api.get(endpoint, { params: { q } })
        .then((r) => { setList(r.data); setError('') })
        .catch((e) => setError(errorMessage(e)))
    }, 300)
    return () => clearTimeout(id)
  }, [endpoint, q])

  return (
    <>
      <PageHeader title={title} subtitle={subtitle} />
      <div className="relative mb-4 max-w-sm">
        <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-ink-400" />
        <input value={q} onChange={(e) => setQ(e.target.value)} placeholder={t('Search by name or city')}
          className="h-10 w-full rounded-xl border border-ink-200 bg-white pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400" />
      </div>
      {error ? <ErrorState message={error} /> : !list ? <PageLoader /> : list.length === 0 ? (
        <EmptyState icon={Users} title={t('Nobody found')} text={emptyText} />
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {list.map((p) => (
            <Card key={p.id} className="p-4">
              <div className="flex items-center gap-3">
                <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-brand-50 font-semibold text-brand-700">{initials(p.name)}</span>
                <div className="min-w-0">
                  <p className="truncate font-semibold text-ink-900">{p.name}</p>
                  <p className="flex items-center gap-1 truncate text-xs text-ink-500"><MapPin className="h-3.5 w-3.5 shrink-0" /> {p.address}</p>
                </div>
              </div>
              <div className="mt-4 flex items-center justify-between gap-2 border-t border-ink-100 pt-3">
                <CallButton phone={p.phone} label={t('Call')} />
                {renderAction?.(p)}
              </div>
            </Card>
          ))}
        </div>
      )}
    </>
  )
}

export function Contractors() {
  const { t } = useT()
  return (
    <Directory
      endpoint="/api/directory/contractors"
      title={t('Find contractors')}
      subtitle={t('Call a contractor, then start a project with them')}
      emptyText={t('No contractors have registered in this area yet.')}
      renderAction={(p) => <Link to={`/projects/new?contractor=${p.id}`}><Button size="sm">{t('Start project')}</Button></Link>}
    />
  )
}

export function Labourers() {
  const { t } = useT()
  return (
    <Directory
      endpoint="/api/directory/labourers"
      title={t('Find labour')}
      subtitle={t('Registered labourers. Assign them from a project page.')}
      emptyText={t('No labourers have registered yet.')}
    />
  )
}
