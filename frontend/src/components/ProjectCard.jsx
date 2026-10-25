import { Link } from 'react-router-dom'
import { MapPin, Ruler } from 'lucide-react'
import { Card } from './ui'
import StatusBadge from './Status'
import { useT } from '../i18n/LanguageContext'
import { useAuth } from '../context/AuthContext'
import { LABELS } from '../lib/labels'
import { formatINR, formatNumber } from '../lib/format'

export default function ProjectCard({ project }) {
  const { t } = useT()
  const { user } = useAuth()
  // show the "other side" of the project
  const other = user.roleName === 'HOUSE_OWNER'
    ? { label: 'Contractor', name: project.contractor?.name }
    : { label: 'Owner', name: project.houseOwner?.name || 'Self-managed' }

  return (
    <Link to={`/projects/${project.id}`}>
      <Card className="h-full p-4 transition-shadow hover:shadow-md">
        <div className="flex items-start justify-between gap-3">
          <h3 className="font-semibold text-ink-900">{project.title}</h3>
          <StatusBadge group="projectStatus" value={project.status} />
        </div>
        <div className="mt-2 space-y-1 text-sm text-ink-500">
          <p className="flex items-center gap-1.5"><MapPin className="h-4 w-4" /> {project.location}</p>
          <p className="flex items-center gap-1.5"><Ruler className="h-4 w-4" /> {formatNumber(project.areaSqFt)} sq ft · {t(LABELS.contractType[project.contractType])}</p>
        </div>
        <div className="mt-3 flex items-end justify-between border-t border-ink-100 pt-3">
          <div>
            <p className="text-xs text-ink-500">{t('Estimated cost')}</p>
            <p className="font-semibold text-ink-900">{formatINR(project.totalEstimatedCost)}</p>
          </div>
          <p className="text-right text-xs text-ink-500">{t(other.label)}<br /><span className="text-sm font-medium text-ink-800">{other.name}</span></p>
        </div>
      </Card>
    </Link>
  )
}
