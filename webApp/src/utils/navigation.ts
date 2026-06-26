import type { NavItem } from '@/types/navigation'

export const mainNavItems: NavItem[] = [
  { label: 'Home', to: '/' },
  { label: 'About', to: '/about' },
  { label: 'Features', to: '/features' },
  { label: 'Contact', to: '/contact' },
  { label: 'FAQ', to: '/faq' },
]

export const legalNavItems: NavItem[] = [
  { label: 'Privacy Policy', to: '/privacy-policy' },
  { label: 'Terms of Service', to: '/terms-of-service' },
  { label: 'Cookie Policy', to: '/cookie-policy' },
  { label: 'Community Guidelines', to: '/community-guidelines' },
]
