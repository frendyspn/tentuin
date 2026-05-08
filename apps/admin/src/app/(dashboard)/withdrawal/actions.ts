'use server'

import { revalidatePath } from 'next/cache'
import { createServiceRoleClient } from '@/lib/supabase/server'
import { processWithdrawal } from '@tentuin/supabase'

export async function approveWithdrawalAction(withdrawalId: string) {
  const supabase = createServiceRoleClient()
  const { data: { session } } = await supabase.auth.getSession()
  // Service role tidak punya session — gunakan service_role token langsung
  const serviceToken = process.env.SUPABASE_SERVICE_ROLE_KEY!
  await processWithdrawal(withdrawalId, 'transferred', null, serviceToken)
  revalidatePath('/withdrawal')
}

export async function rejectWithdrawalAction(withdrawalId: string, notes: string) {
  const serviceToken = process.env.SUPABASE_SERVICE_ROLE_KEY!
  await processWithdrawal(withdrawalId, 'rejected', notes, serviceToken)
  revalidatePath('/withdrawal')
}
