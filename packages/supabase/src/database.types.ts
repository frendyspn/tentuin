// Type ini di-generate otomatis dari Supabase schema.
// Setelah setup Supabase project, jalankan:
// npx supabase gen types typescript --project-id YOUR_PROJECT_ID > packages/supabase/src/database.types.ts

export type Json =
  | string
  | number
  | boolean
  | null
  | { [key: string]: Json | undefined }
  | Json[]

export interface Database {
  public: {
    Tables: {
      profiles: {
        Row: {
          id: string
          full_name: string | null
          school_name: string | null
          city: string | null
          birth_year: number | null
          avatar_url: string | null
          has_completed_onboarding: boolean
          created_at: string
          updated_at: string
        }
        Insert: {
          id: string
          full_name?: string | null
          school_name?: string | null
          city?: string | null
          birth_year?: number | null
          avatar_url?: string | null
          has_completed_onboarding?: boolean
          created_at?: string
          updated_at?: string
        }
        Update: {
          id?: string
          full_name?: string | null
          school_name?: string | null
          city?: string | null
          birth_year?: number | null
          avatar_url?: string | null
          has_completed_onboarding?: boolean
          updated_at?: string
        }
      }
    }
    Views: Record<string, never>
    Functions: Record<string, never>
    Enums: Record<string, never>
  }
}
