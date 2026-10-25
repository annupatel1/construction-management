import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api, { errorMessage } from '../../lib/api'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { Alert, Button, Card, Input, PageHeader, Select, Textarea } from '../../components/ui'

export default function NewRequirement() {
  const { t } = useT()
  const toast = useToast()
  const navigate = useNavigate()
  const [form, setForm] = useState({ city: '', area: '', pincode: '', areaSqFt: '', contractType: 'LABOUR_MATERIAL', description: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setError(''); setBusy(true)
    try {
      await api.post('/api/requirements', { ...form, areaSqFt: Number(form.areaSqFt), description: form.description || null })
      toast.success(t('Requirement posted'))
      navigate('/requirements')
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <>
      <PageHeader title={t('Post a requirement')} subtitle={t('Describe the work. Contractors in your city can see it and call you.')} />
      <Card className="max-w-2xl p-5">
        <form onSubmit={submit} className="space-y-4">
          {error && <Alert>{error}</Alert>}
          <div className="grid gap-4 sm:grid-cols-2">
            <Input label={t('City')} value={form.city} onChange={set('city')} placeholder="Indore" required />
            <Input label={t('Area / locality')} value={form.area} onChange={set('area')} placeholder="Vijay Nagar" required />
            <Input label={t('Pincode')} value={form.pincode} onChange={set('pincode')} inputMode="numeric" maxLength={6} placeholder="452010" required />
            <Input label={t('Area (sq ft)')} type="number" inputMode="decimal" min="1" step="any" value={form.areaSqFt} onChange={set('areaSqFt')} required />
          </div>
          <Select label={t('Contract type')} value={form.contractType} onChange={set('contractType')}
            hint={form.contractType === 'LABOUR_ONLY' ? t('You supply the material, the contractor supplies labour') : t('The contractor supplies both labour and material')}>
            {options('contractType').map((o) => <option key={o} value={o}>{t(LABELS.contractType[o])}</option>)}
          </Select>
          <Textarea label={t('Description (optional)')} value={form.description} onChange={set('description')}
            placeholder={t('e.g. Ground + 1 floor house, 3 bedrooms, work to start next month')} />
          <Button type="submit" size="lg" loading={busy}>{t('Post requirement')}</Button>
        </form>
      </Card>
    </>
  )
}
