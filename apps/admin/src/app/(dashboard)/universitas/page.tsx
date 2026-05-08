import { createAdminServerClient } from '@/lib/supabase/server'
import { getAllUniversitiesAdmin } from '@tentuin/supabase'
import { DataTable } from '@/components/data-table/DataTable'
import { universityColumns } from '@/components/universitas/UniversityColumns'

export default async function UniversitasPage() {
  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()

  const universities = session
    ? await getAllUniversitiesAdmin(session.access_token).catch(() => [])
    : []

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-xl font-bold text-gray-900">Kelola Universitas</h1>
        <p className="text-sm text-gray-500 mt-1">{universities.length} universitas aktif</p>
      </div>

      <DataTable
        columns={universityColumns}
        data={universities}
        searchKey="name"
        searchPlaceholder="Cari nama universitas…"
      />
    </div>
  )
}
