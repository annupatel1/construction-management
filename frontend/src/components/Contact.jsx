import { Phone } from 'lucide-react'

/** tap-to-call button, the most useful action on a phone */
export default function CallButton({ phone, label }) {
  if (!phone) return null
  return (
    <a href={`tel:+91${phone}`}
      className="inline-flex h-9 items-center gap-1.5 rounded-lg border border-ink-200 bg-white px-3 text-sm font-medium text-ink-700 hover:bg-ink-50">
      <Phone className="h-4 w-4 text-brand-600" />
      {label || `+91 ${phone}`}
    </a>
  )
}
