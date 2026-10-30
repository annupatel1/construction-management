import { useState } from 'react'
import { Download } from 'lucide-react'
import { Button } from './ui'
import { useT } from '../i18n/LanguageContext'
import { useToast } from '../context/ToastContext'
import { downloadFile, errorMessage } from '../lib/api'

export default function ExportButton({
  path,
  label = 'Download CSV',
  variant = 'secondary',
  size = 'sm',
  className = '',
}) {
  const { t } = useT()
  const toast = useToast()
  const [loading, setLoading] = useState(false)

  const run = async () => {
    setLoading(true)
    try {
      await downloadFile(path)
      toast.success(t('Download started'))
    } catch (err) {
      toast.error(errorMessage(err, t('Could not download the report')))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Button type="button" variant={variant} size={size} loading={loading}
      onClick={run} className={className}>
      <Download className="h-4 w-4" /> {t(label)}
    </Button>
  )
}
