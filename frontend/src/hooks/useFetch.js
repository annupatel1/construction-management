import { useCallback, useEffect, useRef, useState } from 'react'
import { errorMessage } from '../lib/api'

/** const { data, loading, error, reload } = useFetch(() => api.get('/x').then(r => r.data), [deps]) */
export default function useFetch(fn, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null })
  const alive = useRef(true)
  const ticket = useRef(0)

  useEffect(() => {
    alive.current = true
    return () => { alive.current = false }
  }, [])

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const run = useCallback(() => {
    const mine = ++ticket.current
    setState((s) => ({ ...s, loading: true, error: null }))
    return fn()
      .then((data) => alive.current && mine === ticket.current && setState({ data, loading: false, error: null }))
      .catch((err) => alive.current && mine === ticket.current &&
        setState({ data: null, loading: false, error: errorMessage(err, 'Could not load data') }))
  }, deps)

  useEffect(() => { run() }, [run])

  return { ...state, reload: run }
}
