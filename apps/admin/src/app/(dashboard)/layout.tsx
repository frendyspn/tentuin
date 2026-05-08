import { redirect } from 'next/navigation'
import { createAdminServerClient } from '@/lib/supabase/server'
import { Sidebar } from '@/components/layout/Sidebar'
import { Topbar } from '@/components/layout/Topbar'

export default async function DashboardLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const supabase = await createAdminServerClient()
  const {
    data: { user },
  } = await supabase.auth.getUser()

  if (!user) {
    redirect('/login')
  }

  // Cek role admin
  const { data: profile } = await supabase
    .from('profiles')
    .select('role')
    .eq('id', user.id)
    .single()

  if (profile?.role !== 'admin') {
    await supabase.auth.signOut()
    redirect('/login?error=unauthorized')
  }

  return (
    <div className="flex min-h-screen bg-[#F7F8FA]">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Topbar userEmail={user.email} />
        <main className="flex-1 p-6">{children}</main>
      </div>
    </div>
  )
}
