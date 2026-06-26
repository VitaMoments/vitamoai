import { useState } from 'react'
import type { FormEvent } from 'react'

type ContactState = {
  name: string
  email: string
  message: string
}

const initialState: ContactState = {
  name: '',
  email: '',
  message: '',
}

export function ContactPage() {
  const [form, setForm] = useState<ContactState>(initialState)
  const [status, setStatus] = useState<string>('')

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    if (!form.name.trim() || !form.email.trim() || !form.message.trim()) {
      setStatus('Please complete all fields before sending your message.')
      return
    }

    setStatus('Thanks for your message. We will get back to you soon.')
    setForm(initialState)
  }

  return (
    <section className="rounded-2xl bg-white p-6 shadow-sm ring-1 ring-slate-200 sm:p-10">
      <h1 className="text-3xl font-semibold text-slate-900 sm:text-4xl">Contact</h1>
      <p className="mt-3 text-slate-700">
        Questions, feedback, or partnership ideas? Send us a message and we will respond as soon as possible.
      </p>
      <form onSubmit={onSubmit} className="mt-6 space-y-4">
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-800">Name</span>
          <input
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            className="w-full rounded-xl border border-slate-300 px-4 py-3 text-slate-900 outline-none ring-emerald-400 focus:ring"
            type="text"
            autoComplete="name"
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-800">Email</span>
          <input
            value={form.email}
            onChange={(event) => setForm({ ...form, email: event.target.value })}
            className="w-full rounded-xl border border-slate-300 px-4 py-3 text-slate-900 outline-none ring-emerald-400 focus:ring"
            type="email"
            autoComplete="email"
          />
        </label>
        <label className="block">
          <span className="mb-1 block text-sm font-medium text-slate-800">Message</span>
          <textarea
            value={form.message}
            onChange={(event) => setForm({ ...form, message: event.target.value })}
            className="min-h-32 w-full rounded-xl border border-slate-300 px-4 py-3 text-slate-900 outline-none ring-emerald-400 focus:ring"
          />
        </label>
        <button
          type="submit"
          className="rounded-full bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700"
        >
          Send Message
        </button>
        {status ? <p className="text-sm text-slate-700">{status}</p> : null}
      </form>
    </section>
  )
}
