'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { toast } from 'sonner'
import { Plus, Pencil } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { schoolSchema, type SchoolFormValues } from '@/lib/validators/school.schema'
import { createSchoolAction, updateSchoolAction } from '@/app/(dashboard)/sekolah/actions'
import type { SchoolRow } from '@tentuin/supabase'

interface SchoolFormDialogProps {
  school?: SchoolRow
}

export function SchoolFormDialog({ school }: SchoolFormDialogProps) {
  const [open, setOpen] = useState(false)
  const isEdit = !!school

  const form = useForm<SchoolFormValues>({
    resolver: zodResolver(schoolSchema),
    defaultValues: {
      name: school?.name ?? '',
      city: school?.city ?? '',
      province: school?.province ?? '',
      total_students: school?.total_students ?? 0,
      address: school?.address ?? '',
      email: school?.email ?? '',
      phone: school?.phone ?? '',
      is_active: school?.is_active ?? true,
    },
  })

  const onSubmit = async (values: SchoolFormValues) => {
    try {
      if (isEdit && school) {
        await updateSchoolAction(school.id, values)
        toast.success('Data sekolah berhasil diperbarui.')
      } else {
        await createSchoolAction(values)
        toast.success('Sekolah berhasil ditambahkan.')
      }
      setOpen(false)
      form.reset()
    } catch {
      toast.error('Terjadi kesalahan. Coba lagi.')
    }
  }

  return (
    <>
      <Button
        variant={isEdit ? 'outline' : 'default'}
        size={isEdit ? 'icon' : 'default'}
        onClick={() => setOpen(true)}
      >
        {isEdit ? <Pencil className="h-4 w-4" /> : <><Plus className="h-4 w-4 mr-1.5" />Tambah Sekolah</>}
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>{isEdit ? 'Edit Sekolah' : 'Tambah Sekolah Baru'}</DialogTitle>
          </DialogHeader>

          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <div className="col-span-2 space-y-1.5">
                <Label>Nama Sekolah *</Label>
                <Input {...form.register('name')} placeholder="SMA Negeri 1 Jakarta" />
                {form.formState.errors.name && (
                  <p className="text-xs text-red-500">{form.formState.errors.name.message}</p>
                )}
              </div>
              <div className="space-y-1.5">
                <Label>Kota *</Label>
                <Input {...form.register('city')} placeholder="Jakarta" />
              </div>
              <div className="space-y-1.5">
                <Label>Provinsi *</Label>
                <Input {...form.register('province')} placeholder="DKI Jakarta" />
              </div>
              <div className="space-y-1.5">
                <Label>Jumlah Siswa *</Label>
                <Input
                  {...form.register('total_students')}
                  type="number"
                  min={1}
                  placeholder="1000"
                />
              </div>
              <div className="space-y-1.5">
                <Label>Telepon</Label>
                <Input {...form.register('phone')} placeholder="021-XXXXXXX" />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label>Email</Label>
                <Input {...form.register('email')} type="email" placeholder="info@sekolah.sch.id" />
              </div>
              <div className="col-span-2 space-y-1.5">
                <Label>Alamat</Label>
                <Input {...form.register('address')} placeholder="Jl. Merdeka No. 1" />
              </div>
            </div>

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setOpen(false)}>
                Batal
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting ? 'Menyimpan…' : 'Simpan'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  )
}
