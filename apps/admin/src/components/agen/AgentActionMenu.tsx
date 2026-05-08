'use client'

import { MoreHorizontal, ShieldOff, ShieldCheck } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { suspendAgentAction, reactivateAgentAction } from '@/app/(dashboard)/agen/actions'
import { toast } from 'sonner'

interface AgentActionMenuProps {
  agentId: string
  status: string
  name: string
}

export function AgentActionMenu({ agentId, status, name }: AgentActionMenuProps) {
  const handleSuspend = async () => {
    try {
      await suspendAgentAction(agentId)
      toast.success(`${name} berhasil disuspend.`)
    } catch {
      toast.error('Gagal menyuspend agen.')
    }
  }

  const handleReactivate = async () => {
    try {
      await reactivateAgentAction(agentId)
      toast.success(`${name} berhasil diaktifkan kembali.`)
    } catch {
      toast.error('Gagal mengaktifkan agen.')
    }
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="h-8 w-8">
          <MoreHorizontal className="h-4 w-4" />
          <span className="sr-only">Aksi</span>
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuLabel>Aksi</DropdownMenuLabel>
        <DropdownMenuSeparator />
        {status !== 'suspended' && (
          <DropdownMenuItem
            className="text-red-600 focus:text-red-600"
            onClick={handleSuspend}
          >
            <ShieldOff className="mr-2 h-4 w-4" />
            Suspend Agen
          </DropdownMenuItem>
        )}
        {status === 'suspended' && (
          <DropdownMenuItem onClick={handleReactivate}>
            <ShieldCheck className="mr-2 h-4 w-4" />
            Aktifkan Kembali
          </DropdownMenuItem>
        )}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
