import { Card, CardContent } from '@/components/ui/card'
import { formatRupiah } from '@/lib/format'
import { Clock, CheckCircle2, DollarSign } from 'lucide-react'
import type { AgentCommissionWithAgent } from '@tentuin/supabase'

interface CommissionSummaryCardsProps {
  commissions: AgentCommissionWithAgent[]
}

export function CommissionSummaryCards({ commissions }: CommissionSummaryCardsProps) {
  const totalPending = commissions
    .filter((c) => c.status === 'pending')
    .reduce((s, c) => s + c.total_amount, 0)

  const totalPaid = commissions
    .filter((c) => c.status === 'paid')
    .reduce((s, c) => s + c.total_amount, 0)

  const totalAll = commissions.reduce((s, c) => s + c.total_amount, 0)

  return (
    <div className="grid grid-cols-3 gap-4">
      <Card>
        <CardContent className="pt-4 pb-4">
          <div className="flex items-center gap-2 mb-1">
            <Clock className="h-4 w-4 text-amber-500" />
            <span className="text-xs text-gray-500">Total Pending</span>
          </div>
          <p className="font-bold text-gray-900">{formatRupiah(totalPending)}</p>
          <p className="text-xs text-gray-400 mt-0.5">
            {commissions.filter((c) => c.status === 'pending').length} agen
          </p>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="pt-4 pb-4">
          <div className="flex items-center gap-2 mb-1">
            <CheckCircle2 className="h-4 w-4 text-green-500" />
            <span className="text-xs text-gray-500">Total Dibayar</span>
          </div>
          <p className="font-bold text-gray-900">{formatRupiah(totalPaid)}</p>
          <p className="text-xs text-gray-400 mt-0.5">
            {commissions.filter((c) => c.status === 'paid').length} agen
          </p>
        </CardContent>
      </Card>
      <Card>
        <CardContent className="pt-4 pb-4">
          <div className="flex items-center gap-2 mb-1">
            <DollarSign className="h-4 w-4 text-[#5C59F8]" />
            <span className="text-xs text-gray-500">Total Semua</span>
          </div>
          <p className="font-bold text-gray-900">{formatRupiah(totalAll)}</p>
          <p className="text-xs text-gray-400 mt-0.5">{commissions.length} agen</p>
        </CardContent>
      </Card>
    </div>
  )
}
