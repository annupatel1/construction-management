import { Badge } from './ui'
import { useT } from '../i18n/LanguageContext'
import { GROUP_STATUS_TONE, LABELS, STATUS_TONE } from '../lib/labels'

/** <StatusBadge group="projectStatus" value="ONGOING" /> */
export default function StatusBadge({ group, value }) {
  const { t } = useT()
  if (!value) return null
  return <Badge tone={GROUP_STATUS_TONE[group]?.[value] || STATUS_TONE[value] || 'neutral'}>{t(LABELS[group]?.[value] || value)}</Badge>
}
