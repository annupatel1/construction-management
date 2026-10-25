import { useRef } from 'react'

/** Six single-digit boxes with auto-advance, backspace and paste support. */
export default function OtpInput({ value, onChange, disabled }) {
  const refs = useRef([])
  const digits = Array.from({ length: 6 }, (_, i) => value[i] || '')

  const setAt = (i, d) => {
    const next = digits.slice()
    next[i] = d
    onChange(next.join(''))
  }

  const onInput = (i, e) => {
    const d = e.target.value.replace(/\D/g, '').slice(-1)
    setAt(i, d)
    if (d && i < 5) refs.current[i + 1]?.focus()
  }

  const onKeyDown = (i, e) => {
    if (e.key === 'Backspace' && !digits[i] && i > 0) refs.current[i - 1]?.focus()
    if (e.key === 'ArrowLeft' && i > 0) refs.current[i - 1]?.focus()
    if (e.key === 'ArrowRight' && i < 5) refs.current[i + 1]?.focus()
  }

  const onPaste = (e) => {
    const text = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6)
    if (!text) return
    e.preventDefault()
    onChange(text)
    refs.current[Math.min(text.length, 5)]?.focus()
  }

  return (
    <div className="flex justify-center gap-2 sm:gap-3" onPaste={onPaste}>
      {digits.map((d, i) => (
        <input
          key={i}
          ref={(el) => (refs.current[i] = el)}
          value={d}
          onChange={(e) => onInput(i, e)}
          onKeyDown={(e) => onKeyDown(i, e)}
          onFocus={(e) => e.target.select()}
          inputMode="numeric"
          autoComplete={i === 0 ? 'one-time-code' : 'off'}
          maxLength={1}
          disabled={disabled}
          aria-label={`Digit ${i + 1}`}
          className="h-12 w-11 rounded-xl border border-ink-200 bg-white text-center text-xl font-semibold text-ink-900
                     focus:border-brand-400 focus:outline-none focus:ring-2 focus:ring-brand-400 sm:h-14 sm:w-12"
        />
      ))}
    </div>
  )
}
