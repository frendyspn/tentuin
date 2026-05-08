'use client'

import { useRouter } from 'next/navigation'
import { LogOut, User } from 'lucide-react'
import { createAdminBrowserClient } from '@/lib/supabase/client'
import { Button } from '@/components/ui/button'

interface TopbarProps {
  userEmail: string | undefined
}

export function Topbar({ userEmail }: TopbarProps) {
  const router = useRouter()

  const handleSignOut = async () => {
    const supabase = createAdminBrowserClient()
    await supabase.auth.signOut()
    router.push('/login')
    router.refresh()
  }

  return (
    <header className="h-14 border-b border-gray-100 bg-white flex items-center justify-between px-6">
      <div />
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <div className="w-7 h-7 rounded-full bg-[#EEEEFF] flex items-center justify-center">
            <User className="h-3.5 w-3.5 text-[#5C59F8]" />
          </div>
          <span className="hidden sm:block">{userEmail}</span>
        </div>
        <Button variant="ghost" size="icon" onClick={handleSignOut} title="Keluar">
          <LogOut className="h-4 w-4 text-gray-500" />
        </Button>
      </div>
    </header>
  )
}
