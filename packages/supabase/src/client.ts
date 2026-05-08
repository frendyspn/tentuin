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

// Try to create the React Native–flavoured client (with AsyncStorage).
// In Next.js / Node.js context AsyncStorage is unavailable — fall back to a
// plain client. The admin app never calls supabase.auth.* from this module
// (it uses @supabase/ssr instead), so the fallback client is never exercised.
let _supabase: ReturnType<typeof createClient<Database>>

try {
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
} catch {
  // AsyncStorage not available (Next.js / server context) — use basic client
  _supabase = createClient<Database>(supabaseUrl, supabaseAnonKey)
}

export const supabase = _supabase
