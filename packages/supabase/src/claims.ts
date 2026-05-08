import { supabaseUrl, supabaseAnonKey } from './client'
import { NetworkError, SessionExpiredError } from './universities'
import type { AgentRow } from './agents'

// ─── Types ────────────────────────────────────────────────────────────────────

export type SchoolClaim = {
  id: string
  agent_id: string
  school_id: string
  is_active: boolean
  claimed_at: string
  released_at: string | null
}

export type UniversityClaim = {
  id: string
  agent_id: string
  university_id: string
  is_active: boolean
  claimed_at: string
  released_at: string | null
}

export type SchoolClaimWithSchool = SchoolClaim & {
  school: {
    id: string
    name: string
    city: string
    province: string
    total_students: number
    logo_url: string | null
  }
}

export type UniversityClaimWithUniversity = UniversityClaim & {
  university: {
    id: string
    name: string
    short_name: string
    city: string
    logo_url: string | null
    quota_balance: number
  }
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

// ─── School Claims ────────────────────────────────────────────────────────────

/** Klaim sekolah untuk agen. Gagal jika sekolah sudah diklaim agen lain. */
export const claimSchool = async (
  agentId: string,
  schoolId: string,
  token: string
): Promise<SchoolClaim> => {
  const existing = await restFetch<SchoolClaim[]>(
    `agent_school_claims?school_id=eq.${schoolId}&is_active=eq.true&select=agent_id`,
    { token }
  )
  if (existing.length > 0) {
    throw new Error('Sekolah ini sudah diklaim oleh agen lain.')
  }
  const rows = await restFetch<SchoolClaim[]>('agent_school_claims', {
    method: 'POST',
    body: { agent_id: agentId, school_id: schoolId },
    token,
  })
  return rows[0]!
}

/** List sekolah yang diklaim agen beserta data sekolah */
export const getAgentSchoolClaims = async (
  agentId: string,
  token: string
): Promise<SchoolClaimWithSchool[]> => {
  return restFetch<SchoolClaimWithSchool[]>(
    `agent_school_claims?agent_id=eq.${agentId}&is_active=eq.true` +
    `&select=*,school:schools(id,name,city,province,total_students,logo_url)` +
    `&order=claimed_at.desc`,
    { token }
  )
}

/** Cek apakah sekolah tersedia untuk diklaim (belum ada klaim aktif) */
export const isSchoolClaimable = async (
  schoolId: string,
  token: string
): Promise<boolean> => {
  const rows = await restFetch<SchoolClaim[]>(
    `agent_school_claims?school_id=eq.${schoolId}&is_active=eq.true&select=id`,
    { token }
  )
  return rows.length === 0
}

// ─── University Claims ────────────────────────────────────────────────────────

/** Klaim universitas untuk agen. Gagal jika universitas sudah diklaim agen lain. */
export const claimUniversity = async (
  agentId: string,
  universityId: string,
  token: string
): Promise<UniversityClaim> => {
  const existing = await restFetch<UniversityClaim[]>(
    `agent_university_claims?university_id=eq.${universityId}&is_active=eq.true&select=agent_id`,
    { token }
  )
  if (existing.length > 0) {
    throw new Error('Universitas ini sudah diklaim oleh agen lain.')
  }
  const rows = await restFetch<UniversityClaim[]>('agent_university_claims', {
    method: 'POST',
    body: { agent_id: agentId, university_id: universityId },
    token,
  })
  return rows[0]!
}

/** List universitas yang diklaim agen beserta data universitas */
export const getAgentUniversityClaims = async (
  agentId: string,
  token: string
): Promise<UniversityClaimWithUniversity[]> => {
  return restFetch<UniversityClaimWithUniversity[]>(
    `agent_university_claims?agent_id=eq.${agentId}&is_active=eq.true` +
    `&select=*,university:universities(id,name,short_name,city,logo_url,quota_balance)` +
    `&order=claimed_at.desc`,
    { token }
  )
}

/** Cek apakah universitas tersedia untuk diklaim */
export const isUniversityClaimable = async (
  universityId: string,
  token: string
): Promise<boolean> => {
  const rows = await restFetch<UniversityClaim[]>(
    `agent_university_claims?university_id=eq.${universityId}&is_active=eq.true&select=id`,
    { token }
  )
  return rows.length === 0
}

/**
 * Pindahkan semua klaim agen nonaktif ke Agen 001 (owner).
 * Dipanggil oleh cron job dengan service_role token.
 */
export const transferClaimsToOwner = async (
  inactiveAgentId: string,
  ownerAgent: AgentRow,
  serviceToken: string
): Promise<void> => {
  const now = new Date().toISOString()

  // Release school claims lama
  await restFetch(
    `agent_school_claims?agent_id=eq.${inactiveAgentId}&is_active=eq.true`,
    { method: 'PATCH', body: { is_active: false, released_at: now }, token: serviceToken }
  )
  // Release university claims lama
  await restFetch(
    `agent_university_claims?agent_id=eq.${inactiveAgentId}&is_active=eq.true`,
    { method: 'PATCH', body: { is_active: false, released_at: now }, token: serviceToken }
  )

  // Ambil ID sekolah & universitas yang dilepas
  const [schoolClaims, uniClaims] = await Promise.all([
    restFetch<SchoolClaim[]>(
      `agent_school_claims?agent_id=eq.${inactiveAgentId}&released_at=eq.${now}&select=school_id`,
      { token: serviceToken }
    ),
    restFetch<UniversityClaim[]>(
      `agent_university_claims?agent_id=eq.${inactiveAgentId}&released_at=eq.${now}&select=university_id`,
      { token: serviceToken }
    ),
  ])

  // Buat klaim baru untuk owner
  await Promise.all([
    ...schoolClaims.map((c) =>
      restFetch('agent_school_claims', {
        method: 'POST',
        body: { agent_id: ownerAgent.id, school_id: c.school_id },
        token: serviceToken,
      })
    ),
    ...uniClaims.map((c) =>
      restFetch('agent_university_claims', {
        method: 'POST',
        body: { agent_id: ownerAgent.id, university_id: c.university_id },
        token: serviceToken,
      })
    ),
  ])
}
