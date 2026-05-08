/**
 * Admin-only Supabase queries.
 * Menggunakan restFetch lokal — TIDAK mengimport client.ts (React Native).
 * Semua fungsi membutuhkan token (user JWT atau service_role key).
 */

import { supabaseUrl, supabaseAnonKey } from './client'
import { NetworkError, SessionExpiredError } from './universities'
import type { AgentRow } from './agents'
import type { SchoolRow } from './schools'
import type { AgentCommission, AgentWithdrawal } from './commissions'
import type { UniversityRow } from './universities'

// ─── restFetch ────────────────────────────────────────────────────────────────

const restFetch = async <T>(
  path: string,
  options?: { method?: string; body?: unknown; token?: string },
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
        Prefer: method === 'POST' ? 'return=representation' : method === 'PATCH' ? 'return=representation' : '',
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

// ─── Types ────────────────────────────────────────────────────────────────────

export type AgentWithCommissionTotal = AgentRow & {
  total_commission: number
  agent_commissions: { total_amount: number }[]
}

export type SchoolWithClaim = SchoolRow & {
  claiming_agent: { id: string; full_name: string; referral_code: string } | null
  annual_target: number | null
}

export type UniversityWithClaim = UniversityRow & {
  claiming_agent: { id: string; full_name: string; referral_code: string } | null
}

export type WithdrawalWithAgent = AgentWithdrawal & {
  agent: {
    full_name: string
    referral_code: string
    bank_name: string | null
    bank_account_number: string | null
    bank_account_name: string | null
  }
}

export type AgentCommissionWithAgent = AgentCommission & {
  agent: { full_name: string; referral_code: string }
}

// ─── Agent Admin Functions ────────────────────────────────────────────────────

/**
 * Admin: semua agen dengan total komisi all-time.
 */
export const getAllAgents = async (token: string): Promise<AgentWithCommissionTotal[]> => {
  const rows = await restFetch<AgentWithCommissionTotal[]>(
    'agents?select=*,agent_commissions(total_amount)&order=created_at.desc',
    { token },
  )
  return rows.map((r) => ({
    ...r,
    total_commission: (r.agent_commissions ?? []).reduce((sum, c) => sum + c.total_amount, 0),
  }))
}

/**
 * Admin: suspend agen (status = 'suspended').
 */
export const suspendAgent = async (agentId: string, token: string): Promise<void> => {
  await restFetch(`agents?id=eq.${agentId}`, {
    method: 'PATCH',
    body: { status: 'suspended', updated_at: new Date().toISOString() },
    token,
  })
}

/**
 * Admin: aktifkan kembali agen (status = 'active').
 */
export const reactivateAgent = async (agentId: string, token: string): Promise<void> => {
  await restFetch(`agents?id=eq.${agentId}`, {
    method: 'PATCH',
    body: { status: 'active', updated_at: new Date().toISOString() },
    token,
  })
}

// ─── School Admin Functions ───────────────────────────────────────────────────

/**
 * Admin: semua sekolah (termasuk nonaktif) + agen klaim + target tahun ini.
 */
export const getAllSchoolsAdmin = async (
  token: string,
  year: number = new Date().getFullYear(),
): Promise<SchoolWithClaim[]> => {
  const [schools, claims, targets] = await Promise.all([
    restFetch<SchoolRow[]>('schools?order=name.asc&select=*', { token }),
    restFetch<{ school_id: string; agent: { id: string; full_name: string; referral_code: string } }[]>(
      'agent_school_claims?is_active=eq.true&select=school_id,agent:agents(id,full_name,referral_code)',
      { token },
    ),
    restFetch<{ school_id: string; annual_target: number }[]>(
      `school_targets?year=eq.${year}&select=school_id,annual_target`,
      { token },
    ),
  ])

  const claimMap = new Map(claims.map((c) => [c.school_id, c.agent]))
  const targetMap = new Map(targets.map((t) => [t.school_id, t.annual_target]))

  return schools.map((s) => ({
    ...s,
    claiming_agent: claimMap.get(s.id) ?? null,
    annual_target: targetMap.get(s.id) ?? null,
  }))
}

/**
 * Admin: buat sekolah baru.
 */
export const createSchool = async (
  data: Omit<SchoolRow, 'id' | 'created_at' | 'updated_at'>,
  token: string,
): Promise<SchoolRow> => {
  const rows = await restFetch<SchoolRow[]>('schools', {
    method: 'POST',
    body: data,
    token,
  })
  return rows[0]!
}

/**
 * Admin: update data sekolah.
 */
export const updateSchool = async (
  schoolId: string,
  data: Partial<Omit<SchoolRow, 'id' | 'created_at'>>,
  token: string,
): Promise<SchoolRow> => {
  const rows = await restFetch<SchoolRow[]>(`schools?id=eq.${schoolId}`, {
    method: 'PATCH',
    body: { ...data, updated_at: new Date().toISOString() },
    token,
  })
  return rows[0]!
}

// ─── University Admin Functions ───────────────────────────────────────────────

/**
 * Admin: semua universitas partner + agen klaim.
 */
export const getAllUniversitiesAdmin = async (token: string): Promise<UniversityWithClaim[]> => {
  const [universities, claims] = await Promise.all([
    restFetch<UniversityRow[]>(
      'universities?is_active=eq.true&order=name.asc&select=*',
      { token },
    ),
    restFetch<{ university_id: string; agent: { id: string; full_name: string; referral_code: string } }[]>(
      'agent_university_claims?is_active=eq.true&select=university_id,agent:agents(id,full_name,referral_code)',
      { token },
    ),
  ])

  const claimMap = new Map(claims.map((c) => [c.university_id, c.agent]))

  return universities.map((u) => ({
    ...u,
    claiming_agent: claimMap.get(u.id) ?? null,
  }))
}

// ─── Withdrawal Admin Functions ───────────────────────────────────────────────

/**
 * Admin: semua withdrawal (opsional filter status) + info agen.
 */
export const getAllWithdrawals = async (
  token: string,
  status?: 'requested' | 'approved' | 'rejected' | 'transferred',
): Promise<WithdrawalWithAgent[]> => {
  let path =
    'agent_withdrawals?select=*,agent:agents(full_name,referral_code,bank_name,bank_account_number,bank_account_name)&order=requested_at.desc'
  if (status) path += `&status=eq.${status}`
  return restFetch<WithdrawalWithAgent[]>(path, { token })
}

// ─── Commission Admin Functions ───────────────────────────────────────────────

/**
 * Admin: semua komisi agen untuk bulan & tahun tertentu + nama agen.
 */
export const getAllAgentCommissions = async (
  month: number,
  year: number,
  token: string,
): Promise<AgentCommissionWithAgent[]> => {
  return restFetch<AgentCommissionWithAgent[]>(
    `agent_commissions?month=eq.${month}&year=eq.${year}` +
      `&select=*,agent:agents(full_name,referral_code)&order=total_amount.desc`,
    { token },
  )
}
