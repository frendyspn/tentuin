'use client'

import { ColumnDef } from '@tanstack/react-table'
import Link from 'next/link'
import { Badge } from '@/components/ui/badge'
import { SchoolFormDialog } from './SchoolFormDialog'
import type { SchoolWithClaim } from '@tentuin/supabase'

export const schoolColumns: ColumnDef<SchoolWithClaim>[] = [
  {
    accessorKey: 'name',
    header: 'Nama Sekolah',
    cell: ({ row }) => (
      <Link
        href={`/sekolah/${row.original.id}`}
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
    accessorKey: 'total_students',
    header: 'Siswa',
    cell: ({ row }) => (
      <span>{(row.getValue('total_students') as number).toLocaleString('id-ID')}</span>
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
    accessorKey: 'annual_target',
    header: 'Target Tahunan',
    cell: ({ row }) => {
      const target = row.original.annual_target
      return target ? (
        <span className="font-medium">{target.toLocaleString('id-ID')} siswa</span>
      ) : (
        <span className="text-gray-400 text-sm">Belum diset</span>
      )
    },
  },
  {
    accessorKey: 'is_active',
    header: 'Status',
    cell: ({ row }) => (
      <Badge variant={row.getValue('is_active') ? 'success' : 'gray'}>
        {row.getValue('is_active') ? 'Aktif' : 'Nonaktif'}
      </Badge>
    ),
  },
  {
    id: 'actions',
    cell: ({ row }) => <SchoolFormDialog school={row.original} />,
  },
]
