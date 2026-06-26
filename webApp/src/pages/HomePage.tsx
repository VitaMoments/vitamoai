import { Link } from 'react-router-dom'

export function HomePage() {
  return (
    <section className="space-y-6">
      <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 sm:p-10">
        <p className="mb-3 inline-block rounded-full bg-emerald-100 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-800">
          Privacy-first and European
        </p>
        <h1 className="text-3xl font-semibold leading-tight text-slate-900 sm:text-5xl">
          Help People Live A Little Healthier Every Day
        </h1>
        <p className="mt-4 max-w-2xl text-base leading-relaxed text-slate-700 sm:text-lg">
          VitaMo is a social platform where people support each other in physical health, mental wellbeing, mindset,
          finances, personal growth, and healthy habits while staying in control of their personal data.
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link
            to="/features"
            className="rounded-full bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700"
          >
            Explore Features
          </Link>
          <Link
            to="/about"
            className="rounded-full bg-white px-5 py-3 text-sm font-semibold text-slate-800 ring-1 ring-slate-300 transition hover:bg-slate-100"
          >
            Learn About VitaMo
          </Link>
        </div>
      </div>
      <div className="grid gap-4 sm:grid-cols-3">
        <article className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-semibold text-slate-900">Privacy by default</h2>
          <p className="mt-2 text-sm text-slate-700">Collect only what is needed. Users decide what they share.</p>
        </article>
        <article className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-semibold text-slate-900">Built for Europe</h2>
          <p className="mt-2 text-sm text-slate-700">Designed with GDPR principles and data minimization in mind.</p>
        </article>
        <article className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
          <h2 className="text-lg font-semibold text-slate-900">User control first</h2>
          <p className="mt-2 text-sm text-slate-700">
            Clear settings for profile visibility, notifications, and personal data.
          </p>
        </article>
      </div>
    </section>
  )
}
