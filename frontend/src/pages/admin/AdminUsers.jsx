import api from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useT } from '../../i18n/LanguageContext'
import { LABELS } from '../../lib/labels'
import { Badge, Card, ErrorState, PageHeader, PageLoader } from '../../components/ui'

export default function AdminUsers() {
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(() => api.get('/api/users').then((r) => r.data))
  return (
    <>
      <PageHeader title={t('Users')} subtitle={t('Everyone registered on the platform')} />
      {loading ? <PageLoader /> : error ? <ErrorState message={error} onRetry={reload} /> : (
        <Card className="overflow-x-auto">
          <table className="w-full min-w-[640px] text-left text-sm">
            <thead className="border-b border-ink-100 bg-ink-50 text-xs uppercase tracking-wide text-ink-500">
              <tr>
                <th className="px-4 py-3">{t('Name')}</th><th className="px-4 py-3">{t('Role')}</th>
                <th className="px-4 py-3">{t('Mobile number')}</th><th className="px-4 py-3">Email</th><th className="px-4 py-3">{t('Address')}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100">
              {data.map((u) => (
                <tr key={u.id}>
                  <td className="px-4 py-3 font-medium text-ink-900">{u.name}</td>
                  <td className="px-4 py-3"><Badge tone="brand">{t(LABELS.role[u.roleName])}</Badge></td>
                  <td className="px-4 py-3">{u.phone} {!u.phoneVerified && <Badge tone="warning">{t('Unverified')}</Badge>}</td>
                  <td className="px-4 py-3 text-ink-600">{u.email || '-'}</td>
                  <td className="px-4 py-3 text-ink-600">{u.address}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </>
  )
}
