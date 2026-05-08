import { createAdminServerClient } from '@/lib/supabase/server'
import { getAllSchoolsAdmin } from '@tentuin/supabase'
import { DataTable } from '@/components/data-table/DataTable'
import { schoolColumns } from '@/components/sekolah/SchoolColumns'
import { SchoolFormDialog } from '@/components/sekolah/SchoolFormDialog'

export default async function SekolahPage() {
  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()

  const schools = session
    ? await getAllSchoolsAdmin(session.access_token).catch(() => [])
    : []

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Kelola Sekolah</h1>
          <p className="text-sm text-gray-500 mt-1">{schools.length} sekolah terdaftar</p>
        </div>
        <SchoolFormDialog />
      </div>

      <DataTable
        columns={schoolColumns}
        data={schools}
        searchKey="name"
        searchPlaceholder="Cari nama sekolah…"
      />
    </div>
  )
}
