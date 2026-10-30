import { useCallback, useEffect, useState } from 'react'

/** Seconds left until "resend" is allowed again. */
export default function useCountdown(initial = 30) {
  const [left, setLeft] = useState(initial)
  useEffect(() => {
    if (left <= 0) return
    const id = setTimeout(() => setLeft((s) => s - 1), 1000)
    return () => clearTimeout(id)
  }, [left])
  const restart = useCallback((s = initial) => setLeft(s), [initial])
  return [left, restart]
}
