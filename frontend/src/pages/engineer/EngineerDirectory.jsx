import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BadgeCheck, MapPin, PencilRuler, Search, SlidersHorizontal } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { initials } from '../../lib/format'
import { Badge, Card, EmptyState, ErrorState, Input, PageHeader, PageLoader, Select } from '../../components/ui'

export default function EngineerDirectory() {
  const { t } = useT()
  const [filters, setFilters] = useState({ q: '', city: '', specialty: '', minExperience: '', availability: '' })
  const [list, setList] = useState(null)
  const [error, setError] = useState('')

  const set = (key) => (e) => setFilters((f) => ({ ...f, [key]: e.target.value }))

  useEffect(() => {
    const timer = setTimeout(() => {
      const params = Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== ''))
      if (params.minExperience) params.minExperience = Number(params.minExperience)
      api.get('/api/directory/engineers', { params })
        .then((r) => { setList(r.data); setError('') })
        .catch((e) => setError(errorMessage(e)))
    }, 300)
    return () => clearTimeout(timer)
  }, [filters])

  return (
    <>
      <PageHeader
        title={t('Find engineers')}
        subtitle={t('Search engineers by city, specialty, experience and availability. Contact details stay private until they accept an invitation.')}
      />

      <Card className="mb-5 p-4">
        <div className="relative mb-3">
          <Search className="pointer-events-none absolute left-3 top-3 h-4 w-4 text-ink-400" />
          <input value={filters.q} onChange={set('q')} placeholder={t('Search name, skill or city')}
            className="h-10 w-full rounded-xl border border-ink-200 bg-white pl-9 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400" />
        </div>
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <Input label={t('City')} value={filters.city} onChange={set('city')} placeholder={t('Any city')} />
          <Input label={t('Specialty')} value={filters.specialty} onChange={set('specialty')} placeholder={t('Any specialty')} />
          <Select label={t('Minimum experience')} value={filters.minExperience} onChange={set('minExperience')}>
            <option value="">{t('Any experience')}</option>
            <option value="2">2+ {t('years')}</option>
            <option value="5">5+ {t('years')}</option>
            <option value="10">10+ {t('years')}</option>
          </Select>
          <Select label={t('Availability')} value={filters.availability} onChange={set('availability')}>
            <option value="">{t('Any availability')}</option>
            {options('engineerAvailability').map((key) => <option key={key} value={key}>{t(LABELS.engineerAvailability[key])}</option>)}
          </Select>
        </div>
      </Card>

      {error ? <ErrorState message={error} /> : !list ? <PageLoader /> : list.length === 0 ? (
        <EmptyState icon={PencilRuler} title={t('No engineers found')}
          text={t('Try a different city or specialty, or check again after more engineers join.')} />
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
          {list.map((engineer) => (
            <Link key={engineer.userId} to={`/engineers/${engineer.userId}`}>
              <Card className="h-full p-4 transition-shadow hover:shadow-md">
                <div className="flex items-start gap-3">
                  <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-brand-50 font-semibold text-brand-700">
                    {initials(engineer.name)}
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="truncate font-semibold text-ink-900">{engineer.name}</p>
                      {engineer.credentialStatus === 'VERIFIED' && <BadgeCheck className="h-4 w-4 text-emerald-600" />}
                    </div>
                    <p className="truncate text-sm text-ink-600">{engineer.specialty}</p>
                    <p className="mt-0.5 flex items-center gap-1 truncate text-xs text-ink-500">
                      <MapPin className="h-3.5 w-3.5 shrink-0" /> {engineer.city}
                    </p>
                  </div>
                </div>

                <div className="mt-3 flex flex-wrap gap-1.5">
                  <Badge tone={engineer.availability === 'AVAILABLE' ? 'success' : engineer.availability === 'BUSY' ? 'warning' : 'neutral'}>
                    {t(LABELS.engineerAvailability[engineer.availability])}
                  </Badge>
                  <Badge tone="brand">{engineer.experienceYears}+ {t('years')}</Badge>
                  {!engineer.profileComplete && <Badge tone="warning">{t('Profile incomplete')}</Badge>}
                </div>

                {engineer.skills.length > 0 && (
                  <p className="mt-3 line-clamp-2 text-xs text-ink-500">{engineer.skills.slice(0, 4).join(' · ')}</p>
                )}
                <div className="mt-3 flex items-center justify-between border-t border-ink-100 pt-3 text-xs">
                  <span className="text-ink-500">{engineer.serviceArea || t('Service area not set')}</span>
                  <span className="font-medium text-brand-700">{t('View profile')} →</span>
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}

      <p className="mt-4 flex items-center gap-1.5 text-xs text-ink-500">
        <SlidersHorizontal className="h-3.5 w-3.5" /> {t('Engineers are shown with public profile details only.')}
      </p>
    </>
  )
}
