import { supabaseUrl, supabaseAnonKey } from './client'
import { NetworkError, SessionExpiredError } from './universities'

// ─── Types ────────────────────────────────────────────────────────────────────

export type SchoolRow = {
  id: string
  name: string
  npsn: string | null
  city: string
  province: string
  address: string | null
  email: string | null
  phone: string | null
  logo_url: string | null
  total_students: number
  is_active: boolean
  created_at: string
  updated_at: string
}

export type SchoolTarget = {
  id: string
  school_id: string
  year: number
  annual_target: number
  monthly_targets: number[] | null
  created_at: string
  updated_at: string
}

export type SchoolProgress = {
  school: SchoolRow
  target: SchoolTarget | null
  registered_count: number      // total siswa terdaftar tahun ini
  monthly_registered: number[]  // [0..11] siswa per bulan
  current_month: number         // 1-12
  cumulative_target: number     // target kumulatif hingga bulan ini
  is_on_track: boolean          // apakah komisi bulan ini cair
  annual_target_reached: boolean
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

// ─── Queries ──────────────────────────────────────────────────────────────────

/** Semua sekolah aktif — untuk browse agen */
export const getSchools = async (filters?: {
  city?: string
  province?: string
  search?: string
  claimable?: boolean  // hanya tampilkan yang belum diklaim
}): Promise<SchoolRow[]> => {
  let q = 'schools?is_active=eq.true&order=name.asc'
  if (filters?.city)     q += `&city=eq.${encodeURIComponent(filters.city)}`
  if (filters?.province) q += `&province=eq.${encodeURIComponent(filters.province)}`
  if (filters?.search)   q += `&name=ilike.*${encodeURIComponent(filters.search)}*`
  return restFetch<SchoolRow[]>(`${q}&select=*`)
}

/** Detail satu sekolah */
export const getSchool = async (schoolId: string): Promise<SchoolRow | null> => {
  const rows = await restFetch<SchoolRow[]>(
    `schools?id=eq.${schoolId}&select=*`
  )
  return rows[0] ?? null
}

/** Target sekolah untuk tahun tertentu */
export const getSchoolTarget = async (
  schoolId: string,
  year: number,
  token: string
): Promise<SchoolTarget | null> => {
  const rows = await restFetch<SchoolTarget[]>(
    `school_targets?school_id=eq.${schoolId}&year=eq.${year}&select=*`,
    { token }
  )
  return rows[0] ?? null
}

/** Set / update target sekolah */
export const setSchoolTarget = async (
  schoolId: string,
  year: number,
  annualTarget: number,
  monthlyTargets: number[] | null,
  token: string
): Promise<SchoolTarget> => {
  const existing = await getSchoolTarget(schoolId, year, token)
  if (existing) {
    const rows = await restFetch<SchoolTarget[]>(
      `school_targets?school_id=eq.${schoolId}&year=eq.${year}`,
      {
        method: 'PATCH',
        body: { annual_target: annualTarget, monthly_targets: monthlyTargets },
        token,
      }
    )
    return rows[0]!
  }
  const rows = await restFetch<SchoolTarget[]>('school_targets', {
    method: 'POST',
    body: { school_id: schoolId, year, annual_target: annualTarget, monthly_targets: monthlyTargets },
    token,
  })
  return rows[0]!
}

/**
 * Hitung progress target sekolah untuk tahun & bulan tertentu.
 * Menentukan apakah komisi bulan ini layak cair.
 */
export const getSchoolProgress = async (
  schoolId: string,
  year: number,
  month: number,   // 1-12
  token: string
): Promise<SchoolProgress> => {
  const [school, target, registrations] = await Promise.all([
    getSchool(schoolId),
    getSchoolTarget(schoolId, year, token),
    // Hitung siswa yang daftar dari sekolah ini tahun ini (via profiles.school_name atau school_id nanti)
    restFetch<{ created_at: string }[]>(
      `profiles?school_name=not.is.null&created_at=gte.${year}-01-01T00:00:00Z` +
      `&created_at=lt.${year + 1}-01-01T00:00:00Z&select=created_at`,
      { token }
    ),
  ])

  if (!school) throw new Error('Sekolah tidak ditemukan')

  // Hitung per bulan (index 0 = Januari)
  const monthly_registered = Array(12).fill(0) as number[]
  registrations.forEach((r) => {
    const m = new Date(r.created_at).getMonth() // 0-indexed
    monthly_registered[m]++
  })

  const registered_count = monthly_registered.reduce((a, b) => a + b, 0)

  // Target kumulatif hingga bulan ke-N
  const annual_target = target?.annual_target ?? Math.ceil(school.total_students * 0.1)
  let cumulative_target = 0
  if (target?.monthly_targets) {
    // Gunakan target bulanan kustom
    cumulative_target = target.monthly_targets.slice(0, month).reduce((a, b) => a + b, 0)
  } else {
    // Default: proporsional (annual_target × bulan / 12)
    cumulative_target = Math.ceil((annual_target * month) / 12)
  }

  // Kumulatif aktual hingga bulan ini
  const cumulative_actual = monthly_registered.slice(0, month).reduce((a, b) => a + b, 0)

  // Target tahunan tercapai jika kumulatif sebelum bulan ini sudah >= annual_target
  const cumulative_before = monthly_registered.slice(0, month - 1).reduce((a, b) => a + b, 0)
  const annual_target_reached = cumulative_before >= annual_target

  const is_on_track = annual_target_reached || cumulative_actual >= cumulative_target

  return {
    school,
    target: target ?? null,
    registered_count,
    monthly_registered,
    current_month: month,
    cumulative_target,
    is_on_track,
    annual_target_reached,
  }
}
