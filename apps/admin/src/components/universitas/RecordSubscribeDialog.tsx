'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { PlusCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from '@/components/ui/dialog'
import { subscribeSchema, type SubscribeFormValues } from '@/lib/validators/subscribe.schema'
import { recordSubscribeAction } from '@/app/(dashboard)/universitas/actions'
import { formatRupiah } from '@/lib/format'

interface RecordSubscribeDialogProps {
  universityId: string
  universityName: string
  agentId: string | null
}

export function RecordSubscribeDialog({
  universityId,
  universityName,
  agentId,
}: RecordSubscribeDialogProps) {
  const [open, setOpen] = useState(false)

  const form = useForm<SubscribeFormValues>({
    resolver: zodResolver(subscribeSchema),
    defaultValues: { amount: 0, quota_purchased: 0 },
  })

  const onSubmit = async (values: SubscribeFormValues) => {
    try {
      await recordSubscribeAction(universityId, values.amount, values.quota_purchased, agentId)
      toast.success(
        `Subscribe ${universityName} berhasil dicatat. Kuota +${values.quota_purchased.toLocaleString('id-ID')}.`,
      )
      setOpen(false)
      form.reset()
    } catch {
      toast.error('Gagal mencatat subscribe.')
    }
  }

  const watchAmount = form.watch('amount')

  return (
    <>
      <Button onClick={() => setOpen(true)} size="sm">
        <PlusCircle className="h-4 w-4 mr-1.5" />
        Catat Subscribe
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>Catat Subscribe Universitas</DialogTitle>
            <DialogDescription>
              {universityName}
              {agentId ? (
                <span className="text-[#5C59F8] ml-1.5">· Komisi 10% otomatis ke agen klaim</span>
              ) : (
                <span className="text-gray-400 ml-1.5">· Tidak ada agen klaim</span>
              )}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-1.5">
              <Label>Nilai Subscribe (Rp) *</Label>
              <Input
                {...form.register('amount')}
                type="number"
                min={100000}
                placeholder="5000000"
              />
              {watchAmount > 0 && (
                <p className="text-xs text-gray-400">= {formatRupiah(watchAmount)}</p>
              )}
              {form.formState.errors.amount && (
                <p className="text-xs text-red-500">{form.formState.errors.amount.message}</p>
              )}
            </div>

            <div className="space-y-1.5">
              <Label>Kuota Dibeli (jumlah prospek) *</Label>
              <Input
                {...form.register('quota_purchased')}
                type="number"
                min={1}
                placeholder="5000"
              />
              {form.formState.errors.quota_purchased && (
                <p className="text-xs text-red-500">{form.formState.errors.quota_purchased.message}</p>
              )}
            </div>

            {agentId && watchAmount > 0 && (
              <div className="bg-[#EEEEFF] rounded-lg px-3.5 py-3 text-sm">
                <p className="text-[#5C59F8] font-medium">
                  Komisi agen: {formatRupiah(Math.floor(watchAmount * 0.1))}
                </p>
                <p className="text-[#5C59F8]/70 text-xs mt-0.5">10% dari nilai subscribe</p>
              </div>
            )}

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                Batal
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting ? 'Menyimpan…' : 'Catat'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  )
}
