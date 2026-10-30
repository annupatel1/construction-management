import { APP_NAME } from '../config'

export default function Logo({ light = false, size = 'md' }) {
  const box = size === 'lg' ? 'h-11 w-11' : 'h-9 w-9'
  return (
    <div className="flex items-center gap-2.5">
      <span className={`${box} flex items-center justify-center rounded-xl bg-brand-600`}>
        <svg viewBox="0 0 64 64" className="h-2/3 w-2/3" fill="none">
          <path d="M12 50V30l20-16 20 16v20H38V37H26v13z" fill="#fff" />
        </svg>
      </span>
      <span className={`text-lg font-semibold tracking-tight ${light ? 'text-white' : 'text-ink-900'}`}>{APP_NAME}</span>
    </div>
  )
}
