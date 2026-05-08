import { supabaseUrl, supabaseAnonKey } from './client'
import { NetworkError, SessionExpiredError } from './universities'

// ─── Types ────────────────────────────────────────────────────────────────────

export const PROSPECT_VALUE    = 1000  // Rp per siswa per universitas
export const AGENT_RATE        = 0.10  // 10%
export const SCHOOL_RATE       = 0.10  // 10%
export const SUBSCRIBE_AGENT_RATE = 0.10  // 10% dari nilai subscribe

export type AgentCommission = {
  id: string
  agent_id: string
  month: number
  year: number
  stream_a_amount: number
  stream_b_amount: number
  total_amount: number
  status: 'pending' | 'paid' | 'cancelled'
  notes: string | null
  created_at: string
  updated_at: string
}

export type SchoolCommission = {
  id: string
  school_id: string
  agent_id: string | null
  month: number
  year: number
  amount: number
  status: 'pending' | 'paid' | 'cancelled'
  created_at: string
  updated_at: string
}

export type AgentWithdrawal = {
  id: string
  agent_id: string
  amount: number
  status: 'requested' | 'approved' | 'rejected' | 'transferred'
  requested_at: string
  processed_at: string | null
  admin_notes: string | null
}

export type CommissionSummary = {
  year: number
  months: {
    month: number
    stream_a: number
    stream_b: number
    total: number
    status: 'pending' | 'paid' | 'cancelled'
  }[]
  total_pending: number
  total_paid: number
  total_all: number
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

// ─── Stream B: Komisi Subscribe Universitas ───────────────────────────────────

/**
 * Catat subscribe universitas & langsung tambah komisi Stream B ke agen.
 * Dipanggil oleh admin saat universitas melakukan pembayaran.
 */
export const recordUniversitySubscribe = async (
  universityId: string,
  amount: number,
  quotaPurchased: number,
  agentId: string | null,
  token: string
): Promise<void> => {
  const commissionAgent = agentId ? Math.floor(amount * SUBSCRIBE_AGENT_RATE) : 0

  // Catat log subscribe
  await restFetch('university_subscribe_logs', {
    method: 'POST',
    body: { university_id: universityId, agent_id: agentId, amount, quota_purchased: quotaPurchased, commission_agent: commissionAgent },
    token,
  })

  // Update quota_balance universitas
  await restFetch(`universities?id=eq.${universityId}`, {
    method: 'PATCH',
    body: { quota_balance: quotaPurchased },  // akan di-increment via RPC idealnya
    token,
  })

  // Tambahkan ke agent_commissions Stream B jika ada agen
  if (agentId && commissionAgent > 0) {
    const now = new Date()
    const month = now.getMonth() + 1
    const year = now.getFullYear()
    await upsertAgentCommission(agentId, month, year, { stream_b_delta: commissionAgent }, token)
  }
}

// ─── Stream A: Komisi Prospek Siswa (Cron Bulanan) ────────────────────────────

type ProspectLog = {
  id: string
  university_id: string
  student_id: string
  school_id: string | null
  agent_id: string | null
}

type SchoolTargetRow = {
  school_id: string
  annual_target: number
  monthly_targets: number[] | null
}

type SchoolRow = { id: string; total_students: number }

/**
 * Kalkulasi dan catat komisi Stream A untuk satu bulan.
 * Dipanggil oleh Supabase Edge Function cron setiap akhir bulan.
 * Gunakan service_role token.
 */
export const calculateMonthlyStreamA = async (
  month: number,
  year: number,
  serviceToken: string
): Promise<{ schools_processed: number; total_commission: number }> => {
  const startOf = `${year}-${String(month).padStart(2, '0')}-01T00:00:00Z`
  const nextMonth = month === 12 ? `${year + 1}-01-01T00:00:00Z` : `${year}-${String(month + 1).padStart(2, '0')}-01T00:00:00Z`
  const startOfYear = `${year}-01-01T00:00:00Z`

  // Ambil semua prospect logs bulan ini yang belum dihitung
  const logs = await restFetch<ProspectLog[]>(
    `prospect_usage_logs?used_at=gte.${startOf}&used_at=lt.${nextMonth}` +
    `&commission_calculated=eq.false&select=id,university_id,student_id,school_id,agent_id`,
    { token: serviceToken }
  )

  if (logs.length === 0) return { schools_processed: 0, total_commission: 0 }

  // Group by school_id
  const bySchool = new Map<string, ProspectLog[]>()
  logs.forEach((l) => {
    if (!l.school_id) return
    const arr = bySchool.get(l.school_id) ?? []
    arr.push(l)
    bySchool.set(l.school_id, arr)
  })

  // Ambil target semua sekolah yang terlibat
  const schoolIds = Array.from(bySchool.keys()).join(',')
  const [targets, schools, yearlyRegs] = await Promise.all([
    restFetch<SchoolTargetRow[]>(
      `school_targets?school_id=in.(${schoolIds})&year=eq.${year}&select=school_id,annual_target,monthly_targets`,
      { token: serviceToken }
    ),
    restFetch<SchoolRow[]>(
      `schools?id=in.(${schoolIds})&select=id,total_students`,
      { token: serviceToken }
    ),
    // Hitung siswa daftar per sekolah sejak awal tahun hingga akhir bulan ini
    restFetch<{ created_at: string }[]>(
      `profiles?created_at=gte.${startOfYear}&created_at=lt.${nextMonth}&select=created_at`,
      { token: serviceToken }
    ),
  ])

  const targetMap = new Map(targets.map((t) => [t.school_id, t]))
  const schoolMap = new Map(schools.map((s) => [s.id, s]))

  let schools_processed = 0
  let total_commission = 0

  for (const [schoolId, schoolLogs] of bySchool) {
    const target = targetMap.get(schoolId)
    const school = schoolMap.get(schoolId)
    if (!school) continue

    // Hitung kumulatif siswa daftar dari sekolah ini sejak Jan hingga akhir bulan ini
    // (simplified: gunakan total dari yearlyRegs — idealnya filter by school_id via join)
    const annualTarget = target?.annual_target ?? Math.ceil(school.total_students * 0.1)

    let cumulativeTarget: number
    if (target?.monthly_targets) {
      cumulativeTarget = target.monthly_targets.slice(0, month).reduce((a, b) => a + b, 0)
    } else {
      cumulativeTarget = Math.ceil((annualTarget * month) / 12)
    }

    // Hitung kumulatif aktual (perkiraan — idealnya dari tabel terpisah)
    const cumulativeActual = yearlyRegs.filter((r) => {
      const m = new Date(r.created_at).getMonth() + 1
      return m <= month
    }).length

    // Cek target tahunan sudah tercapai sebelum bulan ini
    const cumulativeBefore = yearlyRegs.filter((r) => {
      const m = new Date(r.created_at).getMonth() + 1
      return m < month
    }).length
    const annualTargetReached = cumulativeBefore >= annualTarget

    const isEligible = annualTargetReached || cumulativeActual >= cumulativeTarget
    if (!isEligible) continue

    // Hitung komisi
    const prospectCount = schoolLogs.length
    const agentAmount  = Math.floor(prospectCount * PROSPECT_VALUE * AGENT_RATE)
    const schoolAmount = Math.floor(prospectCount * PROSPECT_VALUE * SCHOOL_RATE)

    // Ambil agent_id dari klaim sekolah
    const claimRows = await restFetch<{ agent_id: string }[]>(
      `agent_school_claims?school_id=eq.${schoolId}&is_active=eq.true&select=agent_id`,
      { token: serviceToken }
    )
    const agentId = claimRows[0]?.agent_id ?? null

    // Upsert agent_commissions Stream A
    if (agentId) {
      await upsertAgentCommission(agentId, month, year, { stream_a_delta: agentAmount }, serviceToken)
    }

    // Upsert school_commissions
    await upsertSchoolCommission(schoolId, agentId, month, year, schoolAmount, serviceToken)

    // Mark logs sebagai sudah dihitung
    const logIds = schoolLogs.map((l) => l.id).join(',')
    await restFetch(
      `prospect_usage_logs?id=in.(${logIds})`,
      { method: 'PATCH', body: { commission_calculated: true }, token: serviceToken }
    )

    schools_processed++
    total_commission += agentAmount + schoolAmount
  }

  return { schools_processed, total_commission }
}

// ─── Upsert Helpers ───────────────────────────────────────────────────────────

const upsertAgentCommission = async (
  agentId: string,
  month: number,
  year: number,
  delta: { stream_a_delta?: number; stream_b_delta?: number },
  token: string
): Promise<void> => {
  const existing = await restFetch<AgentCommission[]>(
    `agent_commissions?agent_id=eq.${agentId}&month=eq.${month}&year=eq.${year}&select=*`,
    { token }
  )
  if (existing[0]) {
    await restFetch(
      `agent_commissions?agent_id=eq.${agentId}&month=eq.${month}&year=eq.${year}`,
      {
        method: 'PATCH',
        body: {
          stream_a_amount: existing[0].stream_a_amount + (delta.stream_a_delta ?? 0),
          stream_b_amount: existing[0].stream_b_amount + (delta.stream_b_delta ?? 0),
        },
        token,
      }
    )
  } else {
    await restFetch('agent_commissions', {
      method: 'POST',
      body: {
        agent_id: agentId,
        month,
        year,
        stream_a_amount: delta.stream_a_delta ?? 0,
        stream_b_amount: delta.stream_b_delta ?? 0,
      },
      token,
    })
  }
}

const upsertSchoolCommission = async (
  schoolId: string,
  agentId: string | null,
  month: number,
  year: number,
  amount: number,
  token: string
): Promise<void> => {
  const existing = await restFetch<SchoolCommission[]>(
    `school_commissions?school_id=eq.${schoolId}&month=eq.${month}&year=eq.${year}&select=*`,
    { token }
  )
  if (existing[0]) {
    await restFetch(
      `school_commissions?school_id=eq.${schoolId}&month=eq.${month}&year=eq.${year}`,
      { method: 'PATCH', body: { amount: existing[0].amount + amount }, token }
    )
  } else {
    await restFetch('school_commissions', {
      method: 'POST',
      body: { school_id: schoolId, agent_id: agentId, month, year, amount },
      token,
    })
  }
}

// ─── Riwayat & Summary ────────────────────────────────────────────────────────

/** Riwayat komisi agen per tahun */
export const getAgentCommissions = async (
  agentId: string,
  year: number,
  token: string
): Promise<AgentCommission[]> => {
  return restFetch<AgentCommission[]>(
    `agent_commissions?agent_id=eq.${agentId}&year=eq.${year}&order=month.asc&select=*`,
    { token }
  )
}

/** Summary komisi agen: total pending, paid, all */
export const getAgentCommissionSummary = async (
  agentId: string,
  year: number,
  token: string
): Promise<CommissionSummary> => {
  const rows = await getAgentCommissions(agentId, year, token)
  const total_pending = rows.filter((r) => r.status === 'pending').reduce((a, r) => a + r.total_amount, 0)
  const total_paid    = rows.filter((r) => r.status === 'paid').reduce((a, r) => a + r.total_amount, 0)
  const total_all     = rows.reduce((a, r) => a + r.total_amount, 0)
  return {
    year,
    months: rows.map((r) => ({
      month: r.month,
      stream_a: r.stream_a_amount,
      stream_b: r.stream_b_amount,
      total: r.total_amount,
      status: r.status,
    })),
    total_pending,
    total_paid,
    total_all,
  }
}

// ─── Withdrawal ───────────────────────────────────────────────────────────────

/** Agen request withdrawal */
export const requestWithdrawal = async (
  agentId: string,
  amount: number,
  token: string
): Promise<AgentWithdrawal> => {
  const rows = await restFetch<AgentWithdrawal[]>('agent_withdrawals', {
    method: 'POST',
    body: { agent_id: agentId, amount },
    token,
  })
  return rows[0]!
}

/** Riwayat withdrawal agen */
export const getAgentWithdrawals = async (
  agentId: string,
  token: string
): Promise<AgentWithdrawal[]> => {
  return restFetch<AgentWithdrawal[]>(
    `agent_withdrawals?agent_id=eq.${agentId}&order=requested_at.desc&select=*`,
    { token }
  )
}

/** Admin: approve / reject withdrawal */
export const processWithdrawal = async (
  withdrawalId: string,
  status: 'approved' | 'rejected' | 'transferred',
  adminNotes: string | null,
  serviceToken: string
): Promise<void> => {
  await restFetch(`agent_withdrawals?id=eq.${withdrawalId}`, {
    method: 'PATCH',
    body: { status, admin_notes: adminNotes, processed_at: new Date().toISOString() },
    token: serviceToken,
  })
}
