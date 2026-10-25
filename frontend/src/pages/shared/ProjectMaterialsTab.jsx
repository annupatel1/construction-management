import { useEffect, useState } from 'react'
import {
  AlertTriangle, ArrowDownToLine, ArrowUpFromLine, Boxes, Check, ClipboardList,
  Factory, Package, Pencil, Phone, Plus, ShoppingCart, X,
} from 'lucide-react'
import api, { errorMessage } from '../../lib/api'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { useT } from '../../i18n/LanguageContext'
import { LABELS, options } from '../../lib/labels'
import { formatDate, formatINR, todayISO } from '../../lib/format'
import StatusBadge from '../../components/Status'
import { Alert, Button, Card, Input, Modal, Progress, Select, Textarea } from '../../components/ui'
const canManageMaterials = (role) => role === 'CONTRACTOR' || role === 'ADMIN'
const canRequestMaterials = (role) =>
  role === 'HOUSE_OWNER' || role === 'CONTRACTOR' || role === 'ENGINEER' || role === 'ADMIN'
const canRecordStock = (role) =>
  role === 'CONTRACTOR' || role === 'ENGINEER' || role === 'ADMIN'
const canViewProcurement = (role) =>
  role === 'HOUSE_OWNER' || role === 'CONTRACTOR' || role === 'ADMIN'

const qty = (value) =>
  new Intl.NumberFormat('en-IN', { maximumFractionDigits: 3 }).format(Number(value) || 0)

function Panel({ title, icon: Icon, action, children }) {
  return (
    <Card className="p-4">
      <div className="mb-1 flex flex-wrap items-center justify-between gap-2">
        <h3 className="flex items-center gap-2 font-semibold text-ink-900">
          {Icon && <Icon className="h-4 w-4 text-ink-500" />} {title}
        </h3>
        {action}
      </div>
      {children}
    </Card>
  )
}

function EmptyLine({ children }) {
  return <p className="py-6 text-center text-sm text-ink-500">{children}</p>
}

export function MaterialsTab({
  project, stockLedger, suppliers = [], requests = [], orders = [], role, onChange,
}) {
  const { t } = useT()
  const { user } = useAuth()
  const toast = useToast()
  const [dialog, setDialog] = useState(null)

  const materials = stockLedger?.materials || []
  const activeMaterialCount = materials.filter((material) => material.active !== false).length
  const movements = stockLedger?.movements || []
  const lowStockCount = stockLedger?.lowStockCount || 0
  const manage = canManageMaterials(role)
  const request = canRequestMaterials(role)
  const recordStock = canRecordStock(role)
  const viewProcurement = canViewProcurement(role)
  const base = `/api/projects/${project.id}`
  const close = () => setDialog(null)
  const saved = () => {
    setDialog(null)
    onChange()
  }

  const reviewRequest = async (materialRequest, approve) => {
    let reviewNote = null
    if (!approve) {
      reviewNote = window.prompt(t('Reason for rejection (optional)'))
      if (reviewNote === null) return
    }
    try {
      await api.post(`${base}/material-requests/${materialRequest.id}/${approve ? 'approve' : 'reject'}`,
        { reviewNote: reviewNote?.trim() || null })
      toast.success(approve ? t('Material request approved') : t('Material request rejected'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  const cancelRequest = async (materialRequest) => {
    if (!window.confirm(t('Cancel this material request?'))) return
    try {
      await api.post(`${base}/material-requests/${materialRequest.id}/cancel`)
      toast.success(t('Material request cancelled'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  const cancelOrder = async (order) => {
    if (!window.confirm(t('Cancel this purchase order?'))) return
    try {
      await api.post(`${base}/purchase-orders/${order.id}/cancel`)
      toast.success(t('Purchase order cancelled'))
      onChange()
    } catch (err) { toast.error(errorMessage(err)) }
  }

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <p className="text-sm text-ink-500">
          {t('Stock is calculated from purchase receipts and site movements.')}
        </p>
        <div className="flex flex-wrap gap-2">
          {manage && (
            <Button variant="secondary" onClick={() => setDialog({ type: 'material', item: null })}>
              <Plus className="h-4 w-4" /> {t('Add material')}
            </Button>
          )}
          {manage && (
            <Button variant="secondary" onClick={() => setDialog({ type: 'supplier', item: null })}>
              <Factory className="h-4 w-4" /> {t('Add supplier')}
            </Button>
          )}
          {request && (
            <Button disabled={activeMaterialCount === 0}
              onClick={() => setDialog({ type: 'request', item: null })}>
              <ClipboardList className="h-4 w-4" /> {t('Request material')}
            </Button>
          )}
        </div>
      </div>

      {lowStockCount > 0 && (
        <Alert tone="warning">
          <span className="inline-flex items-center gap-2">
            <AlertTriangle className="h-4 w-4" />
            {t('{n} materials are below their minimum stock level.', { n: lowStockCount })}
          </span>
        </Alert>
      )}

      <Panel title={`${t('Materials')} (${materials.length})`} icon={Boxes}
        action={materials.length > 0 && role === 'HOUSE_OWNER' && (
          <span className="text-xs text-ink-500">{t('Read-only stock view')}</span>
        )}>
        {materials.length === 0 ? (
          <EmptyLine>{manage
            ? t('Add the materials used on this site to start tracking stock.')
            : t('The contractor has not added any materials yet.')}</EmptyLine>
        ) : (
          <div className="divide-y divide-ink-100">
            {materials.map((material) => (
              <div key={material.id} className="py-3 first:pt-1 last:pb-0">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h4 className="font-medium text-ink-900">{material.name}</h4>
                      <StatusBadge group="category" value={material.category} />
                      {material.active === false && (
                        <span className="rounded-full bg-ink-100 px-2 py-0.5 text-xs font-medium text-ink-600">
                          {t('Inactive')}
                        </span>
                      )}
                      {material.lowStock && material.active !== false && (
                        <span className="inline-flex items-center gap-1 text-xs font-medium text-amber-700">
                          <AlertTriangle className="h-3.5 w-3.5" /> {t('Low stock')}
                        </span>
                      )}
                    </div>
                    <p className="mt-1 text-xs text-ink-500">
                      {t('Minimum')}: {qty(material.minimumStock)} {material.unit}
                    </p>
                  </div>
                  <div className="flex flex-wrap items-center gap-3">
                    <div className="text-right">
                      <p className={`text-lg font-semibold ${material.lowStock && material.active !== false ? 'text-amber-700' : 'text-ink-900'}`}>
                        {qty(material.balance)} <span className="text-sm font-normal text-ink-500">{material.unit}</span>
                      </p>
                      <p className="text-xs text-ink-500">{t('In stock')}</p>
                    </div>
                    {recordStock && material.active !== false && (
                      <Button size="sm" variant="secondary"
                        onClick={() => setDialog({ type: 'movement', item: material })}>
                        <ArrowUpFromLine className="h-4 w-4" /> {t('Record movement')}
                      </Button>
                    )}
                    {manage && (
                      <Button size="sm" variant="ghost"
                        onClick={() => setDialog({ type: 'material', item: material })}
                        aria-label={t('Edit material')}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Panel>

      <Panel title={`${t('Material requests')} (${requests.length})`} icon={ClipboardList}>
        {requests.length === 0 ? (
          <EmptyLine>{t('No material requests yet.')}</EmptyLine>
        ) : (
          <div className="divide-y divide-ink-100">
            {requests.map((materialRequest) => (
              <div key={materialRequest.id} className="py-3 first:pt-1 last:pb-0">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <h4 className="font-medium text-ink-900">{materialRequest.materialName}</h4>
                      <StatusBadge group="materialRequestStatus" value={materialRequest.status} />
                    </div>
                    <p className="mt-1 text-sm text-ink-600">
                      {qty(materialRequest.quantity)} {materialRequest.materialUnit}
                      {materialRequest.requestedBy?.name ? ` · ${t('By')} ${materialRequest.requestedBy.name}` : ''}
                    </p>
                    <p className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-ink-500">
                      {materialRequest.neededBy && <span>{t('Needed by')} {formatDate(materialRequest.neededBy)}</span>}
                      <span>{t('Raised')} {formatDate(materialRequest.createdAt?.slice(0, 10))}</span>
                    </p>
                    {materialRequest.notes && <p className="mt-2 text-sm text-ink-700">{materialRequest.notes}</p>}
                    {materialRequest.reviewNote && (
                      <p className="mt-2 rounded-lg bg-ink-50 px-2.5 py-1.5 text-xs text-ink-600">
                        {t('Review')}: {materialRequest.reviewNote}
                      </p>
                    )}
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    {manage && materialRequest.status === 'REQUESTED' && (
                      <>
                        <Button size="sm" onClick={() => reviewRequest(materialRequest, true)}>
                          <Check className="h-4 w-4" /> {t('Approve')}
                        </Button>
                        <Button size="sm" variant="secondary" onClick={() => reviewRequest(materialRequest, false)}>
                          <X className="h-4 w-4" /> {t('Reject')}
                        </Button>
                      </>
                    )}
                    {manage && materialRequest.status === 'APPROVED'
                      && materials.some((item) => item.id === materialRequest.materialId
                        && item.active !== false) && (
                        <Button size="sm" onClick={() => setDialog({ type: 'order', item: materialRequest })}>
                          <ShoppingCart className="h-4 w-4" /> {t('Create purchase order')}
                        </Button>
                      )}
                    {['REQUESTED', 'APPROVED'].includes(materialRequest.status)
                      && (manage || materialRequest.requestedBy?.id === user.id) && (
                        <Button size="sm" variant="ghost" onClick={() => cancelRequest(materialRequest)}>
                          {t('Cancel')}
                        </Button>
                      )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </Panel>

      {viewProcurement && (
        <Panel title={`${t('Purchase orders')} (${orders.length})`} icon={ShoppingCart}
          action={manage && (
            <Button size="sm" variant="secondary"
              onClick={() => setDialog({ type: 'order', item: null })}>
              <Plus className="h-4 w-4" /> {t('New purchase order')}
            </Button>
          )}>
          {orders.length === 0 ? (
            <EmptyLine>{t('No purchase orders yet.')}</EmptyLine>
          ) : (
            <div className="divide-y divide-ink-100">
              {orders.map((order) => {
                const pct = Number(order.quantity) ? (Number(order.receivedQuantity) / Number(order.quantity)) * 100 : 0
                const canReceive = ['ORDERED', 'PARTIALLY_RECEIVED'].includes(order.status)
                return (
                  <div key={order.id} className="py-3 first:pt-1 last:pb-0">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div className="min-w-0">
                        <div className="flex flex-wrap items-center gap-2">
                          <h4 className="font-medium text-ink-900">{order.materialName}</h4>
                          <StatusBadge group="purchaseOrderStatus" value={order.status} />
                        </div>
                        <p className="mt-1 text-sm text-ink-600">
                          {qty(order.quantity)} {order.materialUnit} · {order.supplierName}
                        </p>
                        <p className="mt-1 text-xs text-ink-500">
                          {t('Ordered')} {formatDate(order.orderedDate)}
                          {order.expectedDate ? ` · ${t('Expected')} ${formatDate(order.expectedDate)}` : ''}
                        </p>
                        <p className="mt-1 text-sm font-medium text-ink-800">{formatINR(order.totalAmount)}</p>
                        <div className="mt-2 w-56 max-w-full">
                          <div className="mb-1 flex justify-between text-xs text-ink-500">
                            <span>{t('Received')}</span>
                            <span>{qty(order.receivedQuantity)} / {qty(order.quantity)} {order.materialUnit}</span>
                          </div>
                          <Progress value={pct} />
                        </div>
                        {order.receipts?.length > 0 && (
                          <p className="mt-1 text-xs text-ink-500">
                            {t('{n} receipts recorded', { n: order.receipts.length })}
                          </p>
                        )}
                      </div>
                      {manage && (
                        <div className="flex flex-wrap items-center gap-2">
                          {canReceive && (
                            <Button size="sm" onClick={() => setDialog({ type: 'receive', item: order })}>
                              <ArrowDownToLine className="h-4 w-4" /> {t('Receive')}
                            </Button>
                          )}
                          {order.status !== 'CANCELLED' && Number(order.receivedQuantity) === 0 && (
                            <Button size="sm" variant="ghost" onClick={() => cancelOrder(order)}>
                              {t('Cancel')}
                            </Button>
                          )}
                        </div>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </Panel>
      )}

      {viewProcurement && (
        <Panel title={`${t('Suppliers')} (${suppliers.length})`} icon={Factory}
          action={manage && (
            <Button size="sm" variant="secondary"
              onClick={() => setDialog({ type: 'supplier', item: null })}>
              <Plus className="h-4 w-4" /> {t('Add supplier')}
            </Button>
          )}>
          {suppliers.length === 0 ? (
            <EmptyLine>{t('No suppliers yet.')}</EmptyLine>
          ) : (
            <div className="divide-y divide-ink-100">
              {suppliers.map((supplier) => (
                <div key={supplier.id} className="flex flex-wrap items-start justify-between gap-2 py-3 first:pt-1 last:pb-0">
                  <div className="min-w-0">
                    <p className="font-medium text-ink-900">
                      {supplier.name}
                      {supplier.active === false && (
                        <span className="ml-2 rounded-full bg-ink-100 px-2 py-0.5 text-xs font-medium text-ink-600">
                          {t('Inactive')}
                        </span>
                      )}
                    </p>
                    {supplier.phone && (
                      <a href={`tel:${supplier.phone}`} className="mt-0.5 inline-flex items-center gap-1 text-sm text-brand-700 hover:underline">
                        <Phone className="h-3.5 w-3.5" /> {supplier.phone}
                      </a>
                    )}
                    {supplier.address && <p className="mt-1 text-xs text-ink-500">{supplier.address}</p>}
                    {supplier.notes && <p className="mt-1 text-sm text-ink-600">{supplier.notes}</p>}
                  </div>
                  {manage && (
                    <Button size="sm" variant="ghost"
                      onClick={() => setDialog({ type: 'supplier', item: supplier })}
                      aria-label={t('Edit supplier')}>
                      <Pencil className="h-4 w-4" />
                    </Button>
                  )}
                </div>
              ))}
            </div>
          )}
        </Panel>
      )}

      <Panel title={`${t('Recent stock movements')} (${movements.length})`} icon={Package}>
        {movements.length === 0 ? (
          <EmptyLine>{t('No stock movements yet.')}</EmptyLine>
        ) : (
          <div className="divide-y divide-ink-100">
            {movements.map((movement) => {
              const outgoing = movement.movementType === 'ISSUE' || movement.movementType === 'WASTAGE'
              return (
                <div key={movement.id} className="flex flex-wrap items-start justify-between gap-2 py-2.5 first:pt-1 last:pb-0">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="font-medium text-ink-900">{movement.materialName}</p>
                      <StatusBadge group="stockMovementType" value={movement.movementType} />
                    </div>
                    <p className="mt-1 text-xs text-ink-500">
                      {formatDate(movement.movementDate)}
                      {movement.createdBy?.name ? ` · ${movement.createdBy.name}` : ''}
                    </p>
                    {movement.notes && <p className="mt-1 text-sm text-ink-600">{movement.notes}</p>}
                  </div>
                  <p className={`font-semibold ${outgoing ? 'text-red-600' : 'text-emerald-700'}`}>
                    {outgoing ? '−' : '+'}{qty(movement.quantity)} {movement.materialUnit}
                  </p>
                </div>
              )
            })}
          </div>
        )}
      </Panel>

      <MaterialModal open={dialog?.type === 'material'} material={dialog?.item} project={project}
        onClose={close} onSaved={saved} />
      <SupplierModal open={dialog?.type === 'supplier'} supplier={dialog?.item} project={project}
        onClose={close} onSaved={saved} />
      <MaterialRequestModal open={dialog?.type === 'request'} materials={materials} project={project}
        onClose={close} onSaved={saved} />
      <PurchaseOrderModal open={dialog?.type === 'order'} materialRequest={dialog?.item}
        materials={materials} suppliers={suppliers} requests={requests} project={project}
        onClose={close} onSaved={saved} />
      <GoodsReceiptModal open={dialog?.type === 'receive'} order={dialog?.item} project={project}
        onClose={close} onSaved={saved} />
      <StockMovementModal open={dialog?.type === 'movement'} material={dialog?.item}
        materials={materials} project={project} onClose={close} onSaved={saved} />
    </div>
  )
}

function MaterialModal({ open, material, project, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const empty = { name: '', category: 'CEMENT', unit: 'bags', minimumStock: '0', active: 'true' }
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setForm(material ? {
      name: material.name || '',
      category: material.category || 'CEMENT',
      unit: material.unit || '',
      minimumStock: material.minimumStock ?? '0',
      active: material.active === false ? 'false' : 'true',
    } : empty)
    setError('')
  }, [open, material])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      const payload = {
        name: form.name,
        category: form.category,
        unit: form.unit,
        minimumStock: Number(form.minimumStock) || 0,
        active: form.active === 'true',
      }
      if (material) {
        await api.put(`/api/projects/${project.id}/materials/${material.id}`, payload)
        toast.success(t('Material updated'))
      } else {
        await api.post(`/api/projects/${project.id}/materials`, payload)
        toast.success(t('Material added'))
      }
      onSaved()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={material ? t('Edit material') : t('Add material')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Save')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        <Input label={t('Material name')} value={form.name}
          onChange={(e) => update('name', e.target.value)} required />
        <div className="grid gap-3 sm:grid-cols-2">
          <Select label={t('Category')} value={form.category}
            onChange={(e) => update('category', e.target.value)}>
            {options('category').map((value) => (
              <option key={value} value={value}>{t(LABELS.category[value])}</option>
            ))}
          </Select>
          <Input label={t('Unit')} value={form.unit} placeholder={t('bags, kg, cubic ft')}
            onChange={(e) => update('unit', e.target.value)} required />
        </div>
        <Input label={t('Minimum stock')} type="number" min="0" step="0.001" value={form.minimumStock}
          onChange={(e) => update('minimumStock', e.target.value)}
          hint={t('Show a low-stock warning below this quantity.')} />
        {material && (
          <Select label={t('Status')} value={form.active}
            onChange={(e) => update('active', e.target.value)}>
            <option value="true">{t('Active')}</option>
            <option value="false">{t('Inactive')}</option>
          </Select>
        )}
      </div>
    </Modal>
  )
}

function SupplierModal({ open, supplier, project, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const empty = { name: '', phone: '', address: '', notes: '', active: 'true' }
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setForm(supplier ? {
      name: supplier.name || '',
      phone: supplier.phone || '',
      address: supplier.address || '',
      notes: supplier.notes || '',
      active: supplier.active === false ? 'false' : 'true',
    } : empty)
    setError('')
  }, [open, supplier])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      const payload = {
        name: form.name,
        phone: form.phone || null,
        address: form.address || null,
        notes: form.notes || null,
        active: form.active === 'true',
      }
      if (supplier) {
        await api.put(`/api/projects/${project.id}/suppliers/${supplier.id}`, payload)
        toast.success(t('Supplier updated'))
      } else {
        await api.post(`/api/projects/${project.id}/suppliers`, payload)
        toast.success(t('Supplier added'))
      }
      onSaved()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={supplier ? t('Edit supplier') : t('Add supplier')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Save')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        <Input label={t('Supplier name')} value={form.name}
          onChange={(e) => update('name', e.target.value)} required />
        <Input label={t('Phone')} value={form.phone}
          onChange={(e) => update('phone', e.target.value)} />
        <Input label={t('Address')} value={form.address}
          onChange={(e) => update('address', e.target.value)} />
        <Textarea label={t('Notes')} value={form.notes}
          onChange={(e) => update('notes', e.target.value)} />
        {supplier && (
          <Select label={t('Status')} value={form.active}
            onChange={(e) => update('active', e.target.value)}>
            <option value="true">{t('Active')}</option>
            <option value="false">{t('Inactive')}</option>
          </Select>
        )}
      </div>
    </Modal>
  )
}

function MaterialRequestModal({ open, materials, project, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const activeMaterials = materials.filter((material) => material.active !== false)
  const empty = {
    materialId: activeMaterials[0]?.id || '', quantity: '', neededBy: '', notes: '',
  }
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setForm({ ...empty, materialId: activeMaterials[0]?.id || '' })
    setError('')
  }, [open, materials])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))
  const selected = activeMaterials.find((material) => String(material.id) === String(form.materialId))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      await api.post(`/api/projects/${project.id}/material-requests`, {
        materialId: Number(form.materialId),
        quantity: Number(form.quantity),
        neededBy: form.neededBy || null,
        notes: form.notes || null,
      })
      toast.success(t('Material request raised'))
      onSaved()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={t('Request material')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving} disabled={!activeMaterials.length}>{t('Send request')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        {activeMaterials.length === 0 ? (
          <Alert tone="info">{t('Add a material before raising a request.')}</Alert>
        ) : (
          <>
            <Select label={t('Material')} value={form.materialId}
              onChange={(e) => update('materialId', e.target.value)}>
              {activeMaterials.map((material) => (
                <option key={material.id} value={material.id}>
                  {material.name} ({qty(material.balance)} {material.unit} {t('in stock')})
                </option>
              ))}
            </Select>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input label={t('Quantity')} type="number" min="0.001" step="0.001" value={form.quantity}
                onChange={(e) => update('quantity', e.target.value)}
                hint={selected ? `${qty(selected.balance)} ${selected.unit} ${t('in stock')}` : ''} required />
              <Input label={t('Needed by')} type="date" min={todayISO()} value={form.neededBy}
                onChange={(e) => update('neededBy', e.target.value)} />
            </div>
            <Textarea label={t('Notes')} value={form.notes}
              onChange={(e) => update('notes', e.target.value)}
              hint={t('Optional: grade, brand or delivery instructions.')} />
          </>
        )}
      </div>
    </Modal>
  )
}

function PurchaseOrderModal({
  open, materialRequest, materials, suppliers, requests, project, onClose, onSaved,
}) {
  const { t } = useT()
  const toast = useToast()
  const activeMaterials = materials.filter((material) => material.active !== false)
  const approvedRequests = requests.filter((item) => item.status === 'APPROVED'
    && activeMaterials.some((material) => material.id === item.materialId))
  const activeSuppliers = suppliers.filter((supplier) => supplier.active !== false)
  const empty = {
    requestId: '', materialId: '', supplierId: activeSuppliers[0]?.id || '', quantity: '',
    unitPrice: '', orderedDate: todayISO(), expectedDate: '', notes: '',
  }
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    if (materialRequest) {
      setForm({
        ...empty,
        requestId: String(materialRequest.id),
        materialId: String(materialRequest.materialId),
        quantity: String(materialRequest.quantity),
        supplierId: activeSuppliers[0]?.id || '',
      })
    } else {
      setForm(empty)
    }
    setError('')
  }, [open, materialRequest, suppliers])

  const update = (key, value) => {
    if (key === 'requestId') {
      const selected = approvedRequests.find((item) => String(item.id) === String(value))
      setForm((current) => ({
        ...current,
        requestId: value,
        materialId: selected ? String(selected.materialId) : current.materialId,
        quantity: selected ? String(selected.quantity) : current.quantity,
      }))
      return
    }
    setForm((current) => ({ ...current, [key]: value }))
  }

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      await api.post(`/api/projects/${project.id}/purchase-orders`, {
        materialRequestId: form.requestId ? Number(form.requestId) : null,
        materialId: Number(form.materialId),
        supplierId: Number(form.supplierId),
        quantity: Number(form.quantity),
        unitPrice: Number(form.unitPrice),
        orderedDate: form.orderedDate || null,
        expectedDate: form.expectedDate || null,
        notes: form.notes || null,
      })
      toast.success(t('Purchase order created'))
      onSaved()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={t('New purchase order')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving} disabled={!activeSuppliers.length || !activeMaterials.length}>
            {t('Create purchase order')}
          </Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        {!activeSuppliers.length && <Alert tone="info">{t('Add a supplier before creating a purchase order.')}</Alert>}
        {!activeMaterials.length && <Alert tone="info">{t('Add a material before creating a purchase order.')}</Alert>}
        <Select label={t('Approved request')} value={form.requestId}
          onChange={(e) => update('requestId', e.target.value)}>
          <option value="">{t('No request — order directly')}</option>
          {approvedRequests.map((item) => (
            <option key={item.id} value={item.id}>
              {item.materialName} · {qty(item.quantity)} {item.materialUnit}
            </option>
          ))}
        </Select>
        <div className="grid gap-3 sm:grid-cols-2">
          <Select label={t('Material')} value={form.materialId} disabled={Boolean(form.requestId)}
            onChange={(e) => update('materialId', e.target.value)}>
            <option value="">{t('Select material')}</option>
            {activeMaterials.map((material) => (
              <option key={material.id} value={material.id}>
                {material.name} ({material.unit})
              </option>
            ))}
          </Select>
          <Select label={t('Supplier')} value={form.supplierId}
            onChange={(e) => update('supplierId', e.target.value)}>
            <option value="">{t('Select supplier')}</option>
            {activeSuppliers.map((supplier) => (
              <option key={supplier.id} value={supplier.id}>{supplier.name}</option>
            ))}
          </Select>
        </div>
        <div className="grid gap-3 sm:grid-cols-2">
          <Input label={t('Quantity')} type="number" min="0.001" step="0.001" value={form.quantity}
            readOnly={Boolean(form.requestId)}
            onChange={(e) => update('quantity', e.target.value)} required />
          <Input label={t('Unit price')} type="number" min="0.01" step="0.01" value={form.unitPrice}
            onChange={(e) => update('unitPrice', e.target.value)} required />
        </div>
        <div className="grid gap-3 sm:grid-cols-2">
          <Input label={t('Order date')} type="date" value={form.orderedDate}
            onChange={(e) => update('orderedDate', e.target.value)} />
          <Input label={t('Expected date')} type="date" min={form.orderedDate || todayISO()}
            value={form.expectedDate}
            onChange={(e) => update('expectedDate', e.target.value)} />
        </div>
        <Textarea label={t('Notes')} value={form.notes}
          onChange={(e) => update('notes', e.target.value)}
          hint={t('Optional: delivery instructions or invoice reference.')} />
      </div>
    </Modal>
  )
}

function GoodsReceiptModal({ open, order, project, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const remaining = order ? Math.max(0, Number(order.quantity) - Number(order.receivedQuantity)) : 0
  const empty = () => ({
    receivedQuantity: order ? String(remaining) : '', receivedDate: todayISO(),
    invoiceNumber: '', notes: '',
  })
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open || !order) return
    setForm({
      receivedQuantity: String(Math.max(0, Number(order.quantity) - Number(order.receivedQuantity))),
      receivedDate: todayISO(),
      invoiceNumber: '',
      notes: '',
    })
    setError('')
  }, [open, order])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      await api.post(`/api/projects/${project.id}/purchase-orders/${order.id}/receive`, {
        receivedQuantity: Number(form.receivedQuantity),
        receivedDate: form.receivedDate,
        invoiceNumber: form.invoiceNumber || null,
        notes: form.notes || null,
      })
      toast.success(t('Goods received and added to stock'))
      onSaved()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={t('Receive goods')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Receive goods')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        {order && (
          <div className="rounded-xl bg-ink-50 p-3 text-sm">
            <p className="font-medium text-ink-900">{order.materialName}</p>
            <p className="mt-1 text-ink-600">
              {t('Ordered')}: {qty(order.quantity)} {order.materialUnit} · {t('Received')}: {qty(order.receivedQuantity)}
            </p>
            <p className="mt-1 text-ink-600">{t('Remaining')}: {qty(remaining)} {order.materialUnit}</p>
          </div>
        )}
        <div className="grid gap-3 sm:grid-cols-2">
          <Input label={t('Received quantity')} type="number" min="0.001" step="0.001"
            max={remaining || undefined} value={form.receivedQuantity}
            onChange={(e) => update('receivedQuantity', e.target.value)} required />
          <Input label={t('Received date')} type="date" max={todayISO()} value={form.receivedDate}
            onChange={(e) => update('receivedDate', e.target.value)} required />
        </div>
        <Input label={t('Invoice number')} value={form.invoiceNumber}
          onChange={(e) => update('invoiceNumber', e.target.value)} />
        <Textarea label={t('Notes')} value={form.notes}
          onChange={(e) => update('notes', e.target.value)} />
      </div>
    </Modal>
  )
}

function StockMovementModal({ open, material, materials, project, onClose, onSaved }) {
  const { t } = useT()
  const toast = useToast()
  const activeMaterials = materials.filter((item) => item.active !== false)
  const defaultMaterialId = material?.active !== false && material?.id
    ? material.id
    : (activeMaterials[0]?.id || '')
  const empty = {
    materialId: defaultMaterialId, movementType: 'ISSUE',
    quantity: '', movementDate: todayISO(), notes: '',
  }
  const [form, setForm] = useState(empty)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!open) return
    setForm({
      materialId: material?.active !== false && material?.id
        ? material.id
        : (activeMaterials[0]?.id || ''),
      movementType: 'ISSUE',
      quantity: '',
      movementDate: todayISO(),
      notes: '',
    })
    setError('')
  }, [open, material, materials])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))
  const selected = activeMaterials.find((item) => String(item.id) === String(form.materialId))

  const save = async () => {
    setError('')
    setSaving(true)
    try {
      await api.post(`/api/projects/${project.id}/stock-movements`, {
        materialId: Number(form.materialId),
        movementType: form.movementType,
        quantity: Number(form.quantity),
        movementDate: form.movementDate || null,
        notes: form.notes || null,
      })
      toast.success(t('Stock movement recorded'))
      onSaved()
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const selectedType = options('stockMovementType').filter((type) => type !== 'RECEIPT')

  return (
    <Modal open={open} onClose={onClose} title={t('Record stock movement')}
      footer={(
        <>
          <Button variant="secondary" onClick={onClose}>{t('Cancel')}</Button>
          <Button onClick={save} loading={saving}>{t('Record movement')}</Button>
        </>
      )}>
      <div className="space-y-3">
        {error && <Alert>{error}</Alert>}
        <Select label={t('Material')} value={form.materialId}
          onChange={(e) => update('materialId', e.target.value)}>
          {activeMaterials.map((item) => (
            <option key={item.id} value={item.id}>{item.name} ({item.unit})</option>
          ))}
        </Select>
        {selected && (
          <p className="text-xs text-ink-500">
            {t('Current stock')}: {qty(selected.balance)} {selected.unit}
          </p>
        )}
        <div className="grid gap-3 sm:grid-cols-2">
          <Select label={t('Movement type')} value={form.movementType}
            onChange={(e) => update('movementType', e.target.value)}>
            {selectedType.map((type) => (
              <option key={type} value={type}>{t(LABELS.stockMovementType[type])}</option>
            ))}
          </Select>
          <Input label={t('Quantity')} type="number" min="0.001" step="0.001" value={form.quantity}
            onChange={(e) => update('quantity', e.target.value)} required />
        </div>
        <Input label={t('Date')} type="date" max={todayISO()} value={form.movementDate}
          onChange={(e) => update('movementDate', e.target.value)} />
        <Textarea label={t('Notes')} value={form.notes}
          onChange={(e) => update('notes', e.target.value)}
          hint={t('Optional: where the material was used or returned from.')} />
      </div>
    </Modal>
  )
}
