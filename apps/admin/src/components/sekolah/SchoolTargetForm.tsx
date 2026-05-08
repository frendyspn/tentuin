'use client'

import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { targetSchema, type TargetFormValues } from '@/lib/validators/target.schema'
import { setSchoolTargetAction } from '@/app/(dashboard)/sekolah/actions'
import { formatMonth } from '@/lib/format'

interface SchoolTargetFormProps {
  schoolId: string
  year: number
  currentAnnualTarget?: number
}

export function SchoolTargetForm({ schoolId, year, currentAnnualTarget }: SchoolTargetFormProps) {
  const form = useForm<TargetFormValues>({
    resolver: zodResolver(targetSchema),
    defaultValues: {
      annual_target: currentAnnualTarget ?? 0,
    },
  })

  const onSubmit = async (values: TargetFormValues) => {
    try {
      await setSchoolTargetAction(schoolId, year, values)
      toast.success('Target berhasil disimpan.')
    } catch {
      toast.error('Gagal menyimpan target.')
    }
  }

  return (
    <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
      <div className="max-w-xs space-y-1.5">
        <Label>Target Tahunan (jumlah siswa daftar)</Label>
        <Input
          {...form.register('annual_target')}
          type="number"
          min={1}
          placeholder="Contoh: 100"
        />
        {form.formState.errors.annual_target && (
          <p className="text-xs text-red-500">{form.formState.errors.annual_target.message}</p>
        )}
        <p className="text-xs text-gray-400">
          Jika tidak diset, default = 10% × jumlah siswa sekolah.
          Target bulanan = target tahunan ÷ 12.
        </p>
      </div>

      <Button type="submit" disabled={form.formState.isSubmitting} size="sm">
        {form.formState.isSubmitting ? 'Menyimpan…' : 'Simpan Target'}
      </Button>
    </form>
  )
}
