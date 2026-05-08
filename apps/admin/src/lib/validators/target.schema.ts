import { z } from 'zod'

export const targetSchema = z.object({
  annual_target: z.coerce.number().int().min(1, 'Target tahunan minimal 1'),
  monthly_targets: z
    .array(z.coerce.number().int().min(0))
    .length(12, 'Harus ada 12 bulan')
    .optional(),
})

export type TargetFormValues = z.infer<typeof targetSchema>
