'use client'

import { ColumnDef } from '@tanstack/react-table'
import Link from 'next/link'
import { AgentStatusBadge } from './AgentStatusBadge'
import { AgentActionMenu } from './AgentActionMenu'
import { formatRelative, formatRupiah } from '@/lib/format'
import type { AgentWithCommissionTotal } from '@tentuin/supabase'

export const agentColumns: ColumnDef<AgentWithCommissionTotal>[] = [
  {
    accessorKey: 'full_name',
    header: 'Nama Agen',
    cell: ({ row }) => (
      <Link
        href={`/agen/${row.original.id}`}
        className="font-medium text-[#5C59F8] hover:underline"
      >
        {row.getValue('full_name')}
      </Link>
    ),
  },
  {
    accessorKey: 'referral_code',
    header: 'Kode Referral',
    cell: ({ row }) => (
      <span className="font-mono text-xs bg-gray-100 px-2 py-1 rounded">
        {row.getValue('referral_code')}
      </span>
    ),
  },
  {
    accessorKey: 'status',
    header: 'Status',
    cell: ({ row }) => <AgentStatusBadge status={row.getValue('status')} />,
  },
  {
    accessorKey: 'last_active_at',
    header: 'Aktif Terakhir',
    cell: ({ row }) => {
      const val = row.getValue('last_active_at') as string | null
      return val ? (
        <span className="text-sm text-gray-500">{formatRelative(val)}</span>
      ) : (
        <span className="text-sm text-gray-400">—</span>
      )
    },
  },
  {
    accessorKey: 'total_commission',
    header: 'Total Komisi',
    cell: ({ row }) => (
      <span className="font-medium">{formatRupiah(row.getValue('total_commission'))}</span>
    ),
  },
  {
    id: 'actions',
    cell: ({ row }) => (
      <AgentActionMenu
        agentId={row.original.id}
        status={row.original.status}
        name={row.original.full_name}
      />
    ),
  },
]
