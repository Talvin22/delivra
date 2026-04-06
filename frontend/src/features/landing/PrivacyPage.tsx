import { Link } from 'react-router-dom'

export function PrivacyPage() {
  return (
    <div className="min-h-screen bg-bg-base text-text-primary">
      <nav className="sticky top-0 z-50 bg-bg-surface/80 backdrop-blur border-b border-bg-border">
        <div className="max-w-4xl mx-auto px-6 h-16 flex items-center justify-between">
          <Link to="/" className="text-xl font-bold text-brand tracking-wide">⬡ Delivra</Link>
          <Link to="/" className="text-sm text-text-secondary hover:text-text-primary transition-colors">← Back</Link>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-6 py-16">
        <h1 className="text-3xl font-bold mb-2">Privacy Policy</h1>
        <p className="text-text-muted text-sm mb-10">Last updated: April 6, 2026</p>

        <div className="space-y-8 text-text-secondary leading-relaxed">

          <Section title="1. Who We Are">
            <p>
              Delivra ("we", "us", "our") operates the delivery management platform available at
              <strong> delivra.site</strong>. This Privacy Policy explains how we collect, use, store,
              and protect personal data when you use our service.
            </p>
          </Section>

          <Section title="2. What Data We Collect">
            <p>We collect the following categories of personal data:</p>
            <ul>
              <li><strong>Account data:</strong> username, email address, hashed password, company name.</li>
              <li><strong>Usage data:</strong> delivery tasks, addresses, GPS coordinates of drivers during active navigation sessions, chat messages.</li>
              <li><strong>Technical data:</strong> IP address, browser type, device type, access timestamps (for security and rate limiting purposes).</li>
              <li><strong>Communication data:</strong> emails sent through the platform (password reset, notifications).</li>
            </ul>
            <p>We do <strong>not</strong> collect payment card details directly — payments are processed by a certified payment provider.</p>
          </Section>

          <Section title="3. How We Use Your Data">
            <ul>
              <li>To provide, maintain, and improve the Delivra platform.</li>
              <li>To authenticate users and protect accounts from unauthorized access.</li>
              <li>To send transactional emails (password reset, email verification, delivery notifications).</li>
              <li>To enforce our Terms of Service and prevent abuse.</li>
              <li>To generate aggregated, anonymized usage statistics (no individual identification).</li>
            </ul>
            <p>We do <strong>not</strong> sell personal data to third parties.</p>
          </Section>

          <Section title="4. GPS and Location Data">
            <p>
              GPS location data is collected from drivers <strong>only during active navigation sessions</strong>.
              This data is:
            </p>
            <ul>
              <li>Visible to the dispatcher assigned to the same company.</li>
              <li>Used exclusively for route tracking and off-route detection.</li>
              <li>Not shared with any third party other than HERE Maps (for route recalculation).</li>
              <li>Retained for 90 days after the session ends, then permanently deleted.</li>
            </ul>
          </Section>

          <Section title="5. Third-Party Services">
            <p>We use the following third-party providers who may process data on our behalf:</p>
            <ul>
              <li>
                <strong>HERE Maps</strong> — route calculation and geocoding. Addresses and GPS coordinates
                are transmitted to HERE Maps API. See HERE's privacy policy at developer.here.com.
              </li>
              <li>
                <strong>Gmail / SMTP</strong> — transactional email delivery (notifications, password reset).
                Only the recipient email address and message content are transmitted.
              </li>
            </ul>
          </Section>

          <Section title="6. Data Retention">
            <ul>
              <li>Account data is retained for the duration of the active subscription.</li>
              <li>After account deletion or subscription termination, data is retained for 30 days, then permanently deleted.</li>
              <li>Navigation session GPS data is deleted after 90 days.</li>
              <li>Password reset and email verification tokens expire within 1–24 hours and are invalidated after use.</li>
            </ul>
          </Section>

          <Section title="7. Your Rights">
            <p>You have the right to:</p>
            <ul>
              <li><strong>Access</strong> — request a copy of your personal data.</li>
              <li><strong>Rectification</strong> — correct inaccurate data through account settings.</li>
              <li><strong>Erasure</strong> — request deletion of your account and all associated data.</li>
              <li><strong>Portability</strong> — request an export of your data in a machine-readable format.</li>
              <li><strong>Objection</strong> — object to processing in certain circumstances.</li>
            </ul>
            <p>
              To exercise any of these rights, contact us at{' '}
              <a href="mailto:privacy@delivra.site" className="text-brand hover:underline">privacy@delivra.site</a>.
              We will respond within 30 days.
            </p>
          </Section>

          <Section title="8. Security">
            <p>
              We protect your data using industry-standard measures:
            </p>
            <ul>
              <li>All passwords are hashed using BCrypt — we never store plain-text passwords.</li>
              <li>Authentication uses short-lived JWT tokens (1 hour expiry) and refresh tokens.</li>
              <li>All data is transmitted over HTTPS.</li>
              <li>Access to the database is restricted to server infrastructure only.</li>
              <li>Secrets (API keys, database credentials) are managed via environment variables and never committed to source code.</li>
            </ul>
          </Section>

          <Section title="9. Cookies">
            <p>
              Delivra uses a single session cookie to maintain authentication state. This cookie:
            </p>
            <ul>
              <li>Contains a signed JWT token — no personal data in plain text.</li>
              <li>Is marked HttpOnly and Secure (in production).</li>
              <li>Expires when the session ends or the token expires.</li>
            </ul>
            <p>We do not use tracking cookies or third-party advertising cookies.</p>
          </Section>

          <Section title="10. Changes to This Policy">
            <p>
              We may update this Privacy Policy from time to time. Material changes will be communicated
              via email or a notice on the platform at least 14 days before taking effect.
              Continued use of the platform after the effective date constitutes acceptance of the updated policy.
            </p>
          </Section>

          <Section title="11. Contact">
            <p>
              For privacy-related questions or requests:<br />
              Email: <a href="mailto:privacy@delivra.site" className="text-brand hover:underline">privacy@delivra.site</a>
            </p>
          </Section>

        </div>
      </div>

      <footer className="border-t border-bg-border mt-16">
        <div className="max-w-4xl mx-auto px-6 py-8 flex flex-col sm:flex-row items-center justify-between gap-4 text-sm text-text-muted">
          <Link to="/" className="text-brand font-bold text-base">⬡ Delivra</Link>
          <div className="flex gap-6">
            <Link to="/terms" className="hover:text-text-primary">Terms</Link>
            <Link to="/privacy" className="hover:text-text-primary">Privacy</Link>
          </div>
          <span>© 2026 Delivra</span>
        </div>
      </footer>
    </div>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div>
      <h2 className="text-lg font-semibold text-text-primary mb-3">{title}</h2>
      <div className="space-y-3">{children}</div>
    </div>
  )
}
