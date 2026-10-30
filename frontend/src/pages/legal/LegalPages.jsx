import { Link } from 'react-router-dom'
import LegalShell from '../../components/LegalShell'
import { SUPPORT_EMAIL } from '../../config'

function Section({ title, children }) {
  return (
    <section className="space-y-2">
      <h2 className="text-base font-semibold text-ink-900">{title}</h2>
      {children}
    </section>
  )
}

function List({ items }) {
  return (
    <ul className="list-disc space-y-1.5 pl-5">
      {items.map((item) => <li key={item}>{item}</li>)}
    </ul>
  )
}

const Contact = () => (
  <a href={`mailto:${SUPPORT_EMAIL}`} className="font-medium text-brand-600 hover:underline">{SUPPORT_EMAIL}</a>
)

export function PrivacyPolicy() {
  return (
    <LegalShell title="Privacy policy">
      <p>
        BuildTrack helps house owners, contractors, engineers and labourers coordinate construction
        work and keep site records in one place. This policy explains what we collect during the
        pilot, why we collect it and how to contact us.
      </p>

      <Section title="Information we collect">
        <List items={[
          'Account details: name, mobile number, optional email address, address and role.',
          'Project records: project title, location, scope, budget, dates, drawings or notes you add.',
          'Site records: tasks, milestones, progress updates, photos, material requests, purchase orders and stock movements.',
          'Money records: payments, expenses, attendance, wage rates and wage payments entered by the project participants.',
          'Security and support data: login events, audit events and technical logs such as request id, IP address, browser type and timestamps.',
        ]} />
        <p>
          We do not ask for Aadhaar numbers, bank card details or government identity documents in
          the pilot. Please do not upload them as photos or notes.
        </p>
      </Section>

      <Section title="How we use it">
        <List items={[
          'To create and secure your account, send one-time codes and reset passwords.',
          'To show the right project information to the right owner, contractor, engineer or labourer.',
          'To calculate attendance, wages, payments, expenses and stock balances you ask us to track.',
          'To investigate errors, abuse, duplicate accounts and security incidents.',
          'To comply with applicable law and lawful requests from authorities.',
        ]} />
        <p>We do not sell personal data or use it for third-party advertising.</p>
      </Section>

      <Section title="Who can see it">
        <p>
          Project participants can see the parts of a project relevant to their role. For example,
          an owner sees finance and progress, while an engineer sees site records without supplier
          costs or purchase-order costs. Admins can access platform data for support and safety.
        </p>
        <p>We use service providers for SMS/email delivery, hosting and backups. They process data only to provide those services.</p>
      </Section>

      <Section title="Retention">
        <List items={[
          'Active account and project data is kept while your account is active and while needed for the project record.',
          'One-time codes expire after five minutes and are not retained for marketing.',
          'Security and audit logs are kept for up to 24 months.',
          'Read in-app notifications are kept for up to 90 days; unread notifications are reviewed during the pilot.',
          'Backups are rotated and kept for up to 30 days.',
          'When an account is deleted, we remove or anonymise personal data unless we must keep a record for tax, dispute or legal reasons.',
        ]} />
      </Section>

      <Section title="Your choices">
        <p>
          You can ask to access, correct or delete your personal data, or withdraw consent for
          optional communications. Some records may need to be retained for legal or accounting
          reasons. Write to <Contact /> and we will respond within a reasonable time.
        </p>
      </Section>

      <Section title="Security">
        <p>
          Passwords are stored as one-way hashes. Sessions use signed tokens, role checks are applied
          on the server and sensitive actions are written to an audit trail. Production traffic must
          use HTTPS. No system is perfectly secure, so report suspected incidents promptly.
        </p>
      </Section>

      <Section title="Children">
        <p>BuildTrack is for adults working on construction projects. It is not intended for anyone under 18.</p>
      </Section>

      <Section title="Contact">
        <p>
          Privacy questions and data requests: <Contact />. For security incidents, see the{' '}
          <Link to="/support" className="font-medium text-brand-600 hover:underline">Support page</Link>.
        </p>
      </Section>
    </LegalShell>
  )
}

export function TermsOfUse() {
  return (
    <LegalShell title="Terms of use">
      <p>
        These terms apply to the BuildTrack pilot. By creating an account or using the service, you
        agree to them. If you do not agree, do not use the service.
      </p>

      <Section title="The service">
        <p>
          BuildTrack is a record-keeping and coordination tool for construction projects. It is not a
          bank, escrow service, payment processor, insurer or a substitute for professional
          engineering, legal or safety advice. Payments shown in the app are records entered by
          users; they do not prove that money actually moved.
        </p>
      </Section>

      <Section title="Your account">
        <List items={[
          'Give accurate information and keep your phone number and email up to date.',
          'Keep your password and one-time codes private. You are responsible for activity under your account.',
          'Use the role that matches what you actually do. Do not create accounts to impersonate another person.',
          'Tell us promptly if you believe your account is compromised.',
        ]} />
      </Section>

      <Section title="Project records and money">
        <p>
          Owners, contractors and engineers are responsible for the accuracy of project, attendance,
          wage, payment, expense and stock records they enter. Reconcile money records with your own
          bank, cash book and invoices. BuildTrack does not resolve payment disputes between users.
        </p>
      </Section>

      <Section title="Acceptable use">
        <List items={[
          'Do not upload illegal, abusive, misleading or infringing content.',
          'Do not try to access another user’s project, account or data without permission.',
          'Do not probe, scrape, overload or bypass the service’s security controls.',
          'Do not upload Aadhaar numbers, card details or other unnecessary sensitive identity documents.',
        ]} />
      </Section>

      <Section title="Content you provide">
        <p>
          You keep ownership of your content. You give us permission to store, process and show it to
          the project participants and service providers needed to run BuildTrack. You are responsible
          for having permission to upload photos or notes that include other people.
        </p>
      </Section>

      <Section title="Availability and changes">
        <p>
          The pilot is provided as-is while we improve it. Features, limits and availability may
          change. We may suspend an account or remove content that risks other users, the service or
          the law. We aim to give reasonable notice when we make material changes to these terms.
        </p>
      </Section>

      <Section title="Liability">
        <p>
          To the extent permitted by law, BuildTrack is not liable for indirect or consequential loss,
          lost profits, or losses caused by inaccurate records, off-platform payments or site
          decisions. Nothing here limits rights that cannot be limited under applicable law.
        </p>
      </Section>

      <Section title="Ending use">
        <p>
          You can stop using the service at any time and ask us to delete your account. We may keep
          records where required for legal, tax, dispute or security reasons.
        </p>
      </Section>

      <Section title="Governing law and contact">
        <p>
          These terms are governed by the laws of India. Questions: <Contact />. See also our{' '}
          <Link to="/privacy" className="font-medium text-brand-600 hover:underline">Privacy policy</Link>.
        </p>
      </Section>
    </LegalShell>
  )
}

export function Support() {
  return (
    <LegalShell title="Support">
      <p>
        Need help with your account, a project record or a payment entry? Contact the BuildTrack
        pilot support team and include the project name, your role and the request id shown in the
        error message if you have one.
      </p>

      <div className="rounded-xl border border-brand-200 bg-brand-50 p-4">
        <p className="font-semibold text-brand-800">Support contact</p>
        <p className="mt-1 text-brand-800"><Contact /></p>
        <p className="mt-1 text-xs text-brand-700">Target response: within one business day.</p>
      </div>

      <Section title="Security incidents">
        <p>
          If you suspect unauthorized access, a leaked password or a data exposure, email <Contact />
          {' '}with the subject line “Security incident”. Do not include passwords or one-time codes.
          We will acknowledge the report and investigate.
        </p>
      </Section>

      <Section title="Data requests">
        <p>
          For access, correction or deletion requests, email <Contact /> from the mobile number or
          email registered on your account. We may ask you to verify ownership before acting.
        </p>
      </Section>

      <Section title="Urgent site issues">
        <p>
          BuildTrack support is not an emergency service. For a fire, injury, structural emergency or
          other immediate danger, contact local emergency services and the people on site first.
        </p>
      </Section>

      <Section title="Policies">
        <p>
          Read the <Link to="/privacy" className="font-medium text-brand-600 hover:underline">Privacy policy</Link>
          {' '}and <Link to="/terms" className="font-medium text-brand-600 hover:underline">Terms of use</Link>.
        </p>
      </Section>
    </LegalShell>
  )
}
