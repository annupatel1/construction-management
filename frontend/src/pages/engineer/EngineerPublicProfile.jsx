import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, BadgeCheck, Building2, Globe, MapPin, PencilRuler, UserPlus } from 'lucide-react'
import api from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useAuth } from '../../context/AuthContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS } from '../../lib/labels'
import { initials } from '../../lib/format'
import { Badge, Button, Card, ErrorState, PageLoader, Row, Select } from '../../components/ui'

const get = (url) => api.get(url).then((r) => r.data)

export default function EngineerPublicProfile() {
  const { id } = useParams()
  const { user } = useAuth()
  const { t } = useT()
  const canInvite = user.roleName === 'HOUSE_OWNER' || user.roleName === 'CONTRACTOR'
  const [projectId, setProjectId] = useState('')

  const { data, loading, error, reload } = useFetch(async () => {
    const profile = await get(`/api/engineers/${id}`)
    const projects = canInvite ? await get('/api/projects') : []
    return { profile, projects }
  }, [id, canInvite])

  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const { profile, projects } = data

  return (
    <>
      <Link to={canInvite ? '/engineers' : '/projects'} className="mb-3 inline-flex items-center gap-1 text-sm text-ink-500 hover:text-ink-800">
        <ArrowLeft className="h-4 w-4" /> {canInvite ? t('Engineers') : t('Projects')}
      </Link>

      <Card className="mb-4 p-5">
        <div className="flex flex-wrap items-start gap-4">
          <span className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-50 text-xl font-semibold text-brand-700">
            {initials(profile.name)}
          </span>
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-center gap-2">
              <h1 className="text-xl font-semibold text-ink-900">{profile.name}</h1>
              {profile.credentialStatus === 'VERIFIED' && <BadgeCheck className="h-5 w-5 text-emerald-600" />}
            </div>
            <p className="text-sm text-ink-600">{profile.specialty}</p>
            <div className="mt-2 flex flex-wrap gap-2">
              <Badge tone={profile.availability === 'AVAILABLE' ? 'success' : profile.availability === 'BUSY' ? 'warning' : 'neutral'}>
                {t(LABELS.engineerAvailability[profile.availability])}
              </Badge>
              <Badge tone="brand">{profile.experienceYears}+ {t('years')}</Badge>
              <Badge tone={profile.credentialStatus === 'VERIFIED' ? 'success' : 'neutral'}>
                {t(LABELS.credentialStatus[profile.credentialStatus])}
              </Badge>
            </div>
          </div>
        </div>
        {profile.bio && <p className="mt-4 text-sm leading-6 text-ink-700">{profile.bio}</p>}
      </Card>

      <div className="grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
        <div className="space-y-4">
          <Card className="p-4">
            <h2 className="mb-2 font-semibold text-ink-900">{t('Professional details')}</h2>
            <div className="divide-y divide-ink-100">
              <Row label={t('Experience')}>{profile.experienceYears} {t('years')}</Row>
              <Row label={t('City')}>{profile.city || '-'}</Row>
              <Row label={t('Service area')}>{profile.serviceArea || '-'}</Row>
              <Row label={t('Languages')}>{profile.languages.length ? profile.languages.join(', ') : '-'}</Row>
            </div>
          </Card>

          <Card className="p-4">
            <h2 className="mb-2 font-semibold text-ink-900">{t('Skills')}</h2>
            {profile.skills.length ? (
              <div className="flex flex-wrap gap-2">
                {profile.skills.map((skill) => <Badge key={skill} tone="neutral">{skill}</Badge>)}
              </div>
            ) : <p className="text-sm text-ink-500">{t('No skills listed yet.')}</p>}
            {profile.portfolioUrl?.startsWith('http') && (
              <a href={profile.portfolioUrl} target="_blank" rel="noreferrer"
                className="mt-4 inline-flex items-center gap-1.5 text-sm font-medium text-brand-700 hover:underline">
                <Globe className="h-4 w-4" /> {t('View portfolio')}
              </a>
            )}
          </Card>
        </div>

        <div className="space-y-4">
          <Card className="p-4">
            <h2 className="mb-1 flex items-center gap-2 font-semibold text-ink-900"><PencilRuler className="h-4 w-4" /> {t('Work with this engineer')}</h2>
            <p className="mb-3 text-sm text-ink-500">
              {t('Phone and email stay private until the engineer accepts your project invitation.')}
            </p>
            {canInvite ? (
              projects.length === 0 ? (
                <div className="space-y-3">
                  <p className="text-sm text-ink-600">{t('Start a project first, then invite this engineer.')}</p>
                  <Link to="/projects/new"><Button size="sm"><Building2 className="h-4 w-4" /> {t('Start a project')}</Button></Link>
                </div>
              ) : (
                <div className="space-y-3">
                  <Select label={t('Choose a project')} value={projectId} onChange={(e) => setProjectId(e.target.value)}>
                    <option value="">{t('Select project')}</option>
                    {projects.map((project) => <option key={project.id} value={project.id}>{project.title} · {project.location}</option>)}
                  </Select>
                  <Link to={projectId ? `/projects/${projectId}/invite-engineer?engineer=${profile.userId}` : '#'}
                    onClick={(e) => !projectId && e.preventDefault()}>
                    <Button className="w-full" disabled={!projectId}><UserPlus className="h-4 w-4" /> {t('Invite to project')}</Button>
                  </Link>
                </div>
              )
            ) : (
              <p className="text-sm text-ink-600">{t('Only house owners and contractors can invite engineers to a project.')}</p>
            )}
          </Card>

          <Card className="p-4">
            <h2 className="mb-2 flex items-center gap-2 font-semibold text-ink-900"><MapPin className="h-4 w-4" /> {t('Service area')}</h2>
            <p className="text-sm text-ink-700">{profile.serviceArea || profile.city || '-'}</p>
          </Card>
        </div>
      </div>
    </>
  )
}
