import { HardHat } from 'lucide-react'
import api from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useT } from '../../i18n/LanguageContext'
import { AssignmentCard } from '../Dashboard'
import { EmptyState, ErrorState, PageHeader, PageLoader } from '../../components/ui'

export default function Work() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(() => api.get('/api/assignments').then((r) => r.data))
  return (
    <>
      <PageHeader title={t('My work')} subtitle={t('Sites you are working on')} />
      {loading ? <PageLoader /> : error ? <ErrorState message={error} onRetry={reload} /> : data.length === 0 ? (
        <EmptyState icon={HardHat} title={t('No work assigned yet')} text={t('When a contractor adds you to a site it will show here.')} />
      ) : (
        <div className="grid gap-3 md:grid-cols-2">{data.map((a) => <AssignmentCard key={a.id} a={a} />)}</div>
      )}
    </>
  )
}
