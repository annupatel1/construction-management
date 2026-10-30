import { useEffect, useState } from 'react'
import {
  AlertTriangle, Calendar, Camera, CheckCircle2, ClipboardList, Clock, Flag,
  MessageSquare, Pencil, Plus, Send, Trash2,
} from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { formatDate, formatDateTime, todayISO } from '../../lib/format'
import StatusBadge from '../../components/Status'
import ExportButton from '../../components/ExportButton'
import { Alert, Button, Card, EmptyState, Input, Modal, Progress, Select, Textarea } from '../../components/ui'

const canManageSite = (role) => role === 'CONTRACTOR' || role === 'ADMIN'
const canAuthorReports = (role) => role === 'CONTRACTOR' || role === 'ENGINEER' || role === 'ADMIN'
const canPublishReports = (role) => role === 'HOUSE_OWNER' || role === 'CONTRACTOR' || role === 'ADMIN'
const canManageIssues = (role) => role === 'HOUSE_OWNER' || role === 'CONTRACTOR' || role === 'ADMIN'

function assigneeOptions(project, engineers = []) {
  const seen = new Set()
  const people = []
  const add = (id, name, roleName) => {
    if (!id || seen.has(id)) return
    seen.add(id)
    people.push({ id, name, roleName })
  }
  add(project.houseOwner?.id, project.houseOwner?.name, 'HOUSE_OWNER')
  add(project.contractor?.id, project.contractor?.name, 'CONTRACTOR')
  engineers.forEach((member) => add(member.userId, member.name, 'ENGINEER'))
  return people
}

function PhotoGrid({ photos }) {
  if (!photos?.length) return null
  return (
    <div className="mt-3 grid grid-cols-2 gap-2 sm:grid-cols-3">
      {photos.map((photo) => (
        <a key={photo.id || photo.photoUrl} href={photo.photoUrl} target="_blank" rel="noreferrer"
          className="group overflow-hidden rounded-xl border border-ink-200 bg-ink-50">
          <img src={photo.photoUrl} alt={photo.caption || 'Site photo'} loading="lazy"
            className="h-28 w-full object-cover transition-transform group-hover:scale-[1.02]" />
          {photo.caption && <span className="block truncate px-2 py-1.5 text-xs text-ink-600">{photo.caption}</span>}
        </a>
      ))}
    </div>
  )
}

function PhotoFields({ photos, onChange, t, withDate = false }) {
  const update = (index, key, value) => {
    onChange(photos.map((photo, i) => i === index ? { ...photo, [key]: value } : photo))
  }
  const remove = (index) => onChange(photos.filter((_, i) => i !== index))
  return (
    <div className="space-y-2">
      {photos.map((photo, index) => (
        <div key={index} className="space-y-2 rounded-xl border border-ink-200 bg-ink-50/60 p-2.5">
          <div className="flex items-start gap-2">
            <Input value={photo.photoUrl || ''} onChange={(e) => update(index, 'photoUrl', e.target.value)}
              placeholder="https://… photo link" aria-label={t('Photo URL')} />
            <Button type="button" size="sm" variant="ghost" onClick={() => remove(index)} aria-label={t('Remove photo')}>
              <Trash2 className="h-4 w-4" />
            </Button>
          </div>
          <div className={withDate ? 'grid gap-2 sm:grid-cols-2' : ''}>
            <Input value={photo.caption || ''} onChange={(e) => update(index, 'caption', e.target.value)}
              placeholder={t('Caption')} aria-label={t('Caption')} />
            {withDate && (
              <Input type="date" value={photo.takenDate || ''} onChange={(e) => update(index, 'takenDate', e.target.value)}
                aria-label={t('Photo date')} />
            )}
          </div>
        </div>
      ))}
      <Button type="button" size="sm" variant="secondary"
        onClick={() => onChange([...photos, withDate
          ? { photoUrl: '', caption: '', takenDate: todayISO() }
          : { photoUrl: '', caption: '' }])}>
        <Camera className="h-4 w-4" /> {t('Add photo link')}
      </Button>
    </div>
  )
}

const cleanPhotos = (photos) => (photos || [])
  .filter((photo) => photo.photoUrl?.trim())
  .map((photo) => ({
    photoUrl: photo.photoUrl.trim(),
    caption: photo.caption?.trim() || null,
    ...(photo.takenDate !== undefined ? { takenDate: photo.takenDate || null } : {}),
  }))

/* =========================== SNAPSHOT =========================== */
export function SiteSnapshot({ reports = [], tasks = [], milestones = [], issues = [] }) {
  const { t } = useT()
  const openTasks = tasks.filter((task) => task.status !== 'DONE')
  const overdue = tasks.filter((task) => task.overdue)
  const openIssues = issues.filter((issue) => issue.status !== 'RESOLVED')
  const doneMilestones = milestones.filter((milestone) => milestone.status === 'COMPLETED')
  const latest = reports[0]

  return (
    <Card className="p-4">
      <div className="mb-3 flex items-center justify-between gap-2">
        <h3 className="font-semibold text-ink-900">{t('Site snapshot')}</h3>
        {latest && <StatusBadge group="progressStatus" value={latest.status} />}
      </div>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <div className="rounded-xl bg-ink-50 p-3">
          <p className="text-xs text-ink-500">{t('Open tasks')}</p>
          <p className="mt-1 text-xl font-semibold text-ink-900">{openTasks.length}</p>
        </div>
        <div className="rounded-xl bg-ink-50 p-3">
          <p className="text-xs text-ink-500">{t('Overdue')}</p>
          <p className={`mt-1 text-xl font-semibold ${overdue.length ? 'text-red-600' : 'text-ink-900'}`}>{overdue.length}</p>
        </div>
        <div className="rounded-xl bg-ink-50 p-3">
          <p className="text-xs text-ink-500">{t('Open issues')}</p>
          <p className={`mt-1 text-xl font-semibold ${openIssues.length ? 'text-amber-700' : 'text-ink-900'}`}>{openIssues.length}</p>
        </div>
        <div className="rounded-xl bg-ink-50 p-3">
          <p className="text-xs text-ink-500">{t('Milestones done')}</p>
          <p className="mt-1 text-xl font-semibold text-ink-900">{doneMilestones.length}/{milestones.length}</p>
        </div>
      </div>
      {latest ? (
        <div className="mt-3 border-t border-ink-100 pt-3 text-sm">
          <p className="flex items-center gap-1.5 text-xs font-medium text-ink-500">
            <Calendar className="h-3.5 w-3.5" /> {t('Latest progress report')} · {formatDate(latest.reportDate)}
          </p>
          <p className="mt-1 line-clamp-2 text-ink-800">{latest.completedWork}</p>
        </div>
      ) : (
        <p className="mt-3 border-t border-ink-100 pt-3 text-sm text-ink-500">{t('No progress reports yet.')}</p>
      )}
    </Card>
  )
}

/* =========================== PROGRESS =========================== */
export function ProgressTab({ project, reports = [], role, onChange }) {
  const { t } = useT()
  const { user } = useAuth()
  const toast = useToast()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const canAuthor = canAuthorReports(role)
  const canPublish = canPublishReports(role)
  const canEdit = (report) => report.status !== 'PUBLISHED'
    && (canManageSite(role) || report.author?.id === user.id)

  const showNew = () => {
    setEditing(null)
    setOpen(true)
  }
  const showEdit = (report) => {
    setEditing(report)
    setOpen(true)
  }

  const act = async (report, action) => {
    try {
      await api.post(`/api/projects/${project.id}/progress-reports/${report.id}/${action}`)
      toast.success(action === 'submit' ? t('Report submitted') : t('Report published'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  const remove = async (report) => {
    if (!window.confirm(t('Delete this draft report?'))) return
    try {
      await api.delete(`/api/projects/${project.id}/progress-reports/${report.id}`)
      toast.success(t('Draft deleted'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap justify-end gap-2">
        <ExportButton path={`/api/projects/${project.id}/exports/site.csv`} label="Export site report" />
        {canAuthor && (
          <Button onClick={showNew}><Plus className="h-4 w-4" /> {t('New daily report')}</Button>
        )}
      </div>
      {reports.length === 0 ? (
        <EmptyState icon={ClipboardList} title={t('No progress reports yet')}
          text={canAuthor ? t('Record what was planned and completed on site today.')
            : t('Published site reports will appear here.')} />
      ) : reports.map((report) => (
        <Card key={report.id} className="p-4">
          <div className="flex flex-wrap items-start justify-between gap-2">
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="font-semibold text-ink-900">{formatDate(report.reportDate)}</h3>
                <StatusBadge group="progressStatus" value={report.status} />
              </div>
              <p className="mt-1 text-xs text-ink-500">
                {t('By')} {report.author?.name || '-'}
                {report.publishedAt ? ` · ${t('Published')} ${formatDateTime(report.publishedAt)}` : ''}
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              {canEdit(report) && (
                <Button size="sm" variant="secondary" onClick={() => showEdit(report)}>
                  <Pencil className="h-4 w-4" /> {t('Edit')}
                </Button>
              )}
              {canEdit(report) && report.status === 'DRAFT' && (
                <Button size="sm" onClick={() => act(report, 'submit')}>
                  <Send className="h-4 w-4" /> {t('Submit')}
                </Button>
              )}
              {canPublish && report.status === 'SUBMITTED' && (
                <Button size="sm" onClick={() => act(report, 'publish')}>
                  <CheckCircle2 className="h-4 w-4" /> {t('Publish')}
                </Button>
              )}
              {canEdit(report) && report.status === 'DRAFT' && (
                <Button size="sm" variant="ghost" onClick={() => remove(report)} aria-label={t('Delete draft')}>
                  <Trash2 className="h-4 w-4" />
                </Button>
              )}
            </div>
          </div>

          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <ReportBlock label={t('Planned work')} value={report.plannedWork} />
            <ReportBlock label={t('Completed work')} value={report.completedWork} />
            {report.blockers && <ReportBlock label={t('Blockers')} value={report.blockers} tone="warning" />}
            {report.nextDayPlan && <ReportBlock label={t('Next-day plan')} value={report.nextDayPlan} />}
          </div>
          <PhotoGrid photos={report.photos} />
        </Card>
      ))}

      <ReportModal open={open} report={editing} project={project} role={role}
        onClose={() => setOpen(false)} onSaved={onChange} />
    </div>
  )
}

function ReportBlock({ label, value, tone }) {
  return (
    <div className={`rounded-xl border p-3 ${tone === 'warning' ? 'border-amber-200 bg-amber-50' : 'border-ink-100 bg-ink-50/60'}`}>
      <p className="text-xs font-medium uppercase tracking-wide text-ink-500">{label}</p>
      <p className="mt-1 whitespace-pre-line text-sm text-ink-800">{value}</p>
    </div>
  )
}

function ReportModal({ open, report, project, role, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const empty = () => ({
    reportDate: todayISO(), plannedWork: '', completedWork: '', blockers: '', nextDayPlan: '', photos: [],
  })
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setForm(report ? {
      reportDate: report.reportDate || todayISO(),
      plannedWork: report.plannedWork || '',
      completedWork: report.completedWork || '',
      blockers: report.blockers || '',
      nextDayPlan: report.nextDayPlan || '',
      photos: (report.photos || []).map((photo) => ({
        photoUrl: photo.photoUrl || '', caption: photo.caption || '', takenDate: photo.takenDate || '',
      })),
    } : empty())
    setError('')
  }, [open, report])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      const payload = {
        ...form,
        photos: cleanPhotos(form.photos),
      }
      if (report) {
        await api.put(`/api/projects/${project.id}/progress-reports/${report.id}`, payload)
        toast.success(t('Report updated'))
      } else {
        await api.post(`/api/projects/${project.id}/progress-reports`, payload)
        toast.success(t('Draft report saved'))
      }
      onSaved()
      onClose()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={report ? t('Edit progress report') : t('New daily report')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Save draft')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        <Input label={t('Report date')} type="date" value={form.reportDate}
          onChange={(e) => update('reportDate', e.target.value)} required />
        <Textarea label={t('Planned work')} value={form.plannedWork}
          onChange={(e) => update('plannedWork', e.target.value)} required />
        <Textarea label={t('Completed work')} value={form.completedWork}
          onChange={(e) => update('completedWork', e.target.value)} required />
        <Textarea label={t('Blockers')} value={form.blockers}
          onChange={(e) => update('blockers', e.target.value)}
          hint={t('Optional: delays, missing material, labour shortage…')} />
        <Textarea label={t('Next-day plan')} value={form.nextDayPlan}
          onChange={(e) => update('nextDayPlan', e.target.value)} />
        <div>
          <p className="mb-1.5 text-sm font-medium text-ink-700">{t('Photos')}</p>
          <PhotoFields photos={form.photos} onChange={(photos) => update('photos', photos)} t={t} withDate />
          <p className="mt-1 text-xs text-ink-500">{t('Paste a link to a dated site photo. Up to 10 photos per report.')}</p>
        </div>
      </div>
    </Modal>
  )
}

/* =========================== TASKS =========================== */
export function TasksTab({ project, tasks = [], milestones = [], engineers = [], role, onChange }) {
  const { t } = useT()
  const { user } = useAuth()
  const toast = useToast()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const canManage = canManageSite(role)
  const people = assigneeOptions(project, engineers)
  const openCount = tasks.filter((task) => task.status !== 'DONE').length

  const changeStatus = async (task, status) => {
    let delayReason = null
    if (status === 'BLOCKED') {
      delayReason = window.prompt(t('Why is this task blocked?'))
      if (!delayReason?.trim()) return
    }
    try {
      await api.patch(`/api/projects/${project.id}/tasks/${task.id}/status`, {
        status, delayReason: delayReason?.trim() || null,
      })
      toast.success(t('Task updated'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  const remove = async (task) => {
    if (!window.confirm(t('Delete this task?'))) return
    try {
      await api.delete(`/api/projects/${project.id}/tasks/${task.id}`)
      toast.success(t('Task deleted'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm text-ink-500">{t('{n} open tasks', { n: openCount })}</p>
        {canManage && (
          <Button onClick={() => { setEditing(null); setOpen(true) }}>
            <Plus className="h-4 w-4" /> {t('Add task')}
          </Button>
        )}
      </div>
      {tasks.length === 0 ? (
        <EmptyState icon={ClipboardList} title={t('No tasks yet')}
          text={canManage ? t('Break the site work into assignable tasks.') : t('The contractor has not added tasks yet.')} />
      ) : (
        <div className="space-y-3">
          {tasks.map((task) => {
            const canChangeStatus = canManage || task.assignee?.id === user.id
            return (
              <Card key={task.id} className="p-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="font-semibold text-ink-900">{task.title}</h3>
                      <StatusBadge group="taskStatus" value={task.status} />
                      {task.overdue && <span className="inline-flex items-center gap-1 text-xs font-medium text-red-600"><AlertTriangle className="h-3.5 w-3.5" /> {t('Overdue')}</span>}
                    </div>
                    {task.description && <p className="mt-1 whitespace-pre-line text-sm text-ink-600">{task.description}</p>}
                    <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-ink-500">
                      {task.dueDate && <span className="inline-flex items-center gap-1"><Calendar className="h-3.5 w-3.5" /> {formatDate(task.dueDate)}</span>}
                      {task.assignee && <span>{t('Assigned to')}: <b className="font-medium text-ink-700">{task.assignee.name}</b></span>}
                      {task.milestoneTitle && <span className="inline-flex items-center gap-1"><Flag className="h-3.5 w-3.5" /> {task.milestoneTitle}</span>}
                    </div>
                    {task.delayReason && (
                      <p className="mt-2 rounded-lg bg-amber-50 px-2.5 py-1.5 text-xs text-amber-800">
                        {t('Delay reason')}: {task.delayReason}
                      </p>
                    )}
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    {canChangeStatus ? (
                      <select value={task.status} onChange={(e) => changeStatus(task, e.target.value)}
                        className="h-8 rounded-lg border border-ink-200 bg-white px-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand-400">
                        {options('taskStatus').map((status) => (
                          <option key={status} value={status}>{t(LABELS.taskStatus[status])}</option>
                        ))}
                      </select>
                    ) : null}
                    {canManage && (
                      <Button size="sm" variant="secondary" onClick={() => { setEditing(task); setOpen(true) }}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                    )}
                    {canManage && (
                      <Button size="sm" variant="ghost" onClick={() => remove(task)} aria-label={t('Delete task')}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
              </Card>
            )
          })}
        </div>
      )}
      <TaskModal open={open} task={editing} project={project} milestones={milestones} people={people}
        onClose={() => setOpen(false)} onSaved={onChange} />
    </div>
  )
}

function TaskModal({ open, task, project, milestones, people, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const empty = () => ({ title: '', description: '', milestoneId: '', assigneeId: '', dueDate: '', delayReason: '' })
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setForm(task ? {
      title: task.title || '',
      description: task.description || '',
      milestoneId: task.milestoneId || '',
      assigneeId: task.assignee?.id || '',
      dueDate: task.dueDate || '',
      delayReason: task.delayReason || '',
    } : empty())
    setError('')
  }, [open, task])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      const payload = {
        title: form.title,
        description: form.description || null,
        milestoneId: form.milestoneId ? Number(form.milestoneId) : null,
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
        dueDate: form.dueDate || null,
        delayReason: form.delayReason || null,
      }
      if (task) {
        await api.put(`/api/projects/${project.id}/tasks/${task.id}`, payload)
        toast.success(t('Task updated'))
      } else {
        await api.post(`/api/projects/${project.id}/tasks`, payload)
        toast.success(t('Task added'))
      }
      onSaved()
      onClose()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={task ? t('Edit task') : t('Add task')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Save')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        <Input label={t('Title')} value={form.title} onChange={(e) => update('title', e.target.value)} required />
        <Textarea label={t('Description')} value={form.description}
          onChange={(e) => update('description', e.target.value)} />
        <div className="grid gap-3 sm:grid-cols-2">
          <Select label={t('Milestone')} value={form.milestoneId} onChange={(e) => update('milestoneId', e.target.value)}>
            <option value="">{t('No milestone')}</option>
            {milestones.map((milestone) => (
              <option key={milestone.id} value={milestone.id}>{milestone.title}</option>
            ))}
          </Select>
          <Select label={t('Assignee')} value={form.assigneeId} onChange={(e) => update('assigneeId', e.target.value)}>
            <option value="">{t('Unassigned')}</option>
            {people.map((person) => (
              <option key={person.id} value={person.id}>{person.name} ({t(LABELS.role[person.roleName])})</option>
            ))}
          </Select>
        </div>
        <Input label={t('Due date')} type="date" value={form.dueDate}
          onChange={(e) => update('dueDate', e.target.value)} />
      </div>
    </Modal>
  )
}

/* =========================== MILESTONES =========================== */
export function MilestonesTab({ project, milestones = [], role, onChange }) {
  const { t } = useT()
  const toast = useToast()
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const canManage = canManageSite(role)

  const remove = async (milestone) => {
    if (!window.confirm(t('Delete this milestone? Linked tasks will keep their details but lose this milestone.'))) return
    try {
      await api.delete(`/api/projects/${project.id}/milestones/${milestone.id}`)
      toast.success(t('Milestone deleted'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  return (
    <div className="space-y-4">
      {canManage && (
        <div className="flex justify-end">
          <Button onClick={() => { setEditing(null); setOpen(true) }}>
            <Plus className="h-4 w-4" /> {t('Add milestone')}
          </Button>
        </div>
      )}
      {milestones.length === 0 ? (
        <EmptyState icon={Flag} title={t('No milestones yet')}
          text={canManage ? t('Add target dates so everyone can see the plan.') : t('The contractor has not added milestones yet.')} />
      ) : (
        <div className="space-y-3">
          {milestones.map((milestone) => {
            const pct = milestone.taskCount ? (milestone.completedTaskCount / milestone.taskCount) * 100 : 0
            return (
              <Card key={milestone.id} className="p-4">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="font-semibold text-ink-900">{milestone.title}</h3>
                      <StatusBadge group="milestoneStatus" value={milestone.status} />
                    </div>
                    {milestone.description && <p className="mt-1 text-sm text-ink-600">{milestone.description}</p>}
                    <p className="mt-1 flex items-center gap-1.5 text-xs text-ink-500">
                      <Calendar className="h-3.5 w-3.5" /> {t('Target')}: {formatDate(milestone.targetDate)}
                      {milestone.completedDate ? ` · ${t('Completed')} ${formatDate(milestone.completedDate)}` : ''}
                    </p>
                  </div>
                  {canManage && (
                    <div className="flex items-center gap-2">
                      <Button size="sm" variant="secondary" onClick={() => { setEditing(milestone); setOpen(true) }}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                      <Button size="sm" variant="ghost" onClick={() => remove(milestone)} aria-label={t('Delete milestone')}>
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  )}
                </div>
                <div className="mt-3">
                  <div className="mb-1 flex justify-between text-xs text-ink-500">
                    <span>{t('Tasks completed')}</span>
                    <span>{milestone.completedTaskCount}/{milestone.taskCount}</span>
                  </div>
                  <Progress value={pct} />
                </div>
              </Card>
            )
          })}
        </div>
      )}
      <MilestoneModal open={open} milestone={editing} project={project}
        onClose={() => setOpen(false)} onSaved={onChange} />
    </div>
  )
}

function MilestoneModal({ open, milestone, project, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const empty = () => ({ title: '', description: '', targetDate: todayISO(), status: 'PLANNED', completedDate: '' })
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setForm(milestone ? {
      title: milestone.title || '',
      description: milestone.description || '',
      targetDate: milestone.targetDate || todayISO(),
      status: milestone.status || 'PLANNED',
      completedDate: milestone.completedDate || '',
    } : empty())
    setError('')
  }, [open, milestone])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      const payload = {
        title: form.title,
        description: form.description || null,
        targetDate: form.targetDate,
        status: form.status,
        completedDate: form.status === 'COMPLETED' ? (form.completedDate || null) : null,
      }
      if (milestone) {
        await api.put(`/api/projects/${project.id}/milestones/${milestone.id}`, payload)
        toast.success(t('Milestone updated'))
      } else {
        await api.post(`/api/projects/${project.id}/milestones`, payload)
        toast.success(t('Milestone added'))
      }
      onSaved()
      onClose()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={milestone ? t('Edit milestone') : t('Add milestone')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Save')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        <Input label={t('Title')} value={form.title} onChange={(e) => update('title', e.target.value)} required />
        <Textarea label={t('Description')} value={form.description}
          onChange={(e) => update('description', e.target.value)} />
        <div className="grid gap-3 sm:grid-cols-2">
          <Input label={t('Target date')} type="date" value={form.targetDate}
            onChange={(e) => update('targetDate', e.target.value)} required />
          <Select label={t('Status')} value={form.status} onChange={(e) => update('status', e.target.value)}>
            {options('milestoneStatus').map((status) => (
              <option key={status} value={status}>{t(LABELS.milestoneStatus[status])}</option>
            ))}
          </Select>
        </div>
        {form.status === 'COMPLETED' && (
          <Input label={t('Completed date')} type="date" value={form.completedDate}
            onChange={(e) => update('completedDate', e.target.value)}
            hint={t('Leave blank to use today.')} />
        )}
      </div>
    </Modal>
  )
}

/* =========================== ISSUES =========================== */
export function IssuesTab({ project, issues = [], engineers = [], role, onChange }) {
  const { t } = useT()
  const { user } = useAuth()
  const [open, setOpen] = useState(false)
  const [detailId, setDetailId] = useState(null)
  const canCreate = role === 'HOUSE_OWNER' || role === 'CONTRACTOR' || role === 'ENGINEER' || role === 'ADMIN'
  const people = assigneeOptions(project, engineers)
  const detail = issues.find((issue) => issue.id === detailId) || null
  const openCount = issues.filter((issue) => issue.status !== 'RESOLVED').length

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm text-ink-500">{t('{n} open issues', { n: openCount })}</p>
        {canCreate && (
          <Button onClick={() => setOpen(true)}><Plus className="h-4 w-4" /> {t('Raise issue')}</Button>
        )}
      </div>
      {issues.length === 0 ? (
        <EmptyState icon={AlertTriangle} title={t('No issues reported')}
          text={canCreate ? t('Record quality, safety or delay issues with photo evidence.') : t('No site issues have been reported.')} />
      ) : (
        <div className="space-y-3">
          {issues.map((issue) => {
            return (
              <Card key={issue.id} className="p-4">
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h3 className="font-semibold text-ink-900">{issue.title}</h3>
                      <StatusBadge group="issueStatus" value={issue.status} />
                      <span className="text-xs font-medium text-ink-500">{t(LABELS.issueCategory[issue.category])}</span>
                    </div>
                    {issue.description && <p className="mt-1 line-clamp-2 text-sm text-ink-600">{issue.description}</p>}
                    <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-ink-500">
                      <span>{t('Raised by')}: <b className="font-medium text-ink-700">{issue.raisedBy?.name}</b></span>
                      {issue.assignee && <span>{t('Assigned to')}: <b className="font-medium text-ink-700">{issue.assignee.name}</b></span>}
                      {issue.dueDate && <span className="inline-flex items-center gap-1"><Clock className="h-3.5 w-3.5" /> {formatDate(issue.dueDate)}</span>}
                      {issue.comments?.length > 0 && <span className="inline-flex items-center gap-1"><MessageSquare className="h-3.5 w-3.5" /> {issue.comments.length}</span>}
                    </div>
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    <Button size="sm" variant="secondary" onClick={() => setDetailId(issue.id)}>{t('View')}</Button>
                  </div>
                </div>
                <PhotoGrid photos={issue.photos} />
              </Card>
            )
          })}
        </div>
      )}
      <IssueModal open={open} project={project} people={people}
        onClose={() => setOpen(false)} onSaved={onChange} />
      <IssueDetailModal open={Boolean(detail)} issue={detail} project={project} role={role}
        onClose={() => setDetailId(null)} onChange={onChange} />
    </div>
  )
}

function IssueModal({ open, project, people, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const empty = () => ({ title: '', description: '', category: 'QUALITY', assigneeId: '', dueDate: '', photos: [] })
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (open) {
      setForm(empty())
      setError('')
    }
  }, [open])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      await api.post(`/api/projects/${project.id}/issues`, {
        title: form.title,
        description: form.description || null,
        category: form.category,
        assigneeId: form.assigneeId ? Number(form.assigneeId) : null,
        dueDate: form.dueDate || null,
        photos: cleanPhotos(form.photos),
      })
      toast.success(t('Issue raised'))
      onSaved()
      onClose()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={t('Raise issue')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Raise issue')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        <Input label={t('Title')} value={form.title} onChange={(e) => update('title', e.target.value)} required />
        <Textarea label={t('Description')} value={form.description}
          onChange={(e) => update('description', e.target.value)} />
        <div className="grid gap-3 sm:grid-cols-2">
          <Select label={t('Category')} value={form.category} onChange={(e) => update('category', e.target.value)}>
            {options('issueCategory').map((category) => (
              <option key={category} value={category}>{t(LABELS.issueCategory[category])}</option>
            ))}
          </Select>
          <Select label={t('Assignee')} value={form.assigneeId} onChange={(e) => update('assigneeId', e.target.value)}>
            <option value="">{t('Unassigned')}</option>
            {people.map((person) => (
              <option key={person.id} value={person.id}>{person.name} ({t(LABELS.role[person.roleName])})</option>
            ))}
          </Select>
        </div>
        <Input label={t('Due date')} type="date" value={form.dueDate}
          onChange={(e) => update('dueDate', e.target.value)} />
        <div>
          <p className="mb-1.5 text-sm font-medium text-ink-700">{t('Photos')}</p>
          <PhotoFields photos={form.photos} onChange={(photos) => update('photos', photos)} t={t} />
          <p className="mt-1 text-xs text-ink-500">{t('Paste links to photos of the issue. Up to 10 photos.')}</p>
        </div>
      </div>
    </Modal>
  )
}

function IssueDetailModal({ open, issue, project, role, onClose, onChange }) {
  const { t } = useT()
  const { user } = useAuth()
  const toast = useToast()
  const [comment, setComment] = useState('')
  const [status, setStatus] = useState('OPEN')
  const [resolutionNote, setResolutionNote] = useState('')
  const [resolutionPhotoUrl, setResolutionPhotoUrl] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!open || !issue) return
    setComment('')
    setStatus(issue.status)
    setResolutionNote(issue.resolutionNote || '')
    setResolutionPhotoUrl(issue.resolutionPhotoUrl || '')
  }, [open, issue?.id])

  if (!issue) return null
  const canUpdateStatus = canManageIssues(role) || issue.assignee?.id === user.id

  const addComment = async () => {
    if (!comment.trim()) return
    setBusy(true)
    try {
      await api.post(`/api/projects/${project.id}/issues/${issue.id}/comments`, { message: comment.trim() })
      setComment('')
      toast.success(t('Comment added'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) } finally { setBusy(false) }
  }

  const updateStatus = async () => {
    if (status === 'RESOLVED' && !resolutionNote.trim()) {
      toast.error(t('Add a resolution note before resolving the issue'))
      return
    }
    setBusy(true)
    try {
      await api.patch(`/api/projects/${project.id}/issues/${issue.id}/status`, {
        status,
        resolutionNote: status === 'RESOLVED' ? resolutionNote.trim() : null,
        resolutionPhotoUrl: status === 'RESOLVED' ? (resolutionPhotoUrl.trim() || null) : null,
      })
      toast.success(t('Issue updated'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) } finally { setBusy(false) }
  }

  return (
    <Modal open={open} onClose={onClose} title={issue.title}
      footer={<Button variant="secondary" onClick={onClose}>{t('Close')}</Button>}>
      <div className="space-y-4">
        <div className="flex flex-wrap items-center gap-2">
          <StatusBadge group="issueStatus" value={issue.status} />
          <span className="text-xs font-medium text-ink-500">{t(LABELS.issueCategory[issue.category])}</span>
        </div>
        {issue.description && <p className="whitespace-pre-line text-sm text-ink-700">{issue.description}</p>}
        <div className="grid gap-1 text-sm text-ink-600 sm:grid-cols-2">
          <p>{t('Raised by')}: <b className="font-medium text-ink-900">{issue.raisedBy?.name}</b></p>
          <p>{t('Assignee')}: <b className="font-medium text-ink-900">{issue.assignee?.name || t('Unassigned')}</b></p>
          <p>{t('Due date')}: <b className="font-medium text-ink-900">{formatDate(issue.dueDate)}</b></p>
          <p>{t('Raised')}: <b className="font-medium text-ink-900">{formatDateTime(issue.createdAt)}</b></p>
        </div>
        <PhotoGrid photos={issue.photos} />

        {issue.resolutionNote && (
          <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-3">
            <p className="text-xs font-semibold uppercase tracking-wide text-emerald-700">{t('Resolution')}</p>
            <p className="mt-1 whitespace-pre-line text-sm text-emerald-900">{issue.resolutionNote}</p>
            {issue.resolutionPhotoUrl && (
              <a href={issue.resolutionPhotoUrl} target="_blank" rel="noreferrer"
                className="mt-2 inline-block text-sm font-medium text-emerald-700 underline">
                {t('View resolution photo')}
              </a>
            )}
          </div>
        )}

        {canUpdateStatus && (
          <div className="rounded-xl border border-ink-200 p-3">
            <p className="mb-2 text-sm font-semibold text-ink-900">{t('Update status')}</p>
            <div className="space-y-2">
              <Select label={t('Status')} value={status} onChange={(e) => setStatus(e.target.value)}>
                {options('issueStatus').map((value) => (
                  <option key={value} value={value}>{t(LABELS.issueStatus[value])}</option>
                ))}
              </Select>
              {status === 'RESOLVED' && (
                <>
                  <Textarea label={t('Resolution note')} value={resolutionNote}
                    onChange={(e) => setResolutionNote(e.target.value)} required />
                  <Input label={t('Resolution photo link')} value={resolutionPhotoUrl}
                    onChange={(e) => setResolutionPhotoUrl(e.target.value)} placeholder="https://…" />
                </>
              )}
              <Button size="sm" onClick={updateStatus} loading={busy}>{t('Update status')}</Button>
            </div>
          </div>
        )}

        <div>
          <p className="mb-2 flex items-center gap-1.5 text-sm font-semibold text-ink-900">
            <MessageSquare className="h-4 w-4" /> {t('Comments')} ({issue.comments?.length || 0})
          </p>
          <div className="space-y-2">
            {(issue.comments || []).map((item) => (
              <div key={item.id} className="rounded-xl bg-ink-50 p-3">
                <p className="text-xs font-medium text-ink-700">{item.author?.name} · {formatDateTime(item.createdAt)}</p>
                <p className="mt-1 whitespace-pre-line text-sm text-ink-700">{item.message}</p>
              </div>
            ))}
            {(!issue.comments || issue.comments.length === 0) && (
              <p className="text-sm text-ink-500">{t('No comments yet.')}</p>
            )}
          </div>
          <div className="mt-3 space-y-2">
            <Textarea value={comment} onChange={(e) => setComment(e.target.value)}
              placeholder={t('Add a comment…')} aria-label={t('Comment')} />
            <Button size="sm" variant="secondary" onClick={addComment} loading={busy} disabled={!comment.trim()}>
              <Send className="h-4 w-4" /> {t('Add comment')}
            </Button>
          </div>
        </div>
      </div>
    </Modal>
  )
}
