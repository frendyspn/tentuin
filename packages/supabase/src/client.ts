import { createClient } from '@supabase/supabase-js'
import AsyncStorage from '@react-native-async-storage/async-storage'
import type { Database } from './database.types'

// Variabel ini di-inject dari masing-masing apps via environment variable
export const supabaseUrl     = process.env.EXPO_PUBLIC_SUPABASE_URL     as string
export const supabaseAnonKey = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY as string

if (!supabaseUrl || !supabaseAnonKey) {
  throw new Error(
    'Supabase URL dan Anon Key belum diset. ' +
    'Copy .env.example ke .env dan isi dengan credentials dari Supabase dashboard.'
  )
}

export const supabase = createClient<Database>(supabaseUrl, supabaseAnonKey, {
  auth: {
    storage: AsyncStorage,
    autoRefreshToken: true,
    persistSession: true,
    detectSessionInUrl: false,
  },
})
