import { useState } from 'react'
import { Link } from 'react-router-dom'
import { CalendarDays, Check, Clock, PencilRuler, UserPlus, Wallet, X } from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import useFetch from '../../hooks/useFetch'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { formatDate } from '../../lib/format'
import StatusBadge from '../../components/Status'
import {
  Alert, Button, Card, EmptyState, ErrorState, Modal, PageHeader, PageLoader, Textarea,
} from '../../components/ui'

const get = (url) => api.get(url).then((r) => r.data)

export default function Invitations() {
  const { user } = useAuth()
  const { t } = useT()
  const toast = useToast()
  const isEngineer = user.roleName === 'ENGINEER'
  const canInvite = user.roleName === 'HOUSE_OWNER' || user.roleName === 'CONTRACTOR'

  const { data, loading, error, reload } = useFetch(
    () => get(isEngineer ? '/api/engineer-invitations/inbox' : '/api/engineer-invitations/sent'),
    [isEngineer],
  )
  const [busyId, setBusyId] = useState(null)
  const [actionError, setActionError] = useState('')
  const [declineTarget, setDeclineTarget] = useState(null)
  const [declineMessage, setDeclineMessage] = useState('')

  if (loading) return <PageLoader />
  if (error) return <ErrorState message={error} onRetry={reload} />

  const act = async (invitation, action, body = null) => {
    setActionError('')
    setBusyId(invitation.id)
    try {
      await api.post(`/api/engineer-invitations/${invitation.id}/${action}`, body)
      toast.success(action === 'accept' ? t('Invitation accepted') : action === 'decline' ? t('Invitation declined') : t('Invitation cancelled'))
      setDeclineTarget(null)
      setDeclineMessage('')
      reload()
    } catch (err) { setActionError(errorMessage(err)) } finally { setBusyId(null) }
  }

  const ordered = [...data].sort((a, b) => (a.status === 'PENDING' ? 0 : 1) - (b.status === 'PENDING' ? 0 : 1) || b.id - a.id)
  const pending = data.filter((i) => i.status === 'PENDING')

  return (
    <>
      <PageHeader
        title={t('Engineer invitations')}
        subtitle={isEngineer
          ? t('Invitations from owners and contractors. You get project access only after you accept.')
          : t('Invitations you sent to engineers.')}
        actions={canInvite ? <Link to="/engineers"><Button><UserPlus className="h-4 w-4" /> {t('Find engineers')}</Button></Link> : null}
      />

      {actionError && <Alert className="mb-4">{actionError}</Alert>}
      {isEngineer && pending.length > 0 && (
        <Alert tone="info" className="mb-4">
          {t('You have {n} pending invitation(s).', { n: pending.length })}
        </Alert>
      )}

      {data.length === 0 ? (
        <EmptyState
          icon={UserPlus}
          title={isEngineer ? t('No invitations yet') : t('No invitations sent yet')}
          text={isEngineer
            ? t('When an owner or contractor invites you, it will show here.')
            : t('Find an engineer, open their profile and invite them to one of your projects.')}
          action={canInvite ? <Link to="/engineers"><Button>{t('Find engineers')}</Button></Link> : null}
        />
      ) : (
        <div className="space-y-3">
          {ordered.map((invitation) => {
            const pendingInvite = invitation.status === 'PENDING'
            return (
              <Card key={invitation.id} className="p-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h2 className="truncate font-semibold text-ink-900">{invitation.projectTitle}</h2>
                      <StatusBadge group="invitationStatus" value={invitation.status} />
                    </div>
                    <p className="mt-0.5 text-sm text-ink-500">
                      {isEngineer
                        ? `${t('From')} ${invitation.inviter.name}`
                        : `${t('To')} ${invitation.engineer.name}`}
                      {' · '}{invitation.projectLocation}
                    </p>
                  </div>
                  {invitation.status === 'ACCEPTED' && (
                    <Link to={`/projects/${invitation.projectId}`}>
                      <Button size="sm" variant="secondary">{t('Open project')}</Button>
                    </Link>
                  )}
                </div>

                <p className="mt-3 whitespace-pre-line text-sm leading-6 text-ink-700">{invitation.scope}</p>

                <div className="mt-3 grid gap-2 border-t border-ink-100 pt-3 text-xs text-ink-600 sm:grid-cols-3">
                  <span className="flex items-center gap-1.5"><CalendarDays className="h-3.5 w-3.5" />
                    {invitation.startDate ? formatDate(invitation.startDate) : t('Start not set')}
                    {' → '}
                    {invitation.endDate ? formatDate(invitation.endDate) : t('Open ended')}
                  </span>
                  {invitation.feeTerms && <span className="flex items-center gap-1.5"><Wallet className="h-3.5 w-3.5" /> {invitation.feeTerms}</span>}
                  {invitation.responseDeadline && <span className="flex items-center gap-1.5"><Clock className="h-3.5 w-3.5" /> {t('Reply by')} {formatDate(invitation.responseDeadline)}</span>}
                </div>
                {invitation.requestedPermissions && (
                  <p className="mt-2 text-xs font-medium text-ink-500">
                    {t('Access offered')}: {t('Project site access')}
                  </p>
                )}

                {pendingInvite && isEngineer && (
                  <div className="mt-4 flex flex-wrap gap-2 border-t border-ink-100 pt-3">
                    <Button size="sm" loading={busyId === invitation.id} onClick={() => act(invitation, 'accept')}>
                      <Check className="h-4 w-4" /> {t('Accept')}
                    </Button>
                    <Button size="sm" variant="secondary" onClick={() => { setDeclineTarget(invitation); setDeclineMessage(''); setActionError('') }}>
                      <X className="h-4 w-4" /> {t('Decline')}
                    </Button>
                  </div>
                )}
                {pendingInvite && canInvite && (
                  <div className="mt-4 border-t border-ink-100 pt-3">
                    <Button size="sm" variant="secondary" loading={busyId === invitation.id} onClick={() => act(invitation, 'cancel')}>
                      <X className="h-4 w-4" /> {t('Cancel invitation')}
                    </Button>
                  </div>
                )}
                {invitation.responseMessage && invitation.status === 'DECLINED' && (
                  <p className="mt-3 rounded-xl bg-ink-50 px-3 py-2 text-sm text-ink-600">
                    {t('Engineer note')}: {invitation.responseMessage}
                  </p>
                )}
              </Card>
            )
          })}
        </div>
      )}

      {canInvite && (
        <p className="mt-4 flex items-center gap-1.5 text-xs text-ink-500">
          <PencilRuler className="h-3.5 w-3.5" /> {t('Pending invitations do not grant project access.')}
        </p>
      )}

      <Modal
        open={!!declineTarget}
        onClose={() => setDeclineTarget(null)}
        title={t('Decline invitation')}
        footer={(
          <>
            <Button variant="secondary" onClick={() => setDeclineTarget(null)}>{t('Cancel')}</Button>
            <Button loading={busyId === declineTarget?.id}
              onClick={() => act(declineTarget, 'decline', { responseMessage: declineMessage || null })}>
              {t('Decline invitation')}
            </Button>
          </>
        )}
      >
        <p className="mb-3 text-sm text-ink-600">{t('You can add a short note for the owner or contractor.')}</p>
        <Textarea label={t('Message (optional)')} value={declineMessage} onChange={(e) => setDeclineMessage(e.target.value)} rows={3} />
      </Modal>
    </>
  )
}
