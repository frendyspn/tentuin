import { supabase } from './client'

// ─── Push Token ───────────────────────────────────────────────────────────────

/**
 * Simpan Expo push token ke kolom push_token di tabel profiles.
 * Dipanggil setiap kali user login & permission notifikasi di-grant.
 */
export const savePushToken = async (
  userId: string,
  token: string,
  accessToken: string,
): Promise<void> => {
  const q = supabase.from('profiles') as any
  const { error } = await q
    .update({ push_token: token, updated_at: new Date().toISOString() })
    .eq('id', userId)

  if (error) {
    console.warn('[Notifications] Failed to save push token:', error.message)
  }
}

/**
 * Hapus push token dari database (dipanggil saat sign out).
 */
export const clearPushToken = async (userId: string): Promise<void> => {
  const q = supabase.from('profiles') as any
  const { error } = await q
    .update({ push_token: null, updated_at: new Date().toISOString() })
    .eq('id', userId)

  if (error) {
    console.warn('[Notifications] Failed to clear push token:', error.message)
  }
}
