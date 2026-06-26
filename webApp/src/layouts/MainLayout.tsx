import { Outlet } from 'react-router-dom'
import { SiteFooter } from '@/components/layout/SiteFooter'
import { SiteHeader } from '@/components/layout/SiteHeader'

export function MainLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-gradient-to-b from-emerald-50 via-white to-cyan-50 text-slate-900">
      <SiteHeader />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:px-6 sm:py-10">
        <Outlet />
      </main>
      <SiteFooter />
    </div>
  )
}
