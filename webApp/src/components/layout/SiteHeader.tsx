import { NavLink } from 'react-router-dom'
import { mainNavItems } from '@/utils/navigation'

export function SiteHeader() {
  return (
    <header className="border-b border-slate-200 bg-white/95 backdrop-blur">
      <div className="mx-auto w-full max-w-6xl px-4 py-4 sm:px-6">
        <div className="flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <NavLink to="/" className="text-xl font-semibold text-slate-900">
              VitaMo
            </NavLink>
            <p className="text-xs text-slate-500 sm:text-sm">European Privacy-First Platform</p>
          </div>
          <nav aria-label="Main navigation" className="overflow-x-auto">
            <ul className="flex min-w-max gap-2 pb-1">
              {mainNavItems.map((item) => (
                <li key={item.to}>
                  <NavLink
                    to={item.to}
                    className={({ isActive }) =>
                      `rounded-full px-4 py-2 text-sm font-medium transition ${
                        isActive
                          ? 'bg-emerald-600 text-white'
                          : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                      }`
                    }
                  >
                    {item.label}
                  </NavLink>
                </li>
              ))}
            </ul>
          </nav>
        </div>
      </div>
    </header>
  )
}
