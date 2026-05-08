'use client'

import { ColumnDef } from '@tanstack/react-table'
import Link from 'next/link'
import { Badge } from '@/components/ui/badge'
import type { UniversityWithClaim } from '@tentuin/supabase'

export const universityColumns: ColumnDef<UniversityWithClaim>[] = [
  {
    accessorKey: 'name',
    header: 'Nama Universitas',
    cell: ({ row }) => (
      <Link
        href={`/universitas/${row.original.id}`}
        className="font-medium text-[#5C59F8] hover:underline"
      >
        {row.getValue('name')}
      </Link>
    ),
  },
  {
    accessorKey: 'city',
    header: 'Kota',
    cell: ({ row }) => <span className="text-gray-600">{row.getValue('city')}</span>,
  },
  {
    accessorKey: 'quota_balance',
    header: 'Sisa Kuota',
    cell: ({ row }) => (
      <span className="font-medium">{(row.getValue('quota_balance') as number).toLocaleString('id-ID')}</span>
    ),
  },
  {
    accessorKey: 'total_quota_purchased',
    header: 'Total Dibeli',
    cell: ({ row }) => (
      <span className="text-gray-600">{(row.getValue('total_quota_purchased') as number).toLocaleString('id-ID')}</span>
    ),
  },
  {
    accessorKey: 'claiming_agent',
    header: 'Agen Klaim',
    cell: ({ row }) => {
      const agent = row.original.claiming_agent
      return agent ? (
        <div>
          <p className="font-medium text-sm">{agent.full_name}</p>
          <p className="font-mono text-xs text-gray-400">{agent.referral_code}</p>
        </div>
      ) : (
        <Badge variant="gray" className="text-xs">Belum diklaim</Badge>
      )
    },
  },
  {
    accessorKey: 'partner_tier',
    header: 'Tier',
    cell: ({ row }) => {
      const tier = row.getValue('partner_tier') as string | null
      if (!tier) return <span className="text-gray-400 text-xs">—</span>
      return (
        <Badge variant={tier === 'premium' ? 'default' : 'secondary'}>
          {tier === 'premium' ? 'Premium' : 'Basic'}
        </Badge>
      )
    },
  },
]
