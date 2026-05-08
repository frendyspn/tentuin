'use client'

import { useState } from 'react'
import { toast } from 'sonner'
import { Zap } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from '@/components/ui/dialog'
import { triggerStreamAAction } from '@/app/(dashboard)/laporan/actions'
import { formatMonth } from '@/lib/format'

interface TriggerStreamAButtonProps {
  month: number
  year: number
}

export function TriggerStreamAButton({ month, year }: TriggerStreamAButtonProps) {
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleTrigger = async () => {
    setLoading(true)
    try {
      const result = await triggerStreamAAction(month, year)
      toast.success(
        `Kalkulasi selesai: ${result.schools_processed} sekolah diproses, ` +
          `total komisi Rp ${result.total_commission.toLocaleString('id-ID')}.`,
        { duration: 6000 },
      )
      setOpen(false)
    } catch {
      toast.error('Gagal menjalankan kalkulasi Stream A.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Button variant="outline" onClick={() => setOpen(true)} size="sm">
        <Zap className="h-4 w-4 mr-1.5 text-amber-500" />
        Hitung Stream A
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Hitung Komisi Stream A</DialogTitle>
            <DialogDescription>
              Jalankan kalkulasi komisi Stream A untuk {formatMonth(month)} {year}?
              Proses ini akan menandai semua prospect_usage_logs bulan tersebut
              sebagai sudah dihitung.
            </DialogDescription>
          </DialogHeader>
          <div className="bg-amber-50 border border-amber-100 rounded-lg px-3.5 py-3 text-sm text-amber-700">
            ⚠ Hanya jalankan sekali di akhir bulan. Jika dijalankan ulang,
            data yang sudah dihitung akan diabaikan.
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Batal</Button>
            <Button onClick={handleTrigger} disabled={loading}>
              {loading ? 'Menghitung…' : 'Jalankan Kalkulasi'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  )
}
