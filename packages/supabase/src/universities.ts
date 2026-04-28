import { supabase, supabaseUrl, supabaseAnonKey } from './client'

export class NetworkError extends Error { constructor() { super('NETWORK_ERROR') } }
export class SessionExpiredError extends Error { constructor() { super('SESSION_EXPIRED') } }

// ─── Types ────────────────────────────────────────────────────────────────────

export type UniversityRow = {
  id: string
  name: string
  short_name: string
  city: string
  province: string
  type: 'negeri' | 'swasta'
  logo_url: string | null
  cover_url: string | null
  website: string | null
  email: string | null
  phone: string | null
  description: string | null
  is_partner: boolean
  is_active: boolean
  partner_tier: 'basic' | 'premium' | null
  created_at: string
}

export type MajorRow = {
  id: string
  university_id: string
  name: string
  faculty: string | null
  riasec_codes: string[]   // e.g. ['R','I']
  is_active: boolean
}

export type UniversityWithMajors = UniversityRow & {
  majors: MajorRow[]
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

const restFetch = async <T>(path: string, token?: string): Promise<T> => {
  let res: Response
  try {
    res = await fetch(`${supabaseUrl}/rest/v1/${path}`, {
      headers: {
        apikey: supabaseAnonKey,
        Authorization: `Bearer ${token ?? supabaseAnonKey}`,
        'Content-Type': 'application/json',
      },
    })
  } catch {
    throw new NetworkError()
  }
  if (res.status === 401) {
    await supabase.auth.signOut()
    throw new SessionExpiredError()
  }
  if (!res.ok) {
    const body = await res.text()
    throw new Error(`${res.status}: ${body}`)
  }
  return res.json()
}

// ─── Queries ──────────────────────────────────────────────────────────────────

/** Semua universitas aktif — partner muncul paling atas */
export const getUniversities = async (): Promise<UniversityRow[]> => {
  return restFetch<UniversityRow[]>(
    'universities?is_active=eq.true&order=is_partner.desc,name.asc&select=*'
  )
}

/** Universitas partner saja */
export const getPartnerUniversities = async (): Promise<UniversityRow[]> => {
  return restFetch<UniversityRow[]>(
    'universities?is_active=eq.true&is_partner=eq.true&order=partner_tier.desc,name.asc&select=*'
  )
}

/** Detail satu universitas + list jurusannya */
export const getUniversityWithMajors = async (
  universityId: string,
): Promise<UniversityWithMajors> => {
  const [uni, majors] = await Promise.all([
    restFetch<UniversityRow[]>(
      `universities?id=eq.${universityId}&is_active=eq.true&select=*`
    ),
    restFetch<MajorRow[]>(
      `majors?university_id=eq.${universityId}&is_active=eq.true&order=name.asc&select=*`
    ),
  ])
  if (!uni[0]) throw new Error('Universitas tidak ditemukan')
  return { ...uni[0], majors }
}

/** Cari universitas berdasarkan nama (untuk search) */
export const searchUniversities = async (query: string): Promise<UniversityRow[]> => {
  const q = encodeURIComponent(query)
  return restFetch<UniversityRow[]>(
    `universities?is_active=eq.true&or=(name.ilike.*${q}*,short_name.ilike.*${q}*,city.ilike.*${q}*)&order=is_partner.desc,name.asc&select=*`
  )
}

/**
 * Universitas yang punya jurusan sesuai kode RIASEC.
 * Partner selalu muncul duluan.
 */
export const getUniversitiesByRiasec = async (
  riasecCodes: string[],    // e.g. ['R','I','A']
  limit = 20,
): Promise<(UniversityRow & { matching_majors: string[] })[]> => {
  // Fetch semua majors aktif
  const allMajors = await restFetch<MajorRow[]>(
    'majors?is_active=eq.true&select=university_id,name,riasec_codes'
  )

  // Filter di client: hanya majors yang mempunyai minimal satu kode RIASEC yang match
  const matchingMajors = allMajors.filter((m) =>
    m.riasec_codes.some((code) => riasecCodes.includes(code))
  )

  // Group by university
  const uniMap = new Map<string, string[]>()
  matchingMajors.forEach((m) => {
    const existing = uniMap.get(m.university_id) ?? []
    uniMap.set(m.university_id, [...existing, m.name])
  })

  if (uniMap.size === 0) return []

  // Fetch universities
  const ids = Array.from(uniMap.keys()).join(',')
  const unis = await restFetch<UniversityRow[]>(
    `universities?id=in.(${ids})&is_active=eq.true&order=is_partner.desc,name.asc&select=*`
  )

  return unis
    .slice(0, limit)
    .map((u) => ({
      ...u,
      matching_majors: uniMap.get(u.id) ?? [],
    }))
}
