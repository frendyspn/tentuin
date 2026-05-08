import { Badge } from '@/components/ui/badge'

type WithdrawalStatus = 'requested' | 'approved' | 'rejected' | 'transferred'

export function WithdrawalStatusBadge({ status }: { status: WithdrawalStatus }) {
  const map = {
    requested: { label: 'Menunggu', variant: 'warning' as const },
    approved: { label: 'Disetujui', variant: 'success' as const },
    rejected: { label: 'Ditolak', variant: 'error' as const },
    transferred: { label: 'Ditransfer', variant: 'default' as const },
  }
  const { label, variant } = map[status] ?? { label: status, variant: 'gray' as const }
  return <Badge variant={variant}>{label}</Badge>
}
