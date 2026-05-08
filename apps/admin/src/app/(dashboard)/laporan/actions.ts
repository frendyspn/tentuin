'use server'

import { revalidatePath } from 'next/cache'
import { calculateMonthlyStreamA } from '@tentuin/supabase'

export async function triggerStreamAAction(month: number, year: number) {
  const serviceToken = process.env.SUPABASE_SERVICE_ROLE_KEY!
  const result = await calculateMonthlyStreamA(month, year, serviceToken)
  revalidatePath('/laporan')
  return result
}
