import { createAdminServerClient } from '@/lib/supabase/server'
import { getAllAgentCommissions } from '@tentuin/supabase'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { CommissionSummaryCards } from '@/components/laporan/CommissionSummaryCards'
import { TriggerStreamAButton } from '@/components/laporan/TriggerStreamAButton'
import { MonthYearFilter } from '@/components/laporan/MonthYearFilter'
import { formatRupiah, formatMonth } from '@/lib/format'

interface LaporanPageProps {
  searchParams: { month?: string; year?: string }
}

export default async function LaporanPage({ searchParams }: LaporanPageProps) {
  const now = new Date()
  const month = parseInt(searchParams.month ?? String(now.getMonth() + 1), 10)
  const year = parseInt(searchParams.year ?? String(now.getFullYear()), 10)

  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()

  const commissions = session
    ? await getAllAgentCommissions(month, year, session.access_token).catch(() => [])
    : []

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-start justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-900">Laporan Komisi</h1>
          <p className="text-sm text-gray-500 mt-1">
            {formatMonth(month)} {year}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <MonthYearFilter currentMonth={month} currentYear={year} />
          <TriggerStreamAButton month={month} year={year} />
        </div>
      </div>

      {/* Summary cards */}
      <CommissionSummaryCards commissions={commissions} />

      {/* Table */}
      <Card>
        <CardContent className="pt-4">
          {commissions.length === 0 ? (
            <p className="text-sm text-gray-400 text-center py-8">
              Belum ada data komisi untuk {formatMonth(month)} {year}.
              <br />
              <span className="text-xs">Gunakan tombol "Hitung Stream A" untuk menghitung komisi bulan ini.</span>
            </p>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b text-gray-500 text-left">
                  <th className="pb-2 font-medium">Agen</th>
                  <th className="pb-2 font-medium">Stream A</th>
                  <th className="pb-2 font-medium">Stream B</th>
                  <th className="pb-2 font-medium">Total</th>
                  <th className="pb-2 font-medium">Status</th>
                </tr>
              </thead>
              <tbody>
                {commissions.map((c) => (
                  <tr key={c.id} className="border-b last:border-0">
                    <td className="py-2.5">
                      <p className="font-medium">{c.agent.full_name}</p>
                      <p className="font-mono text-xs text-gray-400">{c.agent.referral_code}</p>
                    </td>
                    <td className="py-2.5 text-gray-600">{formatRupiah(c.stream_a_amount)}</td>
                    <td className="py-2.5 text-gray-600">{formatRupiah(c.stream_b_amount)}</td>
                    <td className="py-2.5 font-semibold">{formatRupiah(c.total_amount)}</td>
                    <td className="py-2.5">
                      <CommissionStatusBadge status={c.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

function CommissionStatusBadge({ status }: { status: string }) {
  const map: Record<string, { label: string; variant: 'warning' | 'success' | 'gray' }> = {
    pending: { label: 'Pending', variant: 'warning' },
    paid: { label: 'Dibayar', variant: 'success' },
    cancelled: { label: 'Dibatalkan', variant: 'gray' },
  }
  const { label, variant } = map[status] ?? { label: status, variant: 'gray' as const }
  return <Badge variant={variant}>{label}</Badge>
}
