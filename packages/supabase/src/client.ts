import { createClient } from '@supabase/supabase-js'
import type { Database } from './database.types'

// Support both Expo (EXPO_PUBLIC_*) and Next.js (NEXT_PUBLIC_*) environments
export const supabaseUrl = (
  process.env.EXPO_PUBLIC_SUPABASE_URL ||
  process.env.NEXT_PUBLIC_SUPABASE_URL ||
  ''
) as string

export const supabaseAnonKey = (
  process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY ||
  process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ||
  ''
) as string

// AsyncStorage hanya valid di React Native (butuh `window`). Di Next.js/Node
// server, modul ini ter-resolve tapi getItem-nya crash — jadi kita harus deteksi
// runtime, bukan sekadar try/catch require. Admin app pakai @supabase/ssr
// sendiri, jadi client di sini tidak pernah di-pakai untuk auth — tidak masalah
// kalau tanpa storage.
const isReactNative =
  typeof navigator !== 'undefined' && navigator.product === 'ReactNative'

let _supabase: ReturnType<typeof createClient<Database>>

if (isReactNative) {
  // eslint-disable-next-line @typescript-eslint/no-require-imports
  const AsyncStorage = require('@react-native-async-storage/async-storage').default
  _supabase = createClient<Database>(supabaseUrl, supabaseAnonKey, {
    auth: {
      storage: AsyncStorage,
      autoRefreshToken: true,
      persistSession: true,
      detectSessionInUrl: false,
    },
  })
} else {
  // Next.js server / Node — basic client, no auto-refresh, no persisted session
  _supabase = createClient<Database>(supabaseUrl, supabaseAnonKey, {
    auth: {
      autoRefreshToken: false,
      persistSession: false,
    },
  })
}

export const supabase = _supabase
