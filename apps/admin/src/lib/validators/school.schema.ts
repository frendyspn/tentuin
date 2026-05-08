import { z } from 'zod'

export const schoolSchema = z.object({
  name: z.string().min(3, 'Nama minimal 3 karakter'),
  city: z.string().min(2, 'Kota wajib diisi'),
  province: z.string().min(2, 'Provinsi wajib diisi'),
  total_students: z.coerce.number().int().min(1, 'Jumlah siswa minimal 1'),
  address: z.string().optional(),
  email: z.string().email('Email tidak valid').optional().or(z.literal('')),
  phone: z.string().optional(),
  is_active: z.boolean().default(true),
})

export type SchoolFormValues = z.infer<typeof schoolSchema>
