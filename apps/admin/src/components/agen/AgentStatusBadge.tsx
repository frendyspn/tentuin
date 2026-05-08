import { Badge } from '@/components/ui/badge'

type AgentStatus = 'active' | 'inactive' | 'suspended'

export function AgentStatusBadge({ status }: { status: AgentStatus }) {
  const map = {
    active: { label: 'Aktif', variant: 'success' as const },
    inactive: { label: 'Tidak Aktif', variant: 'gray' as const },
    suspended: { label: 'Disuspend', variant: 'error' as const },
  }
  const { label, variant } = map[status] ?? { label: status, variant: 'gray' as const }
  return <Badge variant={variant}>{label}</Badge>
}
