import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <section className="rounded-2xl bg-white p-6 text-center shadow-sm ring-1 ring-slate-200 sm:p-10">
      <p className="text-sm font-semibold uppercase tracking-wide text-emerald-700">404</p>
      <h1 className="mt-2 text-3xl font-semibold text-slate-900 sm:text-4xl">Page Not Found</h1>
      <p className="mt-3 text-slate-700">The page you requested does not exist or has been moved.</p>
      <Link
        to="/"
        className="mt-6 inline-block rounded-full bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700"
      >
        Back to Home
      </Link>
    </section>
  )
}
