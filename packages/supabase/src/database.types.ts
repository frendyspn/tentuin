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
          push_token: string | null
          role: string
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
          push_token?: string | null
          role?: string
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
          push_token?: string | null
          role?: string
          updated_at?: string
        }
        Relationships: []
      }

      schools: {
        Row: {
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
        Insert: {
          id?: string
          name: string
          npsn?: string | null
          city: string
          province: string
          address?: string | null
          email?: string | null
          phone?: string | null
          logo_url?: string | null
          total_students?: number
          is_active?: boolean
          created_at?: string
          updated_at?: string
        }
        Update: {
          name?: string
          npsn?: string | null
          city?: string
          province?: string
          address?: string | null
          email?: string | null
          phone?: string | null
          logo_url?: string | null
          total_students?: number
          is_active?: boolean
          updated_at?: string
        }
        Relationships: []
      }

      agents: {
        Row: {
          id: string
          full_name: string
          email: string
          phone: string | null
          referral_code: string
          status: 'active' | 'suspended' | 'inactive'
          is_owner: boolean
          last_active_at: string
          bank_name: string | null
          bank_account_number: string | null
          bank_account_name: string | null
          notes: string | null
          created_at: string
          updated_at: string
        }
        Insert: {
          id: string
          full_name: string
          email: string
          phone?: string | null
          referral_code: string
          status?: 'active' | 'suspended' | 'inactive'
          is_owner?: boolean
          last_active_at?: string
          bank_name?: string | null
          bank_account_number?: string | null
          bank_account_name?: string | null
          notes?: string | null
          created_at?: string
          updated_at?: string
        }
        Update: {
          full_name?: string
          phone?: string | null
          status?: 'active' | 'suspended' | 'inactive'
          last_active_at?: string
          bank_name?: string | null
          bank_account_number?: string | null
          bank_account_name?: string | null
          notes?: string | null
          updated_at?: string
        }
        Relationships: []
      }

      agent_school_claims: {
        Row: {
          id: string
          agent_id: string
          school_id: string
          is_active: boolean
          claimed_at: string
          released_at: string | null
        }
        Insert: {
          id?: string
          agent_id: string
          school_id: string
          is_active?: boolean
          claimed_at?: string
          released_at?: string | null
        }
        Update: {
          is_active?: boolean
          released_at?: string | null
        }
        Relationships: []
      }

      agent_university_claims: {
        Row: {
          id: string
          agent_id: string
          university_id: string
          is_active: boolean
          claimed_at: string
          released_at: string | null
        }
        Insert: {
          id?: string
          agent_id: string
          university_id: string
          is_active?: boolean
          claimed_at?: string
          released_at?: string | null
        }
        Update: {
          is_active?: boolean
          released_at?: string | null
        }
        Relationships: []
      }

      school_targets: {
        Row: {
          id: string
          school_id: string
          year: number
          annual_target: number
          monthly_targets: number[] | null
          created_at: string
          updated_at: string
        }
        Insert: {
          id?: string
          school_id: string
          year: number
          annual_target: number
          monthly_targets?: number[] | null
          created_at?: string
          updated_at?: string
        }
        Update: {
          annual_target?: number
          monthly_targets?: number[] | null
          updated_at?: string
        }
        Relationships: []
      }

      university_subscribe_logs: {
        Row: {
          id: string
          university_id: string
          agent_id: string | null
          amount: number
          quota_purchased: number
          commission_agent: number
          subscribed_at: string
        }
        Insert: {
          id?: string
          university_id: string
          agent_id?: string | null
          amount: number
          quota_purchased: number
          commission_agent?: number
          subscribed_at?: string
        }
        Update: Record<string, never>
        Relationships: []
      }

      prospect_usage_logs: {
        Row: {
          id: string
          university_id: string
          student_id: string
          school_id: string | null
          agent_id: string | null
          commission_calculated: boolean
          used_at: string
        }
        Insert: {
          id?: string
          university_id: string
          student_id: string
          school_id?: string | null
          agent_id?: string | null
          commission_calculated?: boolean
          used_at?: string
        }
        Update: {
          commission_calculated?: boolean
        }
        Relationships: []
      }

      agent_commissions: {
        Row: {
          id: string
          agent_id: string
          month: number
          year: number
          stream_a_amount: number
          stream_b_amount: number
          total_amount: number  // generated column
          status: 'pending' | 'paid' | 'cancelled'
          notes: string | null
          created_at: string
          updated_at: string
        }
        Insert: {
          id?: string
          agent_id: string
          month: number
          year: number
          stream_a_amount?: number
          stream_b_amount?: number
          status?: 'pending' | 'paid' | 'cancelled'
          notes?: string | null
          created_at?: string
          updated_at?: string
        }
        Update: {
          stream_a_amount?: number
          stream_b_amount?: number
          status?: 'pending' | 'paid' | 'cancelled'
          notes?: string | null
          updated_at?: string
        }
        Relationships: []
      }

      school_commissions: {
        Row: {
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
        Insert: {
          id?: string
          school_id: string
          agent_id?: string | null
          month: number
          year: number
          amount?: number
          status?: 'pending' | 'paid' | 'cancelled'
          created_at?: string
          updated_at?: string
        }
        Update: {
          amount?: number
          status?: 'pending' | 'paid' | 'cancelled'
          updated_at?: string
        }
        Relationships: []
      }

      agent_withdrawals: {
        Row: {
          id: string
          agent_id: string
          amount: number
          status: 'requested' | 'approved' | 'rejected' | 'transferred'
          requested_at: string
          processed_at: string | null
          admin_notes: string | null
        }
        Insert: {
          id?: string
          agent_id: string
          amount: number
          status?: 'requested' | 'approved' | 'rejected' | 'transferred'
          requested_at?: string
          processed_at?: string | null
          admin_notes?: string | null
        }
        Update: {
          status?: 'requested' | 'approved' | 'rejected' | 'transferred'
          processed_at?: string | null
          admin_notes?: string | null
        }
        Relationships: []
      }
    }
    Views: Record<string, never>
    Functions: Record<string, never>
    Enums: Record<string, never>
    CompositeTypes: Record<string, never>
  }
}
