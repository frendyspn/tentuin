'use server'

import { revalidatePath } from 'next/cache'
import { createAdminServerClient } from '@/lib/supabase/server'
import { recordUniversitySubscribe } from '@tentuin/supabase'

async function getToken() {
  const supabase = await createAdminServerClient()
  const { data: { session } } = await supabase.auth.getSession()
  if (!session) throw new Error('Tidak terautentikasi')
  return session.access_token
}

export async function recordSubscribeAction(
  universityId: string,
  amount: number,
  quotaPurchased: number,
  agentId: string | null,
) {
  const token = await getToken()
  await recordUniversitySubscribe(universityId, amount, quotaPurchased, agentId, token)
  revalidatePath('/universitas')
  revalidatePath(`/universitas/${universityId}`)
}
