import { createAdminServerClient } from '@/lib/supabase/server'
import { getAllAgents } from '@tentuin/supabase'
import { DataTable } from '@/components/data-table/DataTable'
import { agentColumns } from '@/components/agen/AgentColumns'

export default async function AgenPage() {
  const supabase = await createAdminServerClient()
  const {
    data: { session },
  } = await supabase.auth.getSession()

  const agents = session
    ? await getAllAgents(session.access_token).catch(() => [])
    : []

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Kelola Agen</h1>
        <p className="text-sm text-gray-500 mt-1">
          {agents.length} agen terdaftar
        </p>
      </div>

      <DataTable
        columns={agentColumns}
        data={agents}
        searchKey="full_name"
        searchPlaceholder="Cari nama agen…"
      />
    </div>
  )
}
