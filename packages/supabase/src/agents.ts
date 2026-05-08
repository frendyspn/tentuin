import { supabaseUrl, supabaseAnonKey } from './client'
import { NetworkError, SessionExpiredError } from './universities'

// ─── Types ────────────────────────────────────────────────────────────────────

export type AgentStatus = 'active' | 'suspended' | 'inactive'

export type AgentRow = {
  id: string
  full_name: string
  email: string
  phone: string | null
  referral_code: string
  status: AgentStatus
  is_owner: boolean
  last_active_at: string
  bank_name: string | null
  bank_account_number: string | null
  bank_account_name: string | null
  notes: string | null
  created_at: string
  updated_at: string
}

export type RegisterAgentData = {
  id: string
  full_name: string
  email: string
  phone?: string
}

export type UpdateAgentBankData = {
  bank_name: string
  bank_account_number: string
  bank_account_name: string
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

const restFetch = async <T>(
  path: string,
  options?: { method?: string; body?: unknown; token?: string }
): Promise<T> => {
  const { method = 'GET', body, token } = options ?? {}
  let res: Response
  try {
    res = await fetch(`${supabaseUrl}/rest/v1/${path}`, {
      method,
      headers: {
        apikey: supabaseAnonKey,
        Authorization: `Bearer ${token ?? supabaseAnonKey}`,
        'Content-Type': 'application/json',
        Prefer: method === 'POST' ? 'return=representation' : '',
      },
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new NetworkError()
  }
  if (res.status === 401) throw new SessionExpiredError()
  if (!res.ok) {
    const text = await res.text()
    throw new Error(`${res.status}: ${text}`)
  }
  const text = await res.text()
  return (text ? JSON.parse(text) : null) as T
}

/** Generate referral code unik: 3 huruf kapital + 4 digit angka, misal: TEN2401 */
const generateReferralCode = (): string => {
  const letters = 'ABCDEFGHJKLMNPQRSTUVWXYZ'
  const prefix = Array.from({ length: 3 }, () =>
    letters[Math.floor(Math.random() * letters.length)]
  ).join('')
  const suffix = String(Math.floor(1000 + Math.random() * 9000))
  return `${prefix}${suffix}`
}

// ─── Queries ──────────────────────────────────────────────────────────────────

/** Daftarkan agen baru setelah user auth dibuat */
export const registerAgent = async (
  data: RegisterAgentData,
  token: string
): Promise<AgentRow> => {
  const referral_code = generateReferralCode()
  const rows = await restFetch<AgentRow[]>('agents', {
    method: 'POST',
    body: { ...data, referral_code, last_active_at: new Date().toISOString() },
    token,
  })
  return rows[0]!
}

/** Ambil profil agen */
export const getAgentProfile = async (
  agentId: string,
  token: string
): Promise<AgentRow | null> => {
  const rows = await restFetch<AgentRow[]>(
    `agents?id=eq.${agentId}&select=*`,
    { token }
  )
  return rows[0] ?? null
}

/** Update timestamp login — dipanggil setiap kali agen buka aplikasi */
export const updateAgentLastActive = async (
  agentId: string,
  token: string
): Promise<void> => {
  await restFetch(`agents?id=eq.${agentId}`, {
    method: 'PATCH',
    body: { last_active_at: new Date().toISOString() },
    token,
  })
}

/** Update data rekening bank agen */
export const updateAgentBank = async (
  agentId: string,
  data: UpdateAgentBankData,
  token: string
): Promise<void> => {
  await restFetch(`agents?id=eq.${agentId}`, {
    method: 'PATCH',
    body: data,
    token,
  })
}

/** Cek apakah user sudah terdaftar sebagai agen */
export const isRegisteredAgent = async (
  userId: string,
  token: string
): Promise<boolean> => {
  const rows = await restFetch<AgentRow[]>(
    `agents?id=eq.${userId}&select=id`,
    { token }
  )
  return rows.length > 0
}

/**
 * Ambil Agen 001 (owner) — digunakan sebagai fallback klaim saat agen nonaktif.
 * Dipanggil oleh service_role (cron job), bukan dari client.
 */
export const getOwnerAgent = async (token: string): Promise<AgentRow | null> => {
  const rows = await restFetch<AgentRow[]>(
    'agents?is_owner=eq.true&select=*&limit=1',
    { token }
  )
  return rows[0] ?? null
}
