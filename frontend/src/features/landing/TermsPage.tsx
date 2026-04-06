import { Link } from 'react-router-dom'

export function TermsPage() {
  return (
    <div className="min-h-screen bg-bg-base text-text-primary">
      <nav className="sticky top-0 z-50 bg-bg-surface/80 backdrop-blur border-b border-bg-border">
        <div className="max-w-4xl mx-auto px-6 h-16 flex items-center justify-between">
          <Link to="/" className="text-xl font-bold text-brand tracking-wide">⬡ Delivra</Link>
          <Link to="/" className="text-sm text-text-secondary hover:text-text-primary transition-colors">← Back</Link>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-6 py-16">
        <h1 className="text-3xl font-bold mb-2">Public Offer Agreement</h1>
        <p className="text-text-muted text-sm mb-10">Last updated: April 6, 2026</p>

        <div className="prose max-w-none space-y-8 text-text-secondary leading-relaxed">

          <Section title="1. General Provisions">
            <p>
              This Public Offer Agreement (hereinafter — the "Agreement") is an official offer by Delivra
              (hereinafter — the "Service") addressed to any legal entity or individual entrepreneur
              (hereinafter — the "Client") to enter into a contract for the use of the Delivra platform
              under the terms set forth below.
            </p>
            <p>
              Acceptance of this offer is deemed to occur at the moment the Client completes the registration
              procedure on the website <strong>delivra.site</strong>. By registering, the Client confirms
              that they have read, understood, and accepted all terms of this Agreement in full.
            </p>
          </Section>

          <Section title="2. Subject of the Agreement">
            <p>
              The Service provides the Client with access to the Delivra web platform — a SaaS solution for
              delivery management, which includes:
            </p>
            <ul>
              <li>Creation and management of delivery tasks</li>
              <li>Real-time driver GPS tracking and navigation</li>
              <li>Route optimization using HERE Maps API</li>
              <li>Internal messaging between dispatchers and drivers</li>
              <li>Reporting and analytics tools</li>
              <li>Multi-user access with role-based permissions</li>
            </ul>
            <p>
              Access is provided on a subscription basis according to the pricing plan chosen by the Client.
            </p>
          </Section>

          <Section title="3. Subscription Plans and Payment">
            <p>The Service offers the following subscription plans:</p>
            <ul>
              <li>
                <strong>Trial</strong> — free access for 14 calendar days from the date of registration,
                limited to 5 driver accounts. No payment required.
              </li>
              <li>
                <strong>Pro</strong> — full access with unlimited drivers. Billed monthly at the rate
                published on the pricing page at the time of subscription activation.
              </li>
              <li>
                <strong>Enterprise</strong> — custom terms negotiated individually. Contact:
                sales@delivra.site.
              </li>
            </ul>
            <p>
              Payment is made by bank transfer or card payment via the integrated payment processor.
              The subscription renews automatically unless cancelled at least 3 business days before the
              renewal date.
            </p>
          </Section>

          <Section title="4. Refund Policy">
            <p>
              The Client may request a full refund within <strong>7 calendar days</strong> from the date of
              the first paid subscription payment, provided that the platform features have not been
              substantially used (fewer than 10 delivery tasks created).
            </p>
            <p>
              Refund requests must be submitted to support@delivra.site with the subject line
              "Refund Request — [Company Name]". Refunds are processed within 10 business days.
            </p>
            <p>
              No refunds are issued for partial subscription periods after the 7-day window has elapsed.
            </p>
          </Section>

          <Section title="5. Client Obligations">
            <ul>
              <li>Provide accurate registration information and keep it up to date.</li>
              <li>Maintain confidentiality of login credentials and notify the Service immediately of any unauthorized access.</li>
              <li>Use the platform solely for lawful business purposes.</li>
              <li>Not attempt to reverse-engineer, decompile, or interfere with the platform's infrastructure.</li>
              <li>Ensure that data entered into the platform (driver names, addresses, contacts) complies with applicable data protection laws.</li>
            </ul>
          </Section>

          <Section title="6. Service Obligations">
            <ul>
              <li>Provide access to the platform in accordance with the chosen subscription plan.</li>
              <li>Maintain platform availability of at least 99% per calendar month (excluding scheduled maintenance).</li>
              <li>Notify Clients of scheduled maintenance at least 24 hours in advance.</li>
              <li>Store Client data securely and not transfer it to third parties except as required by law or described in the Privacy Policy.</li>
            </ul>
          </Section>

          <Section title="7. Limitation of Liability">
            <p>
              The Service is provided "as is". The Service shall not be liable for:
            </p>
            <ul>
              <li>Losses arising from incorrect routing data provided by third-party map providers (HERE Maps).</li>
              <li>Interruptions caused by force majeure events, internet outages, or third-party infrastructure failures.</li>
              <li>Loss of data due to the Client's failure to maintain adequate backups where such tools are available.</li>
              <li>Any indirect, incidental, or consequential damages arising from the use or inability to use the platform.</li>
            </ul>
            <p>
              The total liability of the Service under this Agreement shall not exceed the amount paid by the Client
              in the 3 months preceding the claim.
            </p>
          </Section>

          <Section title="8. Intellectual Property">
            <p>
              All rights to the Delivra platform, including its code, design, brand elements, and documentation,
              belong exclusively to the Service. The Client receives a non-exclusive, non-transferable right to use
              the platform within the scope defined by this Agreement.
            </p>
            <p>
              Data entered by the Client (delivery tasks, addresses, employee information) remains the property of the Client.
            </p>
          </Section>

          <Section title="9. Term and Termination">
            <p>
              This Agreement comes into effect upon registration and remains in force for the duration of the active
              subscription. Either party may terminate the Agreement:
            </p>
            <ul>
              <li>The Client — by cancelling the subscription and ceasing to use the platform.</li>
              <li>The Service — in case of material breach of this Agreement by the Client, with 5 business days' prior notice.</li>
            </ul>
            <p>
              Upon termination, the Client's data will be retained for 30 days, after which it will be permanently deleted.
              The Client may request an export of their data during this period.
            </p>
          </Section>

          <Section title="10. Governing Law">
            <p>
              This Agreement shall be governed by and construed in accordance with applicable law.
              Any disputes arising out of or in connection with this Agreement shall be resolved through
              negotiation, and if unsuccessful, through the competent court at the registered address of the Service.
            </p>
          </Section>

          <Section title="11. Contact Information">
            <p>
              For questions regarding this Agreement, please contact:<br />
              Email: <a href="mailto:legal@delivra.site" className="text-brand hover:underline">legal@delivra.site</a><br />
              Support: <a href="mailto:support@delivra.site" className="text-brand hover:underline">support@delivra.site</a>
            </p>
          </Section>

        </div>
      </div>

      <Footer />
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

function Footer() {
  return (
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
  )
}
