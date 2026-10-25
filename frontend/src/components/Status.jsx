import { Badge } from './ui'
import { useT } from '../i18n/LanguageContext'
import { LABELS, STATUS_TONE } from '../lib/labels'

/** <StatusBadge group="projectStatus" value="ONGOING" /> */
export default function StatusBadge({ group, value }) {
  const { t } = useT()
  if (!value) return null
  return <Badge tone={STATUS_TONE[value] || 'neutral'}>{t(LABELS[group]?.[value] || value)}</Badge>
}
