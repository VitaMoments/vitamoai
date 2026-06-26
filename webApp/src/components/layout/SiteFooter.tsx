import { Link } from 'react-router-dom'
import { legalNavItems } from '@/utils/navigation'

export function SiteFooter() {
  return (
    <footer className="border-t border-slate-200 bg-slate-50">
      <div className="mx-auto w-full max-w-6xl px-4 py-8 sm:px-6">
        <div className="mb-4 flex flex-wrap gap-3">
          {legalNavItems.map((item) => (
            <Link
              key={item.to}
              to={item.to}
              className="rounded-full bg-white px-3 py-2 text-sm text-slate-700 ring-1 ring-slate-200 transition hover:bg-slate-100"
            >
              {item.label}
            </Link>
          ))}
        </div>
        <p className="text-sm text-slate-600">
          VitaMo helps people become a little healthier every day, with privacy and user control as core values.
        </p>
      </div>
    </footer>
  )
}
