import { Link } from 'react-router-dom'
import { Building2, Plus } from 'lucide-react'
import api from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useAuth } from '../../context/AuthContext'
import { useT } from '../../i18n/LanguageContext'
import ProjectCard from '../../components/ProjectCard'
import { Button, EmptyState, ErrorState, PageHeader, PageLoader } from '../../components/ui'

export default function Projects() {
  const { user } = useAuth()
  const { t } = useT()
  const { data, loading, error, reload } = useFetch(() => api.get('/api/projects').then((r) => r.data))
  const isOwner = user.roleName === 'HOUSE_OWNER'
  const canCreate = isOwner || user.roleName === 'CONTRACTOR'

  return (
    <>
      <PageHeader
        title={t('Projects')}
        subtitle={t('All your construction projects')}
        actions={canCreate && (
          <Link to="/projects/new"><Button><Plus className="h-4 w-4" /> {isOwner ? t('New project') : t('New site')}</Button></Link>
        )}
      />
      {loading ? <PageLoader /> : error ? <ErrorState message={error} onRetry={reload} /> : data.length === 0 ? (
        <EmptyState icon={Building2} title={t('No projects yet')}
          text={isOwner ? t('Find a contractor and start your first project.') : t('Projects will appear here once an owner starts one with you.')}
          action={isOwner
            ? <Link to="/contractors"><Button>{t('Find a contractor')}</Button></Link>
            : <Link to="/projects/new"><Button><Plus className="h-4 w-4" /> {t('New site')}</Button></Link>} />
      ) : (
        <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-3">{data.map((p) => <ProjectCard key={p.id} project={p} />)}</div>
      )}
    </>
  )
}
