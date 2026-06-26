const featureList = [
  {
    title: 'Progress Communities',
    status: 'Available',
    description: 'Share progress updates with communities focused on practical and healthy habits.',
  },
  {
    title: 'Privacy Controls',
    status: 'Available',
    description: 'Set profile visibility and choose what information is visible to others.',
  },
  {
    title: 'Habit Challenges',
    status: 'In development',
    description: 'Join short community challenges designed around consistency and wellbeing.',
  },
  {
    title: 'Wellbeing Insights',
    status: 'In development',
    description: 'Receive clear summaries of your own activity without invasive tracking.',
  },
]

export function FeaturesPage() {
  return (
    <section className="space-y-6">
      <div className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 sm:p-10">
        <h1 className="text-3xl font-semibold text-slate-900 sm:text-4xl">Features</h1>
        <p className="mt-3 text-slate-700">
          These features support healthier living while keeping users in control of their data and visibility.
        </p>
      </div>
      <div className="grid gap-4 sm:grid-cols-2">
        {featureList.map((feature) => (
          <article key={feature.title} className="rounded-2xl bg-white p-5 shadow-sm ring-1 ring-slate-200">
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-lg font-semibold text-slate-900">{feature.title}</h2>
              <span className="rounded-full bg-slate-100 px-3 py-1 text-xs font-semibold text-slate-700">
                {feature.status}
              </span>
            </div>
            <p className="mt-3 text-sm text-slate-700">{feature.description}</p>
          </article>
        ))}
      </div>
    </section>
  )
}
