import { Link } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'

const features = [
  {
    icon: '🗺️',
    title: 'Real-time Navigation',
    desc: 'GPS tracking with live traffic data. Drivers see their route update automatically when they go off-course.',
  },
  {
    icon: '🚛',
    title: 'Truck-optimized Routing',
    desc: 'Routes account for vehicle weight, height, and width — no surprises with low bridges or weight limits.',
  },
  {
    icon: '💬',
    title: 'Built-in Team Chat',
    desc: 'Real-time messaging between dispatchers and drivers, scoped per delivery task. No third-party apps needed.',
  },
  {
    icon: '📊',
    title: 'Reports & Analytics',
    desc: 'One-click Excel export of all delivery data. Filter by company, date range, driver, or status.',
  },
  {
    icon: '🤖',
    title: 'Smart Driver Assignment',
    desc: 'Weighted algorithm ranks available drivers by proximity, workload, success rate, and recency.',
  },
  {
    icon: '🏢',
    title: 'Multi-company Ready',
    desc: 'Full data isolation per company. Manage multiple tenants from one super-admin panel.',
  },
]

const plans = [
  {
    name: 'Trial',
    price: 'Free',
    period: '14 days',
    highlight: false,
    features: [
      'Up to 5 drivers',
      'Task management',
      'Basic navigation',
      'Team chat',
      'Email support',
    ],
    cta: 'Start free trial',
    href: '/register',
  },
  {
    name: 'Pro',
    price: '$49',
    period: 'per month',
    highlight: true,
    features: [
      'Unlimited drivers',
      'Truck-optimized routing',
      'Real-time traffic overlay',
      'Excel reports',
      'Smart driver recommendations',
      'Priority support',
    ],
    cta: 'Get started',
    href: '/register',
  },
  {
    name: 'Enterprise',
    price: 'Custom',
    period: 'contact us',
    highlight: false,
    features: [
      'Everything in Pro',
      'Multiple companies',
      'Super-admin panel',
      'Custom integrations',
      'Dedicated SLA',
      'Onboarding support',
    ],
    cta: 'Contact sales',
    href: 'mailto:sales@delivra.site',
  },
]

export function LandingPage() {
  const user = useAuthStore(s => s.user)
  const primaryRole = useAuthStore(s => s.primaryRole)

  const dashboardHref = () => {
    const role = primaryRole()
    if (role === 'DRIVER') return '/driver'
    if (role === 'DISPATCHER') return '/dispatcher'
    if (role === 'ADMIN' || role === 'SUPER_ADMIN') return '/admin'
    return '/login'
  }

  return (
    <div className="min-h-screen bg-bg-base text-text-primary">

      {/* ── Navbar ── */}
      <nav className="sticky top-0 z-50 bg-bg-surface/80 backdrop-blur border-b border-bg-border">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
          <span className="text-xl font-bold text-brand tracking-wide">⬡ Delivra</span>
          <div className="flex items-center gap-3">
            {user ? (
              <Link
                to={dashboardHref()}
                className="px-4 py-2 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-hover transition-colors"
              >
                Go to dashboard →
              </Link>
            ) : (
              <>
                <Link to="/login" className="text-sm text-text-secondary hover:text-text-primary transition-colors">
                  Sign in
                </Link>
                <Link
                  to="/register"
                  className="px-4 py-2 bg-brand text-white text-sm font-medium rounded-lg hover:bg-brand-hover transition-colors"
                >
                  Get started
                </Link>
              </>
            )}
          </div>
        </div>
      </nav>

      {/* ── Hero ── */}
      <section className="relative overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-brand/10 via-transparent to-transparent pointer-events-none" />
        <div className="max-w-6xl mx-auto px-6 py-28 text-center">
          <div className="inline-block px-3 py-1 rounded-full text-xs font-medium bg-brand/10 text-brand border border-brand/20 mb-6">
            Delivery management platform
          </div>
          <h1 className="text-5xl sm:text-6xl font-bold leading-tight mb-6 tracking-tight">
            Deliver smarter,<br />
            <span className="text-brand">not harder</span>
          </h1>
          <p className="text-lg text-text-secondary max-w-2xl mx-auto mb-10 leading-relaxed">
            Delivra gives dispatchers and drivers everything they need in one place —
            real-time navigation, team chat, route optimization, and reports.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/register"
              className="px-8 py-3.5 bg-brand text-white font-semibold rounded-xl hover:bg-brand-hover transition-colors text-sm"
            >
              Start free trial — no card required
            </Link>
            <Link
              to="/login"
              className="px-8 py-3.5 bg-bg-surface border border-bg-border text-text-primary font-semibold rounded-xl hover:bg-bg-raised transition-colors text-sm"
            >
              Sign in to your account
            </Link>
          </div>
        </div>
      </section>

      {/* ── Features ── */}
      <section className="max-w-6xl mx-auto px-6 py-24">
        <div className="text-center mb-14">
          <h2 className="text-3xl font-bold mb-3">Everything your team needs</h2>
          <p className="text-text-secondary max-w-xl mx-auto">
            Built for logistics companies that need reliability, speed, and clear communication.
          </p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {features.map(f => (
            <div
              key={f.title}
              className="bg-bg-surface border border-bg-border rounded-xl p-6 hover:border-brand/40 transition-colors"
            >
              <div className="text-3xl mb-4">{f.icon}</div>
              <h3 className="font-semibold text-base mb-2">{f.title}</h3>
              <p className="text-text-secondary text-sm leading-relaxed">{f.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ── How it works ── */}
      <section className="bg-bg-surface border-y border-bg-border">
        <div className="max-w-6xl mx-auto px-6 py-24">
          <div className="text-center mb-14">
            <h2 className="text-3xl font-bold mb-3">How it works</h2>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 text-center">
            {[
              { step: '1', title: 'Register your company', desc: 'Create an account in 30 seconds. No credit card needed for the trial.' },
              { step: '2', title: 'Add drivers & tasks', desc: 'Invite your team, create delivery tasks, and assign them with one click.' },
              { step: '3', title: 'Track in real time', desc: 'Watch drivers navigate, chat with them, and export reports at day end.' },
            ].map(item => (
              <div key={item.step} className="flex flex-col items-center">
                <div className="w-12 h-12 rounded-full bg-brand/10 border border-brand/30 flex items-center justify-center text-brand font-bold text-lg mb-4">
                  {item.step}
                </div>
                <h3 className="font-semibold mb-2">{item.title}</h3>
                <p className="text-text-secondary text-sm leading-relaxed">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ── Pricing ── */}
      <section id="pricing" className="max-w-6xl mx-auto px-6 py-24">
        <div className="text-center mb-14">
          <h2 className="text-3xl font-bold mb-3">Simple, transparent pricing</h2>
          <p className="text-text-secondary">Start free. Scale when you're ready.</p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 items-start">
          {plans.map(plan => (
            <div
              key={plan.name}
              className={`rounded-2xl border p-7 flex flex-col gap-6 ${
                plan.highlight
                  ? 'bg-brand text-white border-brand shadow-xl shadow-brand/20'
                  : 'bg-bg-surface border-bg-border'
              }`}
            >
              <div>
                <div className={`text-xs font-semibold uppercase tracking-widest mb-2 ${plan.highlight ? 'text-white/70' : 'text-brand'}`}>
                  {plan.name}
                </div>
                <div className="flex items-end gap-1">
                  <span className="text-4xl font-bold">{plan.price}</span>
                  <span className={`text-sm mb-1 ${plan.highlight ? 'text-white/70' : 'text-text-muted'}`}>
                    {plan.period}
                  </span>
                </div>
              </div>

              <ul className="space-y-2.5 flex-1">
                {plan.features.map(f => (
                  <li key={f} className="flex items-start gap-2 text-sm">
                    <span className={plan.highlight ? 'text-white' : 'text-success'}>✓</span>
                    <span className={plan.highlight ? 'text-white/90' : 'text-text-secondary'}>{f}</span>
                  </li>
                ))}
              </ul>

              <a
                href={plan.href}
                className={`block text-center py-2.5 rounded-xl text-sm font-semibold transition-colors ${
                  plan.highlight
                    ? 'bg-white text-brand hover:bg-white/90'
                    : 'bg-brand/10 text-brand border border-brand/20 hover:bg-brand/20'
                }`}
              >
                {plan.cta}
              </a>
            </div>
          ))}
        </div>
      </section>

      {/* ── CTA banner ── */}
      <section className="bg-brand/5 border-y border-brand/10">
        <div className="max-w-3xl mx-auto px-6 py-20 text-center">
          <h2 className="text-3xl font-bold mb-4">Ready to streamline your deliveries?</h2>
          <p className="text-text-secondary mb-8">
            Join logistics teams already using Delivra to save time and reduce errors.
          </p>
          <Link
            to="/register"
            className="inline-block px-8 py-3.5 bg-brand text-white font-semibold rounded-xl hover:bg-brand-hover transition-colors text-sm"
          >
            Create your free account →
          </Link>
        </div>
      </section>

      {/* ── Footer ── */}
      <footer className="border-t border-bg-border">
        <div className="max-w-6xl mx-auto px-6 py-10 flex flex-col sm:flex-row items-center justify-between gap-4">
          <span className="text-lg font-bold text-brand">⬡ Delivra</span>
          <div className="flex gap-6 text-sm text-text-muted">
            <Link to="/login" className="hover:text-text-primary transition-colors">Sign in</Link>
            <Link to="/register" className="hover:text-text-primary transition-colors">Register</Link>
            <a href="#pricing" className="hover:text-text-primary transition-colors">Pricing</a>
          </div>
          <span className="text-sm text-text-muted">© 2026 Delivra. All rights reserved.</span>
        </div>
      </footer>

    </div>
  )
}
