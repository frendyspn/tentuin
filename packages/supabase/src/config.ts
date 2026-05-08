import { supabaseUrl, supabaseAnonKey } from './client'

export interface AppConfig {
  platform:    string
  min_version: string
  store_url:   string
}

/** Ambil konfigurasi app (min_version, store_url) untuk platform tertentu.
 *  Tidak butuh auth — public read.
 *  Return null kalau gagal (network error, dll) → jangan block user.
 */
export async function getAppConfig(
  platform: 'android' | 'ios',
): Promise<AppConfig | null> {
  try {
    const res = await fetch(
      `${supabaseUrl}/rest/v1/app_config?platform=eq.${platform}&select=platform,min_version,store_url&limit=1`,
      {
        headers: {
          apikey:        supabaseAnonKey,
          Authorization: `Bearer ${supabaseAnonKey}`,
        },
      },
    )
    if (!res.ok) return null
    const data: AppConfig[] = await res.json()
    return data[0] ?? null
  } catch {
    // Network error → jangan paksa update, biarkan user masuk
    return null
  }
}

/** Bandingkan dua versi semver sederhana (major.minor.patch).
 *  Return true kalau `current` lebih lama dari `minimum`.
 */
export function isVersionOutdated(current: string, minimum: string): boolean {
  const parse = (v: string) =>
    v.split('.').map((n) => parseInt(n, 10) || 0)
  const [cMaj, cMin = 0, cPatch = 0] = parse(current)
  const [mMaj, mMin = 0, mPatch = 0] = parse(minimum)
  if (cMaj !== mMaj) return cMaj < mMaj
  if (cMin !== mMin) return cMin < mMin
  return cPatch < mPatch
}
