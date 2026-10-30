import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { BadgeCheck, ExternalLink, PencilRuler } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { Alert, Badge, Button, Card, Input, PageHeader, PageLoader, Select, Textarea } from '../../components/ui'

const EMPTY = {
  specialty: '', skills: '', experienceYears: '', city: '', serviceArea: '', languages: '',
  availability: 'AVAILABLE', bio: '', portfolioUrl: '',
}

export default function EngineerProfile() {
  const { user } = useAuth()
  const { t } = useT()
  const toast = useToast()
  const [form, setForm] = useState(EMPTY)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  useEffect(() => {
    let alive = true
    api.get('/api/engineers/me')
      .then(({ data }) => {
        if (!alive) return
        setProfile(data)
        setForm({
          specialty: data.specialty || '',
          skills: (data.skills || []).join(', '),
          experienceYears: data.experienceYears ?? '',
          city: data.city || '',
          serviceArea: data.serviceArea || '',
          languages: (data.languages || []).join(', '),
          availability: data.availability || 'AVAILABLE',
          bio: data.bio || '',
          portfolioUrl: data.portfolioUrl || '',
        })
      })
      .catch((err) => {
        if (!alive) return
        if (err.response?.status !== 404) setError(errorMessage(err))
      })
      .finally(() => alive && setLoading(false))
    return () => { alive = false }
  }, [])

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const { data } = await api.put('/api/engineers/me', {
        ...form,
        experienceYears: Number(form.experienceYears || 0),
        bio: form.bio || null,
        portfolioUrl: form.portfolioUrl || null,
        skills: form.skills || null,
        serviceArea: form.serviceArea || null,
        languages: form.languages || null,
      })
      setProfile(data)
      setForm((f) => ({ ...f, skills: (data.skills || []).join(', '), languages: (data.languages || []).join(', ') }))
      toast.success(t('Engineer profile saved'))
    } catch (err) { setError(errorMessage(err)) } finally { setBusy(false) }
  }

  if (loading) return <PageLoader />

  return (
    <>
      <PageHeader
        title={t('Engineer profile')}
        subtitle={t('This profile is shown in the engineer directory. Your phone and email are never shown publicly.')}
        actions={profile ? <Link to={`/engineers/${user.id}`}><Button variant="secondary"><ExternalLink className="h-4 w-4" /> {t('View public profile')}</Button></Link> : null}
      />

      {!profile && (
        <Alert tone="info" className="mb-4">
          {t('Complete your profile so owners and contractors can find and invite you.')}
        </Alert>
      )}

      <div className="grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
        <Card className="p-5">
          <form onSubmit={submit} className="space-y-4">
            {error && <Alert>{error}</Alert>}

            <Input label={t('Specialty')} value={form.specialty} onChange={set('specialty')}
              placeholder={t('e.g. Structural engineer')} required />
            <Input label={t('Skills')} value={form.skills} onChange={set('skills')}
              placeholder="RCC, Site supervision, Quality checks"
              hint={t('Separate skills with commas')} />
            <div className="grid gap-3 sm:grid-cols-2">
              <Input label={t('Experience (years)')} type="number" inputMode="numeric" min="0" max="60"
                value={form.experienceYears} onChange={set('experienceYears')} required />
              <Select label={t('Availability')} value={form.availability} onChange={set('availability')}>
                {options('engineerAvailability').map((key) => <option key={key} value={key}>{t(LABELS.engineerAvailability[key])}</option>)}
              </Select>
            </div>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input label={t('City')} value={form.city} onChange={set('city')} placeholder="Indore" required />
              <Input label={t('Service area')} value={form.serviceArea} onChange={set('serviceArea')} placeholder="Indore and nearby" />
            </div>
            <Input label={t('Languages')} value={form.languages} onChange={set('languages')}
              placeholder="Hindi, English" hint={t('Separate languages with commas')} />
            <Textarea label={t('About you')} value={form.bio} onChange={set('bio')} rows={4}
              placeholder={t('Describe your experience and the work you take on')} />
            <Input label={t('Portfolio link (optional)')} value={form.portfolioUrl} onChange={set('portfolioUrl')}
              placeholder="https://..." />

            <Button type="submit" loading={busy} size="lg" className="w-full">
              <PencilRuler className="h-4 w-4" /> {profile ? t('Save profile') : t('Create profile')}
            </Button>
          </form>
        </Card>

        <div className="space-y-4">
          <Card className="p-4">
            <h2 className="mb-2 flex items-center gap-2 font-semibold text-ink-900"><BadgeCheck className="h-4 w-4" /> {t('Profile status')}</h2>
            {profile ? (
              <div className="space-y-3 text-sm">
                <div className="flex items-center justify-between gap-3">
                  <span className="text-ink-500">{t('Completeness')}</span>
                  <Badge tone={profile.profileComplete ? 'success' : 'warning'}>
                    {profile.profileComplete ? t('Complete') : t('Incomplete')}
                  </Badge>
                </div>
                <div className="flex items-center justify-between gap-3">
                  <span className="text-ink-500">{t('Credentials')}</span>
                  <Badge tone={profile.credentialStatus === 'VERIFIED' ? 'success' : 'neutral'}>
                    {t(LABELS.credentialStatus[profile.credentialStatus])}
                  </Badge>
                </div>
              </div>
            ) : <p className="text-sm text-ink-500">{t('Your profile is not created yet.')}</p>}
          </Card>

          <Card className="p-4">
            <h2 className="mb-2 font-semibold text-ink-900">{t('Privacy')}</h2>
            <p className="text-sm leading-6 text-ink-600">
              {t('Owners and contractors see your public profile only. Your phone and email are shared after you accept a project invitation.')}
            </p>
          </Card>
        </div>
      </div>
    </>
  )
}
