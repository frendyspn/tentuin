'use server'

import { revalidatePath } from 'next/cache'
import { createAdminServerClient } from '@/lib/supabase/server'
import { suspendAgent, reactivateAgent } from '@tentuin/supabase'

async function getToken() {
  const supabase = await createAdminServerClient()
  const {
    data: { session },
  } = await supabase.auth.getSession()
  if (!session) throw new Error('Tidak terautentikasi')
  return session.access_token
}

export async function suspendAgentAction(agentId: string) {
  const token = await getToken()
  await suspendAgent(agentId, token)
  revalidatePath('/agen')
}

export async function reactivateAgentAction(agentId: string) {
  const token = await getToken()
  await reactivateAgent(agentId, token)
  revalidatePath('/agen')
}
