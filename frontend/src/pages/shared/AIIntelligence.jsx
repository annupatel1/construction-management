import { useState } from 'react'
import {
  ShieldAlert, HardHat, Sparkles, AlertTriangle, CheckCircle, Flame,
  CloudRain, Wind, Sun, Building2, Send, Download, FileText
} from 'lucide-react'

const FASTAPI_URL = 'http://localhost:8000'

export default function AIIntelligence() {
  const [activeTab, setActiveTab] = useState('safety')
  const [apiKey, setApiKey] = useState('')

  // Tab 1: Safety state
  const [siteFile, setSiteFile] = useState(null)
  const [sitePreview, setSitePreview] = useState(null)
  const [ppeResult, setPpeResult] = useState(null)
  const [ppeLoading, setPpeLoading] = useState(false)

  // Tab 2: Crack state
  const [crackFile, setCrackFile] = useState(null)
  const [crackPreview, setCrackPreview] = useState(null)
  const [crackResult, setCrackResult] = useState(null)
  const [crackLoading, setCrackLoading] = useState(false)

  // Tab 3: Planning state
  const [structureType, setStructureType] = useState('Residential High-Rise (RCC G+20)')
  const [builtUpSqft, setBuiltUpSqft] = useState(25000)
  const [locationTier, setLocationTier] = useState('Tier 1 (Mumbai, Delhi-NCR, Bengaluru)')
  const [completionPercent, setCompletionPercent] = useState(45)
  const [planResult, setPlanResult] = useState(null)
  const [planLoading, setPlanLoading] = useState(false)

  // Tab 4: Weather & Chat state
  const [weatherCondition, setWeatherCondition] = useState('Normal Fair-Weather Conditions (22°C - 30°C)')
  const [weatherData, setWeatherData] = useState(null)
  const [chatMessages, setChatMessages] = useState([
    {
      role: 'assistant',
      content: 'Hello Engineer! I am CivicPulse AI, your on-site civil and structural advisor. Ask me anything about IS 456 concrete codes, OSHA safety rules, or rebar schedules.'
    }
  ])
  const [chatInput, setChatInput] = useState('')
  const [chatLoading, setChatLoading] = useState(false)

  // Handle site image upload
  const handleSiteUpload = (e) => {
    const file = e.target.files[0]
    if (file) {
      setSiteFile(file)
      setSitePreview(URL.createObjectURL(file))
    }
  }

  // Run PPE Audit
  const runPpeAudit = async () => {
    setPpeLoading(true)
    const formData = new FormData()
    if (siteFile) {
      formData.append('file', siteFile)
    } else {
      // Create fallback dummy blob if none selected
      const canvas = document.createElement('canvas')
      canvas.width = 100
      canvas.height = 100
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#f59e0b'
      ctx.fillRect(0, 0, 100, 100)
      const blob = await new Promise((r) => canvas.toBlob(r, 'image/jpeg'))
      formData.append('file', blob, 'sample.jpg')
    }
    if (apiKey) formData.append('api_key', apiKey)

    try {
      const res = await fetch(`${FASTAPI_URL}/api/ppe-audit`, {
        method: 'POST',
        body: formData
      })
      if (res.ok) {
        const data = await res.json()
        setPpeResult(data)
      } else {
        throw new Error('API request failed')
      }
    } catch {
      // Fallback demo data
      setPpeResult({
        safety_score: 76,
        overall_status: 'Action Required',
        workers_detected: 4,
        mode: 'DEMONSTRATION_MODE (FastAPI Fallback)',
        ppe_detection: [
          {
            item: 'Industrial Hard Hats / Helmets (IS 2925 / OSHA 1926.100)',
            status: 'Detected',
            compliance_rate: '100%',
            observations: 'All 4 workers wearing certified Type 1 Class E impact-resistant helmets.'
          },
          {
            item: 'High-Visibility Safety Vests (EN ISO 20471 / IS 15809)',
            status: 'Partial',
            compliance_rate: '75%',
            observations: '3 workers wearing fluorescent Class 2 vests; 1 worker in unreflective clothing.'
          },
          {
            item: 'Steel-Toe Puncture-Resistant Boots (IS 15298)',
            status: 'Detected',
            compliance_rate: '100%',
            observations: 'Heavy-duty steel-toe boots verified on all active personnel.'
          },
          {
            item: 'Fall Arrest Harness & Lifelines (IS 3521 / OSHA 1926.502)',
            status: 'Missing',
            compliance_rate: '0%',
            observations: 'Worker near slab perimeter (>2.4m elevation) lacks personal fall arrest lanyard.'
          }
        ],
        hazards_identified: [
          {
            title: 'Unprotected Slab Edge Fall Hazard',
            severity: 'Critical',
            location: 'North-West perimeter, Level 3 cantilever slab',
            standard_violated: 'OSHA 1926.501(b)(1) & NBC 2016 Part 7',
            corrective_remedy: 'Immediately install 42-inch top-rail and mid-rail perimeter guardrails.'
          },
          {
            title: 'Protruding Rebar Impalement Risk',
            severity: 'High',
            location: 'Column starter bars adjacent to walkway',
            standard_violated: 'OSHA 1926.701(b) / IS 456 Cl. 12.3',
            corrective_remedy: 'Cap all vertical rebar ends with steel-reinforced OSHA mushroom caps.'
          }
        ],
        auditor_summary: 'Baseline helmet and footwear compliance is good. Perimeter fall protection is deficient and requires halting edge works.'
      })
    } finally {
      setPpeLoading(false)
    }
  }

  // Handle crack image upload
  const handleCrackUpload = (e) => {
    const file = e.target.files[0]
    if (file) {
      setCrackFile(file)
      setCrackPreview(URL.createObjectURL(file))
    }
  }

  // Run Crack Diagnosis
  const runCrackDiagnosis = async () => {
    setCrackLoading(true)
    const formData = new FormData()
    if (crackFile) {
      formData.append('file', crackFile)
    } else {
      const canvas = document.createElement('canvas')
      canvas.width = 100
      canvas.height = 100
      const ctx = canvas.getContext('2d')
      ctx.fillStyle = '#94a3b8'
      ctx.fillRect(0, 0, 100, 100)
      const blob = await new Promise((r) => canvas.toBlob(r, 'image/jpeg'))
      formData.append('file', blob, 'crack.jpg')
    }
    if (apiKey) formData.append('api_key', apiKey)

    try {
      const res = await fetch(`${FASTAPI_URL}/api/crack-diagnostics`, {
        method: 'POST',
        body: formData
      })
      if (res.ok) {
        const data = await res.json()
        setCrackResult(data)
      } else {
        throw new Error('API failed')
      }
    } catch {
      setCrackResult({
        defect_type: 'Structural Shear',
        estimated_width_mm: 2.4,
        severity: 'Severe',
        structural_risk: 'Major Structural Hazard',
        is_code_compliant: false,
        code_limit_reference: 'IS 456:2000 Clause 35.3.2 specifies max crack width 0.2mm for moderate exposure; 2.4mm exceeds threshold.',
        root_causes: [
          'Excess diagonal tension shear stress exceeding shear link capacity.',
          'Sub-grade differential settlement triggering torsional shear.'
        ],
        recommended_repairs: [
          {
            step: 1,
            method: 'Low-viscosity epoxy resin injection',
            materials: 'Sikadur 52 / Nitofil LV under 0.2-0.4 N/mm² pressure',
            specification: 'V-groove chasing with nipples placed at 150mm centers'
          },
          {
            step: 2,
            method: 'CFRP Carbon Fiber Structural Laminate Wrap',
            materials: '300 g/m² unidirectional carbon fabric with epoxy saturant',
            specification: 'Apply 2 layers U-jacket wrap per ACI 440.2R'
          }
        ],
        urgency: 'Immediate Intervention (Within 24 Hours)',
        engineer_notes: 'Diagonal 45-degree angle indicates shear fracture near beam-column junction. Propping mandatory before injection.',
        mode: 'DEMONSTRATION_MODE (FastAPI Fallback)'
      })
    } finally {
      setCrackLoading(false)
    }
  }

  // Run Planning & Valuation
  const runPlanningValuation = async () => {
    setPlanLoading(true)
    try {
      const res = await fetch(`${FASTAPI_URL}/api/planning-valuation`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          structure_type: structureType,
          built_up_sqft: builtUpSqft,
          location_tier: locationTier,
          completion_percentage: completionPercent
        })
      })
      if (res.ok) {
        const data = await res.json()
        setPlanResult(data)
      } else {
        throw new Error('API failed')
      }
    } catch {
      // Local computation fallback
      const baseCost = structureType.includes('Commercial') ? 3200 : (structureType.includes('PEB') ? 1600 : 2400)
      const totalBudget = builtUpSqft * baseCost
      const capex = totalBudget * (completionPercent / 100)
      const marketRate = locationTier.includes('Tier 1') ? 14500 : (locationTier.includes('Tier 2') ? 8200 : 4800)
      const fullMarket = builtUpSqft * marketRate
      const currentVal = fullMarket * (0.35 + (completionPercent / 100) * 0.65)
      
      const prompt = `hyperrealistic modern architectural 3D exterior render of ${structureType}, in ${locationTier}, photorealistic 8k`
      const imgUrl = `https://image.pollinations.ai/prompt/${encodeURIComponent(prompt)}?width=1024&height=640&nologo=true&seed=42`

      setPlanResult({
        total_budget_formatted: `₹ ${(totalBudget / 10000000).toFixed(2)} Cr`,
        capex_incurred_formatted: `₹ ${(capex / 10000000).toFixed(2)} Cr`,
        current_asset_valuation_formatted: `₹ ${(currentVal / 10000000).toFixed(2)} Cr`,
        full_market_valuation_formatted: `₹ ${(fullMarket / 10000000).toFixed(2)} Cr`,
        materials: {
          structural_steel_tonnes: ((builtUpSqft * 4.5) / 1000).toFixed(1),
          cement_bags_50kg: Math.round(builtUpSqft * 0.42),
          river_m_sand_cft: Math.round(builtUpSqft * 1.75),
          coarse_aggregates_cft: Math.round(builtUpSqft * 2.2)
        },
        milestone: {
          current_phase: completionPercent < 25 ? 'Phase 1: Substructure & Deep Foundations' : (completionPercent < 60 ? 'Phase 2: Superstructure RCC Frame' : 'Phase 3: MEP & Finishing'),
          phase_status: 'On track according to master schedule',
          progress_percent: completionPercent
        },
        visualization: {
          prompt,
          image_url: imgUrl
        }
      })
    } finally {
      setPlanLoading(false)
    }
  }

  // Handle Weather change
  const handleWeatherSelect = async (condition) => {
    setWeatherCondition(condition)
    try {
      const res = await fetch(`${FASTAPI_URL}/api/weather-advisory?condition=${encodeURIComponent(condition)}`)
      if (res.ok) {
        const data = await res.json()
        setWeatherData(data)
      }
    } catch {
      // Fallback
      if (condition.includes('Wind')) {
        setWeatherData({
          status_level: 'WARNING',
          summary: 'Wind speeds exceed safe operating thresholds for vertical transport and elevated activities.',
          concrete_advisory: 'High winds accelerate surface moisture evaporation. Apply aliphatic alcohol retarders or polythene sheets immediately.',
          mandatory_alerts: [
            { title: 'Tower Crane Suspension', standard: 'OSHA 1926.1431', action: 'Immediate suspension of tower crane hoisting when sustained wind speeds exceed 38 km/h.' },
            { title: 'Suspended Scaffolding Ban', standard: 'OSHA 1926.451', action: 'Cease all swing-stage and suspended facade cradles.' }
          ]
        })
      } else if (condition.includes('Rain')) {
        setWeatherData({
          status_level: 'CRITICAL',
          summary: 'High precipitation increases trench cave-in risks and concrete washout.',
          concrete_advisory: 'Do not cast exposed slabs during active rain. Rainwater dilutes water-cement ratio.',
          mandatory_alerts: [
            { title: 'Excavation Evacuation', standard: 'OSHA 1926.651', action: 'Evacuate all deep trenches and basement pits immediately.' },
            { title: 'Electrical Shut-Off', standard: 'OSHA 1926.404', action: 'Isolate all temporary outdoor distribution boards through 30mA ELCBs.' }
          ]
        })
      } else {
        setWeatherData({
          status_level: 'OPTIMAL',
          summary: 'Ideal ambient conditions for full-scale civil, structural, finishing, and crane operations.',
          concrete_advisory: 'Maintain standard 7-day moist curing for OPC concrete per IS 456 Cl. 13.5.',
          mandatory_alerts: [
            { title: 'Full Velocity Operations', standard: 'General OSHA 1926', action: 'Standard operating protocols active. Daily morning Toolbox Talks required.' }
          ]
        })
      }
    }
  }

  // Handle Chat submit
  const handleChatSubmit = async (e) => {
    e?.preventDefault()
    if (!chatInput.trim()) return

    const userText = chatInput.trim()
    const newHistory = [...chatMessages, { role: 'user', content: userText }]
    setChatMessages(newHistory)
    setChatInput('')
    setChatLoading(true)

    try {
      const res = await fetch(`${FASTAPI_URL}/api/chat`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: userText, history: newHistory, api_key: apiKey })
      })
      if (res.ok) {
        const data = await res.json()
        setChatMessages([...newHistory, { role: 'assistant', content: data.reply }])
      } else {
        throw new Error('Chat failed')
      }
    } catch {
      // Civil engineering domain fallback
      let reply = `Civil Engineer Answer for "${userText}": Ensure full compliance with IS 456:2000 quality standards and OSHA 1926 safety regulations. Maintain concrete cube registers and pour cards.`
      if (userText.toLowerCase().includes('curing')) {
        reply = '**IS 456:2000 Curing Protocol (Cl. 13.5):** OPC concrete requires minimum 7 days moist curing (10 days in hot/dry climate). PPC/Slag concrete requires minimum 10-14 days continuous ponding or wet burlap wrapping.'
      } else if (userText.toLowerCase().includes('striking') || userText.toLowerCase().includes('formwork')) {
        reply = '**IS 456:2000 Striking Periods:** Vertical formwork: 16-24 hrs; Slab soffit: 3 days (props left under); Beam soffit: 7 days; Slab props: 7 days (<4.5m) or 14 days (>4.5m).'
      }
      setChatMessages([...newHistory, { role: 'assistant', content: reply }])
    } finally {
      setChatLoading(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="rounded-2xl border border-ink-700 bg-gradient-to-r from-ink-900 to-ink-950 p-6 shadow-xl">
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <div className="inline-flex items-center gap-1.5 rounded-full bg-brand-500/20 px-3 py-1 text-xs font-semibold text-brand-400">
              <Sparkles className="h-3.5 w-3.5" />
              Capstone Project 1 | Platform ID: 6ac4c98d1dcde6d7857c1642
            </div>
            <h1 className="mt-2 text-2xl font-bold text-white tracking-tight">
              Multimodal Construction Intelligence Platform
            </h1>
            <p className="mt-1 text-sm text-ink-300">
              OSHA 1926 & IS 456 Safety Audits, Concrete Crack Forensic Diagnostics, Real Estate Valuation in INR, and Weather Advisory.
            </p>
          </div>

          <div className="flex items-center gap-2">
            <input
              type="password"
              placeholder="Gemini API Key (optional)"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              className="rounded-lg border border-ink-600 bg-ink-800 px-3 py-1.5 text-xs text-white placeholder-ink-400 focus:outline-none focus:border-brand-500"
            />
            <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${apiKey ? 'bg-emerald-500/20 text-emerald-400' : 'bg-brand-500/20 text-brand-400'}`}>
              {apiKey ? '⚡ Gemini 1.5' : '🛡️ Demo Mode'}
            </span>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-ink-700">
        <button
          onClick={() => setActiveTab('safety')}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-semibold border-b-2 transition-colors ${activeTab === 'safety' ? 'border-brand-500 text-brand-400' : 'border-transparent text-ink-400 hover:text-white'}`}
        >
          <HardHat className="h-4 w-4" /> 1. PPE Safety Vision Audit
        </button>
        <button
          onClick={() => setActiveTab('crack')}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-semibold border-b-2 transition-colors ${activeTab === 'crack' ? 'border-brand-500 text-brand-400' : 'border-transparent text-ink-400 hover:text-white'}`}
        >
          <ShieldAlert className="h-4 w-4" /> 2. Crack Diagnostics
        </button>
        <button
          onClick={() => setActiveTab('planning')}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-semibold border-b-2 transition-colors ${activeTab === 'planning' ? 'border-brand-500 text-brand-400' : 'border-transparent text-ink-400 hover:text-white'}`}
        >
          <Building2 className="h-4 w-4" /> 3. Planning & Real Estate Valuation
        </button>
        <button
          onClick={() => {
            setActiveTab('weather')
            if (!weatherData) handleWeatherSelect(weatherCondition)
          }}
          className={`flex items-center gap-2 px-5 py-3 text-sm font-semibold border-b-2 transition-colors ${activeTab === 'weather' ? 'border-brand-500 text-brand-400' : 'border-transparent text-ink-400 hover:text-white'}`}
        >
          <Wind className="h-4 w-4" /> 4. Weather & Engineering Chatbot
        </button>
      </div>

      {/* TAB 1: PPE SAFETY */}
      {activeTab === 'safety' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-4">
              <h3 className="text-base font-semibold text-white">Upload Site Photo</h3>
              <input
                type="file"
                accept="image/*"
                onChange={handleSiteUpload}
                className="block w-full text-sm text-ink-300 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-brand-600 file:text-white hover:file:bg-brand-500 cursor-pointer"
              />
              <button
                onClick={runPpeAudit}
                disabled={ppeLoading}
                className="w-full flex items-center justify-center gap-2 rounded-xl bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-500 transition-colors disabled:opacity-50"
              >
                {ppeLoading ? 'Running AI Safety Vision Audit...' : '🚀 Run Multimodal Safety Audit'}
              </button>
            </div>

            <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 flex items-center justify-center min-h-[200px]">
              {sitePreview ? (
                <img src={sitePreview} alt="Site preview" className="max-h-60 rounded-lg object-cover" />
              ) : (
                <div className="text-center text-ink-400 text-sm">
                  <HardHat className="h-10 w-10 mx-auto text-ink-600 mb-2" />
                  Upload a site photo or click Run Audit to test benchmark inspection
                </div>
              )}
            </div>
          </div>

          {ppeResult && (
            <div className="space-y-6 pt-4 border-t border-ink-700">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="rounded-xl border border-ink-700 bg-ink-900 p-4 text-center">
                  <div className="text-xs uppercase text-ink-400">Safety Score</div>
                  <div className="text-3xl font-extrabold text-emerald-400 mt-1">{ppeResult.safety_score}/100</div>
                </div>
                <div className="rounded-xl border border-ink-700 bg-ink-900 p-4 text-center">
                  <div className="text-xs uppercase text-ink-400">Overall Status</div>
                  <div className="text-2xl font-bold text-amber-400 mt-1">{ppeResult.overall_status}</div>
                </div>
                <div className="rounded-xl border border-ink-700 bg-ink-900 p-4 text-center">
                  <div className="text-xs uppercase text-ink-400">Workers Audited</div>
                  <div className="text-2xl font-bold text-sky-400 mt-1">{ppeResult.workers_detected} Active</div>
                </div>
              </div>

              {/* PPE Checklist */}
              <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-3">
                <h4 className="text-sm font-semibold text-white">PPE Detection Checklist (OSHA 1926 & IS 15809)</h4>
                {ppeResult.ppe_detection?.map((item, idx) => (
                  <div key={idx} className="flex items-center justify-between p-3 rounded-lg bg-ink-800 border border-ink-700">
                    <div>
                      <div className="text-sm font-medium text-white">{item.item}</div>
                      <div className="text-xs text-ink-400">{item.observations}</div>
                    </div>
                    <span className={`px-2.5 py-1 rounded-full text-xs font-bold ${item.status === 'Detected' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-rose-500/20 text-rose-400'}`}>
                      {item.status} ({item.compliance_rate})
                    </span>
                  </div>
                ))}
              </div>

              {/* Hazards Identified */}
              <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-3">
                <h4 className="text-sm font-semibold text-white">Identified Site Hazards & Corrective Remedies</h4>
                {ppeResult.hazards_identified?.map((h, idx) => (
                  <div key={idx} className="p-4 rounded-lg bg-ink-800 border-l-4 border-rose-500 space-y-1">
                    <div className="flex items-center justify-between">
                      <span className="font-bold text-white text-sm">{h.title}</span>
                      <span className="text-xs px-2 py-0.5 rounded font-bold bg-rose-500/20 text-rose-400">{h.severity}</span>
                    </div>
                    <div className="text-xs text-ink-400">📍 {h.location} | Standard: {h.standard_violated}</div>
                    <div className="text-xs text-sky-400 bg-ink-900/60 p-2 rounded mt-2">🛠️ Remedy: {h.corrective_remedy}</div>
                  </div>
                ))}
              </div>

              {/* Download button */}
              <button
                onClick={() => {
                  const blob = new Blob([JSON.stringify(ppeResult, null, 2)], { type: 'application/json' })
                  const url = URL.createObjectURL(blob)
                  const a = document.createElement('a')
                  a.href = url
                  a.download = 'ppe_safety_report.json'
                  a.click()
                }}
                className="inline-flex items-center gap-2 rounded-lg border border-ink-600 bg-ink-800 px-4 py-2 text-xs font-semibold text-white hover:bg-ink-700"
              >
                <Download className="h-4 w-4" /> Download Structured Safety Report (JSON)
              </button>
            </div>
          )}
        </div>
      )}

      {/* TAB 2: CRACK DIAGNOSTICS */}
      {activeTab === 'crack' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-4">
              <h3 className="text-base font-semibold text-white">Upload Concrete Surface Photo</h3>
              <input
                type="file"
                accept="image/*"
                onChange={handleCrackUpload}
                className="block w-full text-sm text-ink-300 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-brand-600 file:text-white hover:file:bg-brand-500 cursor-pointer"
              />
              <button
                onClick={runCrackDiagnosis}
                disabled={crackLoading}
                className="w-full flex items-center justify-center gap-2 rounded-xl bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-500 transition-colors disabled:opacity-50"
              >
                {crackLoading ? 'Diagnosing Fracture Topology...' : '🔍 Run Forensic Crack Diagnosis'}
              </button>
            </div>

            <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 flex items-center justify-center min-h-[200px]">
              {crackPreview ? (
                <img src={crackPreview} alt="Crack preview" className="max-h-60 rounded-lg object-cover" />
              ) : (
                <div className="text-center text-ink-400 text-sm">
                  <ShieldAlert className="h-10 w-10 mx-auto text-ink-600 mb-2" />
                  Upload concrete/masonry crack photo or click Run Diagnosis
                </div>
              )}
            </div>
          </div>

          {crackResult && (
            <div className="space-y-6 pt-4 border-t border-ink-700">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div className="rounded-xl border border-ink-700 bg-ink-900 p-4 text-center">
                  <div className="text-xs uppercase text-ink-400">Defect Typology</div>
                  <div className="text-xl font-bold text-white mt-1">{crackResult.defect_type}</div>
                </div>
                <div className="rounded-xl border border-ink-700 bg-ink-900 p-4 text-center">
                  <div className="text-xs uppercase text-ink-400">Estimated Width</div>
                  <div className="text-2xl font-bold text-amber-400 mt-1">{crackResult.estimated_width_mm} mm</div>
                </div>
                <div className="rounded-xl border border-ink-700 bg-ink-900 p-4 text-center">
                  <div className="text-xs uppercase text-ink-400">Severity Level</div>
                  <div className="text-2xl font-bold text-rose-400 mt-1">{crackResult.severity}</div>
                </div>
                <div className="rounded-xl border border-ink-700 bg-ink-900 p-4 text-center">
                  <div className="text-xs uppercase text-ink-400">Urgency</div>
                  <div className="text-sm font-bold text-amber-300 mt-2">{crackResult.urgency}</div>
                </div>
              </div>

              <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-sm">
                ⚖️ <strong>IS 456:2000 Compliance:</strong> {crackResult.code_limit_reference}
              </div>

              {/* Repairs */}
              <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-3">
                <h4 className="text-sm font-semibold text-white">Recommended Engineering Repair Protocol</h4>
                {crackResult.recommended_repairs?.map((r, idx) => (
                  <div key={idx} className="p-3 rounded-lg bg-ink-800 border-l-4 border-sky-500 space-y-1">
                    <div className="font-semibold text-white text-sm">Step {r.step}: {r.method}</div>
                    <div className="text-xs text-ink-300">Materials: {r.materials}</div>
                    <div className="text-xs text-sky-400">Spec: {r.specification}</div>
                  </div>
                ))}
              </div>

              <button
                onClick={() => {
                  const blob = new Blob([JSON.stringify(crackResult, null, 2)], { type: 'application/json' })
                  const url = URL.createObjectURL(blob)
                  const a = document.createElement('a')
                  a.href = url
                  a.download = 'crack_diagnostic_report.json'
                  a.click()
                }}
                className="inline-flex items-center gap-2 rounded-lg border border-ink-600 bg-ink-800 px-4 py-2 text-xs font-semibold text-white hover:bg-ink-700"
              >
                <Download className="h-4 w-4" /> Download Forensic Report (JSON)
              </button>
            </div>
          )}
        </div>
      )}

      {/* TAB 3: PLANNING & REAL ESTATE VALUATION */}
      {activeTab === 'planning' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-4">
              <h3 className="text-base font-semibold text-white">Project Parameters</h3>
              <div>
                <label className="text-xs text-ink-400 block mb-1">Structure Type</label>
                <select
                  value={structureType}
                  onChange={(e) => setStructureType(e.target.value)}
                  className="w-full rounded-lg border border-ink-700 bg-ink-800 px-3 py-2 text-sm text-white focus:outline-none"
                >
                  <option>Residential High-Rise (RCC G+20)</option>
                  <option>Commercial Grade-A Office Complex</option>
                  <option>Industrial Pre-Engineered Building (PEB)</option>
                  <option>Luxury Villa / Low-Rise G+2</option>
                </select>
              </div>

              <div>
                <label className="text-xs text-ink-400 block mb-1">Built-Up Area (sq. ft.)</label>
                <input
                  type="number"
                  value={builtUpSqft}
                  onChange={(e) => setBuiltUpSqft(Number(e.target.value))}
                  step="2500"
                  className="w-full rounded-lg border border-ink-700 bg-ink-800 px-3 py-2 text-sm text-white focus:outline-none"
                />
              </div>

              <div>
                <label className="text-xs text-ink-400 block mb-1">Location Tier (Indian Real Estate)</label>
                <select
                  value={locationTier}
                  onChange={(e) => setLocationTier(e.target.value)}
                  className="w-full rounded-lg border border-ink-700 bg-ink-800 px-3 py-2 text-sm text-white focus:outline-none"
                >
                  <option>Tier 1 (Mumbai, Delhi-NCR, Bengaluru)</option>
                  <option>Tier 2 (Pune, Hyderabad, Chennai, Ahmedabad)</option>
                  <option>Tier 3 (Emerging Urban Centers)</option>
                </select>
              </div>

              <div>
                <div className="flex justify-between text-xs text-ink-400 mb-1">
                  <span>Construction Completion</span>
                  <span>{completionPercent}%</span>
                </div>
                <input
                  type="range"
                  min="0"
                  max="100"
                  step="5"
                  value={completionPercent}
                  onChange={(e) => setCompletionPercent(Number(e.target.value))}
                  className="w-full"
                />
              </div>

              <button
                onClick={runPlanningValuation}
                disabled={planLoading}
                className="w-full rounded-xl bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-500 transition-colors"
              >
                {planLoading ? 'Calculating Economics...' : '📊 Calculate INR Valuation & Generate 3D Render'}
              </button>
            </div>

            {planResult ? (
              <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-4">
                <h3 className="text-base font-semibold text-white">Financial Valuation in INR</h3>
                <div className="grid grid-cols-2 gap-3">
                  <div className="p-3 rounded-lg bg-ink-800 border border-ink-700 text-center">
                    <div className="text-xs text-ink-400">Total Budget</div>
                    <div className="text-lg font-extrabold text-sky-400">{planResult.total_budget_formatted}</div>
                  </div>
                  <div className="p-3 rounded-lg bg-ink-800 border border-ink-700 text-center">
                    <div className="text-xs text-ink-400">CapEx Incurred</div>
                    <div className="text-lg font-extrabold text-amber-400">{planResult.capex_incurred_formatted}</div>
                  </div>
                  <div className="p-3 rounded-lg bg-ink-800 border border-ink-700 text-center">
                    <div className="text-xs text-ink-400">Current WIP Value</div>
                    <div className="text-lg font-extrabold text-emerald-400">{planResult.current_asset_valuation_formatted}</div>
                  </div>
                  <div className="p-3 rounded-lg bg-ink-800 border border-ink-700 text-center">
                    <div className="text-xs text-ink-400">Completed Market Value</div>
                    <div className="text-lg font-extrabold text-purple-400">{planResult.full_market_valuation_formatted}</div>
                  </div>
                </div>

                {/* Materials */}
                <div className="pt-2 border-t border-ink-700">
                  <h4 className="text-xs font-bold uppercase text-ink-400 mb-2">BOM Material Quantities</h4>
                  <div className="grid grid-cols-2 gap-2 text-xs">
                    <div className="bg-ink-800 p-2 rounded">Steel: <strong>{planResult.materials?.structural_steel_tonnes} MT</strong></div>
                    <div className="bg-ink-800 p-2 rounded">Cement: <strong>{planResult.materials?.cement_bags_50kg} Bags</strong></div>
                    <div className="bg-ink-800 p-2 rounded">Sand: <strong>{planResult.materials?.river_m_sand_cft} cu ft</strong></div>
                    <div className="bg-ink-800 p-2 rounded">Aggregate: <strong>{planResult.materials?.coarse_aggregates_cft} cu ft</strong></div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 flex items-center justify-center text-ink-400 text-sm">
                Click "Calculate INR Valuation" to generate financial summary
              </div>
            )}
          </div>

          {/* 3D Architectural Render */}
          {planResult?.visualization && (
            <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-3">
              <h3 className="text-base font-semibold text-white">🎨 Conceptual 3D Architectural Rendering (Pollinations / Flux AI)</h3>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-center">
                <div className="md:col-span-2">
                  <img
                    src={planResult.visualization.image_url}
                    alt="3D Architectural Concept"
                    className="w-full rounded-xl object-cover max-h-96 border border-ink-700"
                  />
                </div>
                <div className="bg-ink-800 p-4 rounded-xl border border-ink-700 space-y-2 text-xs text-ink-300">
                  <div className="font-bold text-sky-400">Prompt Specification:</div>
                  <p className="italic font-mono text-[11px] text-ink-200">{planResult.visualization.prompt}</p>
                  <div className="pt-2 text-ink-400">Resolution: 1024x640 Octane Photorealistic</div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 4: WEATHER & CHAT */}
      {activeTab === 'weather' && (
        <div className="space-y-6">
          <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-4">
            <h3 className="text-base font-semibold text-white">Select Site Weather Condition</h3>
            <div className="flex flex-wrap gap-2">
              {[
                'Normal Fair-Weather Conditions (22°C - 30°C)',
                'High Wind (Gale / Gusts > 35 km/h)',
                'Heavy Rain & Monsoonal Storms',
                'Extreme Heat & Solar Radiation (> 40°C)'
              ].map((cond) => (
                <button
                  key={cond}
                  onClick={() => handleWeatherSelect(cond)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-semibold border transition-colors ${weatherCondition === cond ? 'bg-brand-600 border-brand-500 text-white' : 'bg-ink-800 border-ink-700 text-ink-300 hover:text-white'}`}
                >
                  {cond}
                </button>
              ))}
            </div>

            {weatherData && (
              <div className="space-y-3 pt-3 border-t border-ink-700">
                <div className="p-3 rounded-lg bg-ink-800 text-sm text-ink-200">
                  {weatherData.summary}
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <div className="text-xs font-bold uppercase text-ink-400">Mandatory Directives</div>
                    {weatherData.mandatory_alerts?.map((a, i) => (
                      <div key={i} className="p-2.5 rounded bg-ink-800 text-xs space-y-1">
                        <div className="font-bold text-white">{a.title} ({a.standard})</div>
                        <div className="text-ink-300">{a.action}</div>
                      </div>
                    ))}
                  </div>
                  <div className="space-y-2">
                    <div className="text-xs font-bold uppercase text-ink-400">IS 456 Concrete Curing Directive</div>
                    <div className="p-3 rounded bg-ink-800 text-xs text-sky-300 leading-relaxed">
                      {weatherData.concrete_advisory}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Interactive Chatbot */}
          <div className="rounded-xl border border-ink-700 bg-ink-900 p-5 space-y-4">
            <h3 className="text-base font-semibold text-white">💬 CivicPulse Engineering Chatbot (IS 456 & OSHA Advisor)</h3>
            <div className="h-64 overflow-y-auto space-y-3 p-3 rounded-xl bg-ink-950 border border-ink-800">
              {chatMessages.map((msg, i) => (
                <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div className={`max-w-[85%] rounded-xl p-3 text-xs leading-relaxed ${msg.role === 'user' ? 'bg-brand-600 text-white' : 'bg-ink-800 text-ink-200 border border-ink-700'}`}>
                    {msg.content}
                  </div>
                </div>
              ))}
            </div>

            <form onSubmit={handleChatSubmit} className="flex gap-2">
              <input
                type="text"
                placeholder="Ask about rebar lap length, formwork striking period, or OSHA 1926..."
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                className="flex-1 rounded-lg border border-ink-700 bg-ink-800 px-3 py-2 text-xs text-white placeholder-ink-400 focus:outline-none focus:border-brand-500"
              />
              <button
                type="submit"
                disabled={chatLoading}
                className="inline-flex items-center gap-1.5 rounded-lg bg-brand-600 px-4 py-2 text-xs font-semibold text-white hover:bg-brand-500 transition-colors disabled:opacity-50"
              >
                <Send className="h-3.5 w-3.5" /> Send
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
