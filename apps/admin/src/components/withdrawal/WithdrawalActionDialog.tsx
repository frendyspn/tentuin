'use client'

import { useState } from 'react'
import { toast } from 'sonner'
import { Check, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/label'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from '@/components/ui/dialog'
import { approveWithdrawalAction, rejectWithdrawalAction } from '@/app/(dashboard)/withdrawal/actions'
import { formatRupiah } from '@/lib/format'

interface WithdrawalActionDialogProps {
  withdrawalId: string
  agentName: string
  amount: number
}

export function WithdrawalActionDialog({
  withdrawalId,
  agentName,
  amount,
}: WithdrawalActionDialogProps) {
  const [mode, setMode] = useState<'approve' | 'reject' | null>(null)
  const [notes, setNotes] = useState('')
  const [loading, setLoading] = useState(false)

  const handleClose = () => {
    setMode(null)
    setNotes('')
  }

  const handleApprove = async () => {
    setLoading(true)
    try {
      await approveWithdrawalAction(withdrawalId)
      toast.success(`Withdrawal ${agentName} berhasil ditransfer.`)
      handleClose()
    } catch {
      toast.error('Gagal memproses withdrawal.')
    } finally {
      setLoading(false)
    }
  }

  const handleReject = async () => {
    if (!notes.trim()) {
      toast.error('Catatan wajib diisi untuk penolakan.')
      return
    }
    setLoading(true)
    try {
      await rejectWithdrawalAction(withdrawalId, notes)
      toast.success(`Withdrawal ${agentName} berhasil ditolak.`)
      handleClose()
    } catch {
      toast.error('Gagal menolak withdrawal.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <div className="flex gap-1.5">
        <Button
          size="sm"
          variant="outline"
          className="text-green-600 border-green-200 hover:bg-green-50"
          onClick={() => setMode('approve')}
        >
          <Check className="h-3.5 w-3.5 mr-1" /> Approve
        </Button>
        <Button
          size="sm"
          variant="outline"
          className="text-red-600 border-red-200 hover:bg-red-50"
          onClick={() => setMode('reject')}
        >
          <X className="h-3.5 w-3.5 mr-1" /> Tolak
        </Button>
      </div>

      {/* Approve Dialog */}
      <Dialog open={mode === 'approve'} onOpenChange={() => handleClose()}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Konfirmasi Transfer</DialogTitle>
            <DialogDescription>
              Tandai withdrawal {agentName} sebagai sudah ditransfer.
            </DialogDescription>
          </DialogHeader>
          <div className="bg-green-50 rounded-lg px-4 py-3 text-center">
            <p className="text-2xl font-bold text-green-700">{formatRupiah(amount)}</p>
            <p className="text-sm text-green-600 mt-1">{agentName}</p>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={handleClose}>Batal</Button>
            <Button onClick={handleApprove} disabled={loading}
              className="bg-green-600 hover:bg-green-700">
              {loading ? 'Memproses…' : 'Konfirmasi Transfer'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Reject Dialog */}
      <Dialog open={mode === 'reject'} onOpenChange={() => handleClose()}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Tolak Withdrawal</DialogTitle>
            <DialogDescription>
              {agentName} — {formatRupiah(amount)}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-1.5">
            <Label>Alasan Penolakan *</Label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Masukkan alasan penolakan…"
              rows={3}
              className="w-full px-3 py-2 text-sm rounded-lg border border-input focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={handleClose}>Batal</Button>
            <Button
              variant="destructive"
              onClick={handleReject}
              disabled={loading || !notes.trim()}
            >
              {loading ? 'Memproses…' : 'Tolak Withdrawal'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
