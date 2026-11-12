import { Link, useLocation } from 'react-router-dom'
import {
  ArrowRight,
  Brain,
  Building2,
  CalendarCheck,
  Camera,
  CheckCircle2,
  HardHat,
  Hammer,
  Home,
  IndianRupee,
  Package,
  PencilRuler,
  ShieldCheck,
  Smartphone,
  Sparkles,
  TrendingUp,
  Users,
  Wallet,
} from 'lucide-react'
import Logo from '../../components/Logo'
import LegalLinks from '../../components/LegalLinks'
import { SUPPORT_EMAIL } from '../../config'

const features = [
  {
    icon: CalendarCheck,
    title: 'Attendance and wages',
    text: 'Mark full days, half days and overtime. Wage balances update for every labourer and every site without a spreadsheet.',
  },
  {
    icon: Wallet,
    title: 'Payments and expenses',
    text: 'Record owner payments, contractor expenses and material spend, then see the project finance picture in one place.',
  },
  {
    icon: Package,
    title: 'Materials and stock',
    text: 'Raise material requests, approve purchase orders, receive goods and keep an append-only stock ledger.',
  },
  {
    icon: Camera,
    title: 'Progress and site records',
    text: 'Log daily progress, tasks, milestones, issues and site photos so the whole team works from the same status.',
  },
  {
    icon: Brain,
    title: 'AI site intelligence',
    text: 'Check PPE, inspect visible cracks and get a second opinion on site photos, with clear guidance instead of guesswork.',
  },
  {
    icon: ShieldCheck,
    title: 'Roles and privacy',
    text: 'Owners, contractors, engineers, labour and admins only see the project data their role is allowed to see.',
  },
]

const roles = [
  {
    icon: Home,
    title: 'House owners',
    text: 'Track budget, payments, progress and contractor updates without chasing everyone on WhatsApp.',
  },
  {
    icon: Hammer,
    title: 'Contractors',
    text: 'Run multiple sites, manage labour and wages, request materials and keep project money in one place.',
  },
  {
    icon: PencilRuler,
    title: 'Engineers',
    text: 'Supervise work, publish progress reports and collaborate with owners and contractors with scoped access.',
  },
  {
    icon: HardHat,
    title: 'Labour',
    text: 'See assigned sites, attendance and wage balance from a simple, mobile-friendly screen.',
  },
]

const steps = [
  {
    icon: Users,
    title: 'Create your account',
    text: 'Choose your role and verify your email. Owners, contractors, engineers and labour each get the right view.',
  },
  {
    icon: Building2,
    title: 'Add your project',
    text: 'Set the scope, budget and dates, then invite the contractor and engineer who will work on the site.',
  },
  {
    icon: TrendingUp,
    title: 'Run the site',
    text: 'Attendance, materials, photos, progress and money stay connected, so decisions are based on today’s site.',
  },
]

const faqs = [
  {
    q: 'Does BuildTrack move or hold money?',
    a: 'No. BuildTrack is a record-keeping and coordination tool. Payments shown in the app are entered by users and do not prove that money actually moved. Reconcile records with your bank, cash book and invoices.',
  },
  {
    q: 'Can I use it on a phone?',
    a: 'Yes. BuildTrack is designed mobile-first for site use. Open it in your phone browser; no separate app install is required today.',
  },
  {
    q: 'Who can see my project data?',
    a: 'Access is role-based. Owners see project finance and progress, contractors manage labour and site operations, engineers get scoped site access without supplier costs, and labour sees only assigned work and wage information.',
  },
  {
    q: 'Is my data backed up?',
    a: 'The Docker deployment includes a documented MySQL backup script, retention guidance and a restore drill. Ask for the pilot runbook before you go live.',
  },
  {
    q: 'Does the AI replace an engineer?',
    a: 'No. AI results are decision support only. Always verify safety or structural findings with a qualified engineer before acting on them.',
  },
]

function Stat({ value, label }) {
  return (
    <div>
      <p className="text-2xl font-semibold text-white sm:text-3xl">{value}</p>
      <p className="mt-1 text-xs text-ink-300 sm:text-sm">{label}</p>
    </div>
  )
}

function Progress({ label, value, tone = 'bg-brand-400' }) {
  return (
    <div>
      <div className="flex items-center justify-between text-xs">
        <span className="text-ink-300">{label}</span>
        <span className="font-medium text-white">{value}%</span>
      </div>
      <div className="mt-2 h-1.5 overflow-hidden rounded-full bg-white/10">
        <div className={`h-full rounded-full ${tone}`} style={{ width: `${value}%` }} />
      </div>
    </div>
  )
}

function MiniStat({ label, value }) {
  return (
    <div className="rounded-xl border border-white/10 bg-white/5 p-3">
      <p className="text-[11px] uppercase tracking-wide text-ink-400">{label}</p>
      <p className="mt-1 text-sm font-semibold text-white">{value}</p>
    </div>
  )
}

export default function Landing() {
  const location = useLocation()
  const from = location.state?.from

  return (
    <div className="min-h-screen bg-white text-ink-800">
      <header className="sticky top-0 z-40 border-b border-ink-100 bg-white/90 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3.5 sm:px-6">
          <Logo />

          <nav className="hidden items-center gap-7 text-sm font-medium text-ink-600 lg:flex">
            <a href="#features" className="hover:text-ink-900">Features</a>
            <a href="#roles" className="hover:text-ink-900">For your team</a>
            <a href="#how" className="hover:text-ink-900">How it works</a>
            <a href="#ai" className="hover:text-ink-900">AI</a>
            <a href="#pricing" className="hover:text-ink-900">Pricing</a>
          </nav>

          <div className="flex items-center gap-2">
            <Link
              to="/login"
              state={{ from }}
              className="hidden rounded-lg px-3 py-2 text-sm font-medium text-ink-700 hover:bg-ink-50 sm:inline-flex"
            >
              Log in
            </Link>
            <Link
              to="/register"
              state={{ from }}
              className="inline-flex items-center gap-1.5 rounded-lg bg-brand-600 px-3.5 py-2 text-sm font-semibold text-white shadow-sm hover:bg-brand-700"
            >
              Get started
              <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
        </div>
      </header>

      <main>
        {/* Hero */}
        <section className="relative overflow-hidden bg-ink-900 text-white">
          <div className="pointer-events-none absolute -right-32 -top-32 h-96 w-96 rounded-full bg-brand-600/25 blur-3xl" />
          <div className="pointer-events-none absolute -bottom-40 -left-32 h-96 w-96 rounded-full bg-brand-400/10 blur-3xl" />

          <div className="relative mx-auto grid max-w-6xl gap-12 px-4 py-16 sm:px-6 sm:py-20 lg:grid-cols-[1.05fr_0.95fr] lg:items-center lg:py-24">
            <div>
              <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-3 py-1 text-xs font-medium text-ink-200">
                <Sparkles className="h-3.5 w-3.5 text-brand-300" />
                Built for Indian construction sites
              </span>

              <h1 className="mt-5 max-w-2xl text-4xl font-semibold leading-[1.08] tracking-tight sm:text-5xl lg:text-6xl">
                Run every site, crew and rupee from one place.
              </h1>

              <p className="mt-5 max-w-xl text-base leading-7 text-ink-300 sm:text-lg">
                BuildTrack connects house owners, contractors, engineers and labour on one simple
                platform. Attendance, wages, materials, progress and payments stay in sync, even when
                the site is busy and the paperwork is not.
              </p>

              <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                <Link
                  to="/register"
                  state={{ from }}
                  className="inline-flex items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 py-3 text-sm font-semibold text-white shadow-lg shadow-brand-900/30 hover:bg-brand-400"
                >
                  Start with the pilot
                  <ArrowRight className="h-4 w-4" />
                </Link>
                <a
                  href="#features"
                  className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/15 bg-white/5 px-5 py-3 text-sm font-semibold text-white hover:bg-white/10"
                >
                  See what is inside
                </a>
              </div>

              <div className="mt-10 grid max-w-lg grid-cols-3 gap-6 border-t border-white/10 pt-6">
                <Stat value="5 roles" label="Owner to labour" />
                <Stat value="1 view" label="Site truth" />
                <Stat value="₹ INR" label="Money in rupees" />
              </div>

              <p className="mt-5 flex items-center gap-2 text-xs text-ink-400">
                <Smartphone className="h-4 w-4" />
                The app works in English and हिंदी. No card required for the pilot.
              </p>
            </div>

            {/* Product preview */}
            <div className="relative mx-auto w-full max-w-md lg:max-w-none">
              <div className="absolute inset-0 -rotate-2 rounded-3xl bg-brand-500/20 blur-2xl" />
              <div className="relative rounded-3xl border border-white/10 bg-white/[0.06] p-3 shadow-2xl backdrop-blur">
                <div className="rounded-2xl bg-ink-800 p-5">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-[11px] uppercase tracking-[0.18em] text-ink-400">Project overview</p>
                      <p className="mt-1 text-lg font-semibold text-white">Green Valley Villa</p>
                      <p className="mt-0.5 flex items-center gap-1 text-xs text-ink-400">
                        <Building2 className="h-3.5 w-3.5" />
                        Indore, Madhya Pradesh
                      </p>
                    </div>
                    <span className="rounded-full bg-emerald-400/15 px-2.5 py-1 text-[11px] font-medium text-emerald-300">
                      On track
                    </span>
                  </div>

                  <div className="mt-5 space-y-4">
                    <Progress label="Structure" value={72} />
                    <Progress label="Finishing" value={38} tone="bg-sky-400" />
                    <Progress label="Budget used" value={61} tone="bg-amber-400" />
                  </div>

                  <div className="mt-5 rounded-xl border border-white/10 bg-white/5 p-4">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-ink-300">Today&apos;s attendance</span>
                      <span className="font-semibold text-white">18 / 22</span>
                    </div>
                    <div className="mt-3 flex -space-x-2">
                      {['RK', 'SP', 'AM', 'VK', 'JS', 'MD'].map((initials) => (
                        <span
                          key={initials}
                          className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-ink-800 bg-brand-600 text-[10px] font-semibold text-white"
                        >
                          {initials}
                        </span>
                      ))}
                      <span className="flex h-8 w-8 items-center justify-center rounded-full border-2 border-ink-800 bg-ink-700 text-[10px] font-semibold text-ink-200">
                        +12
                      </span>
                    </div>
                  </div>

                  <div className="mt-4 grid grid-cols-2 gap-3">
                    <MiniStat label="Wages this week" value="₹84,500" />
                    <MiniStat label="Material spend" value="₹2.4 L" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Trust strip */}
        <section className="border-b border-ink-100 bg-ink-50">
          <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-center gap-x-8 gap-y-3 px-4 py-5 text-xs font-medium text-ink-500 sm:px-6 sm:text-sm">
            <span className="flex items-center gap-2"><CalendarCheck className="h-4 w-4 text-brand-600" /> Daily attendance</span>
            <span className="flex items-center gap-2"><IndianRupee className="h-4 w-4 text-brand-600" /> Wages and payments</span>
            <span className="flex items-center gap-2"><Package className="h-4 w-4 text-brand-600" /> Materials and stock</span>
            <span className="flex items-center gap-2"><Camera className="h-4 w-4 text-brand-600" /> Progress photos</span>
            <span className="flex items-center gap-2"><ShieldCheck className="h-4 w-4 text-brand-600" /> Role-based access</span>
          </div>
        </section>

        {/* Features */}
        <section id="features" className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">Everything on site</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl">
              Less paperwork. More control.
            </h2>
            <p className="mt-4 text-base leading-7 text-ink-500">
              BuildTrack brings the records that usually live in notebooks, spreadsheets and WhatsApp
              groups into one place the whole project can trust.
            </p>
          </div>

          <div className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {features.map(({ icon: Icon, title, text }) => (
              <article key={title} className="rounded-2xl border border-ink-100 bg-white p-5 shadow-sm transition-shadow hover:shadow-md">
                <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-50 text-brand-700">
                  <Icon className="h-5 w-5" />
                </span>
                <h3 className="mt-4 text-base font-semibold text-ink-900">{title}</h3>
                <p className="mt-2 text-sm leading-6 text-ink-500">{text}</p>
              </article>
            ))}
          </div>
        </section>

        {/* Roles */}
        <section id="roles" className="bg-ink-50">
          <div className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
            <div className="max-w-2xl">
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">One project, the right view</p>
              <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl">
                Built for every person on the site.
              </h2>
              <p className="mt-4 text-base leading-7 text-ink-500">
                Each role gets a focused workspace. Nobody has to scroll through information that is
                not theirs to manage.
              </p>
            </div>

            <div className="mt-10 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {roles.map(({ icon: Icon, title, text }) => (
                <article key={title} className="rounded-2xl border border-ink-200 bg-white p-5">
                  <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-ink-900 text-white">
                    <Icon className="h-5 w-5" />
                  </span>
                  <h3 className="mt-4 text-base font-semibold text-ink-900">{title}</h3>
                  <p className="mt-2 text-sm leading-6 text-ink-500">{text}</p>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* How it works */}
        <section id="how" className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
          <div className="grid gap-10 lg:grid-cols-[0.9fr_1.1fr] lg:items-center">
            <div>
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">Get running in a day</p>
              <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl">
                From first login to first site record.
              </h2>
              <p className="mt-4 text-base leading-7 text-ink-500">
                No long setup project. Create an account, add the project and start recording what
                happens on site. The app is designed for a phone in one hand and a site in front of you.
              </p>
              <div className="mt-6 flex flex-wrap gap-3 text-sm text-ink-600">
                <span className="inline-flex items-center gap-2 rounded-lg bg-ink-50 px-3 py-2">
                  <CheckCircle2 className="h-4 w-4 text-brand-600" /> Email verification
                </span>
                <span className="inline-flex items-center gap-2 rounded-lg bg-ink-50 px-3 py-2">
                  <CheckCircle2 className="h-4 w-4 text-brand-600" /> Role-based access
                </span>
                <span className="inline-flex items-center gap-2 rounded-lg bg-ink-50 px-3 py-2">
                  <CheckCircle2 className="h-4 w-4 text-brand-600" /> CSV exports
                </span>
              </div>
            </div>

            <div className="space-y-3">
              {steps.map(({ icon: Icon, title, text }, index) => (
                <article key={title} className="flex gap-4 rounded-2xl border border-ink-100 bg-white p-5 shadow-sm">
                  <div className="flex flex-col items-center">
                    <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-600 text-white">
                      <Icon className="h-5 w-5" />
                    </span>
                    <span className="mt-2 text-xs font-semibold text-ink-400">0{index + 1}</span>
                  </div>
                  <div>
                    <h3 className="text-base font-semibold text-ink-900">{title}</h3>
                    <p className="mt-1.5 text-sm leading-6 text-ink-500">{text}</p>
                  </div>
                </article>
              ))}
            </div>
          </div>
        </section>

        {/* AI */}
        <section id="ai" className="bg-ink-900 text-white">
          <div className="mx-auto grid max-w-6xl gap-10 px-4 py-16 sm:px-6 sm:py-20 lg:grid-cols-2 lg:items-center">
            <div>
              <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-3 py-1 text-xs font-medium text-ink-200">
                <Brain className="h-3.5 w-3.5 text-brand-300" />
                AI Intelligence
              </span>
              <h2 className="mt-5 max-w-xl text-3xl font-semibold tracking-tight sm:text-4xl">
                A second set of eyes on every site photo.
              </h2>
              <p className="mt-4 max-w-xl text-base leading-7 text-ink-300">
                Upload a site photo and get a structured first look. BuildTrack helps you spot
                missing safety gear, visible cracks and cost signals before they become surprises.
              </p>

              <ul className="mt-6 space-y-3 text-sm text-ink-200">
                {[
                  'PPE check for helmets and safety vests',
                  'Crack and surface inspection with next-step guidance',
                  'INR project valuation ranges from project details',
                  'Weather-aware site advisories',
                ].map((item) => (
                  <li key={item} className="flex items-start gap-2.5">
                    <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-brand-300" />
                    {item}
                  </li>
                ))}
              </ul>

              <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                <Link
                  to="/register"
                  state={{ from }}
                  className="inline-flex items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 py-3 text-sm font-semibold text-white hover:bg-brand-400"
                >
                  Try it in the app
                  <ArrowRight className="h-4 w-4" />
                </Link>
                <a
                  href={`mailto:${SUPPORT_EMAIL}?subject=BuildTrack%20AI%20demo`}
                  className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/15 bg-white/5 px-5 py-3 text-sm font-semibold text-white hover:bg-white/10"
                >
                  Ask for a demo
                </a>
              </div>
            </div>

            <div className="rounded-3xl border border-white/10 bg-white/[0.06] p-5 backdrop-blur">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-sm font-medium">
                  <Camera className="h-4 w-4 text-brand-300" />
                  Site photo analysis
                </div>
                <span className="rounded-full bg-brand-500/20 px-2.5 py-1 text-[11px] font-medium text-brand-200">
                  Demo result
                </span>
              </div>

              <div className="mt-4 grid aspect-[4/3] place-items-center overflow-hidden rounded-2xl border border-white/10 bg-ink-800">
                <div className="relative h-full w-full">
                  <div
                    className="absolute inset-0"
                    style={{
                      backgroundImage:
                        'linear-gradient(rgba(255,255,255,0.04) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.04) 1px, transparent 1px)',
                      backgroundSize: '28px 28px',
                    }}
                  />
                  <div className="absolute left-[16%] top-[22%] h-[38%] w-[26%] rounded-md border-2 border-amber-400/80" />
                  <div className="absolute right-[18%] top-[46%] h-[22%] w-[30%] rounded-md border-2 border-brand-400/80" />
                  <div className="absolute left-1/2 top-1/2 h-20 w-20 -translate-x-1/2 -translate-y-1/2 rounded-full border border-brand-400/40" />
                  <div className="absolute inset-x-4 bottom-4 rounded-xl bg-ink-900/90 p-3 text-xs text-ink-200">
                    <p className="font-medium text-white">2 findings need attention</p>
                    <p className="mt-1 text-ink-400">1 PPE issue · 1 surface crack · confidence 86%</p>
                  </div>
                </div>
              </div>

              <p className="mt-4 text-xs leading-5 text-ink-400">
                AI results are decision support only. Always verify safety or structural findings with
                a qualified engineer before acting on them.
              </p>
            </div>
          </div>
        </section>

        {/* Pricing */}
        <section id="pricing" className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
          <div className="max-w-2xl">
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">Start small</p>
            <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl">
              A pilot that fits a real site.
            </h2>
            <p className="mt-4 text-base leading-7 text-ink-500">
              Use BuildTrack with one project before rolling it out to the whole team. We would rather
              earn your trust on a real site than lock you into a long contract.
            </p>
          </div>

          <div className="mt-10 grid gap-4 lg:grid-cols-[1.15fr_0.85fr]">
            <div className="rounded-3xl border border-brand-200 bg-brand-50 p-6 sm:p-8">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-sm font-semibold text-brand-800">Pilot plan</p>
                  <p className="mt-1 text-3xl font-semibold tracking-tight text-ink-900">Free to evaluate</p>
                </div>
                <span className="rounded-full bg-white px-3 py-1 text-xs font-semibold text-brand-700 ring-1 ring-brand-200">
                  No card required
                </span>
              </div>

              <ul className="mt-6 grid gap-3 text-sm text-ink-700 sm:grid-cols-2">
                {[
                  'One live project to start',
                  'All five roles',
                  'Attendance, wages and payments',
                  'Materials, stock and purchase orders',
                  'Progress reports and site photos',
                  'Email support during the pilot',
                ].map((item) => (
                  <li key={item} className="flex items-start gap-2">
                    <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-brand-600" />
                    {item}
                  </li>
                ))}
              </ul>

              <div className="mt-7 flex flex-col gap-3 sm:flex-row">
                <Link
                  to="/register"
                  state={{ from }}
                  className="inline-flex items-center justify-center gap-2 rounded-xl bg-brand-600 px-5 py-3 text-sm font-semibold text-white hover:bg-brand-700"
                >
                  Create an account
                  <ArrowRight className="h-4 w-4" />
                </Link>
                <a
                  href={`mailto:${SUPPORT_EMAIL}?subject=BuildTrack%20pilot`}
                  className="inline-flex items-center justify-center gap-2 rounded-xl border border-brand-300 bg-white px-5 py-3 text-sm font-semibold text-brand-800 hover:bg-brand-100"
                >
                  Talk to us first
                </a>
              </div>
            </div>

            <div className="rounded-3xl border border-ink-200 bg-white p-6 sm:p-8">
              <p className="text-sm font-semibold text-ink-900">What happens after the pilot?</p>
              <p className="mt-2 text-sm leading-6 text-ink-500">
                We are preparing paid plans for teams that need more projects, more storage and
                priority support. Your pilot feedback shapes the pricing.
              </p>
              <div className="mt-5 rounded-xl bg-ink-50 p-4 text-xs leading-5 text-ink-500">
                BuildTrack records payments you make offline. It is not a bank, escrow service or
                payment processor, and it does not move money.
              </div>
            </div>
          </div>
        </section>

        {/* FAQ */}
        <section className="bg-ink-50">
          <div className="mx-auto max-w-4xl px-4 py-16 sm:px-6 sm:py-20">
            <div className="text-center">
              <p className="text-sm font-semibold uppercase tracking-[0.18em] text-brand-600">Questions</p>
              <h2 className="mt-3 text-3xl font-semibold tracking-tight text-ink-900 sm:text-4xl">
                Before you start.
              </h2>
            </div>

            <div className="mt-8 divide-y divide-ink-200 overflow-hidden rounded-2xl border border-ink-200 bg-white">
              {faqs.map(({ q, a }) => (
                <details key={q} className="group p-5">
                  <summary className="flex cursor-pointer list-none items-center justify-between gap-4 text-sm font-semibold text-ink-900">
                    {q}
                    <span className="text-lg font-normal text-ink-400 transition-transform group-open:rotate-45">+</span>
                  </summary>
                  <p className="mt-2.5 text-sm leading-6 text-ink-500">{a}</p>
                </details>
              ))}
            </div>
          </div>
        </section>

        {/* Final CTA */}
        <section className="mx-auto max-w-6xl px-4 py-16 sm:px-6 sm:py-20">
          <div className="overflow-hidden rounded-3xl bg-ink-900 px-6 py-10 text-center text-white sm:px-10 sm:py-14">
            <h2 className="mx-auto max-w-2xl text-3xl font-semibold tracking-tight sm:text-4xl">
              Put the next site on a better system.
            </h2>
            <p className="mx-auto mt-4 max-w-xl text-base leading-7 text-ink-300">
              Start with one project. Bring your contractor, engineer and crew into the same view.
            </p>
            <div className="mt-7 flex flex-col justify-center gap-3 sm:flex-row">
              <Link
                to="/register"
                state={{ from }}
                className="inline-flex items-center justify-center gap-2 rounded-xl bg-brand-500 px-5 py-3 text-sm font-semibold text-white hover:bg-brand-400"
              >
                Create your account
                <ArrowRight className="h-4 w-4" />
              </Link>
              <Link
                to="/login"
                state={{ from }}
                className="inline-flex items-center justify-center gap-2 rounded-xl border border-white/15 bg-white/5 px-5 py-3 text-sm font-semibold text-white hover:bg-white/10"
              >
                Log in
              </Link>
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t border-ink-200 bg-white">
        <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 sm:px-6 md:grid-cols-[1.4fr_1fr_1fr]">
          <div>
            <Logo />
            <p className="mt-3 max-w-sm text-sm leading-6 text-ink-500">
              Construction management for house owners, contractors, engineers and labour across India.
            </p>
            <p className="mt-4 text-xs text-ink-400">
              BuildTrack is a record-keeping tool, not a bank or payment processor.
            </p>
          </div>

          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-ink-400">Product</p>
            <nav className="mt-3 grid gap-2 text-sm text-ink-600">
              <a href="#features" className="hover:text-ink-900">Features</a>
              <a href="#how" className="hover:text-ink-900">How it works</a>
              <a href="#ai" className="hover:text-ink-900">AI intelligence</a>
              <a href="#pricing" className="hover:text-ink-900">Pilot pricing</a>
            </nav>
          </div>

          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.16em] text-ink-400">Account</p>
            <nav className="mt-3 grid gap-2 text-sm text-ink-600">
              <Link to="/login" state={{ from }} className="hover:text-ink-900">Log in</Link>
              <Link to="/register" state={{ from }} className="hover:text-ink-900">Create account</Link>
              <a href={`mailto:${SUPPORT_EMAIL}`} className="hover:text-ink-900">Contact support</a>
            </nav>
          </div>
        </div>

        <div className="border-t border-ink-100">
          <div className="mx-auto flex max-w-6xl flex-col gap-3 px-4 py-5 sm:flex-row sm:items-center sm:justify-between sm:px-6">
            <p className="text-xs text-ink-400">
              © {new Date().getFullYear()} BuildTrack. All rights reserved.
            </p>
            <LegalLinks />
          </div>
        </div>
      </footer>
    </div>
  )
}
