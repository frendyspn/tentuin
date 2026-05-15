'use client'

import {
  Users,
  School,
  University,
  CreditCard,
  BarChart3,
} from 'lucide-react'
import { SidebarLink } from './SidebarLink'
import { Separator } from '@/components/ui/separator'

const navItems = [
  { href: '/agen', icon: Users, label: 'Kelola Agen' },
  { href: '/sekolah', icon: School, label: 'Kelola Sekolah' },
  { href: '/universitas', icon: University, label: 'Kelola Universitas' },
  { href: '/withdrawal', icon: CreditCard, label: 'Approval Withdrawal' },
  { href: '/laporan', icon: BarChart3, label: 'Laporan Komisi' },
]

export function Sidebar() {
  return (
    <aside className="w-60 min-h-screen bg-white border-r border-gray-100 flex flex-col">
      {/* Logo */}
      <div className="px-5 py-5 flex items-center gap-3">
        <div className="w-8 h-8 rounded-xl bg-[#5C59F8] flex items-center justify-center flex-shrink-0">
          <span className="text-white font-bold text-sm">T</span>
        </div>
        <div>
          <p className="font-bold text-sm text-gray-900">Tentuin</p>
          <p className="text-xs text-gray-400">Admin Panel</p>
        </div>
      </div>

      <Separator />

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1">
        {navItems.map((item) => (
          <SidebarLink key={item.href} {...item} />
        ))}
      </nav>

      {/* Footer */}
      <div className="px-5 py-4 border-t border-gray-100">
        <p className="text-xs text-gray-400">© 2025 Tentuin</p>
      </div>
    </aside>
  )
}
