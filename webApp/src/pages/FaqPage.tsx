const faqs = [
  {
    question: 'What is VitaMo?',
    answer: 'VitaMo is a European social platform that helps people become a little healthier every day.',
  },
  {
    question: 'How does VitaMo handle privacy?',
    answer: 'We follow data minimization and give users clear control over what they share and who can see it.',
  },
  {
    question: 'Do you store tokens in browser storage?',
    answer: 'No. Authentication is designed to use secure HttpOnly cookies.',
  },
  {
    question: 'Can I manage visibility and notifications?',
    answer: 'Yes. User control over profile, visibility, privacy settings, and notifications is a core principle.',
  },
]

export function FaqPage() {
  return (
    <section className="space-y-4 rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 sm:p-10">
      <h1 className="text-3xl font-semibold text-slate-900 sm:text-4xl">Frequently Asked Questions</h1>
      {faqs.map((item) => (
        <details key={item.question} className="rounded-xl border border-slate-200 p-4">
          <summary className="cursor-pointer font-medium text-slate-900">{item.question}</summary>
          <p className="mt-3 text-sm text-slate-700">{item.answer}</p>
        </details>
      ))}
    </section>
  )
}
