export function CookiePolicyPage() {
  return (
    <section className="space-y-4 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 sm:p-10">
      <h1 className="text-3xl font-semibold text-slate-900 sm:text-4xl">Cookie Policy</h1>
      <p className="text-slate-700">
        VitaMo uses essential cookies required for authentication, security, and core platform functionality.
      </p>
      <p className="text-slate-700">
        We do not add optional tracking or analytics cookies without explicit user approval.
      </p>
      <p className="text-slate-700">
        Authentication is handled with secure HttpOnly cookies to reduce client-side exposure risks.
      </p>
    </section>
  )
}
