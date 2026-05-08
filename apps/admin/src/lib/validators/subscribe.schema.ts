import { z } from 'zod'

export const subscribeSchema = z.object({
  amount: z.coerce
    .number()
    .int()
    .min(100_000, 'Minimal Rp 100.000'),
  quota_purchased: z.coerce
    .number()
    .int()
    .min(1, 'Minimal 1 kuota'),
})

export type SubscribeFormValues = z.infer<typeof subscribeSchema>
