import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import hi from './hi'

const LanguageContext = createContext(null)

export function LanguageProvider({ children }) {
  const [lang, setLangState] = useState(localStorage.getItem('cm_lang') || 'en')

  const setLang = useCallback((l) => {
    localStorage.setItem('cm_lang', l)
    setLangState(l)
  }, [])

  useEffect(() => { document.documentElement.lang = lang }, [lang])

  // The English text is the key. Missing Hindi entries fall back to English.
  const t = useCallback((text, vars) => {
    let out = (lang === 'hi' && hi[text]) || text
    if (vars) for (const k of Object.keys(vars)) out = out.replaceAll(`{${k}}`, vars[k])
    return out
  }, [lang])

  return <LanguageContext.Provider value={{ lang, setLang, t }}>{children}</LanguageContext.Provider>
}

export const useT = () => useContext(LanguageContext)
